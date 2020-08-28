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

import java.util.ArrayList;

/**
 * The GroupLiteralParserGroovy class is the groovisized version of the GroupLiteralParser that
 * can be used to support pattern matching functionality in our morphers. All sanity checking
 * has been stripped because we wouldn't get to using this if problems had been caught in
 * full Java versions of the classes.
 * 
 * We hand copy this code into our src/main/resources/plastic-library and drop the package indication.
 */
public class GroupLiteralParserGroovy {

	public static ArrayList<GroupOrLiteralGroovy> parse(PlasticPatternGroovy np, String expression) {
		ArrayList<GroupOrLiteralGroovy> rc = new ArrayList<>();
		
    		boolean inGroup = false;
    		StringBuilder currentPart = null;
    		 
    		for(int i=0; i<expression.length(); i++){
    			if (expression.charAt(i) == '%'){
    				if (inGroup){
    					PlasticGroupAndNameGroovy gan = np.getGroupByName(currentPart.toString());
    					rc.add(new GroupOrLiteralGroovy(gan, null));
    					inGroup = false;
    					currentPart = null;
    				}
    				else{
    					if (currentPart != null)
    						rc.add(new GroupOrLiteralGroovy(null,currentPart.toString()));
    					
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
    		    		
    		if (currentPart != null)
				rc.add(new GroupOrLiteralGroovy(null, currentPart.toString()));
		
		return(rc);
	}
}
