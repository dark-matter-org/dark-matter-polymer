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
