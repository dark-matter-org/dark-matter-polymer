package com.connectifex.polymer.tools.analyzer;

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

public class MorpherAnalyzerTest {

	static String workingDir;
	static String dataDir;

	@Before
	public void initialize() throws IOException, ResultException, DmcValueException, DmcNameClashException{
        File curr = new File(".");
        workingDir = curr.getCanonicalPath();
        dataDir = workingDir + "/src/test/java/com/connectifex/polymer/tools/analyzer/data";
        
		System.out.println("*** Running from: " + workingDir);
	}
	@Test
	public void test() throws IOException, ResultException, DmcValueException, DmcRuleExceptionSet, DmcNameClashException, DmcValueExceptionSet {
		if (workingDir.startsWith(PlasticConstants.BUILD_ENV)) {
			System.out.println("Skipping development test: PlasticGeneratorTest");
			return;
		}

		MorpherAnalyzer analyzer = new MorpherAnalyzer();
		analyzer.analyze("/Users/peter/Downloads/von-bnc-nc-neon", dataDir);
	}
}
