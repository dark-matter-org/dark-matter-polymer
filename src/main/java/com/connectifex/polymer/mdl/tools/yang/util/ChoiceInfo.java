package com.connectifex.polymer.mdl.tools.yang.util;

import java.util.TreeMap;

public class ChoiceInfo {

	private YangStructure	yang;
	private TreeMap<String,TreeMap<String,VariableInfo>>	casesWithVariables;
	private String uniqueName;
	
	public ChoiceInfo(YangStructure yang, int instance) {
		this.yang = yang;
		casesWithVariables = new TreeMap<>();
		
		// We default the unique name
		if (instance == 1)
			uniqueName = yang.name();
		else
			uniqueName = yang.name() + instance;
	}
	
	public String fqn() {
		return(yang.getFullyQualifiedName());
	}
	
	public String name() {
		return(yang.name());
	}
	
//	/**
//	 * Once we've processed the Yang, we cycle over all choices and have them
//	 * create their own unique name if required.
//	 * @param uniqueName
//	 */
//	public void createUniqueName() {
//		uniqueName = yang.parent().name() + "--" + yang.name();
//	}
	
	public String uniqueName() {
		return(uniqueName);
	}
	
	public void addVariable(String caseName, VariableInfo variable) {
		TreeMap<String,VariableInfo> existing = casesWithVariables.get(caseName);
		if (existing == null) {
			existing = new TreeMap<>();
			casesWithVariables.put(caseName,existing);
		}
		existing.put(variable.name(), variable);
	}
	
	public String toYangChoice() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("YangChoice\n");
		sb.append("name  " + uniqueName + "\n");
		
		for(String caseName: casesWithVariables.keySet()) {
			TreeMap<String,VariableInfo> variables = casesWithVariables.get(caseName);
			sb.append("cases " + caseName);
			for(VariableInfo info: variables.values()) {
				sb.append("\n      var=" + info.name());
			}
			sb.append("\n");
		}
		
		YangAttribute descr = yang.singleAttribute(YangConstants.DESCRIPTION);
		
		if (descr == null)
			sb.append("description Extracted from " + yang.getModule().name() + ":" + yang.lineNumber() + "\n");
		else {
			sb.append("description " + descr.value() + "\n");
			sb.append("            Extracted from " + yang.getModule().name() + ":" + yang.lineNumber() + "\n");
		}
		
		if (casesWithVariables.size() == 1) {
			sb.append("            NOTE: since there is only 1 case, we don't actually refer to this from a variable\n");
		}

		return(sb.toString());
	}
	
	/**
	 * This is checked when we dump the variable info. 
	 * @return If there's more than one case in the choice, we return true, otherwise, false.
	 */
	public boolean moreThanOneCase() {
		if (casesWithVariables.size() > 1)
			return(true);
		return(false);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(yang.getFullyQualifiedName() + "\n");
		sb.append("Choice: " + yang.name() + "\n");
		
		for(String caseName: casesWithVariables.keySet()) {
			TreeMap<String,VariableInfo> existing = casesWithVariables.get(caseName);
			sb.append("  Case: " + caseName + "\n");
			
			for(VariableInfo var: existing.values()) {
				sb.append("        " + var.name() + "\n");
			}
		}
		
		return(sb.toString());
	}
}
