package com.connectifex.polymer.mdl.server.extended.util;

import java.util.Iterator;
import java.util.TreeMap;

import org.dmd.dmc.DmcValueException;
import org.dmd.util.exceptions.DebugInfo;
import org.dmd.util.exceptions.ResultException;

import com.connectifex.polymer.mdl.shared.generated.types.ParamWithValue;
import com.connectifex.polymer.mdl.shared.generated.types.ParameterSetREF;


/**
 * The MergedParameterSet allows for the creation of a merged view of more
 * than one ParameterSet. When adding a ParameterSet, an exception will be thrown
 * if there are conflicts between the values associated with parameter names.
 */
public class MergedParameterSet {

	private TreeMap<String, ParamAndSource>	params;

	public MergedParameterSet() {
		params = new TreeMap<>();
	}
	
	/**
	 * Convenience constructor to use a set of ParameterSets.
	 * @param baseps a base parameter set
	 * @param it an iterator over ParameterSets or null
	 */
	public MergedParameterSet(ParameterSet baseps, Iterator<ParameterSetREF> it) throws ResultException {
		params = new TreeMap<>();
		
		mergeParameters(baseps);

		if (it != null) {
			while(it.hasNext()) {
				ParameterSetREF ref = it.next();
				ParameterSet ps = (ParameterSet) ref.getObject().getContainer();
				
				mergeParameters(ps);
			}
		}
	}
	
	/**
	 * Convenience constructor to use a set of ParameterSets.
	 * @param it an iterator over ParameterSets or null
	 */
	public MergedParameterSet(Iterator<ParameterSetREF> it) throws ResultException {
		params = new TreeMap<>();

		if (it != null) {
			while(it.hasNext()) {
				ParameterSetREF ref = it.next();
				ParameterSet ps = (ParameterSet) ref.getObject().getContainer();
				
				mergeParameters(ps);
			}
		}
	}
	
	/**
	 * Adds a single parameter value to the set. This is used in our workflow mechanisms
	 * to merge values from configuration sections in device-parameters.
	 * @param name parameter name
	 * @param value parameter value
	 * @param overWrite indicates if we want to overwrite a value if it already exists - this is
	 * used in cases where we want to support default values and overwrite them if the user
	 * has specified a value
	 * @return null, if all goes well, or an error string if we run into problems e.g. duplicate 
	 * parameter name
	 */
	public String addParameter(String name, String value, boolean overWrite) {
		try {
			ParamAndSource pas = params.get(name);
			
			if (overWrite) {
				ParamWithValue pwv = new ParamWithValue(name, value, null);
				params.put(name,new ParamAndSource(pwv, null));				
			}
			else {
				if (pas == null) {
					ParamWithValue pwv = new ParamWithValue(name, value, null);
					params.put(name,new ParamAndSource(pwv, null));
				}
				else {
					return("Duplicate parameter: " + name);
				}
			}
		} catch (DmcValueException e) {
			return("Unexpected exception: " + DebugInfo.getWhereWeAreNow());
		}
		
		return(null);
	}
	
	/**
	 * Adds a single parameter value to the set. This is used in our workflow mechanisms
	 * to merge values from configuration sections in device-parameters.
	 * @param name parameter name
	 * @param value parameter value
	 * @return null, if all goes well, or an error string if we run into problems e.g. duplicate 
	 * parameter name
	 */
	public String addParameter(String name, String value) {
		return(addParameter(name, value, false));
	}
	
	/**
	 * Merges the parameter values from the specified ParameterSet
	 * @param ps the parameter set
	 * @throws ResultException if there are colliding names with mismatched values
	 */
	public void mergeParameters(ParameterSet ps) throws ResultException {
		Iterator<ParamWithValue> pvit = ps.getParams();
		while(pvit.hasNext()) {
			ParamWithValue pwv = pvit.next();
			
			ParamAndSource pas = params.get(pwv.getParamName());
			if (pas == null) {
				params.put(pwv.getParamName(), new ParamAndSource(pwv, ps));
			}
			else {
				if (!pas.pwValue.getParamValue().equals(pwv.getParamValue())) {
					// The values clash for the same parameter name
					ResultException ex = new ResultException("Clashing values for parameter: " + pwv.getParamName() + "  originally from:");
					ex.moreMessages("ParameterSet: " + pas.paramSet.getName());
					ex.moreMessages("  file: " + pas.paramSet.getFile());
					ex.moreMessages("  line: " + pas.paramSet.getLineNumber());
					ex.moreMessages("ParameterSet: " + ps.getName());
					ex.moreMessages("  file: " + ps.getFile());
					ex.moreMessages("  line: " + ps.getLineNumber());
					throw(ex);
				}
			}
		}
		
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for(ParamAndSource pas: params.values()) {
			sb.append(pas.pwValue.toString() + "\n");
		}
		
		return(sb.toString());
	}
	
    /**
     * @param paramName the name of the parameter.
     * @return the parameter value or null if we don't have it.
     */
    public String getValue(String paramName) {
    	ParamAndSource pwv = params.get(paramName);
    	
    	if (pwv == null)
    		return(null);
    	
    	return(pwv.pwValue.getParamValue());
    }
	
    class ParamAndSource {
    	// The value
    	ParamWithValue 	pwValue;
    	
    	// Where we got the first instance of this parameter and value
    	ParameterSet	paramSet;
    	
    	public ParamAndSource(ParamWithValue pwValue, ParameterSet paramSet) {
			this.pwValue 	= pwValue;
			this.paramSet	= paramSet;
		}
    }
}
