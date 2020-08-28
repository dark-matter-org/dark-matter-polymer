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

package com.connectifex.polymer.tools;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

import org.dmd.dmc.DmcNameClashException;
import org.dmd.dmc.DmcValueException;
import org.dmd.dmc.DmcValueExceptionSet;
import org.dmd.dmc.rules.DmcRuleExceptionSet;
import org.dmd.util.exceptions.DebugInfo;
import org.dmd.util.exceptions.ResultException;
import org.dmd.util.formatting.PrintfFormat;

import com.connectifex.polymer.mdl.tools.parameterizer.ParameterizerMain;
import com.connectifex.polymer.mdl.tools.plastic.NewModuleCreator;
import com.connectifex.polymer.mdl.tools.plastic.PlasticGenerator;
import com.connectifex.polymer.mdl.tools.plastic.PlasticPatternRunner;
import com.connectifex.polymer.mdl.tools.plastic.PlasticRunner;
import com.connectifex.polymer.mdl.tools.plastic.util.PlasticLibraryResourceUtil;

/**
 * The PolymerSelector examines the first specified argument and, on the basis of that
 * starts the appropriate sub tool/utility along with the remainder of the arguments provided.
 */

public class PolymerSelector {

	private static String NEW 		= "new";
	private static String VAR 		= "var";
	private static String GEN 		= "gen";
	private static String TEST 		= "test";
	private static String PATTERN 	= "pattern";
	private static String GUIDE 	= "guide";

	int longest;
	// Key: utility name
	// Value: a short description
	private TreeMap<String,String>	utilities;

	public PolymerSelector(String[] args) throws IOException, DmcValueException, ResultException, DmcRuleExceptionSet, DmcNameClashException, DmcValueExceptionSet {
		File curr = new File(".");
		
		init();
		
		if (args.length == 0) {
			displayHelp();
			return;
		}
		
		if (utilities.get(args[0]) == null) {
			System.err.println("Unknown utility: " + args[0] + "\n");
			
			displayHelp();
			System.exit(1);
		}
		
		run(args);
	}
	
//	private void run(String[] args) throws IOException, DmcValueException, ResultException, DmcRuleExceptionSet, DmcNameClashException, DmcValueExceptionSet {
	private void run(String[] args)  {
		String[] newargs = new String[args.length - 1];
		
		for(int i=1; i<args.length; i++) {
			newargs[i-1] = args[i];
		}
				
		try {
			if (args[0].equals(NEW)) {
				NewModuleCreator nmc = new NewModuleCreator();
				nmc.run(newargs);
			}
			else if (args[0].equals(VAR)) {
				ParameterizerMain.main(newargs);
			}
			else if (args[0].equals(GEN)) {
				PlasticGenerator gen = new PlasticGenerator();
				gen.run(newargs);
			}
			else if (args[0].equals(TEST)) {
				PlasticRunner test = new PlasticRunner();
				test.run(newargs);
			}
			else if (args[0].equals(PATTERN)) {
				PlasticPatternRunner test = new PlasticPatternRunner();
				test.run(newargs);
			}
			else if (args[0].equals(GUIDE)) {
				String userGuide = PlasticLibraryResourceUtil.instance().extractUserGuide();
				
				if (Desktop.isDesktopSupported()) {
				    try {
				        File myFile = new File(userGuide);
				        Desktop.getDesktop().open(myFile);
				    } catch (IOException ex) {
				        System.err.println("There's no default application to open .pdf files!\n");
				        System.exit(1);
				    }
				}
			}
		} catch(Exception ex) {
			System.err.println("\n\n" + ex.toString());
		}
	}

	private void init() {
		utilities = new TreeMap<>();
		
		utilities.put(NEW, 		"Runs the new module creation utility");
		utilities.put(VAR, 		"Runs the VARiablizer utility");
		utilities.put(GEN, 		"Runs the plastic file GENeration utility");
		utilities.put(TEST, 	"Runs plastic TEST utility");
		utilities.put(PATTERN, 	"Runs plastic PATTERN test utility");
		utilities.put(GUIDE, 	"Displays the user guide");
		
		for(String key: utilities.keySet()) {
			if (key.length() > longest)
				longest = key.length();
		}
	}
	
	private void displayHelp() {
		System.out.println("\n\nPlease specify one of the following utilities as the first argument: \n");
		
		PrintfFormat format = new PrintfFormat("%-" + longest + "s");
		for(String key: utilities.keySet()) {
			System.out.println(format.sprintf(key) + " " + utilities.get(key));
		}
		
		System.out.println();
	}

}
