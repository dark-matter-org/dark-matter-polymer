package com.connectifex.polymer.mdl.server.extended.plastic.util;

public class PlasticGlobals {
	
	private static PlasticGlobals instance;
	
	private boolean trace;

	private PlasticGlobals() {
		// TODO Auto-generated constructor stub
	}
	
	public static PlasticGlobals instance() {
		if (instance == null)
			instance=  new PlasticGlobals();
		return(instance);
	}
	
	public void trace(boolean flag) {
		trace = flag;
	}
	
	public boolean trace() {
		return(trace);
	}
	
	public void summary(String message) {
		if (!trace)
			System.out.println(message);
	}
	
	public void trace(String message) {
		if (trace)
			System.out.println(message);
	}
}
