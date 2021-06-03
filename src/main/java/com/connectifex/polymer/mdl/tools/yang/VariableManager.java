package com.connectifex.polymer.mdl.tools.yang;

import java.util.Stack;
import java.util.TreeMap;

import org.dmd.util.formatting.PrintfFormat;

import com.connectifex.polymer.mdl.server.extended.plastic.util.PlasticGlobals;
import com.connectifex.polymer.mdl.tools.yang.util.ChoiceInfo;
import com.connectifex.polymer.mdl.tools.yang.util.VariableInfo;
import com.connectifex.polymer.mdl.tools.yang.util.YangStructure;

/**
 * The VariableManager manages a set of Polymer variables and ensures that they unique
 * names depending on where they exist in a YangStructure hierarchy.
 */
public class VariableManager {

	private TreeMap<String,VariableInfo>	variablesByName;
	private int longest;

	// These are injected 
	private Stack<ChoiceInfo>				choiceStack;
	private Stack<String>					caseStack;

	// Used in cases where we need to generate a unique name because we don't
	// have a parent element. We increment this as required.
	private int								uniqueID;

	public VariableManager(Stack<ChoiceInfo> choiceStack, Stack<String> caseStack) {
		this.variablesByName = new TreeMap<>();
		
		this.choiceStack	= choiceStack;
		this.caseStack		= caseStack;
	}
	
	/**
	 * The name of the variable has to be unique, so we maintain a set of names associated
	 * with the JSON schema we're building up. We also take care of indicating that the variable is embedded
	 * within an array and putting [] at the end of its name.
	 * If a variable is duplicated, we proceed up the hierarchy appending previous parent names until 
	 * we get something unique.
	 * @param yang
	 * @return
	 */
	public VariableInfo addVariable(YangStructure yang) {
		PlasticGlobals.instance().trace("addVariable: " + yang.getFullyQualifiedName());
		
		String name = yang.name();
		if (yang.isInList())
			name = name + "[]";
		
		ChoiceInfo choice = null;
		if (choiceStack.size() > 0) {
			PlasticGlobals.instance().trace("CHOICE " + choiceStack.peek().name());
			choice = choiceStack.peek();
		}
		if (caseStack.size() > 0) {
			PlasticGlobals.instance().trace("CASE " + caseStack.peek());
		}

		VariableInfo existing = variablesByName.get(name);

		if (existing == null) {
			existing = new VariableInfo(name, yang);
			variablesByName.put(existing.name(), existing);
		}
		else {
			// We already have a variable with this name, so recurse up the tree, prepending parent
			// names until we get something unique
			YangStructure parent = yang.parent();
			while(true) {
				if (parent == null) {
//					PlasticGlobals.instance().trace("Could not create unique name based on hierarchy!");
//					PlasticGlobals.instance().trace("For YangStructure: " + yang.name() + " fqn: " + yang.getFullyQualifiedName());
//					System.exit(1);
					
					name = name + "--" + uniqueIdSuffix();
					
					existing = variablesByName.get(name);
					if (existing == null) {
						existing = new VariableInfo(name, yang);
						variablesByName.put(existing.name(), existing);
						break;
					}

					PlasticGlobals.instance().trace("Could not create unique name based on hierarchy!");
					PlasticGlobals.instance().trace("For YangStructure: " + yang.name() + " fqn: " + yang.getFullyQualifiedName());
					System.exit(1);

				}
				else {
					name = parent.name() + "--" + name;
					
					existing = variablesByName.get(name);
					if (existing == null) {
						existing = new VariableInfo(name, yang);
						variablesByName.put(existing.name(), existing);
						break;
					}
					parent = parent.parent();
				}
			}
		}
		
		if (existing.name().length() > longest)
			longest = existing.name().length();
		
		if (choice != null) {
			choice.addVariable(caseStack.peek(), existing);
			existing.setChoice(choice);
		}
		
		return(existing);
	}

	
	public void getVariables(StringBuilder sb) {
		longest += 4;
		PrintfFormat format = new PrintfFormat("%-" + longest + "s");

		for(VariableInfo var: variablesByName.values()) {
			sb.append(var.toVariableFormat(format) + "\n");
		}
		
	}
	
	private String uniqueIdSuffix() {
		String rc = "";
		if (uniqueID < 10)
			rc = "00" + uniqueID;
		else if (uniqueID < 100)
			rc = "0" + uniqueID;
		else
			rc = "" + uniqueID;
		
		uniqueID++;
		
		return(rc);
	}

}
