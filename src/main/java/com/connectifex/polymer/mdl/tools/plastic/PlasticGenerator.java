package com.connectifex.polymer.mdl.tools.plastic;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.dmd.dmc.DmcNameClashException;
import org.dmd.dmc.DmcValueException;
import org.dmd.dmc.DmcValueExceptionSet;
import org.dmd.dmc.rules.DmcRuleExceptionSet;
import org.dmd.util.BooleanVar;
//import org.dmd.util.exceptions.DebugInfo;
import org.dmd.util.exceptions.ResultException;
import org.dmd.util.exceptions.ResultSet;
import org.dmd.util.parsing.CommandLine;
import org.dmd.util.parsing.ConfigLocation;
import org.dmd.util.parsing.StringArrayList;

import com.connectifex.polymer.mdl.server.extended.MdlDefinition;
import com.connectifex.polymer.mdl.server.extended.MdlModule;
import com.connectifex.polymer.mdl.server.extended.plastic.PlasticMapping;
import com.connectifex.polymer.mdl.server.extended.plastic.util.PlasticGlobals;
import com.connectifex.polymer.mdl.server.generated.dsd.MdlModuleDefinitionManager;
import com.connectifex.polymer.mdl.server.generated.dsd.MdlModuleGeneratorInterface;
import com.connectifex.polymer.mdl.server.generated.dsd.MdlModuleParsingCoordinator;

public class PlasticGenerator implements MdlModuleGeneratorInterface {

	private static String ___ = "\n";
	
	private static String GENERATED = "./generated";

	private MdlModuleParsingCoordinator	parser;
	private CommandLine			commandLine 	= new CommandLine();
	private BooleanVar			helpFlag		= new BooleanVar();
	private BooleanVar			traceFlag		= new BooleanVar();
	private	StringArrayList		srcdirs			= new StringArrayList();
	private	StringArrayList		modules			= new StringArrayList();
	
	private	StringBuffer		outdir			= new StringBuffer();
	
    protected StringArrayList   jars			= new StringArrayList();   	// The jars that will be searched for .Mdl config files
    protected StringArrayList   searchPaths	= new StringArrayList();   		// The srcdirs prefixed with the workspace - useful to pass to config finders

	public PlasticGenerator() {
		commandLine.addOption("-h", 		helpFlag, 		"Dumps the help message");
		commandLine.addOption("-srcdirs", 	srcdirs, 		"Specifies the source directories to search for mapping files.");
		commandLine.addOption("-modules", 	modules, 		"Specifies the modules for which we want to generate plastic files.");
		commandLine.addOption("-trace", 	traceFlag, 		"Turns on detailed tracing.");
		commandLine.addOption("-outdir", 	outdir, 		"The output directory");
	}
	
	public void run(String[] args) throws ResultException, IOException, DmcValueException, DmcNameClashException, DmcRuleExceptionSet, DmcValueExceptionSet {
    	ResultSet rs = new ResultSet();

    	if (!commandLine.parseArgs(rs,args)) {
        	throw(new ResultException(rs));
        }

        if ((args.length == 0) || helpFlag.booleanValue()){
            displayHelp();
            return;
        }
        
        PlasticGlobals.instance().trace(traceFlag.booleanValue());
        
        if (modules.size() == 0) {
    		System.err.println("You must specify at least one module via the -modules option");
        	System.exit(1);
        }
        
        if (outdir.length() == 0) {
        	outdir.append(GENERATED);
//    		System.err.println("You must specify -outdir");
//        	System.exit(1);
        }
        
        File currentDir = new File(".");
        
        if (outdir.toString().startsWith(".")) {
			String alt = currentDir.getCanonicalPath() + outdir.toString().replaceFirst(".", "");
        	outdir = new StringBuffer(alt);
        }
        
        if (srcdirs.size() == 0) {
        	// No srcdirs specified, so add the current directory
            searchPaths.add(currentDir.getCanonicalPath());
        }
        else {
        	for(String dir: srcdirs) {
        		if (dir.startsWith(".")) {
        			// Alter the path to be fully qualified by appending the current directory path
        			String alt = currentDir.getCanonicalPath() + dir.replaceFirst(".", "");
        			searchPaths.add(alt);
        		}
        		else {
        			searchPaths.add(dir);
        		}
        	}
        }
        
        
        parser = new MdlModuleParsingCoordinator(this, searchPaths, jars);
        
        for(String module: modules) {
        	parser.generateForConfig(module);
        }
        
	}
	
	public void displayHelp() {
		StringBuffer help = new StringBuffer();
        help.append(___);
        help.append("polymer gen -h -srcdirs -modules -plasticdir -trace \n");
        help.append(___);
        help.append("The polymer gen utility allows you to generate the folder structure and plastic\n");
        help.append("input schemas, output schemas and morphers inferred from your PlasticMappings.\n");
        help.append(___);
        help.append("-h                       Displays help information\n");
        help.append(___);
        help.append("-modules [module names]  Indicates the modules for which generation will take place.\n");
        help.append(___);
        help.append("-srcdirs [dirs]          Indicates one or more directories to search for MDL modules. \n");
        help.append("                         Defaults to the current directory if not specified\n");
        help.append(___);
        help.append("-outdir                  Indicates the base directory where your plastic files will be created.\n");
        help.append("                         Defaults to " + GENERATED + " if not specified\n");
        help.append(___);
        help.append("-trace                   Turns on detailed tracing.\n");
        help.append(___);
        help.append(___);

        help.append(___);
        
        System.out.println(help.toString());
	}

	@Override
	public void parsingComplete(MdlModule module, ConfigLocation location, MdlModuleDefinitionManager definitions) throws ResultException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void objectResolutionComplete(MdlModule module, ConfigLocation location, MdlModuleDefinitionManager definitions) throws ResultException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void generate(MdlModule module, ConfigLocation location, MdlModuleDefinitionManager definitions) throws ResultException, IOException {
		performAdditionalValidation(definitions);
		
		if (definitions.getPlasticMappingCount() == 0) {
			ResultException ex = new ResultException("No PlasticMappings were found in this module: " + module.getName());
			throw(ex);
		}
		
		Iterator<PlasticMapping> pmit = definitions.getAllPlasticMapping();
		while(pmit.hasNext()){
			PlasticMapping pm = pmit.next();
			pm.generatePlasticFiles(outdir.toString());
		}
		
		
	}

	@Override
	public void generate(MdlModuleDefinitionManager definitions) throws ResultException, IOException {
		performAdditionalValidation(definitions);
		
	}
	
	private void performAdditionalValidation(MdlModuleDefinitionManager definitions) throws ResultException {
		ResultException rc = null;

		Iterator<MdlDefinition> it = definitions.getAllMdlDefinition();
		while (it.hasNext()) {
			try {
				MdlDefinition ld = it.next();
				ld.performAdditionalValidation(definitions);

			} catch (ResultException ex) {
				if (rc == null)
					rc = ex;
				else {
					rc.result.addResults(ex.result);
				}
			}
		}

		if (rc != null) {
			throw (rc);
		}
	}

}
