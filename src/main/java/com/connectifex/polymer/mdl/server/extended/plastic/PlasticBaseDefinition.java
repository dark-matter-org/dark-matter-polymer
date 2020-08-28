// Copyright 2020 connectifex
// 
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
// 
//      http://www.apache.org/licenses/LICENSE-2.0
// 
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//

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

