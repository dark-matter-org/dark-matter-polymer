package com.connectifex.polymer.mdl.shared.types;

import java.io.Serializable;

import org.dmd.dmc.DmcAttribute;
import org.dmd.dmc.DmcAttributeInfo;
import org.dmd.dmc.DmcInputStreamIF;
import org.dmd.dmc.DmcOutputStreamIF;
import org.dmd.dmc.DmcValueException;

@SuppressWarnings("serial")
abstract public class DmcTypeJSONTemplateString extends DmcAttribute<JSONTemplateString> implements Serializable {

	public DmcTypeJSONTemplateString() {
		
	}
	public DmcTypeJSONTemplateString(DmcAttributeInfo ai) {
		super(ai);
	}

	@Override
	public JSONTemplateString typeCheck(Object value) throws DmcValueException {
		JSONTemplateString rc = null;
		
		if (value instanceof JSONTemplateString)
			rc = (JSONTemplateString) value;
		else if (value instanceof String)
			rc = new JSONTemplateString((String) value);
		else
            throw(new DmcValueException("Object of class: " + value.getClass().getName() + " passed where object compatible with TemplateString or String expected."));

		return(rc);
	}

	@Override
	public JSONTemplateString cloneValue(JSONTemplateString original) {
		return(new JSONTemplateString(original));
	}

    ////////////////////////////////////////////////////////////////////////////////
    // Serialization

	@Override
	public void serializeValue(DmcOutputStreamIF dos, JSONTemplateString value) throws Exception {
		value.serializeIt(dos);
	}

	@Override
	public JSONTemplateString deserializeValue(DmcInputStreamIF dis) throws Exception {
		JSONTemplateString rc = new JSONTemplateString();
		rc.deserializeIt(dis);
		return(rc);
	}

}
