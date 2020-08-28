package com.connectifex.polymer.mdl.tools.yang.util;

public class YangAttribute {
	
	private String name;
	private String value;
	
	public YangAttribute(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public String toString() {
		return(name + " = " + value);
	}
	
	public String name() {
		return(name);
	}
	
	public String value() {
		return(value);
	}
}
