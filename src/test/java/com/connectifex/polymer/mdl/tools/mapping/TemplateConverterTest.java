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
import com.connectifex.polymer.mdl.tools.mapping.TemplateConverter;

public class TemplateConverterTest {

	static String workingDir;
	static String dataDir;
	private final static String MAPPING_PROJECT = "leap-onf-to-openconfig";
	private final static String CORE 				= "/core-model-1-4-1";
	private final static String ETHERNET_CONTAINER 	= "/ethernet-container-2-0-0";
	private final static String PURE_ETHERNET 		= "/pure-ethernet-structure-2-0-0";

	private final static String IP_INTERFACE 		= "/IpInterface";

	private final static String OUTDIR 		= "/plastic/onfoc";

	@Before
	public void initialize() throws IOException, ResultException, DmcValueException, DmcNameClashException{
        File curr = new File(".");
        workingDir = curr.getCanonicalPath();
        
        int lastSlash = workingDir.lastIndexOf('/');
        
        dataDir = workingDir.substring(0, lastSlash+1) + MAPPING_PROJECT;
        
//        dataDir = workingDir + "/src/test/java/com/connectifex/omni/tools/mapping/data";
        
		System.out.println("*** Generator running from: " + workingDir);
		System.out.println("*** Data dir: " + dataDir);
		
		SchemaManager schema = new SchemaManager();
        MdlSchemaAG sd = new MdlSchemaAG();
        schema.manageSchema(sd.getInstance());

	}

//	@Test
//	public void test() throws IOException, ResultException, DmcValueException, DmcNameClashException, DmcRuleExceptionSet {
//		String[] args = { "-srcdirs", dataDir + CORE, dataDir + ETHERNET_CONTAINER, dataDir + PURE_ETHERNET, "-trace"};
//		TemplateConverter converter = new TemplateConverter();
//		
//		converter.run(args);
//	}

	@Test
	public void test() throws IOException, ResultException, DmcValueException, DmcNameClashException, DmcRuleExceptionSet {
		String[] args = { "-srcdirs", dataDir + IP_INTERFACE, 
				"-trace", 
				"-lineTrace",
				"-filter", "Ip_Interface_Configuration_V1.2",
				"-outdir", dataDir + OUTDIR,
				"-apiname", "onfoc-api"};
		TemplateConverter converter = new TemplateConverter();
		
		converter.run(args);
	}
	
}
