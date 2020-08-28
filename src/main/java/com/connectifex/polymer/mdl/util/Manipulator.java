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

package com.connectifex.polymer.mdl.util;

public class Manipulator {

	/**
	 * We can't just blindly use strings as Groovy variable names, they have to be sanitized and
	 * have additional bits added to them to prevent compile/run errors. A prime example was having
	 * a group name called "def" - when used as is, this was interpreted as a Groovy keyword.
	 * @param value the value to be sanitized.
	 * @return a valid Groovy variable name
	 */
	static public String convertToGroovyVariableName(String value) {
		String rc = value.replace("-", "_");
		rc = "__" + rc;
		return(rc);
	}

	/**
	 * @param value the string to be fixed
	 * @return the string with dashes replaced with underscores
	 */
	static public String fix(String value) {
		return(value.replace("-", "_"));
	}

	/**
	 * @param value the string for which the first letter should be capitalized.
	 * @return take a string like hello and return Hello
	 */
	static public String capFirstChar(String value){
		if (value == null)
			return(null);
    	StringBuffer 	rc 	= new StringBuffer(value);
    	rc.setCharAt(0,Character.toUpperCase(rc.charAt(0)));
    	return(rc.toString());
	}

}
