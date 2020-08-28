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
import com.connectifex.polymer.mdl.tools.plastic.MdlConfigLoader;

public class MdlConfigLoaderTest {

	static String workingDir;
	static String dataDir;

	@Before
	public void initialize() throws IOException, ResultException, DmcValueException, DmcNameClashException{
        File curr = new File(".");
        workingDir = curr.getCanonicalPath();
        dataDir = workingDir + "/src/test/java/com/connectifex/polymer/mdl/tools/plastic/data";
        
		System.out.println("*** Running from: " + workingDir);
	}

	@Test
	public void test() throws ResultException, DmcValueException, IOException, DmcRuleExceptionSet, DmcNameClashException, DmcValueExceptionSet {
		if (workingDir.startsWith(PlasticConstants.BUILD_ENV)) {
			System.out.println("Skipping development test: MdlConfigLoaderTest");
			return;
		}
		
		MdlConfigLoader loader = new MdlConfigLoader();
		
//		String[] args = { "-srcdirs", dataDir, 
//				"-trace", 
//				"-lineTrace",
//				"-filter", "Ip_Interface_Configuration_V1.2",
//				"-outdir", dataDir + OUTDIR,
//				"-apiname", "onfoc-api"};
		
		loader.load(dataDir, "tutorials");
		
	}
}
