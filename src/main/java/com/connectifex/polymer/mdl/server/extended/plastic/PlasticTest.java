package com.connectifex.polymer.mdl.server.extended.plastic;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.dmd.dms.ClassDefinition;                                // Used in derived constructors - (DMWGenerator.java:284)
import org.dmd.dmu.util.json.PrettyJSON;
import org.dmd.util.exceptions.DebugInfo;
import org.dmd.util.exceptions.ResultException;
import org.dmd.util.formatting.PrintfFormat;
import org.json.JSONObject;
import org.opendaylight.plastic.implementation.CartographerWorker;
import org.opendaylight.plastic.implementation.PlasticException;
import org.opendaylight.plastic.implementation.SearchPath;
import org.opendaylight.plastic.implementation.VersionedSchema;

import com.connectifex.polymer.mdl.server.extended.plastic.util.PlasticConstants;
import com.connectifex.polymer.mdl.server.extended.plastic.util.PlasticGlobals;
import com.connectifex.polymer.mdl.server.generated.dmw.PlasticTestDMW;
import com.connectifex.polymer.mdl.server.generated.dsd.MdlModuleDefinitionManager;
import com.connectifex.polymer.mdl.shared.generated.dmo.PlasticTestDMO;


public class PlasticTest extends PlasticTestDMW {
	
	private TreeSet<String> tags;

    public PlasticTest(){
        super();
    }

    public PlasticTest(PlasticTestDMO dmo, ClassDefinition cd){
        super(dmo,cd);
    }

    @Override
    public void performAdditionalValidation(MdlModuleDefinitionManager definitions) throws ResultException {
    	if (tags == null) {
    		tags = new TreeSet<>();
    		// Our name is considered a tag as well
    		tags.add(getName().getNameString());
    		
    		if (getTagsSize() > 0) {
    			Iterator<String> it = getTags();
    			while(it.hasNext())
    				tags.add(it.next());
    		}
    		
    		// The name of a test should start with the name of the plastic mapping
    		// it is meant to exercise! Putting this in because I actually spent half
    		// an hour trying to determine why a test was failing when I had just
    		// changed the mapping to prevent the failure.
    		
    		if (!getName().getNameString().startsWith(getMapping().getName().getNameString())) {
    			ResultException ex = new ResultException("The name of a PlasticTest must start with name of the PlasticMapping it is meant to exercise.");
    			ex.setLocationInfo(getFile(), getLineNumber());
    			throw(ex);
    		}
    	}
    }
    
    public void execute(Set<String> run, Set<String> skip, String plasticdir, PrintfFormat format) throws ResultException {
    	if (skip.size() > 0) {
    		for(String tag: skip) {
    			if (tags.contains(tag)) {
    				PlasticGlobals.instance().trace("Skipping " + getName());
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
    	
    	String fakePath = plasticdir + "/" + PlasticConstants.FAKE;
    	String projPath = plasticdir + "/" + getMapping().getFolderStructure().getName().getNameString();
    	SearchPath path = new SearchPath(projPath + ";" + fakePath);
    	
//    	PlasticGlobals.instance().trace("Plastic path: " + projPath + ";" + fakePath);
    	CartographerWorker cartographer = new CartographerWorker(path);
    	
    	VersionedSchema inSchema = getMapping().getInputVersionedSchema();
    	VersionedSchema outSchema = getMapping().getOutputVersionedSchema();
    	
    	
    	try {
    		PlasticGlobals.instance().trace("\n------------------------------------------\n");
    		PlasticGlobals.instance().trace("Execute " + getName() + "\n");
    		
    		PlasticGlobals.instance().trace("Payload: \n" + PrettyJSON.instance().prettyPrint(new JSONObject(getInputPayload()),true) + "\n");
    		String plasticResult = cartographer.translate(inSchema, outSchema, getInputPayload());
    		
        	PlasticGlobals.instance().trace("Plastic result: \n" + plasticResult + "\n");
        	
        	PlasticGlobals.instance().summary(format.sprintf(getName()) + " - passed");
    	}
    	catch(PlasticException e) {
        	PlasticGlobals.instance().summary(format.sprintf(getName()) + " - failed - " + e.getMessage());
        	PlasticGlobals.instance().trace("Error: " + e.getMessage());
    	}
    	
    	cartographer.close();
    }
    
    
}

