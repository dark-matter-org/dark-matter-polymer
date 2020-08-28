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

package com.connectifex.polymer.mdl.tools.plastic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.dmd.dmc.DmcNameClashException;
import org.dmd.dmc.DmcValueException;
import org.dmd.dmc.DmcValueExceptionSet;
import org.dmd.dmc.rules.DmcRuleExceptionSet;
import org.dmd.util.BooleanVar;
import org.dmd.util.exceptions.ResultException;
import org.dmd.util.exceptions.ResultSet;
import org.dmd.util.parsing.CommandLine;

public class NewModuleCreator {

	private static String ___ = "\n";
	private static String MDL = ".mdl";
	
	final static String camelCase = "[a-zA-Z][a-zA-Z0-9/-]*";


	private CommandLine			commandLine 	= new CommandLine();
	private BooleanVar			helpFlag		= new BooleanVar();
	private	StringBuffer		name			= new StringBuffer();
	private	StringBuffer		outdir			= new StringBuffer();

	public NewModuleCreator() {
		commandLine.addOption("-h", 		helpFlag, 		"Dumps the help message");
		commandLine.addOption("-outdir", 	outdir, 		"The output directory");
		commandLine.addOption("-name", 		name, 			"The name of the module");

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
        
        if (outdir.length() == 0) {;
        	outdir.append(".");
//    		System.err.println("\nYou must specify -outdir\n");
//        	System.exit(1);
        }
        
        File currentDir = new File(".");
        
        if (outdir.toString().startsWith(".")) {
			String alt = currentDir.getCanonicalPath() + outdir.toString().replaceFirst(".", "");
        	outdir = new StringBuffer(alt);
        }

        if (name.length() == 0) {
    		System.err.println("\nYou must specify -name\n");
        	System.exit(1);
        }
        
        if (!name.toString().matches(camelCase)) {
    		System.err.println("\nThe module name should start with a letter followed by letters, numbers or dashes.\n");
        	System.exit(1);
        }
        
        String fn = outdir.toString() + "/" + name.toString() + MDL;
        File module = new File(fn);
        if (module.exists()) {
    		System.err.println("The specified module already exists: " + fn);
        	System.exit(1);
        }
        
        createModule(fn);
        
        System.out.println("\nModule created: " + fn + "\n\n");
	}	
	
	private void createModule(String fn) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("MdlModule\n");
		sb.append("name " + name.toString() + "\n");
//		sb.append("// Uncomment the following line to insert tracing in your generated morphers\n");
//		sb.append("//insertTracingInMorpher		true\n");
//		sb.append("// Uncomment the following line to allow generated plastic schemas to have errors\n");
//		sb.append("//allowPlasticGenErrors		true\n");
		sb.append("description Add a description of the module here. If your description\n");
		sb.append(" spans multiple lines, start subsequent lines with whitespace.\n");
		sb.append("\n");
		
		sb.append("FolderStructure\n");
		sb.append("name " + name.toString() + "-structure\n");
		sb.append("inFolder1 api-in\n");
//		sb.append("//inFolder2 x\n");
//		sb.append("//inFolder3 y\n");
		sb.append("outFolder1 api-out\n");
//		sb.append("//outFolder2 api-out\n");
//		sb.append("//outFolder3 api-out\n");
		sb.append("description Provide a description of your chosen folder structure.\n");
		sb.append("\n");

		sb.append("// Your PlasticMappings will go here - use \"polymer var\" to help generate them\n");

		createFile(fn, sb.toString());
	}
	
    private static void createFile(String fn, String contents) {
    	
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fn));
			
			out.write(contents);
			
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    		
    }

	
	public void displayHelp() {
		StringBuffer help = new StringBuffer();
        help.append(___);
        help.append("polymer new -h -outdir -name \n");
        help.append(___);
        help.append("The polymer new utility is used to generate a new Mapping Definition Language (MDL)\n");
        help.append("module that contains a base FolderStructure.\n");
        help.append(___);
        help.append("-h                    Displays help information.\n");
        help.append(___);
        help.append("-outdir <dir>         Indicates where to generate the module.\n");
        help.append("                      Defaults to the current directory.\n");
        help.append(___);
        help.append("-name <module name>   Indicates the name of the module. This should be unique across all of your MDL files.\n");
        help.append(___);
        help.append(___);
        
        System.out.println(help.toString());
	}

}
