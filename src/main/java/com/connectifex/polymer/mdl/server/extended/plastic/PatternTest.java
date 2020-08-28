package com.connectifex.polymer.mdl.server.extended.plastic;

import com.connectifex.polymer.mdl.server.extended.plastic.util.PlasticGlobals;
import com.connectifex.polymer.mdl.server.generated.dmw.ExpectedGroupValueIterableDMW;
// Generated from: org.dmd.util.codegen.ImportManager.getFormattedImports(ImportManager.java:82)
// Called from: org.dmd.dmg.generators.DMWGenerator.dumpExtendedClass(DMWGenerator.java:290)
import com.connectifex.polymer.mdl.server.generated.dmw.PatternTestDMW;         // The wrapper we're extending - (DMWGenerator.java:282)
import com.connectifex.polymer.mdl.server.generated.dmw.PlasticGroupAndNameIterableDMW;
import com.connectifex.polymer.mdl.server.generated.dsd.MdlModuleDefinitionManager;
import com.connectifex.polymer.mdl.shared.generated.dmo.PatternTestDMO;         // The wrapper we're extending - (DMWGenerator.java:283)
import com.connectifex.polymer.mdl.shared.generated.types.ExpectedGroupValue;
import com.connectifex.polymer.mdl.shared.generated.types.PlasticGroupAndName;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.dmd.dms.ClassDefinition;                                        // Used in derived constructors - (DMWGenerator.java:284)
import org.dmd.util.exceptions.ResultException;


public class PatternTest extends PatternTestDMW {

	private TreeSet<String> tags;

	public PatternTest(){
        super();
    }

    public PatternTest(PatternTestDMO dmo, ClassDefinition cd){
        super(dmo,cd);
    }

    @Override
    public void performAdditionalValidation(MdlModuleDefinitionManager definitions) throws ResultException {
    	if (tags == null) {
	    	if (getExpectThatHasValue()) {
	    		// Make sure the pattern is initialized
	    		getUsePattern().performAdditionalValidation(definitions);
	    		
	    		ExpectedGroupValueIterableDMW it = getExpectThatIterable();
	    		while(it.hasNext()) {
	    			ExpectedGroupValue egv = it.next();
	    			if (!getUsePattern().hasGroup(egv.getGroupName())) {
	    				ResultException ex = new ResultException("Unknown group in expectThat: " + egv.getGroupName());
	    				ex.moreMessages("The group names for " + getUsePattern().getName() + " are: " + getUsePattern().getGroupNamesAsString());
	    				ex.setLocationInfo(getFile(), getLineNumber());
	    				throw(ex);
	    			}
	    		}
	    	}
	    	
    		tags = new TreeSet<>();
    		// Our name is considered a tag as well
    		tags.add(getName().getNameString());
    		
    		if (getTagsSize() > 0) {
    			Iterator<String> it = getTags();
    			while(it.hasNext())
    				tags.add(it.next());
    		}
    		
    		// The name of a test should start with the name of the plastic pattern
    		// it is meant to exercise! 
    		if (!getName().getNameString().startsWith(getUsePattern().getName().getNameString())) {
    			ResultException ex = new ResultException("The name of a PatternTest must start with name of the PlasticPattern it is meant to exercise.");
    			ex.setLocationInfo(getFile(), getLineNumber());
    			throw(ex);
    		}

    	}
    }
    
    public void execute(Set<String> run, Set<String> skip) {
    	if (skip.size() > 0) {
    		for(String tag: skip) {
    			if (tags.contains(tag)) {
//    				PlasticGlobals.instance().trace("Skipping " + getName());
    				return;
    			}
    		}
    	}
    	if (run.size() > 0) {
    		boolean proceed = false;
    		for(String tag: run) {
    			if (tags.contains(tag)) {
    				proceed = true;
    				break;
    			}
    		}
    		
    		if (!proceed) {
//				PlasticGlobals.instance().trace("Not running " + getName());
    			return;
    		}
    	}
    	
    	if (getUsePattern().matches(getInput().getValue())) {
    		if (getExpectThatHasValue()) {
    			
    			HashMap<String,String> results = getUsePattern().getGroupValues();
    			ExpectedGroupValueIterableDMW egit = getExpectThatIterable();
    			while(egit.hasNext()) {
    				ExpectedGroupValue egv = egit.next();
    				String value = results.get(egv.getGroupName());
    				
    				if (!value.equals(egv.getValue())) {
    					PlasticGlobals.instance().trace(getName() + "    FAILED - Expected that group: " + egv.getGroupName() + " would equal: \"" + egv.getValue() + "\"  but got: \"" + value + "\"\n");
    					return;
    				}
    			}
    			
    			// We matched the expected values
				PlasticGlobals.instance().trace(getName() + " SUCCEEDED - MATCHED");
//				HashMap<String,String> results = getUsePattern().getGroupValues();
//				PlasticGroupAndNameIterableDMW it = getUsePattern().getGroupIterable();
//				
//				while(it.hasNext()) {
//					PlasticGroupAndName pgn = it.next();
//					
//					PlasticGlobals.instance().trace(pgn.getGroupName() + " = " + results.get(pgn.getGroupName()));
//					
//				}
//				PlasticGlobals.instance().trace("");
    		}
    	}
    	else {
    		// We didn't match
    		if (getExpectThatHasValue()) {
				PlasticGlobals.instance().trace(getName() + "    FAILED - NO MATCH\n");
    		} 
    		else {
				PlasticGlobals.instance().trace(getName() + " SUCCEEDED - NO MATCH EXPECTED\n");
    		}
    	}
    }
}

