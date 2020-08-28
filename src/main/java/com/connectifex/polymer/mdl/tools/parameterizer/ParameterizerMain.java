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

package com.connectifex.polymer.mdl.tools.parameterizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.dmd.dmc.DmcValueException;
import org.dmd.util.BooleanVar;
import org.dmd.util.exceptions.ResultException;
import org.dmd.util.exceptions.ResultSet;
import org.dmd.util.parsing.CommandLine;
import org.json.JSONException;

import com.connectifex.util.json.Parameterizer;


public class ParameterizerMain {
	
	private static String ___ = "\n";

	private static boolean inputTypeAvailable;
	private static boolean inputAnalysis;
	
    private final static String IN = "i";
    private final static String OUT = "o";
    
    private static BufferedReader  in = new BufferedReader(new InputStreamReader(System.in));

	private static CommandLine			commandLine 	= new CommandLine();
	private static BooleanVar			helpFlag		= new BooleanVar();

	public static void main(String[] args) throws IOException, DmcValueException, ResultException {
		commandLine.addOption("-h", 		helpFlag, 		"Dumps the help message");
		
    	ResultSet rs = new ResultSet();

    	if (!commandLine.parseArgs(rs,args)) {
        	throw(new ResultException(rs));
        }

        if (helpFlag.booleanValue() || (args.length > 0) ){
            displayHelp();
            return;
        }

		
		Parameterizer p = new Parameterizer();
				
		while(true) {
			inputTypeAvailable = false;
			
			while(!inputTypeAvailable) {
				System.out.println("\nEnter i for input schema or o for output schema:\n\n");
				String input = getLine();
				input = input.trim();
				if (input.equals(IN)) {
					inputAnalysis = true;
					inputTypeAvailable = true;
				}
				else if (input.equals(OUT)) {
					inputAnalysis = false;
					inputTypeAvailable = true;
				}
				else {
					System.out.println("Unrecognized input - please enter 'i' or 'o'\n");
				}
			}
			
			
			System.out.println("Paste in a JSON Yang configuration, end your input with a blank line:\n\n");
			String json = getPastedContent();
			
			if (json.trim().length() == 0) {
				continue;
			}
			
			try {
				p.initialize(json);
			}
			catch(JSONException ex) {
				System.err.println("\n" + ex.getMessage() +"\n");
				continue;
			}
			
			if (p.hasParameters()) {
				if (inputAnalysis) {
//					System.out.println("The example input has indentifiable parameters.\n");
					System.out.println("Here's an example PlasticMapping, minus the output information\n");
					System.out.println(p.getPlasticMapping() + "\n\n");
					
					
					System.out.println("// And here's an example PlasticTest that uses your specified input example:\n");
					System.out.println(p.getPlasticTest());
				}
				else {
					System.out.println("Here is the output information to be added. You will need to merge the");
					System.out.println("variables with those of the input schema.\n");
					
					System.out.println(p.getOutputInfo());
				}
				
			}
			else {
				System.out.println("WARNING: The JSON has no indentifiable parameters:\n\n");
			}
			
		}

		

	}
	
	private static void displayHelp() {
		StringBuffer help = new StringBuffer();
        help.append(___);
        help.append("polymer var -h \n");
        help.append(___);
        help.append("The polymer var utility allows you to interactively parse JSON and generate\n");
        help.append("PlasticMappings and/or thier associated outputSchema information.\n");
        help.append(___);
        help.append(___);
        
        System.out.println(help.toString());
	}

	private static String getPastedContent() throws IOException {
		StringBuilder sb = new StringBuilder();
		
		while(true) {
			String line = getLine();
			
			if (line.length() == 0)
				break;
			
			sb.append(line + "\n");
		}
		
		return(sb.toString());
	}

	/**
	 * Gets info from the user
	 * @return a response entered by the user.
	 * @throws IOException
	 */
	private static String getLine() throws IOException {
		String input = in.readLine();
		if (input == null){
			System.out.println("Exitting...");
			System.exit(0);
		}
		return(input.trim());
	}

}
