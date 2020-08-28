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

package com.connectifex.polymer.mdl.server.extended.plastic.util;

import java.util.ArrayList;

import org.dmd.util.exceptions.ResultException;

import com.connectifex.polymer.mdl.server.extended.plastic.PlasticPattern;
import com.connectifex.polymer.mdl.shared.generated.types.PlasticGroupAndName;



public class GroupLiteralParser {

	public static ArrayList<GroupOrLiteral> parse(PlasticPattern np, String expression) throws ResultException {
		ArrayList<GroupOrLiteral> rc = new ArrayList<>();
		
    		boolean inGroup = false;
    		StringBuilder currentPart = null;
    		 
    		for(int i=0; i<expression.length(); i++){
    			if (expression.charAt(i) == '%'){
    				if (inGroup){
    					PlasticGroupAndName gan = np.getGroupByName(currentPart.toString());
    					if (gan == null){
			    			ResultException ex = new ResultException("Unknown pattern group name: " + currentPart.toString());
			    			ex.moreMessages("In: normalizedForm " + expression);
			    			ex.setLocationInfo(np.getFile(), np.getLineNumber());
			    			throw(ex);
    					}
    					rc.add(new GroupOrLiteral(gan, null));
    					inGroup = false;
    					currentPart = null;
    				}
    				else{
    					if (currentPart != null)
    						rc.add(new GroupOrLiteral(null,currentPart.toString()));
    					
    					currentPart = new StringBuilder();
    					inGroup = true;
    				}
    			}
    			else{
    				if (inGroup){
	    				currentPart.append(expression.charAt(i));
    				}
    				else{
	    				if (currentPart == null)
	    					currentPart = new StringBuilder();
	    				currentPart.append(expression.charAt(i));
    				}
    			}
    		}
    		
    		if (inGroup){
    			ResultException ex = new ResultException("Missing % to close pattern group name insertion: " + expression);
    			ex.moreMessages("In: normalizedForm " + expression);
    			ex.setLocationInfo(np.getFile(), np.getLineNumber());
    			throw(ex);
    		}
    		
    		if (currentPart != null)
				rc.add(new GroupOrLiteral(null, currentPart.toString()));
		
		return(rc);
	}
}
