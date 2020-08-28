package com.connectifex.polymer.mdl.server.extended.mapping;

import java.util.ArrayList;

import org.dmd.dmc.DmcValueException;
import org.dmd.dms.ClassDefinition;                                      // Used in derived constructors - (DMWGenerator.java:284)

import com.connectifex.polymer.mdl.server.generated.dmw.MappingDirectiveDMW;
import com.connectifex.polymer.mdl.shared.generated.dmo.MappingDirectiveDMO;


abstract public class MappingDirective extends MappingDirectiveDMW {
	
	private final static 	String 	PREFIX 		= "directive";
	protected final static 	String 	OPEN_SQUARE = "[";
	protected final static 	char 	SLASH 		= '/';

    public MappingDirective(){
        super();
    }

    public MappingDirective(MappingDirectiveDMO dmo, ClassDefinition cd){
        super(dmo,cd);
    }

    /**
     * Creates a name based on the line number from which the directive was read - this
     * gives us tracability back to the input .csv.
     * @param lineNumber the line number
     * @throws DmcValueException
     */
    protected void setName(int lineNumber) throws DmcValueException {
    	if (lineNumber < 10)
    		setName(PREFIX + "00" + lineNumber);
    	else if (lineNumber < 100)
    		setName(PREFIX + "0" + lineNumber);
    	else
    		setName(PREFIX + lineNumber);
   }
    
   abstract ArrayList<String> getFromPath();
}

