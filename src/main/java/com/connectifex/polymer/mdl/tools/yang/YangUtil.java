package com.connectifex.polymer.mdl.tools.yang;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeMap;

import org.dmd.util.BooleanVar;
import org.dmd.util.exceptions.DebugInfo;
import org.dmd.util.exceptions.ResultException;
import org.dmd.util.exceptions.ResultSet;
import org.dmd.util.parsing.CommandLine;
import org.dmd.util.parsing.StringArrayList;

import com.connectifex.polymer.mdl.server.extended.plastic.util.PlasticGlobals;
import com.connectifex.polymer.mdl.tools.yang.util.ModuleVersion;
import com.connectifex.polymer.mdl.tools.yang.util.YangContext;
import com.connectifex.polymer.mdl.tools.yang.util.YangParser;
import com.connectifex.polymer.mdl.tools.yang.util.YangStructure;

public class YangUtil {

	private static String ___ = "\n";

	private static String DOT_YANG = ".yang";

	private CommandLine			commandLine 	= new CommandLine();
	
	private BooleanVar			helpFlag		= new BooleanVar();
	private BooleanVar			traceFlag		= new BooleanVar();
	private BooleanVar			mappingFlag		= new BooleanVar();
	private BooleanVar			structureFlag	= new BooleanVar();
	private	StringArrayList		srcdirs			= new StringArrayList();
	private	StringBuffer		outdir			= new StringBuffer();
	private	StringBuffer		file			= new StringBuffer();
	
	private YangContext 		context;
	
	private YangParser			parser;

    protected StringArrayList   searchPaths	= new StringArrayList();   		// The srcdirs prefixed with the workspace - useful to pass to config finders

	public YangUtil() {
		commandLine.addOption("-h", 		helpFlag, 		"Dumps the help message");
		commandLine.addOption("-srcdirs", 	srcdirs, 		"Specifies the source directories to search for mapping files.");
		commandLine.addOption("-trace", 	traceFlag, 		"Turns on detailed tracing.");
		commandLine.addOption("-mapping", 	mappingFlag, 	"Creates one or more polyer mappings.");
		commandLine.addOption("-structure", structureFlag, 	"Creates a dump of the structure");
		commandLine.addOption("-outdir", 	outdir, 		"The output directory");
		commandLine.addOption("-file", 		file, 		"The output directory");
	}
	
	public void run(String[] args) throws ResultException, IOException {
    	ResultSet rs = new ResultSet();
		
    	if (!commandLine.parseArgs(rs,args)) {
        	throw(new ResultException(rs));
        }

        if ((args.length == 0) || helpFlag.booleanValue()){
            displayHelp();
            return;
        }
        
        if (file.length() == 0) {
    		System.err.println("You must specify the YANG file to be parsed via the -file option.");
        	System.exit(1);
        }
        
        if (structureFlag.booleanValue() && mappingFlag.booleanValue() ) {
    		System.err.println("You must specify only one of -mapping or -structure");
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

        PlasticGlobals.instance().trace(traceFlag.booleanValue());
        
        context = new YangContext();
		for(String src: searchPaths) {
			context.addYangDirectory(src);
		}
		
		///////////////////////////////////////////////////////////////////////////////////
		String moduleName = file.toString();
		String moduleWithVersion = null;
		
		moduleName = moduleName.replace(DOT_YANG, "");
		
		int amperpos = moduleName.indexOf("@");
		if (amperpos != -1) {
			moduleWithVersion = moduleName;
			moduleName = moduleName.substring(0, amperpos);
		}
		
		DebugInfo.debug("Module name: " + moduleName);
		
		TreeMap<String,ModuleVersion> versions = context.getVersions(moduleName);
		if (versions == null) {
			System.err.println("Could not find any versions of YANG module: " + moduleName);
			System.exit(1);
		}
		
		ModuleVersion version = null;
		if (moduleWithVersion == null) {
			// The user didn't specify a version
			if (versions.size() > 1) {
				System.err.println("We found multiple versions of module: " + moduleName);
				System.err.println("Please specify one of these versions: ");
				for(ModuleVersion mv: versions.values()) {
					System.out.println(mv.nameAndRevision());
				}
				System.err.println();
			}
			
			// Only one version, so we're good
			version = versions.firstEntry().getValue();
			
		}
		else {
			// The user specified a version, see if we found it
			for(ModuleVersion mv: versions.values()) {
				if (mv.nameAndRevision().equals(moduleWithVersion)) {
					version = mv;
					break;
				}
			}
			
			if (version == null) {
				System.err.println("Could not find YANG module: " + moduleWithVersion);
				System.exit(1);
			}
		}

		file = new StringBuffer(version.nameAndRevision() + DOT_YANG);
    	parse(version);
	}
	
	private void parse(ModuleVersion version) throws ResultException, IOException {
		
		parser = new YangParser(context);
		
		PlasticGlobals.instance().trace("Parsing: " + file.toString());
		
		YangStructure root = parser.parse(version.location().getDirectory(),file.toString());
			
		if (traceFlag.booleanValue()) {
			Iterator<YangStructure> it = context.getAllLoadedModules();
			while(it.hasNext()) {
				YangStructure module = it.next();
				System.out.println("Loaded: " + module.name());
			}
			System.out.println();
		}

		if (structureFlag.booleanValue()) {
			System.out.println(root.toString());
			return;
		}
		
		YangToPolymer ytp = new YangToPolymer();
		ytp.convert(context,root,version);
		
//		Iterator<YangStructure> it = context.getAllLoadedModules();
//		while(it.hasNext()) {
//			YangStructure module = it.next();
//			System.out.println("Loaded: " + module.name());
//		}
	}
	
	public void displayHelp() {
		StringBuffer help = new StringBuffer();
        help.append(___);
        help.append("polymer yang -h -srcdirs -file -structure -trace \n");
        help.append(___);
        help.append("The polymer yang utility allows you to read a .yang file and generate various output:\n");
        help.append("  - the structure of a YANG module\n");
        help.append("  - a variabilized input schema for top level containers\n");
        help.append("  - an example JSON payload structure\n");
        help.append("  - \n");
        help.append("  - \n");
        help.append("  - \n");
        help.append(___);
        help.append(___);
        help.append(___);
        help.append(___);
        help.append("-h                 Displays help information\n");
        help.append(___);
        help.append("-srcdirs [dirs]    Indicates one or more directories to search for .yang modules. \n");
        help.append("                   Defaults to the current directory if not specified\n");
        help.append(___);
        help.append("-file <file name>  Indicates the name of the YANG file to be parsed.\n");
        help.append("                   You may specify just the module without revision if there is only one revision.\n");
        help.append(___);
        help.append("-mapping           Creates one or more polymer mappings that contain the variables and input schema.\n");
        help.append("                   Mappings are created for each top-level container or augment statement.\n");
        help.append(___);
        help.append("-structure         Creates a hierarchic dump of the structure of the YANG.\n");
        help.append(___);
//        help.append("-outdir                  Indicates the base directory where your plastic files will be created.\n");
//        help.append("                         Defaults to " + GENERATED + " if not specified\n");
        help.append(___);
        help.append("-trace             Turns on detailed tracing.\n");
        help.append(___);
        help.append(___);

        help.append(___);
        
        System.out.println(help.toString());
	}


}
