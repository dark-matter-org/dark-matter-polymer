package com.connectifex.polymer.mdl.shared.types;

import org.dmd.dmc.DmcValueException;

public class JSONTemplateString extends TemplateString {

	public JSONTemplateString() {
		super();
	}
	
	public JSONTemplateString(String format) throws DmcValueException {
		super(format);
	}
	
	public JSONTemplateString(JSONTemplateString original) {
		format = original.format;
	}
	
}
