package com.connectifex.polymer.mdl.server.extended.plastic;

import org.dmd.dms.ClassDefinition;                                          // Used in derived constructors - (DMWGenerator.java:284)

import com.connectifex.polymer.mdl.server.generated.dmw.PlasticBaseDefinitionDMW;
import com.connectifex.polymer.mdl.shared.generated.dmo.PlasticBaseDefinitionDMO;


abstract public class PlasticBaseDefinition extends PlasticBaseDefinitionDMW {

    public PlasticBaseDefinition(){
        super();
    }

    public PlasticBaseDefinition(PlasticBaseDefinitionDMO dmo, ClassDefinition cd){
        super(dmo,cd);
    }

}

