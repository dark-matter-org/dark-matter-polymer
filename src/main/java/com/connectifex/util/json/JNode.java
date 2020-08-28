package com.connectifex.util.json;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

import org.dmd.dmc.DmcValueException;
import org.json.JSONArray;
import org.json.JSONObject;

import com.connectifex.polymer.mdl.server.extended.plastic.util.PlasticConstants;
import com.connectifex.polymer.mdl.shared.generated.types.PlasticVariable;


/**
 * The JNode provides support mechanisms for the creation of parameterized
 * JSON payloads. It decomposes a given JSON structure into its constituent
 * pieces and characterizes them as a particular JType. The resulting hierarchy
 * is easy to traverse in terms parent/child relationships.
 */
public class JNode {

	// The type of this node
	protected JType		type;
	
	// The key or null if this is STRING, NUMBER, BOOLEAN or NULL
	private String		key;
	
	// A generated uniqueKey for this node so that we can parameterize it
	protected String 	uniqueKey;
	
	// For an OBJECT, this is the longest key
	protected int		longestKey;
	
	// A param-name if this value has been designated as a parameter
	protected String		parameterName;
	
	// The parent node or null if we're the root
	protected JNode		parent;
	
	// Our value as a string if type is STRING, NUMBER, BOOLEAN or NULL
	protected String	value;
	
	// The children of this node
	protected ArrayList<JNode>	children;
	
	/**
	 * Root node constructor
	 * @param object the JSON object we start with.
	 */
	protected JNode(JSONObject object, JNode parent, String key) {
		type			= JType.OBJECT;
		this.key		= key;
		parameterName	= null;
		this.parent		= parent;
		value			= null;
		children		= null;
		
		Set<?> keys = object.keySet();
		if (keys.size() > 0) {
			children = new ArrayList<>();
			
			// We maintain the different groups of things
			TreeMap<String,JNode> primitives = new TreeMap<>(Collator.getInstance(Locale.ENGLISH));
			TreeMap<String,JNode> arrays = new TreeMap<>(Collator.getInstance(Locale.ENGLISH));
			TreeMap<String,JNode> objects = new TreeMap<>(Collator.getInstance(Locale.ENGLISH));

			for(Object k: keys){
				String elementKey = k.toString();
				
				if (elementKey.toString().length() > longestKey)
					longestKey = elementKey.length();
				
				Object value = object.get(elementKey);
				
//				if (value == null) {
//					JNode child = new JNode(elementKey);
//					primitives.put(elementKey, child);
//					continue;
//				}
				
				if (value instanceof JSONObject){
					JNode child = new JNode((JSONObject)value, this, elementKey);
					objects.put(elementKey, child);
				}
				else if (value instanceof JSONArray){
					JNode child = new JNode(this, elementKey, (JSONArray)value);
					arrays.put(elementKey, child);
				}
				else if (value instanceof String){
					JNode child = new JNode(this, JType.STRING, elementKey, value.toString());
					primitives.put(elementKey, child);
				}
				else if (value instanceof Number){
					JNode child = new JNode(this, JType.NUMBER, elementKey, value.toString());
					primitives.put(elementKey, child);
				}
				else if (value instanceof Boolean){
					JNode child = new JNode(this, JType.BOOLEAN, elementKey, value.toString());
					primitives.put(elementKey, child);
				}
				else {
					JNode child = new JNode(this,elementKey);
					primitives.put(elementKey, child);
				}
			}
			
			for(JNode node: primitives.values())
				children.add(node);
			
			for(JNode node: arrays.values())
				children.add(node);
			
			for(JNode node: objects.values())
				children.add(node);
		}
	}
	
	/**
	 * Constructor for ARRAY
	 * @param parent
	 * @param type
	 * @param key
	 */
	private JNode(JNode parent, String key, JSONArray array) {
		this.type		= JType.ARRAY;
		this.key			= key;
		parameterName	= null;
		this.parent		= parent;
		this.value		= null;
		
		if (array.length() > 0) {
			children = new ArrayList<>();
			
			Iterator<Object> it = array.iterator();
			while(it.hasNext()) {
				Object arrayValue = it.next();
				
				if (arrayValue instanceof JSONObject){
					JNode child = new JNode((JSONObject)arrayValue, this, null);
					children.add(child);
				}
				else if (arrayValue instanceof String){
					JNode child = new JNode(this, JType.STRING, null, arrayValue.toString());
					children.add(child);
				}
				else if (arrayValue instanceof Number){
					JNode child = new JNode(this, JType.NUMBER, null, arrayValue.toString());
					children.add(child);
				}
				else if (arrayValue instanceof Boolean){
					JNode child = new JNode(this, JType.BOOLEAN, null, arrayValue.toString());
					children.add(child);
				}
				else {
					JNode child = new JNode(this,null);
					children.add(child);
				}
			}
		}
	}
	
	protected String hierarchy() {
		StringBuilder sb = new StringBuilder();
		hierarchy(sb,"");
		return(sb.toString());
	}
	
	protected String rawkey() {
		return(key);
	}
	/**
	 * Some trickery here to handle the situation where we have our single object
	 * in an array - that's a special case. If we have the key we return it, otherwise we
	 * check to see if we're an object and in an array - and then pass back its key.
	 * @return a key or null
	 */
	protected String key() {
		if (key != null)
			return(key);
		
		if ( (type == JType.OBJECT) && ((parent != null) && (parent.type == JType.ARRAY)))
			return(parent.key);
		
		return(null);
	}
	
	/**
	 * @return true if this a an array with a single object.
	 */
	protected boolean isSingleObjectArray() {
		boolean rc = false;
		if (type == JType.ARRAY) {
			if ( (children != null) && (children.size() == 1)) {
				if (children.get(0).type == JType.OBJECT)
					rc = true;
			}
		}
			
		return(rc);
	}
	
	/**
	 * @return the info from this node as an PlasticVariable parameter.
	 * @throws DmcValueException
	 */
	protected PlasticVariable	getParameter() throws DmcValueException {
		PlasticVariable rc = new PlasticVariable(uniqueKey, null, value, null, null, null, null, null, null, null, null);
		
		return(rc);
	}

	/**
	 * @return true if this node is a primitive type.
	 */
	protected boolean isPrimitive() {
		if (type == JType.OBJECT || type == JType.ARRAY)
			return(false);
		return(true);
	}
	
	/**
	 * Creates a (hopefully) unique key at the specified "depth" up
	 * the hierarchy.
	 * @param depth how far up the hierarchy to go
	 * @param setUnique true if we actually want to set the key as well
	 * @return a generated key
	 */
	String getUniqueKeyBasedOnDepth(int depth, boolean setUnique) {
		String rc = key;
		
		if (parent == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("depth = " + depth + "\n");
			sb.append("JNode " + type);
			if (key == null)
				sb.append(" key: null");
			else
				sb.append(" key: " + key);
			
			throw(new IllegalStateException("No parent - can't ascend hierarchy\n" + sb.toString()));
		}
		
		JNode ancestor = parent;
		
		for(int i=0; i<depth; i++) {
			if (ancestor == null) 
				throw(new IllegalStateException("No ancestor for: " + rc));
			
			if (ancestor.key() == null)
				throw(new IllegalStateException("No key for ancestor for: " + rc));
			
			rc = ancestor.key() + "--" + rc;
			ancestor = ancestor.parent;
		}
		
		if (setUnique)
			uniqueKey = rc;
		
		return(rc);
	}
	
	
	private void hierarchy(StringBuilder sb, String indent) {
		sb.append(indent);
		if (key != null)
			sb.append(key + "  ");
		
		sb.append(type);
		
		if (parent == null)
			sb.append("  parent null\n");
		else
			sb.append("  parent available\n");
		
		if (children != null) {
			for(JNode child: children)
				child.hierarchy(sb, indent + "  ");
		}
	}
	
	/**
	 * Pretty prints the JNode hierarchy with parameters inserted in the appropriate locations.
	 * @param sb the place we're we're building the JSON string
	 * @param allIndent if formatting for part of a DMO, we can indent the entire structure
	 * @param indent the indent at this level
	 * @param longestKeyHere the longest key at this point in the hierarchy so that we can line up the values
	 */
	protected void prettyPrint(StringBuilder sb, String allIndent, String indent, int longestKeyHere) {
		switch(type) {
		case ARRAY:
			if (key == null)
				sb.append(allIndent + indent + " [\n");
			else
				sb.append(allIndent + indent + "\"" + key + "\": [\n");
			
			if (children != null) {
				Iterator<JNode> it = children.iterator();
				while(it.hasNext()) {
					JNode node = it.next();
					node.prettyPrint(sb, allIndent, indent + "  ", longestKeyHere);
					if (it.hasNext())
						sb.append(",\n");
					else
						sb.append("\n");
				}
			}
			
			sb.append(allIndent + indent + "]");
			break;
		case BOOLEAN:
		case NULL:
		case NUMBER:
			if (uniqueKey == null) {
				if (key == null) {
					sb.append(allIndent + indent + value.toString());
				}
				else {
					sb.append(allIndent + indent + "\"" + key + "\": ");
					padding(sb,key,longestKeyHere);
					sb.append(value.toString());
				}
			}
			else {
				if (key == null) {
					// can't happen - if we have a unique key, we have a key
				}
				else {
					sb.append(allIndent + indent + "\"" + key + "\": ");
					padding(sb,key,longestKeyHere);
					sb.append(PlasticConstants.START_MARKER + uniqueKey + PlasticConstants.END_MARKER);
				}
			}
			break;
		case OBJECT:
			if (key == null)
				sb.append(allIndent + indent + "{\n");
			else
				sb.append(allIndent + indent + "\"" + key + "\": {\n");
			
			if (children != null) {
				Iterator<JNode> it = children.iterator();
				while(it.hasNext()) {
					JNode node = it.next();
					// Note: we use the longest key value of the object to pass down
					// the hierarchy - that's not a mistake!
					node.prettyPrint(sb, allIndent, indent + "  ", longestKey);
					if (it.hasNext())
						sb.append(",\n");
					else
						sb.append("\n");
				}
			}
			
			sb.append(allIndent + indent + "}");
			break;
		case STRING:
			if (uniqueKey == null) {
				if (key == null) {
					sb.append(allIndent + indent + "\"" + value.toString() + "\"");
				}
				else {
					sb.append(allIndent + indent + "\"" + key + "\": ");
					padding(sb,key,longestKeyHere);
					sb.append("\"" + value.toString() + "\"");
				}
			}
			else {
				if (key == null) {
					// can't happen - if we have a unique key, we have a key
				}
				else {
					sb.append(allIndent + indent + "\"" + key + "\": ");
					padding(sb,key,longestKeyHere);
					sb.append("\"" + PlasticConstants.START_MARKER + uniqueKey + PlasticConstants.END_MARKER + "\"");
				}
			}
			break;
		}
		
	}
	
	void padding(StringBuilder sb, String key, int longestKeyHere) {
		if (key.length() < longestKeyHere) {
			for(int i=key.length(); i<longestKeyHere; i++)
				sb.append(" ");
		}
	}

	
	/**
	 * Constructor for STRING, NUMBER, BOOLEAN
	 * @param parent the parent node
	 * @param type the appropriate type
	 * @param key the key
	 * @param value a string representation of the value
	 */
	private JNode(JNode parent, JType type, String key, String value) {
		this.type		= type;
		this.key		= key;
		parameterName	= null;
		this.parent		= parent;
		this.value		= value;
		children		= null;
	}
	
	/**
	 * Constructor for NULL
	 * @param key the key
	 */
	private JNode(JNode parent, String key) {
		type			= JType.NULL;
		this.key		= key;
		parameterName	= null;
		this.parent		= parent;
		value			= "null";
		children		= null;
	}
}

enum JType {
	STRING,
	NUMBER,
	BOOLEAN,
	NULL,
	OBJECT,
	ARRAY
}
