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

package com.connectifex.polymer.mdl.shared.types;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import org.dmd.dmc.DmcInputStreamIF;
import org.dmd.dmc.DmcOutputStreamIF;
import org.dmd.dmc.DmcValueException;
import org.dmd.dmc.types.CheapSplitter;
import org.dmd.util.exceptions.ResultException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.connectifex.base.JSONFormatException;
import com.connectifex.polymer.mdl.server.extended.plastic.PlasticBaseDefinition;
import com.connectifex.polymer.mdl.server.extended.plastic.util.PlasticConstants;
import com.connectifex.polymer.mdl.shared.util.PlasticVariableInfo;

/**
 * The PlasticSchema is used to represent an input or output schema used by plastic.
 * It understands the conventions for specifying plastic variables and let's you
 * extract those variables by name.
 */
public class PlasticSchema {
	
	// Marker lengths
	final public static int		SM_ML = 2;
	final public static int		EM_ML = 1;

	final public static String START_MARKER = "${";
	final public static String END_MARKER = "}";
	
	final private static String EQUALS = "=";

	private ArrayList<Element>					elements;
	
	private TreeMap<String,PlasticVariableInfo>	variableInfoByName;

	private String format;

	public PlasticSchema() {
		format = null;
	}
	
	public PlasticSchema(String format) throws DmcValueException {
		this.format = format;
		
		try {
			new JSONObject(format.replaceAll("\\\\n","\\\n"));
		}
		catch(JSONException ex) {
			StringBuilder sb = new StringBuilder();
			sb.append("Malformed JSON. " + ex.getMessage() + ":\n");
			
			ArrayList<String> tokens = CheapSplitter.split(format.replaceAll("\\\\n","\\\n"), '\n', false, false);
			
			int lineNum = 1;
			Iterator<String> it = tokens.iterator();
			while(it.hasNext()) {
				String line = it.next();
				if (lineNum < 10)
					sb.append("           00" + lineNum + "  ");
				else if (lineNum < 100)
					sb.append("           0" + lineNum + "  ");
				else
					sb.append("           " + lineNum + "  ");
				
				sb.append(line);
				if (it.hasNext())
					sb.append("\n");
				lineNum++;
			}

			DmcValueException dve = new DmcValueException(sb.toString());
			throw(dve);
		}
	}
	
	public PlasticSchema(PlasticSchema pt) {
		this.format = new String(pt.format);
	}
	
	public void serializeIt(DmcOutputStreamIF dos) throws Exception {
		dos.writeUTF(format);
	}

	public void deserializeIt(DmcInputStreamIF dis) throws Exception {
		format 	= dis.readUTF();
	}
	
	@Override
	public String toString() {
		// Some tricky stuff here. If we've used preserveNewlines in the attribute definition,
		// we'll have to strip out \n when we return this as a pure string
		if (format.contains("\\n"))
			return(format.replaceAll("\\\\n","\\\n"));
		
		return(format);
	}
	
    /**
     * Parses the format and breaks it down into Elements, which are either chunks of
     * text or references to named values that will be provided by a Section.
     * @param insertMarker a string that indicates where named values may be inserted in the format
     * @throws DmcValueException 
     * @throws ResultException 
     */
    public void initialize(PlasticBaseDefinition def) throws ResultException {
    	// Ensure we only initialize once
    	if (elements == null) {
	    	elements 	= new ArrayList<>();
	    	variableInfoByName		= new TreeMap<>();
	    	
	    	if (this.format == null)
	    		throw(new IllegalStateException("A PlasticSchema should never have a null format"));
	    	
	    	String initFormat = this.format.replaceAll("\\\\n","\\\n");
	    	
	    	// Note: needed this to get rid of the single space at the beginning of
	    	// the lines - not sure why the behavior of this is different than what happens
	    	// with the template definition stuff????
	    	initFormat = initFormat.replaceAll("\\\n ", "\\\n");
	    	
	    	boolean wantEND 	= false;
	    	int		IMstart = 0;
	    	int		textPOS = 0;
	    	
	    	for(int i=0; i<initFormat.length(); ){
    			if (wantEND){
    	    		int endPOS		= initFormat.indexOf(END_MARKER, i);
    	    		
    	    		if (endPOS == -1)
    	    			break;
    	    		
    				// We've hit the terminating IM, grab the value name
    	    		Element element = new Element(null,initFormat.substring(IMstart+SM_ML, endPOS).trim());
    				elements.add(element);
    				
//    				variableInfoByName.put(element.variableName, element);
    				
    				textPOS = endPOS+EM_ML;
    				i 		= endPOS+EM_ML;
    				wantEND = false;
    			}
    			else{
    	    		int startPOS 	= initFormat.indexOf(START_MARKER, i);
    	    		
    	    		if (startPOS == -1)
    	    			break;

    				IMstart = startPOS;
    				wantEND 	= true;
    				
    				if (textPOS < IMstart){
    					elements.add(new Element(initFormat.substring(textPOS,IMstart),null));
    				}
    				
    				i = IMstart+SM_ML;
    			}
	    		
	    	}
	    	
	    	if (wantEND){
	    		// We had a start insert marker but never found the closing one
				ResultException ex = new ResultException("Unmatched insert marker: " + START_MARKER + "" + showErrorLocation(IMstart, initFormat));
				ex.setLocationInfo(def.getFile(), def.getLineNumber());
				throw(ex);   		
	    	}
	    	
	    	if (textPOS < initFormat.length()){
	    		elements.add(new Element(initFormat.substring(textPOS), null));
	    	}
	    		    	
	    	try {
	    		JSONObject json = new JSONObject(initFormat);
		    	descend(json, "");
	    	}
	    	catch(JSONException ex) {
	    		JSONFormatException e = new JSONFormatException(def, ex, initFormat);
	    		throw(e);
	    	}
    	}
    }
    
    public TreeMap<String,PlasticVariableInfo> getVariableInfo(){
    	if (variableInfoByName == null)
    		throw(new IllegalStateException("You haven't initialized the PlasticSchema"));

    	return(variableInfoByName);
    }
    
    private void descend(Object obj, String path) {
    	if (obj instanceof JSONObject) {
    		JSONObject object = (JSONObject) obj;
    		JSONArray names = object.names();
    		if (names != null) {
    			for(int i=0; i<names.length(); i++) {
    				String key = names.getString(i);
    				Object value = object.get(key);
    				
    				if (value instanceof String) {
    					String string = value.toString();
    					if (string.startsWith(START_MARKER) && string.endsWith(END_MARKER)) {
    						String eName = string.substring(2, string.length()-1);
    						if (eName.contains(EQUALS)) {
    							int eqPos = eName.indexOf(EQUALS);
    							eName = eName.substring(0, eqPos);
    						}
//    						DebugInfo.debug("name:  " + eName + " - " + path);
    						
    						PlasticVariableInfo pvi = variableInfoByName.get(eName);
    						if (pvi == null) {
    							pvi = new PlasticVariableInfo(eName);
    							if (path.length() == 0)
    								pvi.addPath(key);
    							else
    								pvi.addPath(path + "/" + key);
    							variableInfoByName.put(eName, pvi);
    						}
    						else {
    							// This should only happen in output schemas where a variable
    							// can occur multiple times
    							pvi.addPath(path);
    						}
    					}
    				}
    				else if (value instanceof JSONObject){
    					if (path.length() == 0)
    						descend(value, key);
    					else
    						descend(value, path + "/" + key);
    				}
    				else if (value instanceof JSONArray){
    					if (path.length() == 0)
    						descend(value, key + PlasticConstants.ARRAY_INDICATOR);
    					else
    						descend(value, path + "/" + key + PlasticConstants.ARRAY_INDICATOR);
    				}
    			}
    		}
    	}
    	else if (obj instanceof JSONArray	) {
    		JSONArray array = (JSONArray) obj;
    		for(int i=0; i<array.length(); i++) {
    			Object entry = array.get(i);
    			descend(entry, path);
    		}
    	}
    	else
    		throw(new IllegalStateException("Can't descend() " + obj.getClass().getName()));
    }
	
    public String getVariableNamesString() {
    	if (elements == null)
    		throw(new IllegalStateException("You haven't initialized the PlasticSchema"));

    	StringBuilder rc = new StringBuilder();

    	for(Element e: elements){
    		if (e.variableName != null) {
    			if (rc.length() > 0)
    				rc.append(", ");
    			rc.append(e.variableName);
    		}
    	}

    	return(rc.toString());
    }
    
    public TreeSet<String>	getVariableNames(){
    	TreeSet<String> rc = new TreeSet<>();

    	if (elements == null)
    		throw(new IllegalStateException("You haven't initialized the PlasticSchema"));

    	for(Element e: elements){
    		if (e.variableName != null)
    			rc.add(e.variableName);
    	}
    	
    	return(rc);
    }
    
    /**
     * @return the names of any variables that are duplicated.
     */
    public TreeSet<String> getVariableNameDuplicates(){
    	if (elements == null)
    		throw(new IllegalStateException("You haven't initialized the PlasticSchema"));

    	TreeSet<String> rc = new TreeSet<>();
    	TreeSet<String> existing = new TreeSet<>();

    	for(Element e: elements){
    		if (e.variableName != null) {
    			if (existing.contains(e.variableName))
    				rc.add(e.variableName);
    			existing.add(e.variableName);
    		}
    	}

    	return(rc);
    }
    
    public String getVariableNameDuplicatesString() {
    	TreeSet<String> dupes = getVariableNameDuplicates();
    	if (dupes.size() == 0)
    		return("");
    	
    	StringBuilder sb = new StringBuilder();
    	for(String dupe: dupes) {
    		sb.append(dupe + " ");
    	}
    	
    	return(sb.toString());
    }
	
    /**
     * The Element stores either a text chunk or a variable name of a value to be inserted.
     */
    class Element {
    	String text;
    	String variableName;
    	
    	// The key paths to this element
    	ArrayList<String> paths;
    	
    	Element(String t, String v){
    		if (t != null){
    			text = t;
    		}
    		if ((v != null) && v.contains(EQUALS)) {
    			int eqPos = v.indexOf(EQUALS);
    			variableName = v.substring(0, eqPos);
    		}
    		else {
    			variableName = v;
    		}
    	}
    	
    	/**
    	 * The segments indicate the 
    	 * @param segments
    	 */
    	void addPath(String path) {
    		
    	}
    }

	private String showErrorLocation(int position, String input){
		StringBuffer sb = new StringBuffer("\n" + input + "\n");
		for(int i=0; i< position; i++){
			sb.append(" ");
		}
		sb.append("^");
		return(sb.toString());
	}

}
