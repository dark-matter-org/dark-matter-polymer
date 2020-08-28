package com.connectifex.polymer.mdl.server.extended.mapping.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import org.dmd.util.exceptions.ResultException;

import com.connectifex.polymer.mdl.server.extended.mapping.MappingDirective;
import com.connectifex.polymer.mdl.shared.generated.enums.ElementTypeEnum;
import com.connectifex.polymer.mdl.shared.generated.enums.PrimitiveTypeEnum;

/**
 * The JsonElement is used to represent an element in an overall JSON structure.
 */
public class JsonElement {
	
	private final static char SLASH = '/';
	private final static String OPEN_BRACKET = "[";
	private final static String CLOSE_BRACKET = "]";

	private final static String ASTERISK 	= "*";
	private final static String COLON 		= ":";
	
	private final static String OPEN_CURLY 	= "{";
	private final static String CLOSE_CURLY = "}";
	private final static String COMMA = ",";

	private final static String INDENT = "    ";
	
	// The directive from which this element was first created
	private MappingDirective				directive;

	private ElementTypeEnum					type;
	
	private TreeMap<String, JsonElement>	children = new TreeMap<>();
	
	// When we insert down the hierarchy, this is the deepest element that got inserted
	// Cheating a bit here by making this static
	private static JsonElement 				deepestElement;
	
	// The name of this element as it appears in path
	private String 							name;
	
	// For a primitive element, this will be the variable name inserted into the
	// JSON schema - it will be shared by element in the other schema
	//
	private String 							variableName;
	
	// Whether this leaf element is a STRING, BOOLEAN or NUMBER
	// Originally made this distinction, but, from the schema generation point of view
	// we don't really need this since all values in the schemas are actually defined
	// as variables and thus, have to be quoted.
	private PrimitiveTypeEnum				valueType;
	
	public JsonElement() {
		valueType = PrimitiveTypeEnum.STRING;
	}
	
	private JsonElement(MappingDirective md, String name, ElementTypeEnum type) {
		directive = md;
		
//		if (name.contains(COLON)) {
//			int pos = name.indexOf(COLON);
//			if ( (pos+1) < name.length()) {
//				this.name = name.substring(pos+1);
//			}
//			else {
//				this.name = name;
//			}
//		}
//		else {
//			this.name = name;
//		}
		
		this.name = name;
		
		this.type = type;
		valueType = PrimitiveTypeEnum.STRING;
	}
	
	/**
	 * @return the raw name of this element.
	 */
	public String name() {
		return(name);
	}
	
	public ElementTypeEnum type() {
		return(type);
	}
	
	public void variableName(String variableName) {
		this.variableName = variableName;
	}
	
	public void valueType(PrimitiveTypeEnum type) {
		this.valueType = type;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("     name: " + name + "\n");
		sb.append("     type: " + type + "\n");
		sb.append("valueType: " + valueType + "\n");
		if (children == null)
			sb.append(" children: 0" + "\n");
		else
			sb.append(" children: " + children.size() + "\n");
		
		return(sb.toString());
	}
	
	/**
	 * Inserts the specified from/to element and returns the last element in the hierarchy.
	 * @param map the Map info
	 * @param side whether we're inserting on the FROM or the TO side
	 * @return the deepest element.
	 * @throws ResultException 
	 */
	public JsonElement insert(MappingDirective md, ArrayList<String>	pathTokens) throws ResultException {
		if (name == null) {
			directive = md;
			
			name = pathTokens.get(0);
			
//			if (isIndexValueSpec(name)) {
			if (name.endsWith(ASTERISK)) {
				throw(new ResultException("An attribute path shouldn't start with an array element."));
			}
			
			if (pathTokens.size() < 2) {
				throw(new ResultException("You must have at least 2 tokens in an attribute path "));
			}
			
			type = ElementTypeEnum.OBJECT;
		}
		
		insert(md, this, pathTokens, 1);
		
		return(deepestElement);
	}
	
	private boolean isIndexValueSpec(String token) throws ResultException {
		if (token.contains(OPEN_BRACKET)) {
			if (token.contains(CLOSE_BRACKET)) {
				return(true);
			}
			throw(new ResultException("Mismatched square brackets"));
		}
		if (token.contains(CLOSE_BRACKET)) {
			if (token.contains(OPEN_BRACKET)) {
				return(true);
			}
			throw(new ResultException("Mismatched square brackets"));
		}
		return(false);
	}
	
	private JsonElement getChild(String name) {
		return(children.get(name));
	}
	
	private void addchild(JsonElement child) {
		if (children == null)
			children = new TreeMap<String, JsonElement>();
		children.put(child.name, child);
	}
	
	private void insert(MappingDirective md, JsonElement parent, ArrayList<String> pathTokens, int depth) throws ResultException {
		String elementName = pathTokens.get(depth);
		if (isIndexValueSpec(elementName)) {
//			parent.type = ElementTypeEnum.INDEXED_ARRAY;
			
			elementName = elementName.replace(OPEN_BRACKET, "");
			elementName = elementName.replace(CLOSE_BRACKET, "");
		}
		
		ElementTypeEnum etype = ElementTypeEnum.OBJECT;
		if (elementName.endsWith(ASTERISK)) {
			etype = ElementTypeEnum.ARRAY;
			elementName = elementName.replace(ASTERISK, "");
		}
		
		if (elementName.contains(COLON)) {
			int pos = elementName.indexOf(COLON);
			if ( (pos+1) < elementName.length()) {
				elementName = elementName.substring(pos+1);
			}			
		}
		
		JsonElement existing = parent.getChild(elementName);
		
		if (existing == null) {
			if (pathTokens.size() == (depth+1)) {
				etype = ElementTypeEnum.PRIMITIVE;
			}
			
			existing = new JsonElement(md, elementName,etype);
			parent.addchild(existing);
			
			deepestElement = existing;
		}
		else {
			if (existing.type != etype) {
				ResultException ex = new ResultException("Type mismatch between path elements: " + elementName);
				ex.moreMessages("Originally " + existing.type + " from line: " + existing.directive.getLineNumber() + " but now: " + etype + " at line: " + md.getLineNumber());
				ex.moreMessages("This usually occurs when you've missed an * to specify that a path element is an array");
				ex.moreMessages("In file: " + md.getFile());
				throw(ex);
			}
		}

		if (pathTokens.size() > (depth+1)) {
			existing.insert(md, existing,pathTokens,depth+1);
		}
	}
	
	public String getSchema() {
		StringBuilder sb = new StringBuilder();
		sb.append(OPEN_CURLY + "\n");
		
		
		sb.append(INDENT + "\"" + name + "\": " + OPEN_CURLY + "\n");
		
		Iterator<JsonElement> it = children.values().iterator();
		while(it.hasNext()) {
			JsonElement child = it.next();
			child.addToHierarchy(sb, INDENT + INDENT);
			
			if (it.hasNext())
				sb.append(COMMA + "\n");
		}
		
		sb.append("\n" + INDENT + CLOSE_CURLY + "\n");
		
		sb.append(CLOSE_CURLY + "\n");
		
		
//		JSONObject rc = new JSONObject(sb.toString());
		
		
		return(sb.toString());
	}
	
	private void addToHierarchy(StringBuilder sb, String indent) {
		Iterator<JsonElement> it = null;
		sb.append(indent +  "\"" + name + "\": ");
		
		switch(type) {
		case ARRAY:
//		case INDEXED_ARRAY:
			sb.append(OPEN_BRACKET + "\n");
			
			sb.append(indent + INDENT + OPEN_CURLY + "\n");

			it = children.values().iterator();
			while(it.hasNext()) {
				JsonElement child = it.next();
				child.addToHierarchy(sb, indent + INDENT + INDENT);
				
				if (it.hasNext())
					sb.append(COMMA + "\n");
			}

			sb.append("\n" + indent + INDENT + CLOSE_CURLY + "\n");

			sb.append("\n" + indent + CLOSE_BRACKET);
			

			break;
		case OBJECT:
			sb.append(OPEN_CURLY + "\n");

			it = children.values().iterator();
			while(it.hasNext()) {
				JsonElement child = it.next();
				child.addToHierarchy(sb, indent + INDENT);
				
				if (it.hasNext())
					sb.append(COMMA + "\n");
			}

			sb.append("\n" + indent + CLOSE_CURLY);
			
			break;
		case PRIMITIVE:
			if (valueType == PrimitiveTypeEnum.STRING) {
				sb.append("\"${" + variableName + "}\"");
			}
			else {
				// NOTE: it looks like everything has to be in quotes, whether String
				// or numbers or booleans
//				sb.append("${" + variableName + "}");
				sb.append("\"${" + variableName + "}\"");
			}
			break;
		}
	}
	
}
