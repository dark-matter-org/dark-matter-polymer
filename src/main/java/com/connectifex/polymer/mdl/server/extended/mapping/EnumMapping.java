package com.connectifex.polymer.mdl.server.extended.mapping;

// Generated from: org.dmd.util.codegen.ImportManager.getFormattedImports(ImportManager.java:82)
// Called from: org.dmd.dmg.generators.DMWGenerator.dumpExtendedClass(DMWGenerator.java:290)

import org.dmd.dmc.DmcValueException;
import org.dmd.dms.ClassDefinition;                                 // Used in derived constructors - (DMWGenerator.java:284)

import com.connectifex.polymer.mdl.server.generated.dmw.EnumMappingDMW;
import com.connectifex.polymer.mdl.shared.generated.dmo.EnumMappingDMO;
import com.connectifex.polymer.mdl.shared.generated.types.EnumValueInfo;


public class EnumMapping extends EnumMappingDMW {

	private final static String NOT_FOUND_STRING	= "Not Found";
	private final static String NOT_FOUND			= "NOT_FOUND";

    public EnumMapping(){
        super();
    }

    public EnumMapping(EnumMappingDMO dmo, ClassDefinition cd){
        super(dmo,cd);
    }

    public void addEnumValue(String[] columns) throws DmcValueException {
    	if (columns[2].equals(NOT_FOUND_STRING)) {
    		addEnumValues(new EnumValueInfo(columns[1], NOT_FOUND));
    	}
    	else {
    		addEnumValues(new EnumValueInfo(columns[1], columns[2]));
    	}
    }
}

