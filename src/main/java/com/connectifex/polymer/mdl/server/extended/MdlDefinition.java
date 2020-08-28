package com.connectifex.polymer.mdl.server.extended;

import org.dmd.dms.ClassDefinition;                                  // Used in derived constructors - (DMWGenerator.java:284)
import org.dmd.util.exceptions.ResultException;

import com.connectifex.polymer.mdl.server.generated.dmw.MdlDefinitionDMW;
import com.connectifex.polymer.mdl.server.generated.dsd.MdlModuleDefinitionManager;
import com.connectifex.polymer.mdl.shared.generated.dmo.MdlDefinitionDMO;


abstract public class MdlDefinition extends MdlDefinitionDMW {

    public MdlDefinition(){
        super();
    }

    public MdlDefinition(MdlDefinitionDMO dmo, ClassDefinition cd){
        super(dmo,cd);
    }

	public void performAdditionalValidation(MdlModuleDefinitionManager definitions) throws ResultException {
		
		
	}

	/**
	 * @return a shortened version of the file name and line number.
	 */
	public String getFileAndLine() {
		int lastSlash = getFile().lastIndexOf('/');
		if (lastSlash != -1) {
			return("File: " + getFile().substring(lastSlash+1) + " Line: " + getLineNumber());
		}
		else {
			return("File: " + getFile() + " Line: " + getLineNumber());
		}
	}
	
	/**
	 * @return the class of object, it's name and location.
	 */
	public String getLocationInfo() {
		return(getConstructionClassName() + ": " + getName() + " -- " + getFileAndLine());
	}

}

