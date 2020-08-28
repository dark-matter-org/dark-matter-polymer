package com.connectifex.polymer.mdl.tools.yang;

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

public class YangUtilTestWithTrace {

	static String workingDir;
	static String dataDir;

	@Before
	public void initialize() throws IOException, ResultException, DmcValueException, DmcNameClashException{
        File curr = new File(".");
        workingDir = curr.getCanonicalPath();
        dataDir = workingDir + "/src/test/java/com/connectifex/polymer/mdl/tools/yang/data";
                
		System.out.println("*** Running from: " + workingDir);
	}

	@Test
	public void test() throws ResultException, IOException, DmcValueException, DmcNameClashException, DmcRuleExceptionSet, DmcValueExceptionSet {
		if (workingDir.startsWith(PlasticConstants.BUILD_ENV)) {
			System.out.println("Skipping development test: YangUtilTest");
			return;
		}
		
		YangUtil util = new YangUtil();
		
		String[] args = { "-srcdirs", dataDir, 
//				"-file", "ietf-l3vpn-svc@2018-01-19.yang",
				"-file", "ietf-l3vpn-svc",
				"-trace",
//				"-plasticdir", plasticdir
		};

		util.run(args);
	}
}
