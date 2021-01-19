package com.connectifex.polymer.mdl.shared.types;

import java.util.ArrayList;
import java.util.TreeSet;

import org.dmd.dmc.DmcInputStreamIF;
import org.dmd.dmc.DmcOutputStreamIF;
import org.dmd.dmc.DmcValueException;
import org.dmd.util.exceptions.ResultException;

import com.connectifex.polymer.mdl.server.extended.plastic.PlasticBaseDefinition;
import com.connectifex.polymer.mdl.server.extended.util.CharacterEscapes;
import com.connectifex.polymer.mdl.server.extended.util.MergedParameterSet;
import com.connectifex.polymer.mdl.server.extended.util.ParameterSet;


public class TemplateString {
	
	// Marker length
	final public static int		ML = 2;
	
	final public static String START_MARKER = "{{";
	final public static String END_MARKER = "}}";
	
	protected ArrayList<Element>	elements;

	protected String format;

	public TemplateString() {
		format = null;
	}
	
	public TemplateString(String format)  throws DmcValueException {
		this.format = format;
	}
	
	public TemplateString(TemplateString ts) {
		format = new String(ts.format);
	}
	
	public void serializeIt(DmcOutputStreamIF dos) throws Exception {
		dos.writeUTF(format);
	}

	public void deserializeIt(DmcInputStreamIF dis) throws Exception {
		format 	= dis.readUTF();
	}
	
	/**
	 * Fills the template with any required parameters
	 * @param ps the available parameters
	 * @return the filled template
	 */
	public String fillTemplate(ParameterSet ps) {
		return(fillTemplate(ps, null));
	}
	
	/**
	 * Fills the template with any required parameters
	 * @param ps the available parameters
	 * @return the filled template
	 */
	public String fillTemplate(MergedParameterSet mps) {
		return(fillTemplate(mps, null));
	}
	
	/**
	 * Fills the template with any required parameters and performs character escapes
	 * if the context requires it e.g. when filling URI templates
	 * @param ps the available parameters
	 * @param escapes character escapes if required, or null
	 * @return the filled template
	 */
	public String fillTemplate(ParameterSet ps, CharacterEscapes escapes) {
    	if (elements == null)
    		throw(new IllegalStateException("The TemplateString has not been initialized yet!"));

    	StringBuilder sb = new StringBuilder();
		
    	for(Element element: elements){
    		if (element.valueName == null)
    			sb.append(element.text);
    		else {
    			String value = ps.getValue(element.valueName);
    			if ( (value != null) && (escapes != null)){
    				value = escapes.escapeIfNecessary(value);
    			}
    			sb.append(value);
    		}
    	}
		
		
		return(sb.toString());
	}
	
	/**
	 * Fills the template with any required parameters and performs character escapes
	 * if the context requires it e.g. when filling URI templates
	 * @param ps the available parameters
	 * @param escapes character escapes if required, or null
	 * @return the filled template
	 */
	public String fillTemplate(MergedParameterSet ps, CharacterEscapes escapes) {
    	if (elements == null)
    		throw(new IllegalStateException("The TemplateString has not been initialized yet!"));

    	StringBuilder sb = new StringBuilder();
		
    	for(Element element: elements){
    		if (element.valueName == null)
    			sb.append(element.text);
    		else {
    			String value = ps.getValue(element.valueName);
    			if ( (value != null) && (escapes != null)){
    				value = escapes.escapeIfNecessary(value);
    			}
    			sb.append(value);
    		}
    	}
		
		return(sb.toString());
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
//	    	String insertMarker = START_MARKER;
	    	// Shorthand for the marker length
//	    	int ML = insertMarker.length();
	    	elements = new ArrayList<Element>();
	    	
	    	if (this.format == null)
	    		throw(new IllegalStateException("A TemplateString should never have a null format"));
	    	
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
    	    		
//        			System.out.println("FOUND END " + endPOS);
    				// We've hit the terminating IM, grab the value name
    				elements.add(new Element(null,initFormat.substring(IMstart+ML, endPOS).trim()));
    				textPOS = endPOS+ML;
    				i 		= endPOS+ML;
    				wantEND = false;
    			}
    			else{
    	    		int startPOS 	= initFormat.indexOf(START_MARKER, i);
    	    		
    	    		if (startPOS == -1)
    	    			break;

//    	    		System.out.println("FOUND START " + startPOS);
    				IMstart = startPOS;
    				wantEND 	= true;
    				
    				if (textPOS < IMstart){
    					elements.add(new Element(initFormat.substring(textPOS,IMstart),null));
    				}
    				
    				i = IMstart+ML;
    			}
	    		
	    	}
	    	
	//    	System.out.println("DONE ");
	    	
	    	if (wantEND){
	    		// We had a start insert marker but never found the closing one
				ResultException ex = new ResultException("Unmatched insert marker: " + START_MARKER + "" + showErrorLocation(IMstart, initFormat));
				ex.setLocationInfo(def.getFile(), def.getLineNumber());
				throw(ex);   		
	    	}
	    	
	    	if (textPOS < initFormat.length()){
	    		elements.add(new Element(initFormat.substring(textPOS), null));
	    	}
    	}
    }
    
    /**
     * Checks to see if any of our required parameters are missing from the ParameterSet.
     * @param ps the ParameterSet
     * @return null if all is well or a list of the missing parameters.
     */
    public String getMissingParameters(ParameterSet ps) {
    	if (elements == null)
    		throw(new IllegalStateException("You haven't initialized the TemplateString"));

    	StringBuilder sb = null;
    	for(Element e: elements){
    		if (e.valueName != null){
    			if (ps.getValue(e.valueName) == null) {
    				if (sb == null) {
    					sb = new StringBuilder();
    					sb.append(e.valueName);
    				}
    				else
    					sb.append(", " + e.valueName);
    				
    			}
     		}
    	}
    	if (sb == null)
    		return(null);
    	return(sb.toString());
    }
    
    /**
     * Checks to see if any of our required parameters are missing from the MergedParameterSet.
     * @param mps the MergedParameterSet
     * @return null if all is well or a list of the missing parameters.
     */
    public String getMissingParameters(MergedParameterSet mps) {
    	if (elements == null)
    		throw(new IllegalStateException("You haven't initialized the TemplateString"));

    	StringBuilder sb = null;
    	for(Element e: elements){
    		if (e.valueName != null){
    			if (mps.getValue(e.valueName) == null) {
    				if (sb == null) {
    					sb = new StringBuilder();
    					sb.append(e.valueName);
    				}
    				else
    					sb.append(", " + e.valueName);
    				
    			}
     		}
    	}
    	if (sb == null)
    		return(null);
    	return(sb.toString());
    }
    
    public String getParameterNames() {
    	if (elements == null)
    		throw(new IllegalStateException("You haven't initialized the TemplateString"));

    	StringBuilder rc = new StringBuilder();

    	for(Element e: elements){
    		if (e.valueName != null) {
    			if (rc.length() > 0)
    				rc.append(", ");
    			rc.append(e.valueName);
    		}
    	}

    	return(rc.toString());
    }

    /**
     * @return the set of parameter names in this template.
     */
    public TreeSet<String> getParameterNameSet() {
    	if (elements == null)
    		throw(new IllegalStateException("You haven't initialized the TemplateString"));

    	TreeSet<String> rc = new TreeSet<String>();

    	for(Element e: elements){
    		if (e.valueName != null) {
    			rc.add(e.valueName);
     		}
    	}

    	return(rc);
    }

    public int getParameterCount() {
    	int rc = 0;
    	if (elements == null)
    		throw(new IllegalStateException("You haven't initialized the TemplateString"));
    	
    	for(Element e: elements) {
    		if (e.valueName != null)
    			rc++;
    	}
    	
    	return(rc);
    }


    /**
     * The Element stores either a text chunk or a the name of a value to be inserted.
     */
    class Element {
    	String text;
    	String valueName;
    	
    	Element(String t, String v){
    		if (t != null){
    			text = t;
    		}
    		valueName = v;
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
