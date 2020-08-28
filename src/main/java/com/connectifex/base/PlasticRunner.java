package com.connectifex.base;

public class PlasticRunner {
	
	private static PlasticRunner instance;

	private PlasticRunner() {
		// TODO Auto-generated constructor stub
	}
	
	public static PlasticRunner instance() {
		if (instance == null)
			instance = new PlasticRunner();
		return(instance);
	}
	
	
}
