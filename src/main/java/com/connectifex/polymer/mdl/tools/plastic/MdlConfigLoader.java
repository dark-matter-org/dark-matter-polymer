package com.connectifex.polymer.mdl.tools.plastic;

import java.io.IOException;
import java.util.Iterator;

import org.dmd.dmc.DmcNameClashException;
import org.dmd.dmc.DmcValueException;
import org.dmd.dmc.DmcValueExceptionSet;
import org.dmd.dmc.rules.DmcRuleExceptionSet;
import org.dmd.util.exceptions.ResultException;
import org.dmd.util.parsing.ConfigLocation;

import com.connectifex.polymer.mdl.server.extended.MdlDefinition;
import com.connectifex.polymer.mdl.server.extended.MdlModule;
import com.connectifex.polymer.mdl.server.generated.dsd.MdlModuleDefinitionManager;
import com.connectifex.polymer.mdl.server.generated.dsd.MdlModuleGenUtility;

public class MdlConfigLoader extends MdlModuleGenUtility {
	
	private MdlModuleDefinitionManager definitionManager;
	
	public MdlConfigLoader() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Loads the configuration from the specified directory.
	 * @param configDir the config directory.
	 * @throws ResultException
	 * @throws DmcValueException
	 * @throws IOException
	 * @throws DmcRuleExceptionSet
	 * @throws DmcNameClashException
	 * @throws DmcValueExceptionSet
	 */
	public void load(String configDir) throws ResultException, DmcValueException, IOException, DmcRuleExceptionSet, DmcNameClashException, DmcValueExceptionSet {
		String[] args = { "-srcdir", configDir };
		
		super.run(args);
	}
	
	/**
	 * Loads only the specified module.
	 * @param configDir the configuration directory
	 * @param module the module to load
	 * @throws ResultException
	 * @throws DmcValueException
	 * @throws IOException
	 * @throws DmcRuleExceptionSet
	 * @throws DmcNameClashException
	 * @throws DmcValueExceptionSet
	 */
	public void load(String configDir, String module) throws ResultException, DmcValueException, IOException, DmcRuleExceptionSet, DmcNameClashException, DmcValueExceptionSet {
		String[] args = { "-srcdir", configDir , "-targets", module};
		
		super.run(args);
	}
	
	public MdlModuleDefinitionManager definitionManager() {
		return(definitionManager);
	}

	@Override
	public void parsingComplete(MdlModule module, ConfigLocation location, MdlModuleDefinitionManager definitions) throws ResultException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void objectResolutionComplete(MdlModule module, ConfigLocation location, MdlModuleDefinitionManager definitions) throws ResultException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void generate(MdlModule module, ConfigLocation location, MdlModuleDefinitionManager definitions) throws ResultException, IOException {
		definitionManager = definitions;
		performAdditionalValidation(definitions);
	}

	@Override
	public void generate(MdlModuleDefinitionManager definitions) throws ResultException, IOException {
		definitionManager = definitions;
		performAdditionalValidation(definitions);
	}

	@Override
	public void displayHelp() {
		// TODO Auto-generated method stub
		
	}

	private void performAdditionalValidation(MdlModuleDefinitionManager definitions) throws ResultException {
		ResultException rc = null;

		Iterator<MdlDefinition> it = definitions.getAllMdlDefinition();
		while (it.hasNext()) {
			try {
				MdlDefinition ld = it.next();
				ld.performAdditionalValidation(definitions);

			} catch (ResultException ex) {
				if (rc == null)
					rc = ex;
				else {
					rc.result.addResults(ex.result);
				}
			}
		}

		if (rc != null) {
			throw (rc);
		}
	}
}
