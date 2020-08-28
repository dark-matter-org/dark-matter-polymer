package com.connectifex.polymer.tools;

import java.io.IOException;

import org.dmd.dmc.DmcNameClashException;
import org.dmd.dmc.DmcValueException;
import org.dmd.dmc.DmcValueExceptionSet;
import org.dmd.dmc.rules.DmcRuleExceptionSet;
import org.dmd.util.exceptions.ResultException;

public class PolymerMain {

	public static void main(String[] args) {
		try {
			PolymerSelector selector = new PolymerSelector(args);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DmcValueException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ResultException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DmcRuleExceptionSet e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DmcNameClashException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DmcValueExceptionSet e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
