package com.connectifex.polymer.mdl.tools.yang.util;

import java.io.File;
import java.io.IOException;

import org.dmd.dmc.DmcNameClashException;
import org.dmd.dmc.DmcValueException;
import org.dmd.util.exceptions.ResultException;
import org.junit.Before;
import org.junit.Test;

public class YangParserTest {

	static String workingDir;
	static String workspaceDir;
	static String openconfigDir;
	static String ietfDir;

	@Before
	public void initialize() throws IOException, ResultException, DmcValueException, DmcNameClashException{
        File curr = new File(".");
        workingDir = curr.getCanonicalPath();
                
		System.out.println("*** Running from: " + workingDir);
		workspaceDir = workingDir.substring(0, workingDir.lastIndexOf("/"));
		System.out.println(workspaceDir);
		
		// https://github.com/openconfig/public
		openconfigDir 	= workspaceDir + "/public/release/models";
		
		// https://github.com/YangModels
		ietfDir			= workspaceDir + "/yang/standard/ietf/RFC";
		
	}

	@Test
	public void test() throws ResultException, IOException {
		File ocd = new File(openconfigDir);
		File ietfd = new File(ietfDir);
		
		if (ocd.exists() && ietfd.exists()) {
			YangContext context = new YangContext();
			context.addYangDirectory(openconfigDir);
			context.addYangDirectory(ietfDir);
			
			YangParser parser = new YangParser(context);
//			parser.trace(true);
			
			parser.parse(openconfigDir + "/policy", "openconfig-routing-policy.yang");
		}
	}
}
