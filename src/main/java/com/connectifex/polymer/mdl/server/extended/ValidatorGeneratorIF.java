package com.connectifex.polymer.mdl.server.extended;

import org.dmd.util.exceptions.ResultException;

import com.connectifex.polymer.mdl.server.extended.plastic.PlasticMapping;
import com.connectifex.polymer.mdl.shared.generated.types.PlasticVariable;

/**
 * The standard interface implemented by PolymerValidator derivatives.
 */
public interface ValidatorGeneratorIF {

	/**
	 * This is used to validate a default value if one is supplied for a variable.
	 * @param mapping the mapping being sanity checked
	 * @param variable the variable with a default value
	 * @return null if valid and an error message if not.
	 */
	public void checkDefault(PlasticMapping mapping, PlasticVariable variable) throws ResultException;
	
	/**
	 * @param mapping the mapping
	 * @param variable the variable
	 * @return any code required to generate initialization of static data required
	 * by this validator. If there's nothing, just return an empty string.
	 */
	public String getInitialization(PlasticMapping mapping, PlasticVariable variable);
	
	/**
	 * @param mapping the mapping
	 * @param variable the variable
	 * @return any code required to run in the morpher's constructor
	 * by this validator. If there's nothing, just return an empty string.
	 */
	public String getConstructorInfo(PlasticMapping mapping, PlasticVariable variable);
	
	/**
	 * @param mapping the mapping
	 * @param variable the variable
	 * @return the code that actually calls the validation method.
	 */
	public String getCall(PlasticMapping mapping, PlasticVariable variable);
	
	/**
	 * The generated method must accept the name of the variable being validated and its value as
	 * a string. For example: private String checkThis(String variableName, String input)
	 * @param mapping the mapping
	 * @param variable the variable
	 * @return the method that will perform the validation. 
	 */
	public String getImplementation(PlasticMapping mapping, PlasticVariable variable);
}
