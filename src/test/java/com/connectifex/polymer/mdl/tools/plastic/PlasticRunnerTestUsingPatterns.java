package com.connectifex.polymer.mdl.tools.plastic;

import java.io.File;
import java.io.IOException;

import org.dmd.dmc.DmcNameClashException;
import org.dmd.dmc.DmcValueException;
import org.dmd.dmc.DmcValueExceptionSet;
import org.dmd.dmc.rules.DmcRuleExceptionSet;
import org.dmd.util.exceptions.ResultException;
import org.junit.Before;
import org.junit.Test;

import com.connectifex.polymer.mdl.server.extended.plastic.util.PlasticConstants;
import com.connectifex.polymer.mdl.tools.plastic.PlasticRunner;

public class PlasticRunnerTestUsingPatterns {

	static String workingDir;
	static String dataDir;
	static String plasticdir;

	@Before
	public void initialize() throws IOException, ResultException, DmcValueException, DmcNameClashException{
        File curr = new File(".");
        workingDir = curr.getCanonicalPath();
        dataDir = workingDir + "/src/test/java/com/connectifex/polymer/mdl/tools/plastic/data";
        
        plasticdir = workingDir + "/src/test/java/com/connectifex/polymer/mdl/tools/plastic/generated";
        
		System.out.println("*** Running from: " + workingDir);
	}

	@Test
	public void test() throws ResultException, IOException, DmcValueException, DmcNameClashException, DmcRuleExceptionSet, DmcValueExceptionSet {
		if (workingDir.startsWith(PlasticConstants.BUILD_ENV)) {
			System.out.println("Skipping development test: PlasticRunnerTest");
			return;
		}

		String[] args = { "-srcdirs", dataDir, 
				"-modules", "patterns",
//				"-trace", "true",
				"-plasticdir", plasticdir
		};

		PlasticRunner runner = new PlasticRunner();
		
		runner.run(args);
	}

}
