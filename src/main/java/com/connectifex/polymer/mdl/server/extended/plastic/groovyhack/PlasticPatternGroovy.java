package com.connectifex.polymer.mdl.server.extended.plastic.groovyhack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The PlasticPatternGroovy class is the groovisized version of the extended PlasticPattern class. All sanity checking
 * has been stripped, because we would only get this far if we passed the sanity checking of PlasticPattern.
 * 
 * We hand copy this code into our src/main/resources/plastic-library and drop the package indication.
 */
public class PlasticPatternGroovy {

	private ArrayList<PlasticGroupAndNameGroovy> 		groupInfo;
	private String										patternString;
	private String										normalizedForm;
	
	private ArrayList<Matcher>							matchers;
	private TreeMap<String,PlasticGroupAndNameGroovy>	groups;	
	private ArrayList<GroupOrLiteralGroovy>				normalizedFormInfo;
	private Matcher										lastMatch;
	
	private int maxGroups;

    public PlasticPatternGroovy(ArrayList<PlasticGroupAndNameGroovy> groupInfo, String pattern, String normalizedForm){
    	this.groupInfo 		= groupInfo;
    	this.patternString 	= pattern;
    	this.normalizedForm	= normalizedForm;
    	
    	initializeMatchers();
    	initializeGroups();
        
    	if (normalizedForm != null) {
    		normalizedFormInfo = GroupLiteralParserGroovy.parse(this, normalizedForm);
    	}
    }

    public boolean hasGroup(String groupName){
    	initializeGroups();
    	if (groups.get(groupName) == null)
    		return(false);
    	return(true);
    }
    
    public PlasticGroupAndNameGroovy getGroupByName(String groupName){
    	return(groups.get(groupName));
    }
    
    public Iterator<String> getGroupNames(){
    	return(groups.keySet().iterator());
    }
    
    public String getGroupNamesAsString(){
    	String rc = "";
    	Iterator<String> it = groups.keySet().iterator();
    	while(it.hasNext()) {
    		String group = it.next();
    		rc = rc + group;
    		if (it.hasNext())
    			rc = rc + ", ";
    	}
    	return(rc);
    }
    
    void initializeMatchers(){
    	if (matchers == null){
	    	matchers = new ArrayList<>();
	    	
    		String patternStr = patternString;
    		Pattern pattern = Pattern.compile(patternStr);
    		Matcher matcher = pattern.matcher("");
    		
    		if (matcher.groupCount() > maxGroups){
    			maxGroups = matcher.groupCount();
    		}
    		
    		matchers.add(matcher);
    	}
    }
    
    void initializeGroups() {
    	if (groups == null){
	    	groups = new TreeMap<>();
	    	
	    	Iterator<PlasticGroupAndNameGroovy> it = groupInfo.iterator();
	    	TreeSet<Integer> numbers = new TreeSet<>();
	    	
	    	while(it.hasNext()){
	    		PlasticGroupAndNameGroovy gan = it.next();
	    		groups.put(gan.getGroupName(), gan);
	    		numbers.add(gan.getGroupNumber());
	    	}
    	}
    }
    
    public String getGroupValue(String groupName){
    	String rc = null;
    	
    	if (lastMatch == null)
    		return(rc);
    	
    	PlasticGroupAndNameGroovy gan = groups.get(groupName);
    	
    	if (gan == null){
    		throw(new IllegalStateException("No such group: " + groupName));
    	}
    	
    	rc = lastMatch.group(gan.getGroupNumber());
    	
    	return(rc);
    }
    
    public String getNormalizedStringFromLastMatch(){
    	if (normalizedForm == null)
    		return(null);
    	
    	if (lastMatch == null)
    		return(null);
    	
    	HashMap<String,String> groupValues = getGroupValues();
    	StringBuilder sb = new StringBuilder();
    	
    	for(GroupOrLiteralGroovy gol: normalizedFormInfo){
    		sb.append(gol.toString(groupValues));
    	}
    	
    	return(sb.toString());
    }
    
    /**
     * @param value the string value to be tested
     * @return true if any of our patterns match the value
     */
    public boolean matches(String value){
    	if (value == null)
    		return(false);
    	
    	boolean rc = false;
    	lastMatch = null;
    	
    	for(Matcher matcher: matchers){
    		if (matcher.reset(value).matches()){
    			rc = true;
    			lastMatch = matcher;
    			break;
    		}
    	}
    	
    	return(rc);
    }
    
    public boolean find(String value){
    	boolean rc = false;

    	for(Matcher matcher: matchers){
    		if (matcher.reset(value).find()){
    			rc = true;
    			break;
    		}
    	}

    	return(rc);
    }
    
    public HashMap<String,String> getGroupValues(){
    	HashMap<String, String> rc = new HashMap<>();
    	
    	if (lastMatch != null){
    		for(PlasticGroupAndNameGroovy gan: groups.values()){
    			// If we happen to have more groups defined than what we matched - don't
    			// go any further
    			if (gan.getGroupNumber() <= lastMatch.groupCount())
    				rc.put(gan.getGroupName(), lastMatch.group(gan.getGroupNumber()));
    		}
    	}
    	
    	return(rc);
    }
}

