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

package com.connectifex.polymer.mdl.server.extended.plastic.groovyhack;

/**
 * This is based on the PlasticGroupAndName class that's a complex type.
 * It has been stripped down to allow for it to be used in Groovy.
 * This is a temporary hack.
 */
public class PlasticGroupAndNameGroovy {

    // The number of the pattern group
    Integer groupNumberV;

    // The name of the pattern group
    String groupNameV;

    // An optional note
    String noteV;

    /**
     * Default constructor.
     */
    public PlasticGroupAndNameGroovy(){
    }

    /**
     * All fields constructor.
     * Generated from: org.dmd.dms.util.NewComplexTypeFormatter.dumpComplexType(NewComplexTypeFormatter.java:186)
     */
    public PlasticGroupAndNameGroovy(Integer groupNumber_, String groupName_, String note_) {
        groupNumberV = groupNumber_;
        groupNameV = groupName_;
        noteV = note_;
    }

    /**
     * String form.
     * Generated from: org.dmd.dms.util.NewComplexTypeFormatter.dumpComplexType(NewComplexTypeFormatter.java:403)
     */
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append(groupNumberV.toString());
        sb.append(' ');
        sb.append(groupNameV.toString());
        if (noteV != null){
            sb.append(' ');
            sb.append("note=" + "\"" + noteV.toString() + "\"");
        }

        return(sb.toString());
    }

    public Integer getGroupNumber(){
        return(groupNumberV);
    }

    public String getGroupName(){
        return(groupNameV);
    }

    public String getNote(){
        return(noteV);
    }

}
