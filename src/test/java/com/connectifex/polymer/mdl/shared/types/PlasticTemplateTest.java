package com.connectifex.polymer.mdl.shared.types;

import java.io.File;
import java.io.IOException;

import org.dmd.dmc.DmcNameClashException;
import org.dmd.dmc.DmcValueException;
import org.dmd.dmc.DmcValueExceptionSet;
import org.dmd.dmc.rules.DmcRuleExceptionSet;
import org.dmd.util.exceptions.ResultException;
import org.junit.Before;
import org.junit.Test;

import com.connectifex.polymer.mdl.server.extended.plastic.PlasticMapping;
import com.connectifex.polymer.mdl.shared.types.PlasticSchema;
import com.connectifex.polymer.mdl.tools.plastic.MdlConfigLoader;

public class PlasticTemplateTest {
	static String workingDir;
	static String dataDir;

	@Before
	public void initialize() throws IOException, ResultException, DmcValueException, DmcNameClashException{
        File curr = new File(".");
        workingDir = curr.getCanonicalPath();
        dataDir = workingDir + "/src/test/java/com/connectifex/polymer/mdl/shared/types/data";
                
		System.out.println("*** Running from: " + workingDir);
		System.out.println("datadir: " + dataDir);
	}

	@Test
	public void test() throws ResultException, DmcValueException, IOException, DmcRuleExceptionSet, DmcNameClashException, DmcValueExceptionSet {
		MdlConfigLoader loader = new MdlConfigLoader();
		loader.load(dataDir, "test");
		
		PlasticMapping test1 = loader.definitionManager().getPlasticMappingDefinition("test1");
		
		System.out.println(test1.toOIF());
		
		PlasticSchema pt = new PlasticSchema(test1.getInputSchema());
		pt.initialize(test1);
		
		System.out.println(pt.getVariableNamesString());
	}
}
