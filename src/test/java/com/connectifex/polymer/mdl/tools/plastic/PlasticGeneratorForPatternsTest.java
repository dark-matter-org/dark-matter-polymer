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
import com.connectifex.polymer.mdl.tools.plastic.PlasticGenerator;

public class PlasticGeneratorForPatternsTest {

	static String workingDir;
	static String dataDir;
	static String outdir;
	
	@Before
	public void initialize() throws IOException, ResultException, DmcValueException, DmcNameClashException{
        File curr = new File(".");
        workingDir = curr.getCanonicalPath();
        dataDir = workingDir + "/src/test/java/com/connectifex/polymer/mdl/tools/plastic/data";
        
        outdir = workingDir + "/src/test/java/com/connectifex/polymer/mdl/tools/plastic/generated";
        
		System.out.println("*** Running from: " + workingDir);
		
	}

	@Test
	public void test() throws Exception {
		if (workingDir.startsWith(PlasticConstants.BUILD_ENV)) {
			System.out.println("Skipping development test: PlasticGeneratorTest");
			return;
		}
		
		String[] args = { "-srcdirs", dataDir, 
				"-modules", "patterns",
				"-trace", "true",
				"-outdir", outdir
		};

		PlasticGenerator generator = new PlasticGenerator();
		
		try {
			generator.run(args);
		} catch (ResultException | IOException | DmcValueException | DmcNameClashException | DmcRuleExceptionSet
				| DmcValueExceptionSet e) {
			System.err.println("\n\n" + e.toString());
			throw(e);
		}
	}

}
