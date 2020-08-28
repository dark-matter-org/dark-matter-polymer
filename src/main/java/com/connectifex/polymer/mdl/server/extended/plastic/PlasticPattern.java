package com.connectifex.polymer.mdl.server.extended.plastic;

import com.connectifex.polymer.mdl.server.extended.plastic.util.GroupLiteralParser;
import com.connectifex.polymer.mdl.server.extended.plastic.util.GroupOrLiteral;
import com.connectifex.polymer.mdl.server.generated.dmw.PlasticGroupAndNameIterableDMW;
// Generated from: org.dmd.util.codegen.ImportManager.getFormattedImports(ImportManager.java:82)
// Called from: org.dmd.dmg.generators.DMWGenerator.dumpExtendedClass(DMWGenerator.java:290)
import com.connectifex.polymer.mdl.server.generated.dmw.PlasticPatternDMW;         // The wrapper we're extending - (DMWGenerator.java:282)
import com.connectifex.polymer.mdl.server.generated.dsd.MdlModuleDefinitionManager;
import com.connectifex.polymer.mdl.shared.generated.dmo.PlasticPatternDMO;         // The wrapper we're extending - (DMWGenerator.java:283)
import com.connectifex.polymer.mdl.shared.generated.types.PlasticGroupAndName;
import com.connectifex.polymer.mdl.shared.generated.types.PlasticVariable;
import com.connectifex.polymer.mdl.util.Manipulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dmd.dms.ClassDefinition;                                           // Used in derived constructors - (DMWGenerator.java:284)
import org.dmd.util.exceptions.DebugInfo;
import org.dmd.util.exceptions.ResultException;


public class PlasticPattern extends PlasticPatternDMW {

	private ArrayList<Matcher>				matchers;
	private TreeMap<String,PlasticGroupAndName>	groups;	
	private ArrayList<GroupOrLiteral>		normalizedFormInfo;
	private Matcher	lastMatch;
	
	private int maxGroups;

    public PlasticPattern(){
        super();
    }

    public PlasticPattern(PlasticPatternDMO dmo, ClassDefinition cd){
        super(dmo,cd);
    }

    @Override
    public void performAdditionalValidation(MdlModuleDefinitionManager definitions) throws ResultException {
    	// Ensure that we only do this once - because the PatternTest may call this to ensure we're initialized
    	if (matchers == null) {
	    	initializeMatchers();
	    	initializeGroups();
	    	
	    	if (getNormalizedForm() != null){
	    		if (groups.size() == 0){
	    			ResultException ex = new ResultException("You cannot specify a normalizedForm if you have not specified any group names.");
	    			ex.setLocationInfo(getFile(), getLineNumber());
	    			throw(ex);
	    		}
	    		
	    		normalizedFormInfo = GroupLiteralParser.parse(this, getNormalizedForm());
	    	}
	    	
	    	if (groups.size() > maxGroups){
				ResultException ex = new ResultException("The maximum number of groups defined in any of your patterns is: " + maxGroups);
				ex.moreMessages("But you have specified " + groups.size() + " groups. You should have a maximum of " + maxGroups);
				ex.setLocationInfo(getFile(), getLineNumber());
				throw(ex);
	    	}
    	}
    }
    
    
    public boolean hasGroup(String groupName) throws ResultException {
    	initializeGroups();
    	if (groups.get(groupName) == null)
    		return(false);
    	return(true);
    }
    
    public PlasticGroupAndName getGroupByName(String groupName){
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
	    	
//	    	StringIterableDMW it = getPatternIterable();
//	    	while(it.hasNext()){
	    		String patternStr = getPattern();
	    		Pattern pattern = Pattern.compile(patternStr);
	    		Matcher matcher = pattern.matcher("");
	    		
	    		if (matcher.groupCount() > maxGroups){
	    			maxGroups = matcher.groupCount();
	    		}
	    		
	    		matchers.add(matcher);
//	    	}
    	}
    }
    
    void initializeGroups() throws ResultException {
    	if (groups == null){
	    	groups = new TreeMap<>();
	    	
	    	PlasticGroupAndNameIterableDMW it = getGroupIterable();
	    	TreeSet<Integer> numbers = new TreeSet<>();
	    	
	    	while(it.hasNext()){
	    		PlasticGroupAndName gan = it.next();
	    		if (groups.get(gan.getGroupName()) != null){
	    			ResultException ex = new ResultException();
	    			ex.addError("Duplicate group name in NamedPattern: " + gan.getGroupName(), this.getFile(), this.getLineNumber());
	    			throw(ex);
	    		}
	    		if (numbers.contains(gan.getGroupNumber())){
	    			ResultException ex = new ResultException();
	    			ex.addError("Duplicate group number in NamedPattern: " + gan.getGroupNumber(), this.getFile(), this.getLineNumber());
	    			throw(ex);
	    		}
	    		groups.put(gan.getGroupName(), gan);
	    		numbers.add(gan.getGroupNumber());
	    	}
    	}
    }
    
    public String getGroupValue(String groupName){
    	String rc = null;
    	
    	if (lastMatch == null)
    		return(rc);
    	
    	PlasticGroupAndName gan = groups.get(groupName);
    	
    	if (gan == null){
    		throw(new IllegalStateException("No such group: " + groupName));
    	}
    	
    	rc = lastMatch.group(gan.getGroupNumber());
    	
    	return(rc);
    }
    
    public String getNormalizedStringFromLastMatch(){
    	if (getNormalizedForm() == null)
    		return(null);
    	
    	if (lastMatch == null)
    		return(null);
    	
    	HashMap<String,String> groupValues = getGroupValues();
    	StringBuilder sb = new StringBuilder();
    	
    	for(GroupOrLiteral gol: normalizedFormInfo){
    		sb.append(gol.toString(groupValues));
    	}
    	
    	return(sb.toString());
    }
    
    /**
     * @param value the string value to be tested
     * @return true if any of our patterns match the value
     */
    public boolean matches(String value){
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
    		for(PlasticGroupAndName gan: groups.values()){
    			// If we happen to have more groups defined than what we matched - don't
    			// go any further
    			if (gan.getGroupNumber() <= lastMatch.groupCount())
    				rc.put(gan.getGroupName(), lastMatch.group(gan.getGroupNumber()));
    		}
    	}
    	
    	return(rc);
    }

	@Override
	public void checkDefault(PlasticMapping mapping, PlasticVariable variable) throws ResultException {
		if (variable.getDefault() == null)
			return;

		if (!matches(variable.getDefault())) {
			ResultException ex = new ResultException("Invalid default: " + variable.getDefault() + " for variable: "+ variable.getName());
			ex.moreMessages("The default should match pattern: \"" + getPattern() + "\"");
			ex.moreMessages("Validated against PlasticPattern " + getName() + " - " + getFileAndLine());
			ex.setLocationInfo(mapping.getFile(), mapping.getLineNumber());
			throw(ex);
		}
		
	}

	@Override
	public String getInitialization(PlasticMapping mapping, PlasticVariable variable) {
		StringBuffer sb = new StringBuffer();
		
		sb.append("    // Generated from: " + DebugInfo.getWhereWeAreNow() + "\n");
		sb.append("    // For PlasticPattern: " + getName() + "\n");
		sb.append("    PlasticPatternGroovy                 " + Manipulator.convertToGroovyVariableName(variable.getName()) + "Pattern\n");
		sb.append("    ArrayList<PlasticGroupAndNameGroovy> " + Manipulator.convertToGroovyVariableName(variable.getName()) + "Info = new ArrayList<PlasticGroupAndNameGroovy>()\n");

		PlasticGroupAndNameIterableDMW it = getGroupIterable();
		while(it.hasNext()) {
			PlasticGroupAndName pgn = it.next();
			sb.append("    PlasticGroupAndNameGroovy " + Manipulator.convertToGroovyVariableName(pgn.getGroupName()) + " = new PlasticGroupAndNameGroovy(" + pgn.getGroupNumber()+ ", \"" + pgn.getGroupName() + "\", null)\n");
		}
		sb.append("\n");
		
		sb.append("\n");
		
		return(sb.toString());
	}
	
	@Override
	public String getConstructorInfo(PlasticMapping mapping, PlasticVariable variable) {
		StringBuffer sb = new StringBuffer();

		PlasticGroupAndNameIterableDMW it = getGroupIterable();
		while(it.hasNext()) {
			PlasticGroupAndName pgn = it.next();
			sb.append("        " + Manipulator.convertToGroovyVariableName(variable.getName()) + "Info.add(" + Manipulator.convertToGroovyVariableName(pgn.getGroupName()) + ")\n");
		}
		sb.append("\n");
		
		// BIG NOTE: HAVE TO USE SINGLE QUOTES!!!!
		// If we use double quotes and the pattern has a dollar sign in it, Groovy complains, because apparently, in double
		// quotes, the dollar sign implies interpolation
		if (getNormalizedForm() == null)
			sb.append("        " + Manipulator.convertToGroovyVariableName(variable.getName()) + "Pattern = new PlasticPatternGroovy(" + Manipulator.convertToGroovyVariableName(variable.getName()) + "Info, '" + getPattern() + "', null)\n");
		else
			sb.append("        " + Manipulator.convertToGroovyVariableName(variable.getName()) + "Pattern = new PlasticPatternGroovy(" + Manipulator.convertToGroovyVariableName(variable.getName()) + "Info, '" + getPattern() + "', \"" + getNormalizedForm() + "\")\n");
		
		return(sb.toString());
	}

	@Override
	public String getCall(PlasticMapping mapping, PlasticVariable variable) {
		StringBuffer sb = new StringBuffer();
		
		sb.append("        // Generated from: " + DebugInfo.getWhereWeAreNow() + "\n");
		sb.append("        if (!" + Manipulator.convertToGroovyVariableName(variable.getName()) + "Pattern.matches(inputs.get(\"" + variable.getName() + "\"))){\n");
		sb.append("             abort('Value: \"' + inputs.get(\"" + variable.getName() + "\") + '\" does not match pattern: \"" + getPattern() + "\"  - for variable: " + variable.getName() + "')\n");
		sb.append("        }\n");
		
		if (variable.getSplit() != null) {
			// We're splitting the variable if we successfully match
			sb.append("        else{\n");
			sb.append("            // Generated from: " + DebugInfo.getWhereWeAreNow() + "\n");
			sb.append("            // Remove the base input\n");
			sb.append("            inputs.remove(\"" + variable.getName() + "\")\n");
			sb.append("            \n");
			sb.append("            // Add the split values\n");
			sb.append("            HashMap<String,String> splitValues = " + Manipulator.convertToGroovyVariableName(variable.getName()) + "Pattern.getGroupValues()\n");
			sb.append("            for(String key: splitValues.keySet()){\n");
			sb.append("                inputs.put(key, splitValues.get(key))\n");
			sb.append("            }\n");
			sb.append("        }\n");
		}
		
		// split

		return(sb.toString());		
	}

	@Override
	public String getImplementation(PlasticMapping mapping, PlasticVariable variable) {
		return "    // PlasticPattern TODO\n\n";
	}

}

