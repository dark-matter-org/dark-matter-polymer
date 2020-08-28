package com.connectifex.polymer.mdl.server.extended;

import org.dmd.dms.ClassDefinition;                              // Used in derived constructors - (DMWGenerator.java:284)

import com.connectifex.polymer.mdl.server.generated.dmw.MdlModuleDMW;
import com.connectifex.polymer.mdl.shared.generated.dmo.MdlModuleDMO;


public class MdlModule extends MdlModuleDMW {

    public MdlModule(){
        super();
    }

    public MdlModule(MdlModuleDMO dmo, ClassDefinition cd){
        super(dmo,cd);
    }

}

