package com.connectifex.polymer.mdl.server.extended.mapping;

import java.util.ArrayList;

import org.dmd.dmc.DmcValueException;
import org.dmd.dmc.types.CheapSplitter;
import org.dmd.dms.ClassDefinition;                            // Used in derived constructors - (DMWGenerator.java:284)

import com.connectifex.polymer.mdl.server.generated.dmw.MapAnyDMW;
import com.connectifex.polymer.mdl.shared.generated.dmo.MapAnyDMO;


public class MapAny extends MapAnyDMW {

    public MapAny(){
        super();
    }

    public MapAny(MapAnyDMO dmo, ClassDefinition cd){
        super(dmo,cd);
    }

    public MapAny(String[] columns, String fileName, int lineNumber) throws DmcValueException {
    	setName(lineNumber);
    	setLineNumber(lineNumber);
    	setFile(fileName);
    	
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

