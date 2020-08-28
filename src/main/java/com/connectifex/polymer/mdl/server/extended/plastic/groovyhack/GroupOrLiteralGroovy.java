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

import java.util.HashMap;

/**
 * The GroupOrLiteralGroovy class is the groovisized version of the GroupOrLiteral class.
 * It makes use of the PlasticGroupAndNameGroovy class.
 * 
 * We hand copy this code into our src/main/resources/plastic-library and drop the package indication.
 */
public class GroupOrLiteralGroovy {
	private PlasticGroupAndNameGroovy group;
	private String literal;
	
	public GroupOrLiteralGroovy(PlasticGroupAndNameGroovy group, String literal) {
		this.group = group;
		this.literal = literal;
	}
	
	/**
	 * @param map the values of the tuples from a matched pattern
	 * @return the literal if this is a literal or the value 
	 */
	public String toString(HashMap<String,String> map){
		if (group == null)
			return(literal);
		
		return(map.get(group.getGroupName()));
	}
}
