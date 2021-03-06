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

package com.connectifex.polymer.mdl.tools.yang;

import java.io.File;
import java.io.IOException;

import org.dmd.dmc.DmcNameClashException;
import org.dmd.dmc.DmcValueException;
import org.dmd.util.exceptions.ResultException;
import org.junit.Before;
import org.junit.Test;

public class YangUtilTest {

	static String workingDir;
	static String dataDir;

	static String workspaceDir;
	static String openconfigDir;
	static String ietfDir;

	@Before
	public void initialize() throws IOException, ResultException, DmcValueException, DmcNameClashException{
        File curr = new File(".");
        workingDir = curr.getCanonicalPath();
        dataDir = workingDir + "/src/test/java/com/connectifex/polymer/mdl/tools/yang/data";
                
		System.out.println("*** Running from: " + workingDir);
		
		workspaceDir = workingDir.substring(0, workingDir.lastIndexOf("/"));
		System.out.println(workspaceDir);
		
		// https://github.com/openconfig/public
		openconfigDir 	= workspaceDir + "/public/release/models";
		
		// https://github.com/YangModels
		ietfDir			= workspaceDir + "/yang/standard/ietf/RFC";

	}

//	@Test
//	public void test() throws ResultException, IOException, DmcValueException, DmcNameClashException, DmcRuleExceptionSet, DmcValueExceptionSet {
//		if (workingDir.startsWith(PlasticConstants.BUILD_ENV)) {
//			System.out.println("Skipping development test: YangUtilTest");
//			return;
//		}
//		
//		YangUtil util = new YangUtil();
//		
//		String[] args = { "-srcdirs", dataDir, 
////				"-file", "ietf-l3vpn-svc@2018-01-19.yang",
//				"-file", "ietf-l3vpn-svc",
////				"-trace",
////				"-plasticdir", plasticdir
//		};
//
//		util.run(args);
//	}
	
	@Test
	public void test2() throws ResultException, IOException {
		File ocd = new File(openconfigDir);
		File ietfd = new File(ietfDir);
		
		if (ocd.exists() && ietfd.exists()) {
			YangUtil util = new YangUtil();

			String[] args = { "-srcdirs", openconfigDir, ietfDir, 
					"-file", "openconfig-routing-policy",
//					"-trace",
//					"-plasticdir", plasticdir
			};

			util.run(args);

		}
		
	}
}
