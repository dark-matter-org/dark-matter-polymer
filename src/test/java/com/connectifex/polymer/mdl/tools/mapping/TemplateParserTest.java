package com.connectifex.polymer.mdl.tools.mapping;

import java.io.File;
import java.io.IOException;

import org.dmd.dmc.DmcNameClashException;
import org.dmd.dmc.DmcValueException;
import org.dmd.dmc.rules.DmcRuleExceptionSet;
import org.dmd.dms.SchemaManager;
import org.dmd.util.exceptions.ResultException;
import org.junit.Before;
import org.junit.Test;

import com.connectifex.polymer.mdl.server.generated.MdlSchemaAG;
import com.connectifex.polymer.mdl.tools.mapping.TemplateParser;

public class TemplateParserTest {

	static String workingDir;
	static String dataDir;

	@Before
	public void initialize() throws IOException, ResultException, DmcValueException, DmcNameClashException{
        File curr = new File(".");
        workingDir = curr.getCanonicalPath();
        dataDir = workingDir + "/src/test/java/com/connectifex/omni/tools/mapping/data";
        
		System.out.println("*** Generator running from: " + workingDir);
		
		SchemaManager schema = new SchemaManager();
        MdlSchemaAG sd = new MdlSchemaAG();
        schema.manageSchema(sd.getInstance());

	}

	@Test
	public void test() throws IOException, ResultException, DmcValueException, DmcNameClashException, DmcRuleExceptionSet {
		// NEED TO GRAB THE DATA
		
//		TemplateParser p = new TemplateParser();
////		p.parseCSV(dataDir + "/mapping-template-20191202-02.csv");
//		
//		p.trace(true);
//		p.parseCSV(dataDir + "/20200128/EthernetContainerCapability.csv");
//		
//		p.parseCSV(dataDir + "/20200128/EthernetContainerConfiguration.csv");
//		
//		p.parseCSV(dataDir + "/20200128/EthernetContainerCurrentPerformance.csv");
//		
//		p.parseCSV(dataDir + "/20200128/EthernetContainerCurrentProblems.csv");
//		
//		p.parseCSV(dataDir + "/20200128/EthernetContainerHistoricalPerformance.csv");
//		
//		p.parseCSV(dataDir + "/20200128/EthernetContainerStatus.csv");
//		
		
	}
	
}
