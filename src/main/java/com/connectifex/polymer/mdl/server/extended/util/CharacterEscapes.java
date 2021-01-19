package com.connectifex.polymer.mdl.server.extended.util;

import java.util.HashMap;

/**
 * The CharacterEscapes class allows for the definition of characters and a sequence with
 * which there are replaced in certain contexts e.g. escaping slashes in parameters inserted
 * in URI strings. This is defined internally for now, but, in the future, it might be useful
 * to allow for external definitions.
 * @author peter
 *
 */
public class CharacterEscapes {
	
	HashMap<String,String>	escapes;

	public CharacterEscapes() {
		escapes = new HashMap<>();
	}
	
	public void addEscape(char character, String escapeSequence) {
		if (escapes.get(""+character) != null) {
			throw(new IllegalStateException("Duplicate character added: " + character));
		}
		
		escapes.put(""+character, escapeSequence);
	}
	
	/**
	 * Will return the modified String for any of the characters that require escapes.
	 * @param input
	 * @return the modified String or the original input.
	 */
	public String escapeIfNecessary(String input) {
		String rc = input;
		for(String character: escapes.keySet()) {
			if (input.contains(character)) {
				rc = rc.replace(character, escapes.get(character));
			}
		}
		return(rc);
	}
}
