package com.connectifex.polymer.mdl.shared.types;

import java.io.Serializable;

import org.dmd.dmc.DmcAttribute;
import org.dmd.dmc.DmcAttributeInfo;
import org.dmd.dmc.DmcInputStreamIF;
import org.dmd.dmc.DmcOutputStreamIF;
import org.dmd.dmc.DmcValueException;

@SuppressWarnings("serial")
abstract public class DmcTypeTemplateString extends DmcAttribute<TemplateString> implements Serializable {

	public DmcTypeTemplateString() {
		
	}
	public DmcTypeTemplateString(DmcAttributeInfo ai) {
		super(ai);
	}

	@Override
	public TemplateString typeCheck(Object value) throws DmcValueException {
		TemplateString rc = null;
		
		if (value instanceof TemplateString)
			rc = (TemplateString) value;
		else if (value instanceof String)
			rc = new TemplateString((String) value);
		else
            throw(new DmcValueException("Object of class: " + value.getClass().getName() + " passed where object compatible with TemplateString or String expected."));

		return(rc);
	}

	@Override
	public TemplateString cloneValue(TemplateString original) {
		return(new TemplateString(original));
	}

    ////////////////////////////////////////////////////////////////////////////////
    // Serialization

	@Override
	public void serializeValue(DmcOutputStreamIF dos, TemplateString value) throws Exception {
		value.serializeIt(dos);
	}

	@Override
	public TemplateString deserializeValue(DmcInputStreamIF dis) throws Exception {
		TemplateString rc = new TemplateString();
		rc.deserializeIt(dis);
		return(rc);
	}

}
