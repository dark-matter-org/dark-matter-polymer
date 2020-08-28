package com.connectifex.polymer.mdl.server.extended.mapping;

import java.util.ArrayList;

import org.dmd.dmc.DmcValueException;
import org.dmd.dmc.types.CheapSplitter;
import org.dmd.dms.ClassDefinition;                                // Used in derived constructors - (DMWGenerator.java:284)

import com.connectifex.polymer.mdl.server.generated.dmw.MapComplexDMW;
import com.connectifex.polymer.mdl.shared.generated.dmo.MapComplexDMO;


public class MapComplex extends MapComplexDMW {

    public MapComplex(){
        super();
    }

    public MapComplex(MapComplexDMO dmo, ClassDefinition cd){
        super(dmo,cd);
    }
    
    public MapComplex(String[] columns, String fileName, int lineNumber) throws DmcValueException {
    	setName(lineNumber);
    	setLineNumber(lineNumber);
    	setFile(fileName);
    	
    }
    
    /**
     * Each subsequent row after the first that created this complex mapping will have
     * path, type and attribute name.
     * @param columns the columns of the row
     */
    public void addField(String[] columns) {
    	
    }

	@Override
	ArrayList<String> getFromPath() {
		ArrayList<String>	rc = null;
		
		if (getFromAttrPath() != null) {
			rc = CheapSplitter.split(getFromAttrPath(), SLASH, false, true);
			
//			if (rc.size() > 0) {
//				int last = rc.size()-1;
//				int osPos = rc.get(last).indexOf(OPEN_SQUARE);
//				if (osPos != -1) {
//					String newPart = rc.get(last).substring(0,osPos);
//					rc.remove(last);
//					rc.add(newPart);
//				}
//			}
		}
		else {
			rc = new ArrayList<>();
		}
		return(rc);
	}

}

