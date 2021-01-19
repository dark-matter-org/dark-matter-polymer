package com.connectifex.polymer.mdl.server.extended.util;

import com.connectifex.polymer.mdl.server.generated.dmw.ParamWithValueIterableDMW;
// Generated from: org.dmd.util.codegen.ImportManager.getFormattedImports(ImportManager.java:82)
// Called from: org.dmd.dmg.generators.DMWGenerator.dumpExtendedClass(DMWGenerator.java:290)
import com.connectifex.polymer.mdl.server.generated.dmw.ParameterSetDMW;         // The wrapper we're extending - (DMWGenerator.java:282)
import com.connectifex.polymer.mdl.server.generated.dsd.MdlModuleDefinitionManager;
import com.connectifex.polymer.mdl.shared.generated.dmo.ParameterSetDMO;         // The wrapper we're extending - (DMWGenerator.java:283)
import com.connectifex.polymer.mdl.shared.generated.types.ParamWithValue;

import java.util.Iterator;
import java.util.TreeMap;

import org.dmd.dms.ClassDefinition;                                              // Used in derived constructors - (DMWGenerator.java:284)
import org.dmd.util.exceptions.ResultException;


public class ParameterSet extends ParameterSetDMW {
	
	private TreeMap<String, ParamWithValue>	params;

    public ParameterSet(){
        super();
    }

    public ParameterSet(ParameterSetDMO dmo, ClassDefinition cd){
        super(dmo,cd);
    }

    /**
     * Checks:
     * - that each ParamWithValue has a unique name
     */
    @Override
    public void performAdditionalValidation(MdlModuleDefinitionManager definitions) throws ResultException {
    	if (params == null) {
    		// Because of dependencies on merging ParameterSets, we allow this to be called
    		// from multiple locations, but only initialize once - see ValidationRun
    		
	    	params = new TreeMap<String, ParamWithValue>();
	    	
	    	ParamWithValueIterableDMW it = getParameterValuesIterable();
	    	while(it.hasNext()) {
	    		ParamWithValue pwv = it.next();
	    		if (params.get(pwv.getParamName()) != null) {
	    			ResultException ex = new ResultException("Duplicate parameterValues name: " + pwv.getParamName());
	    			ex.setLocationInfo(getFile(), getLineNumber());
	    			throw(ex);
	    		}
	    		params.put(pwv.getParamName(), pwv);
	    	}
    	}
	}
    
    /**
     * @param paramName the name of the parameter.
     * @return the parameter value or null if we don't have it.
     */
    public String getValue(String paramName) {
    	ParamWithValue pwv = params.get(paramName);
    	
    	if (pwv == null)
    		return(null);
    	
    	return(pwv.getParamValue());
    }
    
    public Iterator<ParamWithValue> getParams(){
    	if (params == null)
    		throw(new IllegalStateException("performAdditionalValidation() has not been called on this ParameterSet"));
    	
    	return(params.values().iterator());
    }

}

