package com.connectifex.polymer.mdl.shared.types;

import java.io.Serializable;

import org.dmd.dmc.DmcAttribute;
import org.dmd.dmc.DmcAttributeInfo;
import org.dmd.dmc.DmcInputStreamIF;
import org.dmd.dmc.DmcOutputStreamIF;
import org.dmd.dmc.DmcValueException;

@SuppressWarnings("serial")
abstract public class DmcTypePlasticSchema extends DmcAttribute<PlasticSchema> implements Serializable {

	public DmcTypePlasticSchema() {
		
	}
	public DmcTypePlasticSchema(DmcAttributeInfo ai) {
		super(ai);
	}

	@Override
	public PlasticSchema typeCheck(Object value) throws DmcValueException {
		PlasticSchema rc = null;
		
		if (value instanceof PlasticSchema)
			rc = (PlasticSchema) value;
		else if (value instanceof String)
			rc = new PlasticSchema((String) value);
		else
            throw(new DmcValueException("Object of class: " + value.getClass().getName() + " passed where object compatible with PlasticSchema or String expected."));

		return(rc);
	}

	@Override
	public PlasticSchema cloneValue(PlasticSchema original) {
		return(new PlasticSchema(original));
	}

    ////////////////////////////////////////////////////////////////////////////////
    // Serialization

	@Override
	public void serializeValue(DmcOutputStreamIF dos, PlasticSchema value) throws Exception {
		value.serializeIt(dos);
	}

	@Override
	public PlasticSchema deserializeValue(DmcInputStreamIF dis) throws Exception {
		PlasticSchema rc = new PlasticSchema();
		rc.deserializeIt(dis);
		return(rc);
	}

}
