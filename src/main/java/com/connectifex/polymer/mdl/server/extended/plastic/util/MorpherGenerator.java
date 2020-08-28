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

package com.connectifex.polymer.mdl.server.extended.plastic.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.dmd.dmc.types.CheapSplitter;
import org.dmd.util.codegen.ImportManager;
import org.dmd.util.codegen.Manipulator;
import org.dmd.util.exceptions.DebugInfo;

import com.connectifex.polymer.mdl.server.extended.plastic.PlasticMapping;
import com.connectifex.polymer.mdl.server.extended.validation.PolymerValidator;
import com.connectifex.polymer.mdl.shared.generated.types.PlasticVariable;
import com.connectifex.polymer.mdl.shared.generated.types.PolymerValidatorREF;

/**
 * The MorpherGenerator generates a Groovy class derived from:
 * org.opendaylight.plastic.implementation.ExtendedBasicMorpher
 * 
 * Examples of code used in the generated code base has been borrowed from
 * tests associated with te plastic code base. Look in:
 * plastic/src/test/groovy/org/opendaylight/plastic/implementation/author/BetterJsonSpec.groovy
 * BetterJson lets you inspect things, but not alter them
 * 
 * @author peter
 *
 */
public class MorpherGenerator {

	private final static String GROOVY 	= ".groovy";
	private final static String DASH 	= "-";

	public static void generateOutputMorpher(String outdir, PlasticMapping mapping) {

		ImportManager imports = new ImportManager();
		imports.addImport("org.opendaylight.plastic.implementation.ExtendedBasicMorpher", "Our base class");
		imports.addImport("org.opendaylight.plastic.implementation.author.BetterJson", "Better access to JSON structures");
		imports.addImport("groovy.json.*", "JSON handling");
		
		if (mapping.wantTracing())
			imports.addImport("PolymerTrace", "Tracing support");
		
		if (mapping.hasOptional())
			imports.addImport("OptionalValueRemover", "To remove optional values");
		
		// Add additional imports of required
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(imports.getFormattedImports());
		
		String mdn = createDirIfRequired(outdir + "/" + mapping.getFolderStructure().getMorphersVersionedDirName(mapping.getOutputVersion()));
		String fcn = mapping.getFolderStructure().getOutFileNamePrefix() + DASH + mapping.getName() + PlasticConstants.OUTPUT + DASH + mapping.getOutputVersion();
		String fn = mdn + "/" + fcn + GROOVY;
		
		String cn = createClassName(fn);
		
		PlasticGlobals.instance().trace("\nMorpher fn: " + fn);
		PlasticGlobals.instance().trace("     Class: " + cn);
		
    	sb.append("class " + cn + " extends ExtendedBasicMorpher {\n\n");
    	
    	addStaticInitializers(sb, mapping);
    	
    	addConstructor(sb, cn, mapping);
    	
    	addTweakInputs(sb, mapping);
    	
    	addTweakValues(sb, mapping);
    	
    	addTweakParsed(sb, mapping);
    	
    	addValidationMethods(sb, mapping);
    	
    	sb.append("}\n\n");

    	createFile(fn, sb.toString());
	}
	
	private static void addStaticInitializers(StringBuilder sb, PlasticMapping mapping) {
		if (mapping.hasValidation()) {
			for(PlasticVariable pv: mapping.getValidationVariables()) {
				Iterator<PolymerValidatorREF> it = pv.getValidate();
				while(it.hasNext()) {
					PolymerValidatorREF ref = it.next();
					PolymerValidator validator = (PolymerValidator) ref.getObject().getContainer();
					
					sb.append(validator.getInitialization(mapping, pv));
				}
			}
		}
		
		if (mapping.hasSplitVariables()) {
			for(PlasticVariable pv: mapping.getSplitVariables()) {
				// TODO: the splitting mechanism should have its own interface, not validator
				
				PolymerValidator validator = (PolymerValidator) pv.getSplit().getObject().getContainer();
				sb.append(validator.getInitialization(mapping, pv));
			}
		}
	}

	private static void addConstructor(StringBuilder sb, String className, PlasticMapping mapping) {
		if (mapping.hasOptional()) {
			sb.append("\n");			
			sb.append("    // We have optional values and keep track of keys without values here\n");			
			sb.append("    ArrayList<String> noValueKeys;\n");			
			sb.append("\n");			
			sb.append("    // This will be set to true if any optional values need to be removed\n");			
			sb.append("    boolean           removeOptional;\n");			
			sb.append("\n");			
		}
		sb.append("    " + className + "() {\n");
		sb.append("        info(\"Processing morpher " + className + "\")\n");
		sb.append("        \n");
		
		sb.append("        \n");
		
		if (mapping.ignoreUnusedInputs())
			sb.append("        ignoreUnusedInputs()\n");
		
		if (mapping.ignoreUnusedOutputs())
			sb.append("        ignoreUnusedOutputs()\n");
		
		if (mapping.hasOptional()) {
			sb.append("\n");			
			sb.append("        optionalInputs()\n");
			sb.append("        noValueKeys    = new ArrayList<String>()\n");			
			sb.append("        removeOptional = false\n");			
		}
		
		if (mapping.hasValidation()) {
			sb.append("\n");			
			for(PlasticVariable pv: mapping.getValidationVariables()) {
				Iterator<PolymerValidatorREF> it = pv.getValidate();
				while(it.hasNext()) {
					PolymerValidatorREF ref = it.next();
					PolymerValidator validator = (PolymerValidator) ref.getObject().getContainer();
					
					sb.append(validator.getConstructorInfo(mapping, pv));
				}
			}
		}
		
		if (mapping.hasSplitVariables()) {
			for(PlasticVariable pv: mapping.getSplitVariables()) {
				// TODO: the splitting mechanism should have its own interface, not validator
				
				PolymerValidator validator = (PolymerValidator) pv.getSplit().getObject().getContainer();
				sb.append(validator.getConstructorInfo(mapping, pv));
			}
		}


		sb.append("    }\n");
	}
	
	private static void addTweakInputs(StringBuilder sb, PlasticMapping mapping) {
		sb.append("\n");
		sb.append("    // Generated from: " + DebugInfo.getWhereWeAreNow() + "\n");
		sb.append("    // inputs  java.util.LinkedHashMap\n");
		sb.append("    // payload groovy.json.internal.LazyMap\n");
		sb.append("    // - input value validation\n");
		sb.append("    // - calculating an input value based on examining the payload\n");
		sb.append("    void tweakInputs(Map inputs, Object payload) {\n");
		sb.append("        info(\"Processing tweakInputs\")\n");
		sb.append("        \n");
		
		if (mapping.wantTracing())
			sb.append("        PolymerTrace.traceTweakInputs(inputs,payload)\n");

		if (mapping.hasAnyDefaults() || mapping.hasOptional()) {
			sb.append("        Set<String> keys = null\n\n");			
		}
		
		if (mapping.hasOptional()) {
			sb.append("\n");
			sb.append("        // Keep track of keys with no values so that they can be marked for removal in tweakValues()\n");
			sb.append("        keys = inputs.keySet()\n");
			sb.append("        for(String key: keys){\n");
			sb.append("            if (inputs.get(key) == null){\n");
			sb.append("                noValueKeys.add(key)\n");
			sb.append("            }\n");
			sb.append("        }\n");
		}
		
		if (mapping.hasScalarDefaults()) {
			sb.append("        // Insert defaults for scalar values\n");
			sb.append("        keys = inputs.keySet()\n");
			sb.append("        for(String key: keys){\n");
			
			ArrayList<PlasticVariable> defaults = mapping.getScalarDefaults();
			for(PlasticVariable pv: defaults) {
				sb.append("            if (key.equals(\"" + pv.getName() + "\") && (inputs.get(key) == null) )\n");
				sb.append("                inputs.put(key,\"" + pv.getDefault() + "\")\n");
			}
			sb.append("        }\n\n");
		}
		
		if (mapping.hasValidation()) {
			for(PlasticVariable pv: mapping.getValidationVariables()) {
				Iterator<PolymerValidatorREF> it = pv.getValidate();
				while(it.hasNext()) {
					PolymerValidatorREF ref = it.next();
					PolymerValidator validator = (PolymerValidator) ref.getObject().getContainer();
					
					sb.append(validator.getCall(mapping, pv));
				}
			}
		}
		
		if (mapping.hasSplitVariables()) {
			for(PlasticVariable pv: mapping.getSplitVariables()) {
				// TODO: the splitting mechanism should have its own interface, not validator
				
				PolymerValidator validator = (PolymerValidator) pv.getSplit().getObject().getContainer();
				sb.append(validator.getCall(mapping, pv));
			}
		}
		

		// If any autogenerated tweaks - generate them
		sb.append("        \n");
		// If any manual tweaks add them
		sb.append("        \n");
    	sb.append("    }\n");
		
	}
	
	private static void addTweakValues(StringBuilder sb, PlasticMapping mapping) {
		sb.append("\n");
		sb.append("    // Generated from: " + DebugInfo.getWhereWeAreNow() + "\n");
		sb.append("    // inputs  java.util.LinkedHashMap\n");
		sb.append("    // outputs java.util.LinkedHashMap\n");
		sb.append("    void tweakValues(Map inputs, Map outputs) {\n");
		sb.append("        info(\"Processing tweakValues\")\n");
		sb.append("        \n");
		
		if (mapping.hasArrayDefaults()) {
			sb.append("        Set<String> keys = null\n");			
			sb.append("        // Insert defaults for values in arrays\n");
			sb.append("        keys = outputs.keySet()\n");
			sb.append("        for(String key: keys){\n");

			ArrayList<PlasticVariable> defaults = mapping.getArrayDefaults();
			for(PlasticVariable pv: defaults) {
				int sbPos = pv.getName().indexOf('[');
				String key = pv.getName().substring(0, sbPos+1);
				
				sb.append("            if (key.startsWith(\"" + key + "\") && (outputs.get(key) == null) )\n");
				sb.append("                outputs.put(key,\"" + pv.getDefault() + "\")\n");
			}
			sb.append("        }\n\n");
		}
		
		if (mapping.hasOptional()) {
			sb.append("        // Insert magic value that flags optional values to remove in tweakParsed()\n");
			sb.append("        for(String key: noValueKeys){\n");
			sb.append("            if (outputs.get(key) == null){\n");
			sb.append("                outputs.put(key,\"ReMoVeOpTiOnAl\")        \n");
			sb.append("                removeOptional = true\n");
			sb.append("            }\n");
			sb.append("        }\n");
		}
		
		if (mapping.hasEncodedURLs()) {
			sb.append("\n");
			for(PlasticVariable pv: mapping.getEncodedURLs()) {
				sb.append("        outputs['" + pv.getName()+ "'] = URLEncoder.encode(inputs['" + pv.getName() + "'], \"UTF-8\")\n\n");
			}
		}
		
		if (mapping.wantTracing())
			sb.append("        PolymerTrace.traceTweakValues(inputs,outputs)\n");
		// If any autogenerated tweaks - generate them
		sb.append("        \n");
		// If any manual tweaks add them
		sb.append("        \n");
    	sb.append("    }\n");
		
	}

	private static void addTweakParsed(StringBuilder sb, PlasticMapping mapping) {
		sb.append("\n");
		sb.append("    // Generated from: " + DebugInfo.getWhereWeAreNow() + "\n");
		sb.append("    // input  groovy.json.internal.LazyMap\n");
		sb.append("    // output java.util.LinkedHashMap\n");
		sb.append("    void tweakParsed(Object payload, Object output) {\n");
		sb.append("        info(\"Processing tweakParsed\")\n");
		sb.append("        \n");
		
		if (mapping.hasOptional()) {
			String tracing = "false";
			
			if (mapping.wantTracing())
				tracing = "true";
			
			sb.append("        if (removeOptional){\n");
			sb.append("            OptionalValueRemover.removeOptional(output, " + tracing + ")\n");
			sb.append("        }\n");
			sb.append("        \n");
		}
		
		
		
		if (mapping.wantTracing())
			sb.append("        PolymerTrace.traceTweakParsed(payload,output)\n");
		// If any autogenerated tweaks - generate them
		sb.append("        \n");
		// If any manual tweaks add them
		sb.append("        \n");
    	sb.append("    }\n");
		
	}
	
	private static void addValidationMethods(StringBuilder sb, PlasticMapping mapping) {
		if (mapping.hasValidation()) {
			for(PlasticVariable pv: mapping.getValidationVariables()) {
				Iterator<PolymerValidatorREF> it = pv.getValidate();
				while(it.hasNext()) {
					PolymerValidatorREF ref = it.next();
					PolymerValidator validator = (PolymerValidator) ref.getObject().getContainer();
					
					sb.append(validator.getImplementation(mapping, pv));
				}
			}
		}
	}

	/**
	 * Creates the class name based on the file name
	 * @param fn the file name
	 * @return a legal groovy class name
	 */
	private static String createClassName(String fn) {
		String tmp = null;
		StringBuilder sb = new StringBuilder();
		
		int lastSlash = fn.lastIndexOf('/');
		tmp = fn.substring(lastSlash+1);
		tmp = tmp.replace(".groovy", "");
		tmp = tmp.replace("-", " ");
		tmp = tmp.replace(".", " ");
		ArrayList<String> tokens = CheapSplitter.split(tmp, ' ', false, true);
		
		for(int i=0; i<tokens.size()-2; i++) {
			sb.append(Manipulator.capFirstChar(tokens.get(i)) + "_");
		}
		
		sb.append(Manipulator.capFirstChar(tokens.get(tokens.size()-2)) + "_");
		sb.append(Manipulator.capFirstChar(tokens.get(tokens.size()-1)));
		
		return(sb.toString());
	}

    private static String createDirIfRequired(String dn) {
    	File dir = new File(dn);
    	if (!dir.exists()) {
    		PlasticGlobals.instance().trace("Creating: " + dn);
    		dir.mkdirs();
    	}
    	return(dn);
    }
    
    private static void createFile(String fn, String contents) {
    	File file = new File(fn);
    	
//    	if (!file.exists()) {
    		try {
    			BufferedWriter out = new BufferedWriter(new FileWriter(fn));
    			
    			out.write(contents);
    			
    			out.close();
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		
//    	}
    }
    


}
