package com.connectifex.polymer.mdl.server.extended.mapping;

// Generated from: org.dmd.util.codegen.ImportManager.getFormattedImports(ImportManager.java:82)
// Called from: org.dmd.dmg.generators.DMWGenerator.dumpExtendedClass(DMWGenerator.java:290)

import java.util.ArrayList;

import org.dmd.dmc.DmcValueException;
import org.dmd.dmc.types.CheapSplitter;
import org.dmd.dms.ClassDefinition;                         // Used in derived constructors - (DMWGenerator.java:284)
import org.dmd.util.exceptions.DebugInfo;

import com.connectifex.polymer.mdl.server.generated.dmw.MapDMW;
import com.connectifex.polymer.mdl.shared.generated.dmo.MapDMO;


public class Map extends MapDMW {
	
//	private final static String ELIPSIS = "...";
	private final static String NOT_FOUND = "Not Found";
	
	// Set to true if we have both the from and to sides of the mapping
	private boolean canMap;

    public Map(){
        super();
    }

    public Map(MapDMO dmo, ClassDefinition cd){
        super(dmo,cd);
    }
    
    public boolean canMap() {
    	return(canMap);
    }

    public Map(String[] columns, String fileName, int lineNumber) throws DmcValueException {
    	canMap = true;
    	
    	setName(lineNumber);
    	setLineNumber(lineNumber);
    	setFile(fileName);
//    	if (columns[1].startsWith(ELIPSIS)) {
//    		setFromAttrPath(columns[1].substring(3));
//    		setFromAttrName(columns[2]);
//    		setFromAttrType(columns[3]);
//    	}
//    	else {
//    		setFromAttrPath(columns[1]);
//    	}
		setFromAttrPath(columns[1]);
		setFromAttrName(columns[2]);
		setFromAttrType(columns[3]);
		
		
		if (columns.length > 4) {
			if (columns[4].equals(NOT_FOUND)) {
				canMap = false;
				return;
			}
		}
		
		if (columns.length >= 7) {
			setToAttrPath(columns[4]);
			setToAttrName(columns[5]);
			setToAttrType(columns[6]);
		}
		
		if (columns.length == 8) {
			setEnumMapping(columns[7]);
		}
		
    }

	@Override
	ArrayList<String> getFromPath() {
		ArrayList<String>	rc = CheapSplitter.split(getFromAttrPath(), SLASH, false, true);
		
//		if (rc.size() > 0) {
//			int last = rc.size()-1;
//			int osPos = rc.get(last).indexOf(OPEN_SQUARE);
//			if (osPos != -1) {
//				String newPart = rc.get(last).substring(0,osPos);
//				rc.remove(last);
//				rc.add(newPart);
//			}
//		}
		return(rc);
	}
}

