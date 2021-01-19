package com.connectifex.polymer.mdl.shared.types;

import static org.junit.Assert.assertEquals;

import org.dmd.dmc.DmcValueException;
import org.dmd.util.exceptions.ResultException;
import org.junit.Test;

import com.connectifex.polymer.mdl.server.extended.util.ParameterSet;

public class TemplateStringTest {

	@Test
	public void test() throws DmcValueException, ResultException {
		TemplateString ts = new TemplateString("show interface " + TemplateString.START_MARKER + "interfaceName" + TemplateString.END_MARKER + " | display json");
		ts.initialize(null);
		
		assertEquals("Should be one parameter", 1, ts.getParameterCount());
		
		System.out.println(ts.getParameterNames());		
		
	}

	@Test
	public void test2() throws DmcValueException, ResultException {
		TemplateString ts = new TemplateString("show interface " + TemplateString.START_MARKER + "interfaceName" + TemplateString.END_MARKER+ " | display " + TemplateString.START_MARKER + "displayType" + TemplateString.END_MARKER);
		ts.initialize(null);
		
		assertEquals("Should be one parameter", 2, ts.getParameterCount());
		
		System.out.println(ts.getParameterNames());
		
		
	}
}
