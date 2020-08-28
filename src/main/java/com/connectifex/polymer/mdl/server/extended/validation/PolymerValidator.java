package com.connectifex.polymer.mdl.server.extended.validation;

import org.dmd.dms.ClassDefinition;                                     // Used in derived constructors - (DMWGenerator.java:284)

import com.connectifex.polymer.mdl.server.generated.dmw.PolymerValidatorDMW;
import com.connectifex.polymer.mdl.shared.generated.dmo.PolymerValidatorDMO;


abstract public class PolymerValidator extends PolymerValidatorDMW {

    public PolymerValidator(){
        super();
    }

    public PolymerValidator(PolymerValidatorDMO dmo, ClassDefinition cd){
        super(dmo,cd);
    }

}

