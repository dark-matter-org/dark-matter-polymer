package com.connectifex.polymer.mdl.tools.plastic;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeSet;

import org.dmd.dmc.DmcNameClashException;
import org.dmd.dmc.DmcValueException;
import org.dmd.dmc.DmcValueExceptionSet;
import org.dmd.dmc.rules.DmcRuleExceptionSet;
import org.dmd.util.BooleanVar;
import org.dmd.util.exceptions.ResultException;
import org.dmd.util.exceptions.ResultSet;
import org.dmd.util.parsing.CommandLine;
import org.dmd.util.parsing.ConfigLocation;
import org.dmd.util.parsing.StringArrayList;

import com.connectifex.polymer.mdl.server.extended.MdlDefinition;
import com.connectifex.polymer.mdl.server.extended.MdlModule;
import com.connectifex.polymer.mdl.server.extended.plastic.PatternTest;
import com.connectifex.polymer.mdl.server.extended.plastic.util.PlasticGlobals;
import com.connectifex.polymer.mdl.server.generated.dsd.MdlModuleDefinitionManager;
import com.connectifex.polymer.mdl.server.generated.dsd.MdlModuleGeneratorInterface;
import com.connectifex.polymer.mdl.server.generated.dsd.MdlModuleParsingCoordinator;

public class PlasticPatternRunner implements MdlModuleGeneratorInterface {
	
	
	private static String ___ = "\n";

	private MdlModuleParsingCoordinator	parser;
	private CommandLine			commandLine 	= new CommandLine();
	private BooleanVar			helpFlag		= new BooleanVar();
//	private BooleanVar			traceFlag		= new BooleanVar();
	private	StringArrayList		srcdirs			= new StringArrayList();
	private	StringArrayList		modules			= new StringArrayList();

	private	StringArrayList		run				= new StringArrayList();
	private TreeSet<String>		runSet			= new TreeSet<String>();
	
	private	StringArrayList		skip			= new StringArrayList();
	private TreeSet<String>		skipSet			= new TreeSet<String>();

    protected StringArrayList   jars			= new StringArrayList();   	// The jars that will be searched for .Mdl config files
    protected StringArrayList   searchPaths		= new StringArrayList();   		// The srcdirs prefixed with the workspace - useful to pass to config finders

    public PlasticPatternRunner() {
		commandLine.addOption("-h", 			helpFlag, 		"Dumps the help message");
		commandLine.addOption("-srcdirs", 		srcdirs, 		"Specifies the source directories to search for mapping files.");
		commandLine.addOption("-modules", 		modules, 		"Specifies the modules for which we want to generate plastic files.");
//		commandLine.addOption("-trace", 		traceFlag, 		"Turns on detailed tracing.");
		
		commandLine.addOption("-run", 			run, 			"The tags to tests to run.");
		commandLine.addOption("-skip", 			skip, 			"The tags to tests to skip.");
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
        
        PlasticGlobals.instance().trace(true);
//        PlasticGlobals.instance().trace(traceFlag.booleanValue());
        
        if (modules.size() == 0) {
    		System.err.println("You must specify at least one module via the -modules option");
        	System.exit(1);
        }
                
        File currentDir = new File(".");
        
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
        
        if (run.size() > 0) {
        	for(String tag: run)
        		runSet.add(tag);
        }
        
        if (skip.size() > 0) {
        	for(String tag: skip)
        		skipSet.add(tag);
        }
        
        if ( (run.size() > 0) && (skip.size() > 0)) {
        	for(String tag: run) {
        		if (skipSet.contains(tag)) {
            		System.err.println("Duplicate tag in -run and -skip: " + tag);
                	System.exit(1);
        		}
        	}
        }
        
        parser = new MdlModuleParsingCoordinator(this, searchPaths, jars);
        
        for(String module: modules) {
        	parser.generateForConfig(module);
        }
        
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

		if (definitions.getPatternTestCount() == 0) {
			ResultException ex = new ResultException("No PatternTests were found in this module: " + module.getName());
			throw(ex);
		}
		PlasticGlobals.instance().trace("\n\n");
		
		Iterator<PatternTest> it = definitions.getAllPatternTest();
		while(it.hasNext()) {
			PatternTest pt = it.next();
			pt.execute(runSet, skipSet);
		}
	}

	@Override
	public void generate(MdlModuleDefinitionManager definitions) throws ResultException, IOException {
		performAdditionalValidation(definitions);
		
	}

	@Override
	public void displayHelp() {
		StringBuffer help = new StringBuffer();
        help.append(___);
        help.append("polymer pattern -h -srcdirs -modules -plasticdir -trace -run -skip\n");
        help.append(___);
        help.append("The polymer pattern utility allows you run PatternTests that exercise your\n");
        help.append("pattern matching. By default, all tests will be executed unless you specify\n");
        help.append("the tags of tests to run or the tags of tests to skip.\n");
        help.append(___);
        help.append("-h               Displays help information\n");
        help.append(___);
        help.append("-srcdirs [dir]   Indicates one or more directories to search for MDL modules.  \n");
        help.append(___);
        help.append("-modules         Indicates the modules to be loaded.\n");
        help.append(___);
        help.append("-trace           Will display tracing of test execution.\n");
        help.append(___);
        help.append("-run [tag]       One or more tags of pattern tests that you want to run.\n");
        help.append(___);
        help.append("-skip [tag]      One or more tags of pattern tests you want to skip.\n");
        help.append(___);
        help.append("Note: the name of a PatternTest is considered its primary tag, but you may also add other tags.\n");
        help.append(___);

        System.out.println(help.toString());
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
