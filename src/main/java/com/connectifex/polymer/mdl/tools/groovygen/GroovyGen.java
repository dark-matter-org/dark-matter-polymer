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

package com.connectifex.polymer.mdl.tools.groovygen;

import java.io.IOException;

import org.dmd.dmc.DmcNameClashException;
import org.dmd.dmc.DmcValueException;
import org.dmd.dmc.rules.DmcRuleExceptionSet;
import org.dmd.dms.SchemaDefinition;
import org.dmd.dms.SchemaManager;
import org.dmd.dms.util.DmsSchemaParser;
import org.dmd.util.exceptions.ResultException;
import org.dmd.util.parsing.CommandLine;
import org.dmd.util.parsing.ConfigFinder;
import org.dmd.util.parsing.ConfigLocation;
import org.dmd.util.parsing.ConfigVersion;
import org.dmd.util.parsing.StringArrayList;

/**
 * The GroovyGen class borrows its basic form from the org.dmd.dms.tools.dmogenerator.DmoGenUtility
 * and allows for generation of Groovy classes that represent dark-matter ComplexTypeDefinitions.
 * This is experimental. The ComplexTypeDefinition in question should not have any object references
 * and must have a tags value of GroovyGen.
 *
 */
public class GroovyGen {

	// Our base schema manager
	SchemaManager		dmsSchema;
	
	// The schema manager that will hold definitions read by the schema parser
	SchemaManager		readSchemas;
	
	// Finds our available schemas
	ConfigFinder		finder;
	
	// The thing that parses the available schemas
	DmsSchemaParser		parser;

	CommandLine		cl;
	StringArrayList	srcdir 		= new StringArrayList();
	StringBuffer	workspace	= new StringBuffer();
	StringArrayList	targets		= new StringArrayList();
	StringBuffer	outdir		= new StringBuffer();

	public GroovyGen() {
		// TODO Auto-generated constructor stub
	}
	
	public void run(String[] args) throws ResultException, DmcValueException, DmcNameClashException, IOException {
		cl = new CommandLine();
        cl.addOption("-srcdir",		srcdir,  	"The source directories to search.");
        cl.addOption("-workspace", 	workspace, 	"The workspace prefix");
        cl.addOption("-targets",	targets,	"Indicates you only want to generate for the specified configs");
		
		cl.parseArgs(args);
		
		System.out.print("TARGETS(" + targets.size() + "): ");
		for(int i=0; i<targets.size(); i++)
			System.out.print(targets.get(i) + " ");
		System.out.println();

		dmsSchema = new SchemaManager();
		readSchemas = null;

		if (srcdir.size() > 0){
			StringArrayList search = srcdir;
			if (workspace.length() > 0){
				StringArrayList augmented = new StringArrayList();
				for(String dir: srcdir){
					augmented.add(workspace.toString() + "/" + dir);
				}
				search = augmented;
			}
			finder = new ConfigFinder(search.iterator());
		}
		else
			finder = new ConfigFinder();
		
		finder.addSuffix(".dms");
		finder.addJarPrefix("dark-matter-data");
		finder.findConfigs();
		
		parser = new DmsSchemaParser(dmsSchema, finder);

    	for(ConfigVersion version: finder.getVersions().values()){
    		ConfigLocation loc = version.getLatestVersion();
    		if (!loc.isFromJAR()){
    			// Wasn't in a jar, so try to generate
    			
    			if (targets.contains(loc.getConfigName())){
    				generateFromConfig(loc);
    			}
    			else{
    				System.out.println("groovygen: " + loc.getConfigName() + " is not in the -targets list - not generating:  " + loc.getDirectory() + "\n");
    			}
    		}
    	}
	}

	void generateFromConfig(ConfigLocation location) throws DmcNameClashException{
    	try {
    		// Create a new manager into which the parsed schemas will be loaded
    		readSchemas = new SchemaManager();
    		
    		// Parse the specified schema
			SchemaDefinition sd = parser.parseSchema(readSchemas, location.getConfigName(), false);
			
			System.out.println(sd.toOIF());
			
//			if ((sd != null) && checkRules.booleanValue()){
//				parser.checkRules(sd);
//			}
//			
//			if (checkOnly.booleanValue())
//				return;
//			
//			if (docdir.length() > 0){
//				if (workspace.length() > 0)
//					docGenerator.dumpSchemaDoc(workspace.toString() + "/" + docdir.toString(), readSchemas);
//				else
//					docGenerator.dumpSchemaDoc(docdir.toString(), readSchemas);
//				
//				docGenerator.addReadSchemas(readSchemas);
//			}
//			else{
//				// Generate the code
//				
//				FileUpdateManager.instance().reportProgress(System.out);
//				FileUpdateManager.instance().reportErrors(System.err);
//				FileUpdateManager.instance().generationStarting();
//				
//				codeGenerator.generateCode(readSchemas, sd, location);
//				
//				FileUpdateManager.instance().generationComplete();
//			}
//						
		} catch (ResultException e) {
			System.err.println(e.toString());
			System.exit(1);
		} catch (DmcValueException e) {
			System.err.println(e.toString());
			e.printStackTrace();
			System.exit(1);
		} catch (DmcRuleExceptionSet e) {
			System.err.println(e.toString());
//			e.printStackTrace();
			System.exit(1);
		}
//		} catch (IOException e) {
//			System.err.println(e.toString());
//			e.printStackTrace();
//			System.exit(1);
//		}
			
	}

}
