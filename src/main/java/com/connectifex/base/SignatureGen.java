// Copyright 2020 connectifex
// 
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
// 
//      http://www.apache.org/licenses/LICENSE-2.0
// 
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//

package com.connectifex.base;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import org.dmd.dmu.util.json.PrettyJSON;
import org.dmd.util.exceptions.DebugInfo;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * The SignatureGen utility takes JSON payload and generates a unique signature for the structure
 * to a specified depth. This signature can then be used to look up how the payload should be
 * decomposed into multiple RESTCONF requests with their associated URLs.
 *
 */
public class SignatureGen {

//	public SignatureGen() {
//		
//	}
	
	/**
	 * Reads the contents of the specified file and, if it contains a valid JSON object, returns the signature.
	 * @param fn a fully qualified file name
	 * @param maxDepth the maximum depth to which we descend when creating the signature.
	 * @return the signature string
	 * @throws IOException 
	 */
	static public String signatureFromFile(String fn, int maxDepth) throws IOException {		
		String line = null;
		StringBuilder json = new StringBuilder();
		
        BufferedReader input =  new BufferedReader  (new FileReader(fn));  
        while ((line = input.readLine()) != null) {  
          json.append(line);
        }  
        input.close();  

		return(signatureFromJSON(json.toString(), maxDepth));
	}
	
	/**
	 * Generates a signature from the specified JSON object. The keys of the top level object
	 * are traversed in sorted order and those that correspond to objects or arrays of objects
	 * are appended to the signature.
	 * <p/>
	 * 
	 * @param json the object to be analysed
	 * @param maxDepth the maximum depth of the analysis
	 * @return the signature
	 */
	static public String signatureFromJSON(String json, int maxDepth) {		
		JSONObject obj = new JSONObject(json);
		JSONObject sortedObj = new JSONObject(PrettyJSON.instance().prettyPrint(obj, true));
		
		StringBuilder sig = new StringBuilder();
		buildSignature(sig, sortedObj, 1, maxDepth);
		
//		System.out.println(JSONSmartPath.getPathsAndExamplesAsString(obj,5) + "\n\n");
		
//		TreeMap<String,TreeSet<String>> paths = JSONSmartPath.getPaths(obj);
//		for(String path:paths.keySet()) {
//			if (path.contains("key==value"))
//				continue;
//			System.out.println(path);
//		}
		
		return(sig.toString());
	}
	
	static private void buildSignature(StringBuilder sig, JSONObject json, int currentDepth, int maxDepth) {
		if (currentDepth > maxDepth)
			return;
		
		Iterator<String>	keys = json.keys();
		TreeSet<String>		sortedKeys = new TreeSet<String>();
		while(keys.hasNext()) {
			sortedKeys.add(keys.next());
		}
		
		for(String key: sortedKeys) {
			 Object obj = json.get(key);
			 if (obj instanceof JSONObject) {
				 if (sig.length() > 0)
					 sig.append("--");
				 sig.append(key);
				 
				 buildSignature(sig, (JSONObject)obj, currentDepth+1, maxDepth);
			 }
			 else if (obj instanceof JSONArray) {
				 if (sig.length() > 0)
					 sig.append("--");
				 sig.append(key);				 
			 }
			 else {
				 DebugInfo.debug("Not handling: " + key + " " + obj.getClass().getName());
			 }
		}
		
	}
	
	
	public ArrayList<String> getUrlPatterns(JSONObject obj, int maxDepth){
		
		System.out.println(JSONSmartPath.getPathsAndExamplesAsString(obj,5) + "\n\n");
		
		ArrayList<String> rc = new ArrayList<>();
		
		analyze(rc, obj, "", 1, maxDepth);
		
		return(rc);
	}
	
	private void analyze(ArrayList<String> urls, JSONObject json, String prefix, int currentDepth, int maxDepth) {
		if (currentDepth > maxDepth)
			return;

		Iterator<String>	keys = json.keys();
		TreeSet<String>		sortedKeys = new TreeSet<String>();
		while(keys.hasNext()) {
			sortedKeys.add(keys.next());
		}

		for(String key: sortedKeys) {
			 Object obj = json.get(key);
			 if (obj instanceof JSONObject) {
				 
			 }
			 else if (obj instanceof JSONArray) {
				 
			 }
			 else if (obj instanceof String) {
				 
			 }
			 else if (obj instanceof Number) {
				 
			 }
			 else if (obj instanceof Boolean) {
				 
			 }
			else{
				// This happens if there is literally a null for the value
			}
		}
		
	}
	
	/**
	 * We scan the given object for keys that contain Strings or Numbers and return them as a |
	 * separated list. This is used when dealing with arrays of objects.
	 * @param obj the object to be scanned
	 * @return a string with keys, separated by |
	 */
	@SuppressWarnings("unused")
	private String getPotentialKeys(JSONObject json) {
		StringBuilder rc = new StringBuilder();

		Iterator<String>	keys = json.keys();
		TreeSet<String>		sortedKeys = new TreeSet<String>();
		while(keys.hasNext()) {
			sortedKeys.add(keys.next());
		}

		for(String key: sortedKeys) {
			 Object obj = json.get(key);
			 if (obj instanceof JSONObject) {
				 
			 }
			 else if (obj instanceof JSONArray) {
				 
			 }
			 else if (obj instanceof String) {
				 if (rc.length() > 0)
					rc.append(" | ");
				rc.append(key);
			 }
			 else if (obj instanceof Number) {
				 if (rc.length() > 0)
					rc.append(" | ");
				rc.append(key);				 
			 }
			 else if (obj instanceof Boolean) {
				 
			 }
			else{
				// This happens if there is literally a null for the value
			}
		}

		return(rc.toString());
	}
}
