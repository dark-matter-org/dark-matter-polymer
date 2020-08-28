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

package com.connectifex.polymer.mdl.server.extended.plastic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import org.dmd.dms.ClassDefinition;                                   // Used in derived constructors - (DMWGenerator.java:284)
import org.dmd.dmu.util.json.PrettyJSON;
import org.dmd.util.exceptions.ResultException;
import org.json.JSONObject;
import org.opendaylight.plastic.implementation.VersionedSchema;

import com.connectifex.polymer.mdl.server.extended.plastic.util.MorpherGenerator;
import com.connectifex.polymer.mdl.server.extended.plastic.util.PlasticConstants;
import com.connectifex.polymer.mdl.server.extended.plastic.util.PlasticGlobals;
import com.connectifex.polymer.mdl.server.extended.validation.PolymerValidator;
import com.connectifex.polymer.mdl.server.generated.dmw.PlasticMappingDMW;
import com.connectifex.polymer.mdl.server.generated.dmw.PlasticVariableIterableDMW;
import com.connectifex.polymer.mdl.server.generated.dsd.MdlModuleDefinitionManager;
import com.connectifex.polymer.mdl.shared.generated.dmo.PlasticMappingDMO;
import com.connectifex.polymer.mdl.shared.generated.types.PlasticVariable;
import com.connectifex.polymer.mdl.shared.generated.types.PolymerValidatorREF;
import com.connectifex.polymer.mdl.shared.util.VariableAssociator;
import com.connectifex.polymer.mdl.tools.plastic.util.PlasticLibraryResourceUtil;


public class PlasticMapping extends PlasticMappingDMW {
	
	private final static String README 			= "/README.md";
	private final static String FAKE_NOTE 		= "Fake folder structure - DO NOT ADD FILES HERE!";
	
	private final static String ARRAY_INDICATOR = "[*]";
	
	// Indicates if we have any optional parameters - if so, we'll need a morpher
	private ArrayList<PlasticVariable> optional;

	// NOTE: originally, tried to specify defaults via the file mechanism, but it didn't work
	// for array defaults (see below). So, now, for the sake of consistency all defaults are
	// handled in the morphers.
	
	// Indicates if we have any scalar default values
	private ArrayList<PlasticVariable>	scalarDefaults;
	
	// There's no easy way to specify defaults for variables in arrays (using the defaults file)
	// you would have to specify an array of values and, in cases where where you don't know the
	// number of elements coming in, this isn't helpful. So, for array variables, we generate code
	// to handle this situation.
	// That's why we separate scalar and array defaults.
	private ArrayList<PlasticVariable>	arrayDefaults;
	
	// If a PlasticVariable has any validate indications, we record them here so that the MorpherGenerator
	// can quickly determine if it needs to insert initializations and validation methods.
	private ArrayList<PlasticVariable>	validationRequired;
	
	// If the variable has split specified, we add it here. 
	private ArrayList<PlasticVariable>		splitRequired;
	
	// All split variables by name
	private TreeMap<String,PlasticVariable>	splitByName;
	
	// Indicates variables that need to have URL encoding run against them
	private ArrayList<PlasticVariable>	encodeURLs;
	
	// The associator maintains information about the association between input and output variables
	// as well as complete paths to these variables within the JSON structure.
	private VariableAssociator associator;
	
	
	private TreeMap<String,PlasticVariable>	allVariables;

    public PlasticMapping(){
        super();
    }

    public PlasticMapping(PlasticMappingDMO dmo, ClassDefinition cd){
        super(dmo,cd);
    }

    @Override
    /**
     * Check that variable names are unique across variables, morpherInVariables and morpherOutVariables
     * Check that we don't have duplicate variable names in the input schemas
     */
    public void performAdditionalValidation(MdlModuleDefinitionManager definitions) throws ResultException {
    	allVariables = new TreeMap<>();
    	
    	// These methods update allVariables
    	checkUniquenames(getVariablesIterable());
    	
    	checkUniquenames(getMorpherInVariablesIterable());
    	checkUniquenames(getMorpherOutVariablesIterable());
    	
    	PlasticVariableIterableDMW pvi = getVariablesIterable();
    	while(pvi.hasNext()) {
    		PlasticVariable pv = pvi.next();
    		if ( (pv.getOptional() != null) && pv.getOptional()) {
    			if (pv.getDefault() != null) {
    	    		ResultException ex = new ResultException("You cannot specify both optional and default for variable: " + pv.getName());
    	    		ex.setLocationInfo(getFile(), getLineNumber());
    	    		throw(ex);
    			}
    		}
    		
    		if ( (pv.getValidateSize() > 0) && (pv.getSplit() != null) ){
	    		ResultException ex = new ResultException("You cannot specify both validate and split for variable: " + pv.getName());
	    		ex.setLocationInfo(getFile(), getLineNumber());
	    		throw(ex);
    		}
    		
    		if (pv.getValidateSize() > 0) {
    			if (validationRequired == null)
    				validationRequired = new ArrayList<PlasticVariable>();
    			validationRequired.add(pv);
    			
    			if (pv.getDefault() != null) {
    				// Validate the default
    				Iterator<PolymerValidatorREF> it = pv.getValidate();
    				while(it.hasNext()) {
    					PolymerValidatorREF ref = it.next();
    					PolymerValidator validator = (PolymerValidator) ref.getObject().getContainer();
    					
    					// Ensure the validator has been validated itself
    					validator.performAdditionalValidation(definitions);
    					
    					validator.checkDefault(this, pv);
    				}
    			}
    		}
    		
    		if (pv.getSplit() != null) {
    			if (splitRequired == null) {
    				splitRequired = new ArrayList<>();
    				splitByName = new TreeMap<>();
    			}
    			splitRequired.add(pv);
    			splitByName.put(pv.getName(), pv);
    			
    			PlasticPattern pattern = (PlasticPattern) pv.getSplit().getObject().getContainer();
    			if (pattern.getGroupSize() < 2) {
    	    		ResultException ex = new ResultException("The PlasticPattern used to split: " + pv.getName() + " must have 2 or more groups.");
    	    		ex.setLocationInfo(getFile(), getLineNumber());
    	    		throw(ex);
    			}
    			
    			// Make sure the pattern has been validated
    			pattern.performAdditionalValidation(definitions);
    			
    			Iterator<String> groupIt = pattern.getGroupNames();
    			while(groupIt.hasNext()) {
    				String gn = groupIt.next();
    				PlasticVariable existing = allVariables.get(gn);
    				if (existing == null) {
        	    		ResultException ex = new ResultException("You must create a variable called: " + gn + " to represent the value created by splitting variable: " + pv.getName());
        	    		ex.setLocationInfo(getFile(), getLineNumber());
        	    		throw(ex);
    				}
    				else {
    					// For now, we don't allow other operation on split variables
    					StringBuilder sb = new StringBuilder();
    					if (existing.hasValidate())
    						sb.append("validate");
    					if (existing.getSplit() != null) {
    						if (sb.length() > 0)
    							sb.append(", ");
    						sb.append("split");
    					}
    					if ( (existing.getEncodeURL() != null) && existing.getEncodeURL()) {
    						if (sb.length() > 0)
    							sb.append(", ");
    						sb.append("encodeURL");
    					}
    					if ( (existing.getOptional() != null) && existing.getOptional()) {
    						if (sb.length() > 0)
    							sb.append(", ");
    						sb.append("optional");
    					}
    					
    					if (sb.length() > 0) {
            	    		ResultException ex = new ResultException("The variable: " + gn + " created by splitting variable: " + pv.getName());
            	    		ex.moreMessages("May not specify: " + sb.toString());
            	    		ex.setLocationInfo(getFile(), getLineNumber());
            	    		throw(ex);
    					}
    				}
    			}
    		}
    		
    		if ( (pv.getEncodeURL() != null) && pv.getEncodeURL()) {
    			if (encodeURLs == null)
    				encodeURLs = new ArrayList<PlasticVariable>();
    			encodeURLs.add(pv);
    		}
    	}
    	
    	getInputSchema().initialize(this);
    	
    	// For now, duplicates in the input schema are considered an error
    	String inDupes = getInputSchema().getVariableNameDuplicatesString();
    	if (inDupes.length() > 0) {
    		ResultException ex = new ResultException("Duplicated variable name(s) in inputSchema: " + inDupes);
    		ex.setLocationInfo(getFile(), getLineNumber());
    		throw(ex);
    	}
    	
    	// The output schema, isn't checked for duplicates - we may use the same value from the input
    	// to fill multiple locations in the output
    	getOutputSchema().initialize(this);
    	
    	TreeSet<String> inNames = getInputSchema().getVariableNames();
    	TreeSet<String> outNames = getOutputSchema().getVariableNames();
    	
    	for(String outName: outNames) {
    		if (!inNames.contains(outName)) {
    			if (getDefinedInMdlModule().isAllowPlasticGenErrors()) {
    				System.out.println("WARNING: Output variable not defined in inputSchema: " + outName);
    				System.out.println(getLocationInfo() + "\n");
    				continue;
    			}
    			if (allVariables.get(outName) == null) {
        			ResultException ex = new ResultException("Output variable not defined in inputSchema: " + outName);
            		ex.setLocationInfo(getFile(), getLineNumber());
            		throw(ex);
    			}
    			else {
    				// We've found the variable in our variables, so it must have come from a split variable
    			}
    		}
    		
    		if (allVariables.get(outName) == null) {
    			ResultException ex = new ResultException("Output variable not defined in variables: " + outName);
        		ex.setLocationInfo(getFile(), getLineNumber());
        		throw(ex);
    		}
    		
    		if (splitByName != null) {
    			if (splitByName.get(outName) != null) {
        			ResultException ex = new ResultException("You can't use variable: " + outName + " in your outputSchema. It is split into other variables.");
            		ex.setLocationInfo(getFile(), getLineNumber());
            		throw(ex);
    			}
    		}
    	}
    	
    	for(String inName: inNames) {
    		if (!outNames.contains(inName)) {
    			if (allowPlasticGenErrors()) {
    				System.out.println("WARNING: Skipping Input variable not used in outputSchema: " + inName);
    				System.out.println(getLocationInfo() + "\n");
    				continue;
    			}
    			
    			if ( (splitByName != null) && (splitByName.get(inName) != null)) {
    				// This is okay, we're removing the original the input variable when we split
    			}
    			else {
	    			ResultException ex = new ResultException("Input variable not used in outputSchema: " + inName);
	        		ex.setLocationInfo(getFile(), getLineNumber());
	        		throw(ex);
    			}
    		}
    	}
    	
    	PlasticVariableIterableDMW it = getVariablesIterable();
    	while(it.hasNext()) {
    		PlasticVariable variable = it.next();
    		
    		if (!inNames.contains(variable.getName())) {
    			
    		}
    		
    		if (!outNames.contains(variable.getName())) {
    			if (allowPlasticGenErrors()) {
    				System.out.println("WARNING: Skipping variable not used in outputSchema: " + variable.getName());
    				System.out.println(getLocationInfo() + "\n");
    				continue;
    			}
    			
    			// The variable is not used in the output schema, that's only okay, if the
    			// the variable is being split.
    			if (variable.getSplit() == null) {
        			ResultException ex = new ResultException("Variable not used in outputSchema: " + variable.getName());
            		ex.setLocationInfo(getFile(), getLineNumber());
            		throw(ex);
    			}
    		}
    		
    		if ( (variable.getOptional() != null) && variable.getOptional()) {
    			if (optional == null)
    				optional = new ArrayList<>();
    			optional.add(variable);
    		}
    		if (variable.getDefault() != null) {
    			if (variable.getName().contains(ARRAY_INDICATOR)) {
    				if (arrayDefaults == null)
    					arrayDefaults = new ArrayList<PlasticVariable>();
    				arrayDefaults.add(variable);
    			}
    			else {
	    			if (scalarDefaults == null)
	    				scalarDefaults = new ArrayList<>();
	    			scalarDefaults.add(variable);
    			}
    		}
    	}
    	
    	for(String inName: inNames) {
    		if (allVariables.get(inName) == null) {
    			if (getDefinedInMdlModule().isAllowPlasticGenErrors()) {
    				System.out.println("WARNING: Variable in inputSchema is not defined: " + inName);
    				System.out.println(getLocationInfo() + "\n");
    				continue;
    			}

    			ResultException ex = new ResultException("Variable in inputSchema is not defined: " + inName);
    			ex.moreMessages("The variable must be defined in either: variables or morpherInVariables");
    			ex.setLocationInfo(getFile(), getLineNumber());
    			throw(ex);
    		}
    	}
    	
    	for(String outName: outNames) {
    		if (allVariables.get(outName) == null) {
    			if (getDefinedInMdlModule().isAllowPlasticGenErrors()) {
    				System.out.println("WARNING: Variable in outputSchema is not defined: " + outName);
    				System.out.println(getLocationInfo() + "\n");
    				continue;
    			}

    			ResultException ex = new ResultException("Variable in outputSchema is not defined: " + outName);
    			ex.moreMessages("The variable must be defined in either: variables or morpherOutVariables");
    			ex.setLocationInfo(getFile(), getLineNumber());
    			throw(ex);
    		}
    	}
    	    
    	// Originally going to be used to navigate the output structure and remove
    	// optional attributes, but the brute force OptionalValueRemover was much
    	// easier as an approach. May eventually need info about the paths to particular
    	// elements, so will leave the VariableAssociator code for now.
//    	associator = new VariableAssociator(this);
//    	associator.addInVariables(getInputSchema().getVariableInfo());
//    	associator.addOutVariables(getOutputSchema().getVariableInfo());
//    	associator.validate();
//    	DebugInfo.debug("\nVariable Associations\n\n" + associator.toString());
    	
    	
    }
    
    public PlasticVariable getVariableByName(String name) {
    	return(allVariables.get(name));
    }
    
    /**
     * @return true if the insertTracingInMorpher flag is set for this mapping or on module
     * where this mapping is defined.
     */
    public boolean wantTracing() {
    	if (isInsertTracingInMorpher())
    		return(true);
    	if (getDefinedInMdlModule().isInsertTracingInMorpher())
    		return(true);
    	return(false);
    }
    
    /**
     * @return true if the ignoreUnusedInputs flag is set for this mapping or on module
     * where this mapping is defined.
     */
    public boolean ignoreUnusedInputs() {
    	if (isIgnoreUnusedInputs())
    		return(true);
    	if (getDefinedInMdlModule().isIgnoreUnusedInputs())
    		return(true);
    	return(false);
    }
    
    /**
     * @return true if the ignoreUnusedOutputs flag is set for this mapping or on module
     * where this mapping is defined.
     */
    public boolean ignoreUnusedOutputs() {
    	if (isIgnoreUnusedOutputs())
    		return(true);
    	if (getDefinedInMdlModule().isIgnoreUnusedOutputs())
    		return(true);
    	return(false);
    }
    
    /**
     * @return true if the allowPlasticGenErrors is set for this mapping or on module
     * where this mapping is defined.
     */
    public boolean allowPlasticGenErrors() {
    	if (isAllowPlasticGenErrors())
    		return(true);
    	if (getDefinedInMdlModule().isAllowPlasticGenErrors())
    		return(true);
    	return(false);
    }
    
    /**
     * @return true if we have any variable marked as optional.
     */
    public boolean hasOptional() {
    	if (optional == null)
    		return(false);
    	return(true);
    }
    
    /**
     * @return true if any of our variables have validation indications
     */
    public boolean hasValidation() {
    	if (validationRequired == null)
    		return(false);
    	return(true);
    }
    
    /**
     * @return the variables that require validation.
     */
    public ArrayList<PlasticVariable> getValidationVariables(){
    	return(validationRequired);
    }
    
    /**
     * @return true if we have any split variables
     */
    public boolean hasSplitVariables() {
    	if (splitRequired == null)
    		return(false);
    	return(true);
    }
    
    /**
     * @return the variables that require splitting
     */
    public ArrayList<PlasticVariable> getSplitVariables(){
    	return(splitRequired);
    }
    
    /**
     * @return true if any variables need URL encoding.
     */
    public boolean hasEncodedURLs() {
    	if (encodeURLs == null)
    		return(false);
    	return(true);
    }
    
    /**
     * @return the variables that need URL encoding.
     */
    public ArrayList<PlasticVariable> getEncodedURLs(){
    	return(encodeURLs);
    }
    
//    /**
//     * @return the associator that let us navigate the output JSON structure to strip out
//     * optional values
//     */
//    public VariableAssociator getOptionalAssociations() {
//    	return(associator);
//    }

    /**
     * @return true if any default scalar values have been provided.
     */
    public boolean hasScalarDefaults() {
    	if (scalarDefaults == null)
    		return(false);
    	return(true);
    }
    
    /**
     * @return true if any defaults are specified
     */
    public boolean hasAnyDefaults() {
    	if ( (scalarDefaults != null) || (arrayDefaults != null))
    		return(true);
    	return(false);
    }
    
    /**
     * @return true if we have defaults for any variable embedded in arrays
     */
    public boolean hasArrayDefaults() {
    	if (arrayDefaults == null)
    		return(false);
    	return(true);
    }
    
    /**
     * @return the defaults for scalar variables
     */
    public ArrayList<PlasticVariable> getScalarDefaults(){
    	return(scalarDefaults);
    }
    
    /**
     * @return the default for array variables
     */
    public ArrayList<PlasticVariable> getArrayDefaults(){
    	return(arrayDefaults);
    }
    
    private void checkUniquenames(PlasticVariableIterableDMW vit) throws ResultException {
    	while(vit.hasNext()) {
    		PlasticVariable pv = vit.next();
    		if (allVariables.get(pv.getName()) == null) {
    			allVariables.put(pv.getName(), pv);
    		}
    		else {
    			ResultException ex = new ResultException("Duplicate variable: " + pv.getName());
    			ex.moreMessages("Variables must be unique across the variables, morpherInVariables and morpherOutVariables lists");
    			ex.setLocationInfo(getFile(), getLineNumber());
    			throw(ex);
    		}
    	}
    }
    
    public VersionedSchema getInputVersionedSchema() {
    	VersionedSchema rc = null;
    	
    	switch(getInputType()) {
    	case JSON:
    		rc = new VersionedSchema(getFolderStructure().getInFileNamePrefix() + "-" + getName().getNameString() + PlasticConstants.INPUT, getInputVersion(), PlasticConstants.JSON);
    		break;
    	}
    	
    	return(rc);
    }
    
//    /**
//     * If we have specified defaults, they will have been dumped next to the input schema for
//     * this mapping. We will read that file and return it as a JSON formatted string.
//     * @param baseDir the root plastic directory
//     * @return the defaults as a JSON object
//     * @throws ResultException 
//     */
//    public String getDefaultsAsJSON(String baseDir) throws ResultException {
//    	String inSchemaDir = baseDir + "/" + getFolderStructure().getInputSchemaDirName(getInputVersion());
//		String defaultsFn = inSchemaDir + "/" + getFolderStructure().getInFileNamePrefix() + "-" + getName() + PlasticConstants.DEFAULTS_FN + getInputVersion() + PlasticConstants.JSON_EXT;
//    	
//		File df = new File(defaultsFn);
//		if (!df.exists()) {
//			ResultException ex = new ResultException("The defaults file for this mapping doesn't exist: " + getName());
//			ex.moreMessages("You must run the plastic generation utility again.");
//			ex.setLocationInfo(getFile(), getLineNumber());
//			throw(ex);
//		}
//
//		readDefaults(defaultsFn);
//    	return(readDefaults(defaultsFn));
//    }
    
    public VersionedSchema getOutputVersionedSchema() {
    	VersionedSchema rc = null;
    	
    	switch(getOutputType()) {
    	case JSON:
    		rc = new VersionedSchema(getFolderStructure().getOutFileNamePrefix() + "-" + getName().getNameString() + PlasticConstants.OUTPUT, getOutputVersion(), PlasticConstants.JSON);
    		break;
    	}
    	
    	return(rc);
    }
    
    /**
     * Generates the input and output schema files, generate a default file if any variables have defaults and generates the output morpher.
     * Also creates the appropriate directory hierarchy if it doesn't already exist.
     * @param outdir the root output directory
     */
    public void generatePlasticFiles(String outdir) {
    	createFakeStructure(outdir);
    	
    	String inSchemaDir = createDirIfRequired(outdir + "/" + getFolderStructure().getInputSchemaDirName(getInputVersion()));
    	String outSchemaDir = createDirIfRequired(outdir + "/" + getFolderStructure().getOutputSchemaDirName(getOutputVersion()));
    	
    	String inSchemaFn = inSchemaDir + "/" + getFolderStructure().getInFileNamePrefix() + "-" + getName() + PlasticConstants.INPUT_FN + getInputVersion() + PlasticConstants.JSON_EXT;
    	String outSchemaFn = outSchemaDir + "/" + getFolderStructure().getOutFileNamePrefix() + "-" + getName() + PlasticConstants.OUTPUT_FN + getOutputVersion() + PlasticConstants.JSON_EXT;
    	
    	if (scalarDefaults != null) {
    		String defaultsFn = inSchemaDir + "/" + getFolderStructure().getInFileNamePrefix() + "-" + getName() + PlasticConstants.DEFAULTS_FN + getInputVersion() + PlasticConstants.JSON_EXT;
    		StringBuilder sb = new StringBuilder();
    		sb.append("{");
    		
    		Iterator<PlasticVariable> it = scalarDefaults.iterator();
    		while(it.hasNext()) {
    			PlasticVariable pv = it.next();
    			sb.append("\"" + pv.getName() + "\": ");
    			
    			if (pv.getDefault().length() == 0) {
    				// Empty string
    				sb.append("\"\"");
    			}
    			else if (pv.getDefault().startsWith(PlasticConstants.OPEN_CURLY)) {
    				// A default object
    				sb.append(pv.getDefault());
    			}
    			else if (pv.getDefault().startsWith(PlasticConstants.OPEN_SQUARE)) {
    				// A default array
    				sb.append(pv.getDefault());
    			}
    			else {
    				// Some other string value
    				sb.append("\"" + pv.getDefault() + "\"");
    			}
    			
    			if (it.hasNext())
    				sb.append(",");
    		}
    		
    		sb.append("}");
    		
//    		writeDefaults(defaultsFn, sb.toString());
    	}
    	
    	// Temporary
    	createDirIfRequired(outdir + "/" + getFolderStructure().getClassifiersDirName());
    	
    	createDirIfRequired(outdir + "/" + getFolderStructure().getLibDirName());
    	PlasticLibraryResourceUtil.instance().initLibraryClasses(outdir + "/" + getFolderStructure().getLibDirName());
    	
    	createDirIfRequired(outdir + "/" + getFolderStructure().getMorphersBaseDirName());
    	
    	PlasticGlobals.instance().trace("\nInput: " + inSchemaFn);
    	PlasticGlobals.instance().trace("Output: " + outSchemaFn);
    	
    	writeSchema(inSchemaFn, getInputSchema().toString());
    	writeSchema(outSchemaFn, getOutputSchema().toString());
    	
    	// Only generate the Morpher under the following conditions
    	if (needMorpher())
    		MorpherGenerator.generateOutputMorpher(outdir, this);
    	
    }
    
    /**
     * @return true if we have defaults, unused inputs/outputs or if validation required.
     */
    private boolean needMorpher() {
    	if (hasOptional() || 
    			hasScalarDefaults() || 
    			hasArrayDefaults() || 
    			ignoreUnusedInputs() || 
    			ignoreUnusedOutputs() ||
    			hasValidation() ||
    			hasEncodedURLs() ||
    			hasSplitVariables()
    			)
    		return(true);
    	
    	return(false);
    }
    
    /**
     * Plastic doesn't like to have a single hierarchy of files specified when called programmatically, so we
     * generate a fake directory structure.
     */
    private void createFakeStructure(String outdir) {
    	String classifiersDir 	= createDirIfRequired(outdir + "/" + PlasticConstants.FAKE + "/" + PlasticConstants.CLASSIFIERS);
    	String libDir 			= createDirIfRequired(outdir + "/" + PlasticConstants.FAKE + "/" + PlasticConstants.LIB);
    	String morphersDir 		= createDirIfRequired(outdir + "/" + PlasticConstants.FAKE + "/" + PlasticConstants.MORPHERS);
    	String schemasDir 		= createDirIfRequired(outdir + "/" + PlasticConstants.FAKE + "/" + PlasticConstants.SCHEMAS);
    	
    	createFileIfRequired(classifiersDir + README, FAKE_NOTE);
    	createFileIfRequired(libDir + README, FAKE_NOTE);
    	createFileIfRequired(morphersDir + README, FAKE_NOTE);
    	createFileIfRequired(schemasDir + README, FAKE_NOTE);
    }
    
    private void writeSchema(String fn, String json) {
    	JSONObject obj = new JSONObject(json);
    	
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fn));
			
			out.write(PrettyJSON.instance().prettyPrint(obj, true));
			
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
//    private void writeDefaults(String fn, String json) {
//    	JSONObject obj = new JSONObject(json);
//    	
//		try {
//			BufferedWriter out = new BufferedWriter(new FileWriter(fn));
//			
//			out.write(PrettyJSON.instance().prettyPrint(obj, true));
//			
//			out.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    }
    
//    private String readDefaults(String fn) {
//    	StringBuilder sb = new StringBuilder();
//    	
//    	try {
//			BufferedReader  in = new BufferedReader(new FileReader(new File(fn)));
//			String line = null;
//			
//			while((line = in.readLine()) != null) {
//				sb.append(line.trim());
//			}
//			in.close();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    	
//    	return(sb.toString());
//    }
    
    private void createFileIfRequired(String fn, String contents) {
    	File file = new File(fn);
    	
    	if (!file.exists()) {
    		try {
    			BufferedWriter out = new BufferedWriter(new FileWriter(fn));
    			
    			out.write(contents);
    			
    			out.close();
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		
    	}
    }
    
    private String createDirIfRequired(String dn) {
    	File dir = new File(dn);
    	if (!dir.exists()) {
    		PlasticGlobals.instance().trace("\nCreating: " + dn);
    		dir.mkdirs();
    	}
    	return(dn);
    }
    
}

