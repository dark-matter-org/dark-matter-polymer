package com.connectifex.base;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeMap;
import java.util.TreeSet;

import org.dmd.dmc.DmcInputStreamIF;
import org.dmd.dmc.DmcOutputStreamIF;
import org.dmd.dmc.DmcValueException;
import org.dmd.dmc.types.CheapSplitter;
import org.dmd.util.exceptions.DebugInfo;
import org.dmd.util.exceptions.ResultException;
import org.dmd.util.formatting.PrintfFormat;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * The JSONSmartPath provides mechanisms for specifying a path through a JSON
 * structure with optional, in-line "filters" that allow selection of objects
 * from arrays of objects.
 * 
 * NOTE: current limitation - we don't take care of comparison values that might include slashes
 */
public class JSONSmartPath {
	
	private static String PATHSEP 	= "/";
	private static String OPEN 		= "[";
	private static String CLOSE 	= "]";
	private static String EQUALS 	= "==";
	
	private String path;
	
	private ArrayList<PathNode>	nodes;

	public JSONSmartPath() {
		path = null;
	}
	
	public JSONSmartPath(String path) throws DmcValueException {
		this.path = path;
		initialize();
	}
	
	/**
	 * A small hack to allow us to throw a ResultException instead of a DmcValueException when we're
	 * using this internally.
	 * @param path
	 * @throws ResultException 
	 * @throws DmcValueException
	 */
	public JSONSmartPath(String path, boolean throwResultException) throws ResultException {
		this.path = path;
		try {
			initialize();
		} catch (DmcValueException e) {
			ResultException ex = new ResultException(e);
			ex.moreMessages("You've screwed things up using this internally!");
			throw(ex);
		}
	}
	
	public JSONSmartPath(JSONSmartPath jsp) {
		path = new String(jsp.path);
		try {
			initialize();
		} catch (DmcValueException e) {
			throw new IllegalStateException(e);
		}
	}
	
	public void serializeIt(DmcOutputStreamIF dos) throws Exception {
		dos.writeUTF(path);
	}

	public void deserializeIt(DmcInputStreamIF dis) throws Exception {
		path = dis.readUTF();
	}
	
	private void initialize() throws DmcValueException {
		if (path == null)
			throw(new DmcValueException("The value for a JSONSmartPath cannot be null"));
		
		if (path.length() == 0)
			throw(new DmcValueException("The value for a JSONSmartPath cannot be zero length"));
		
		if (!path.startsWith(PATHSEP))
			throw(new DmcValueException("The value for a JSONSmartPath must start with: " + PATHSEP));
		
		nodes = new ArrayList<>();
		
		// Note, we don't trim the tokens
		ArrayList<String> tokens = CheapSplitter.split(path, '/', false, false);
		for(String token: tokens) {
			if (token.startsWith(OPEN)) {
				nodes.add(parseFilter(token));
			}
			else {
				nodes.add(new PathNode(token.trim()));
			}
		}
	}
	
	/**
	 * @return the number of elements in the path, this includes keys and filters
	 */
	public int pathElements() {
		return(nodes.size());
	}
	
	public String toString() {
		return(path);
	}
	
		
	/**
	 * Analyzes the specified object and provides representative smart paths for all possible
	 * paths through the hierarchy.
	 * @param object the object to be analyzed
	 * @return a sorted set of paths along with example values for the primitives
	 */
	static public TreeMap<String,TreeSet<String>> getPaths(JSONObject object){
//		TreeSet<String> rc = new TreeSet<>();
		TreeMap<String,TreeSet<String>>	pathAndValue = new TreeMap<>(Collator.getInstance(Locale.ENGLISH));
		
		descendObject("/", object, pathAndValue);
		
		return(pathAndValue);
	}
	
	/**
	 * Analyzes the specified object and provides representative smart paths for all possible
	 * paths through the hierarchy along with example values.
	 * @param object the object to be analyzed
	 * @return a sorted set of paths along with example values for the primitives
	 */
	static public String getPathsAndExamplesAsString(JSONObject object){
		return(getPathsAndExamplesAsString(object, -1));
	}
	
	/**
	 * Analyzes the specified object and provides representative smart paths for all possible
	 * paths through the hierarchy along with example values.
	 * @param object
	 * @param display the number of example values to display. If 0, we display none, if < 0, we display all, if > 0 we display up to that number.
	 * @return
	 */
	static public String getPathsAndExamplesAsString(JSONObject object, int display) {
		StringBuilder sb = new StringBuilder();
		
		TreeMap<String,TreeSet<String>> pathAndValue = getPaths(object);
		
		int longest = 0;
		for(String key: pathAndValue.keySet()) {
			if (key.length() > longest)
				longest = key.length();
		}
		
		PrintfFormat format = new PrintfFormat("%-" + longest + "s");
		
		for(String key: pathAndValue.keySet()) {
			TreeSet<String> examples = pathAndValue.get(key);
			
			if (examples == null)
				System.out.println(key);
			else {
				if (display == 0) {
					// Don't display examples
					sb.append(format.sprintf(key));
				}
				else {
					sb.append(format.sprintf(key) + " -- ");
					int displayed = 1;
					Iterator<String> it = examples.iterator();
					while(it.hasNext()) {
						
						String ex = it.next();
						sb.append(ex);
						
						if (display > 0) {
							// Only display the specified number of examples
							if (displayed == display)
								break;
							displayed++;
						}
							
						if (it.hasNext())
							sb.append(", ");
					}
					sb.append("\n");
				}
			}
		}
		
		return(sb.toString());
	}
	
	static private void descendObject(String basePath, JSONObject object, TreeMap<String,TreeSet<String>> pathAndValue) {
		Iterator<String> keys = object.keys();
		while(keys.hasNext()) {
			String key = keys.next();
			Object value = object.get(key);
			
			String newPath = basePath + key;
			
			if (value instanceof JSONObject) {
				pathAndValue.put(newPath, null);
				descendObject(newPath + "/", (JSONObject) value, pathAndValue);
			}
			else if (value instanceof JSONArray) {
				pathAndValue.put(newPath, null);
				descendArray(newPath + "/", (JSONArray) value, pathAndValue);
			}
			else {
				TreeSet<String> examples = pathAndValue.get(newPath);
				if (examples == null) {
					examples = new TreeSet<>();
					pathAndValue.put(newPath, examples);
				}
				examples.add(value.toString());				
			}
			
		}
	}
	
	static private void descendArray(String basePath, JSONArray array, TreeMap<String,TreeSet<String>> pathAndValue) {
		if (array.isEmpty())
			return;
		
		// We only bother with arrays of objects
		if (array.get(0) instanceof JSONObject) {
			if (array.length() == 1) {
				// If just one object, we don't bother indicating that we need filtering
				descendObject(basePath, (JSONObject)array.get(0), pathAndValue);
			}
			else {
				String filterPath = basePath + "[key==value]/";
				Iterator<Object> it = array.iterator();
				while(it.hasNext()) {
					Object value = it.next();
					if (value instanceof JSONObject)
						descendObject(filterPath, (JSONObject)value, pathAndValue);
					else
						throw(new IllegalStateException("Mix of objects and primitives/arrays in JSONArray"));
				}
			}
		}
	}
	
	/**
	 * We walk the JSON document and try to find a primitive value based
	 * on our specified path.
	 * @param baseObject the base object
	 * @return the value or null.
	 */
	public String getValue(JSONObject baseObject) {
		return(getValue((Object)baseObject));
	}
	
	/**
	 * We walk the JSON document and try to find a primitive value based
	 * on our specified path.
	 * @param baseObject the base object
	 * @return the value or null.
	 * @throws ResultException if the first part of your path is not a key
	 */
	public String getValue(JSONArray baseObject) throws ResultException {
		if (nodes.size() >= 1) {
			if (nodes.get(0).filterKey == null) {
				ResultException ex = new ResultException("You are accessing an array. Your first path element should be a filter based on a key.");
				throw(ex);
			}
		}
		return(getValue((Object)baseObject));
	}
	
	/**
	 * We walk the JSON document and try to find a primitive value based
	 * on our specified path.
	 * @param baseObject the base object
	 * @return the value or null.
	 */
	private String getValue(Object baseObject) {
		String rc = null;
		
		// This will be either a JSONObject or a JSONArray
		Object currentThing	= baseObject;
		
		Iterator<PathNode> it = nodes.iterator();
		while(it.hasNext()) {
			PathNode node = it.next();
			
//			DebugInfo.debug(node.toString());
			
			if (node.key == null) {
				// We're filtering
				if (currentThing instanceof JSONArray) {
					JSONArray array = (JSONArray) currentThing;
					
					if (array.isEmpty()) {
						// Well, that's not useful
						break;
					}
					else {
						Object obj = array.get(0);
						if (obj instanceof JSONObject) {
							// We're going to assume an array of objects
							
							currentThing = findMatch(array, node);
							
							if (currentThing == null) {
								// Couldn't find a match for some reason
								break;
							}
						}
					}
				}
				else {
					// We don't filter on objects, just arrays
					break;
				}
			}
			else {
				if (currentThing instanceof JSONObject) {
					JSONObject object = (JSONObject) currentThing;
				
					Object something = null;
					if (object.has(node.key))
						something = object.get(node.key);
					
					if (something == null) {
						// Don't have that key - error or fail silently
						break;
					}
					else {
						// Have something - are we done?
						if (it.hasNext()) {
							// Not done - so this should be either an object or an object array
							if (something instanceof JSONObject) {
								// Continue processing
								currentThing = something;
							}
							else if (something instanceof JSONArray){
								// Okay - but does it contains objects?
								currentThing = something;
							}
						}
						else {
							// This should be the end of it - so not an object or array
							if (something instanceof JSONObject) {
								// Error or fail silently
							}
							else if (something instanceof JSONArray) {
								// Error or fail silently
							}
							else {
								// This is String, Integer, Boolean or NULL
								rc = something.toString();
							}
						}
					}
				}
			}
		}
		
		return(rc);
	}
	
	
	/**
	 * Cycles through the objects in the array trying to find one that has the filter
	 * key and the specfiid value.
	 * @param objectArray an array of objects
	 * @param filter the filter we have to match
	 * @return a matching JSONObject or null
	 */
	private JSONObject findMatch(JSONArray objectArray, PathNode filter) {
		JSONObject rc = null;
//		DebugInfo.debug("Filtering - " + filter);
		
		Iterator<Object> it = objectArray.iterator();
		while(it.hasNext()) {
			Object something = it.next();
			if (something instanceof JSONObject) {
				JSONObject obj = (JSONObject) something;
				
				Iterator<String> kit = obj.keys();
//				while(kit.hasNext()) {
//					DebugInfo.debug("  key: " + kit.next());
//				}
				
//				DebugInfo.debug("Trying to get key: " + filter.filterKey);
				if (obj.has(filter.filterKey)) {
					Object value = obj.get(filter.filterKey);
					
					if (value != null) {
						// We've got something = see if we have a match
						if (value.toString().equals(filter.filterValue)) {
							// We have a winner!
							rc = obj;
							break;
						}
					}
				}
			}
			else {
				// This isn't an object - naughty, naughty - we have a mixed
				// array - keep going, but we might want to throw a fit
			}
		}
		
		return(rc);
	}
	
	/**
	 * NOTE: current limitation - we don't take care of comparison values that might include slashes
	 * @param filter
	 * @return
	 * @throws DmcValueException
	 */
	private PathNode parseFilter(String filter) throws DmcValueException {
		PathNode rc = null;
		
		if (!filter.endsWith(CLOSE))
			throw new DmcValueException("Missing ] in filter expression: " + filter );
		
		if (filter.contains(EQUALS)) {
			int eqPos = filter.indexOf(EQUALS);
			
			// Skip the opening bracket
			String key = filter.substring(1, eqPos);
			
			if (eqPos + EQUALS.length() + 1 > filter.length())
				throw new DmcValueException("Missing value after " + EQUALS + " in path filter.");
			
			String value = filter.substring(eqPos + EQUALS.length(), filter.length() - 1);
			
			rc = new PathNode(key,value);
		}
		else
			throw new DmcValueException("Missing == in filter expression: " + filter );
		
		return(rc);
	}
	
	class PathNode {
		String key;
		String filterKey;
		String filterValue;
		
		public PathNode(String key) {
			this.key = key;
		}
		
		public PathNode(String filterKey, String filterValue) {
			this.key 			= null;
			this.filterKey 		= filterKey;
			this.filterValue	= filterValue;
		}
		
		public String toString() {
			if (key == null) {
				return("Filter: " + filterKey + " == " + filterValue);
			}
			return("   Key:" + key);
		}
	}
}
