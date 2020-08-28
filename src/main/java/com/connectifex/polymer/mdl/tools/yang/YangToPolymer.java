package com.connectifex.polymer.mdl.tools.yang;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import java.util.TreeMap;

import org.dmd.dmc.types.IntegerVar;
import org.dmd.dmu.util.json.PrettyJSON;
import org.dmd.util.formatting.PrintfFormat;
import org.json.JSONArray;
import org.json.JSONObject;

import com.connectifex.polymer.mdl.server.extended.plastic.util.PlasticGlobals;
import com.connectifex.polymer.mdl.tools.yang.util.ChoiceInfo;
import com.connectifex.polymer.mdl.tools.yang.util.Helper;
import com.connectifex.polymer.mdl.tools.yang.util.ModuleVersion;
import com.connectifex.polymer.mdl.tools.yang.util.VariableInfo;
import com.connectifex.polymer.mdl.tools.yang.util.YangAttribute;
import com.connectifex.polymer.mdl.tools.yang.util.YangConstants;
import com.connectifex.polymer.mdl.tools.yang.util.YangContext;
import com.connectifex.polymer.mdl.tools.yang.util.YangStructure;

/**
 * The YangToPolymer class will attempt to find top level containers or augment statements
 * and generate partial InputSchemas that have the input schema and variable definitions.
 */
public class YangToPolymer {
	
	private JSONObject						object;
	private YangStructure					module;
	private TreeMap<String,VariableInfo>	variablesByName;
	private int longest;
	
	private TreeMap<String,	ChoiceInfo>		choicesByUniqueName;
	
	// Key: the FQN of a choice that may be referenced many times through groupings
	// Value: a counter of the number of times we've used used it
	private TreeMap<String, IntegerVar>		choiceUsageCount;
	
	private Stack<ChoiceInfo>				choiceStack;
	private Stack<String>					caseStack;
	
	public YangToPolymer() {
		// TODO Auto-generated constructor stub
	}
	
	public void convert(YangContext context, YangStructure module, ModuleVersion version) {
		this.module		= module;
		System.out.println();
		
		Iterator<YangStructure>	containers = module.childrenOfType(YangConstants.CONTAINER);
		while(containers.hasNext()) {
			variablesByName 	= new TreeMap<>();
			choicesByUniqueName 		= new TreeMap<>();
			choiceUsageCount	= new TreeMap<>();
			choiceStack 		= new Stack<>();
			caseStack 			= new Stack<>();
			
			YangStructure container = containers.next();
			
			object = new JSONObject();
			descend(container, "", 1, object);
			
//			PlasticGlobals.instance().trace(PrettyJSON.instance().prettyPrint(object, true));
						
			dumpInputSchema(module,version);
			
			for(ChoiceInfo choice: choicesByUniqueName.values()) {
				System.out.println(choice.toYangChoice() + "\n");
			}
		}
	}
	
	private void dumpInputSchema(YangStructure module, ModuleVersion version) {
		longest += 4;
		PrintfFormat format = new PrintfFormat("%-" + longest + "s");
		StringBuilder sb = new StringBuilder();
		sb.append("InputSchema\n");
		sb.append("name            " + module.name() + "\n");
		for(VariableInfo var: variablesByName.values()) {
			sb.append(var.toVariableFormat(format) + "\n");
		}
		sb.append("inputSchema " + PrettyJSON.instance().prettyPrint(object, true, "  ") + "\n");
		sb.append("description Generated from: " + version.nameAndRevision() + " - variables: " + variablesByName.size());
		
		System.out.println(sb.toString());
	}
	
	public void descend(YangStructure node, String indent, int depth, JSONObject object) {
		PlasticGlobals.instance().trace(indent + node.type() + "  " + node.name() + "  depth: " + depth + " children: " + node.childrenSize());
		
		if (node.type().equals(YangConstants.CONTAINER)) {
			JSONObject containerObj = new JSONObject();
			object.put(node.name(), containerObj);
			
			ArrayList<YangAttribute> uses = node.attribute(YangConstants.USES);
			if (uses != null) {
				PlasticGlobals.instance().trace(indent + "Uses entries: " + uses.size());
				for(YangAttribute attr: uses) {
					PlasticGlobals.instance().trace(indent + "Uses: " + attr.value());							
					YangStructure grouping = Helper.getGrouping(module, node, attr.value());
					
					if (grouping == null) {
						throw(new IllegalStateException("Could not find grouping: " + attr.value()));
					}
					else {
						descend(grouping, indent + "    ", depth+1, containerObj);
					}
				}
			}

			Iterator<YangStructure> children = node.children();
			while(children.hasNext()) {
				YangStructure child = children.next();
				descend(child, indent + "    ", depth+1, containerObj);
			}
			
		}
		else if (node.type().equals(YangConstants.GROUPING)){
			ArrayList<YangAttribute> uses = node.attribute(YangConstants.USES);
			if (uses != null) {
				PlasticGlobals.instance().trace(indent + "Uses entries: " + uses.size());
				for(YangAttribute attr: uses) {
					PlasticGlobals.instance().trace(indent + "Uses: " + attr.value() + "\n");							
					YangStructure grouping = Helper.getGrouping(module, node, attr.value());
					
					if (grouping == null) {
						throw(new IllegalStateException("Could not find grouping: " + attr.value()));
					}
					else {
						descend(grouping, indent + "    ", depth+1, object);
					}
				}
			}
			
			Iterator<YangStructure> children = node.children();
			while(children.hasNext()) {
				YangStructure child = children.next();
				descend(child, indent + "    ", depth+1, object);
			}
		}
		else if (node.type().equals(YangConstants.LIST)) {
			JSONArray array = new JSONArray();
			object.put(node.name(), array);
			
			ArrayList<YangAttribute> uses = node.attribute(YangConstants.USES);
			if (uses != null) {
				PlasticGlobals.instance().trace(indent + "Uses entries: " + uses.size());
				for(YangAttribute attr: uses) {
					PlasticGlobals.instance().trace(indent + "Uses: " + attr.value() + "\n");							
					YangStructure grouping = Helper.getGrouping(module, node, attr.value());
					
					if (grouping == null) {
						throw(new IllegalStateException("Could not find grouping: " + attr.value()));
					}
					else {
						descend(grouping, indent + "    ", depth+1, object);
					}
				}
			}

			if (node.childrenSize() == 1) {
				PlasticGlobals.instance().trace(indent + "leaf list");
				
				// Some trickery here, we actually use the child as the basis for
				// the variable.
				VariableInfo info = addVariable(node.getChild(0));
				
				array.put(info.variableInsert());
			}
			else {
				PlasticGlobals.instance().trace(indent + "OBJECT list");
				JSONObject newobj = new JSONObject();
				array.put(newobj);
				
				Iterator<YangStructure> children = node.children();
				while(children.hasNext()) {
					YangStructure child = children.next();
					descend(child, indent + "    ", depth+1, newobj);
				}

			}
		}
		else if (node.type().equals(YangConstants.LEAF)) {
			PlasticGlobals.instance().trace(indent + "leaf");
			
			VariableInfo info = addVariable(node);

			object.put(node.name(), info.variableInsert());
		}
		else if (node.type().equals(YangConstants.LEAF_LIST)) {
			JSONArray array = new JSONArray();
			object.put(node.name(), array);

			PlasticGlobals.instance().trace(indent + "leaf-list");
			
			VariableInfo info = addVariable(node);

			array.put(info.variableInsert());
		}
		else {
			// NOTE: very complicated handling for CHOICE/CASE statements
			// We have to identify the variables somehow so that we know that only one arrangement of
			// of several possible structures will exist in the JSON.
			// We do this by maintaining the ChoiceInfo and associating it with the created variable
			//
			// This gets even more complicated because CHOICEs can be nested!
			if (node.type().equals(YangConstants.CHOICE)) {
				// As if all of this wasn't bad enough, we have choices with duplicate FQNs because they're
				// embedded in groupings. So, we have to prevent stomping on existing choices. We have to
				// keep the choices separate because the variables, if they overlap, have to be given 
				// unique names too.
				// We handle this by maintaining a counter for clashing choices and appending the
				// counter each time find the same choice used in different places
				IntegerVar choiceInstance = choiceUsageCount.get(node.getFullyQualifiedName());
				if (choiceInstance == null) {
					choiceInstance = new IntegerVar(1);
					choiceUsageCount.put(node.getFullyQualifiedName(), choiceInstance);
				}
				else {
					choiceInstance.set(choiceInstance.intValue() + 1); 
				}
				
				ChoiceInfo info = new ChoiceInfo(node,choiceInstance.intValue());
				choicesByUniqueName.put(info.uniqueName(), info);
				
				choiceStack.push(info);
			}
			else if (node.type().equals(YangConstants.CASE)) {
				caseStack.push(node.name());
			}
			
			PlasticGlobals.instance().trace(indent + "GENERIC DESCENT");
			Iterator<YangStructure> children = node.children();
			while(children.hasNext()) {
				YangStructure child = children.next();
				descend(child, indent + "    ", depth+1, object);
			}
			
			if (node.type().equals(YangConstants.CHOICE)) {
				choiceStack.pop();
			}
			else if (node.type().equals(YangConstants.CASE)) {
				caseStack.pop();
			}

		}
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
	private VariableInfo addVariable(YangStructure yang) {
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
					PlasticGlobals.instance().trace("Could not create unique name based on hierarchy!");
					PlasticGlobals.instance().trace("For YangStructure: " + yang.name() + " fqn: " + yang.getFullyQualifiedName());
					System.exit(1);
				}
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
		
		if (existing.name().length() > longest)
			longest = existing.name().length();
		
		if (choice != null) {
			choice.addVariable(caseStack.peek(), existing);
			existing.setChoice(choice);
		}
		
		return(existing);
	}
		
}
