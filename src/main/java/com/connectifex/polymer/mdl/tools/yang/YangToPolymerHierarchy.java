// Copyright 2020 connectifex
// 
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
// 
//      http://www.apache.org/licenses/LICENSE-2.0
// 
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//

package com.connectifex.polymer.mdl.tools.yang;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import java.util.TreeMap;

import org.dmd.dmc.types.IntegerVar;
import org.dmd.dmu.util.json.PrettyJSON;
import org.dmd.util.exceptions.DebugInfo;
import org.dmd.util.formatting.PrintfFormat;

import com.connectifex.polymer.mdl.server.extended.plastic.util.PlasticGlobals;
import com.connectifex.polymer.mdl.shared.generated.enums.HierarchyTypeEnum;
import com.connectifex.polymer.mdl.tools.yang.util.ChoiceInfo;
import com.connectifex.polymer.mdl.tools.yang.util.Helper;
import com.connectifex.polymer.mdl.tools.yang.util.ModuleVersion;
import com.connectifex.polymer.mdl.tools.yang.util.VariableInfo;
import com.connectifex.polymer.mdl.tools.yang.util.YangAttribute;
import com.connectifex.polymer.mdl.tools.yang.util.YangConstants;
import com.connectifex.polymer.mdl.tools.yang.util.YangContext;
import com.connectifex.polymer.mdl.tools.yang.util.YangHierarchyNode;
import com.connectifex.polymer.mdl.tools.yang.util.YangStructure;

/**
 * The YangToPolymerHierarchy class will attempt to find top level containers or augment statements
 * and generate partial InputSchemas that have the input schema and variable definitions.
 * This new approach also allows for the creation of multiple input schemas distinguished
 * by their unique paths.
 */
public class YangToPolymerHierarchy {
	
//	private JSONObject						topLevelObject;
	private YangStructure					module;
	private TreeMap<String,VariableInfo>	variablesByName;
	private int longest;
	
	private TreeMap<String,	ChoiceInfo>		choicesByUniqueName;
	
	// Key: the FQN of a choice that may be referenced many times through groupings
	// Value: a counter of the number of times we've used used it
	private TreeMap<String, IntegerVar>		choiceUsageCount;
	
	private Stack<ChoiceInfo>				choiceStack;
	private Stack<String>					caseStack;
	
	// Used in cases where we need to generate a unique name because we don't
	// have a parent element. We increment this as required.
	private int								uniqueID;
	
	// We maintain the containers that exist in the overall hierarhcy so that
	// we can generate a set of individual input schemas, with their associated
	// paths.
	private ArrayList<YangHierarchyNode>	allContainers;
	
	public YangToPolymerHierarchy() {
		// TODO Auto-generated constructor stub
	}
	
	public void convert(YangContext context, YangStructure module, ModuleVersion version) {
		this.module		= module;
		this.uniqueID	= 1;
		
		System.out.println();
		
		boolean anyThingDumped = false;
		
		Iterator<YangStructure>	containers = module.childrenOfType(YangConstants.CONTAINER);
		while(containers.hasNext()) {
			variablesByName 	= new TreeMap<>();
			choicesByUniqueName = new TreeMap<>();
			choiceUsageCount	= new TreeMap<>();
			choiceStack 		= new Stack<>();
			caseStack 			= new Stack<>();
			allContainers		= new ArrayList<>();
			
			YangStructure container = containers.next();
			
			YangHierarchyNode	topLevelNode = new YangHierarchyNode(HierarchyTypeEnum.CONTAINER, null, container);
			allContainers.add(topLevelNode);
			
//			topLevelObject = new JSONObject();
//			descend(container, "", 1, topLevelObject);
			
//			PlasticGlobals.instance().trace(PrettyJSON.instance().prettyPrint(object, true));
						
			dumpInputSchema(module,version,topLevelNode);
			anyThingDumped = true;
			
			for(ChoiceInfo choice: choicesByUniqueName.values()) {
				System.out.println(choice.toYangChoice() + "\n");
			}
			
			System.out.println("// Containers\n");
			for (YangHierarchyNode node: allContainers) {
				System.out.println(node.getSchema(version));
			}
			System.out.println();

		}
		
		ArrayList<YangAttribute> attrs = module.attribute(YangConstants.USES);
		if (attrs != null) {
			for(YangAttribute ya: attrs) {
//				DebugInfo.debug("uses: " + ya.value());
	
				Iterator<YangStructure>	groupings = module.childrenOfType(YangConstants.GROUPING);
				while(groupings.hasNext()) {
					YangStructure grouping = groupings.next();
					if (grouping.name().equals(ya.value())) {
						variablesByName 	= new TreeMap<>();
						choicesByUniqueName = new TreeMap<>();
						choiceUsageCount	= new TreeMap<>();
						choiceStack 		= new Stack<>();
						caseStack 			= new Stack<>();
						allContainers		= new ArrayList<>();
						
//						topLevelObject = new JSONObject();
//						descend(grouping, "", 1, topLevelObject);
						
						YangHierarchyNode	topLevelNode = new YangHierarchyNode(HierarchyTypeEnum.GROUPING, null, grouping);
						descend(grouping, "", 1, topLevelNode);
						
	//					PlasticGlobals.instance().trace(PrettyJSON.instance().prettyPrint(object, true));
									
						dumpInputSchema(module,version,topLevelNode);
						anyThingDumped = true;
						
						for(ChoiceInfo choice: choicesByUniqueName.values()) {
							System.out.println(choice.toYangChoice() + "\n");
						}
						
						System.out.println("-- containers\n");
						for (YangHierarchyNode node: allContainers) {
							System.out.println(node.getSchema(version));
						}
						System.out.println();

					}
				}
	
			}
		}
		
		if (!anyThingDumped) {
			System.out.println("\nThe module you specified has no top level containers or uses statements, so nothing could be generated\n\n");
		}
		
	}
	
	private void dumpInputSchema(YangStructure module, ModuleVersion version, YangHierarchyNode topLevelNode) {
		System.out.println("NOTE: skipping export of top level structure...\n");
//		longest += 4;
//		PrintfFormat format = new PrintfFormat("%-" + longest + "s");
//		StringBuilder sb = new StringBuilder();
//		sb.append("InputSchema\n");
//		sb.append("name            " + module.name() + "\n");
//		for(VariableInfo var: variablesByName.values()) {
//			sb.append(var.toVariableFormat(format) + "\n");
//		}
//		sb.append("inputSchema " + PrettyJSON.instance().prettyPrint(topLevelNode.object(), true, "  ") + "\n");
//		sb.append("description Generated from: " + version.nameAndRevision() + " - variables: " + variablesByName.size());
//		
//		System.out.println(sb.toString());
		

	}
	
//	public void descend(YangStructure node, String indent, int depth, JSONObject object) {
	public void descend(YangStructure node, String indent, int depth, YangHierarchyNode parentNode) {
		PlasticGlobals.instance().trace(indent + node.type() + "  " + node.name() + "  depth: " + depth + " children: " + node.childrenSize());
		
		if (node.type().equals(YangConstants.CONTAINER)) {
//			JSONObject containerObj = new JSONObject();
//			object.put(node.name(), containerObj);
			
			YangHierarchyNode hierarchyNode = new YangHierarchyNode(HierarchyTypeEnum.CONTAINER, parentNode, node);
			allContainers.add(hierarchyNode);
			
			ArrayList<YangAttribute> uses = node.attribute(YangConstants.USES);
			if (uses != null) {
				PlasticGlobals.instance().trace(indent + "Uses entries: " + uses.size());
				for(YangAttribute attr: uses) {
					PlasticGlobals.instance().trace(indent + "Uses: " + attr.value());	
					
					YangStructure grouping = Helper.getGrouping(node.moduleWhereYouAreDefined(), node, attr.value());
					
					if (grouping == null) {
						throw(new IllegalStateException("Could not find grouping: " + attr.value()));
					}
					else {
						descend(grouping, indent + "    ", depth+1, hierarchyNode);
					}
				}
			}

			Iterator<YangStructure> children = node.children();
			while(children.hasNext()) {
				YangStructure child = children.next();
				descend(child, indent + "    ", depth+1, hierarchyNode);
			}
			
		}
		else if (node.type().equals(YangConstants.GROUPING)){
			ArrayList<YangAttribute> uses = node.attribute(YangConstants.USES);
			if (uses != null) {
				PlasticGlobals.instance().trace(indent + "Uses entries: " + uses.size());
				for(YangAttribute attr: uses) {
					PlasticGlobals.instance().trace(indent + "Uses: " + attr.value() + "\n");	
					
					YangStructure grouping = Helper.getGrouping(node.moduleWhereYouAreDefined(), node, attr.value());
					
					if (grouping == null) {
						throw(new IllegalStateException("Could not find grouping: " + attr.value()));
					}
					else {
						descend(grouping, indent + "    ", depth+1, parentNode);
					}
				}
			}
			
			Iterator<YangStructure> children = node.children();
			while(children.hasNext()) {
				YangStructure child = children.next();
				descend(child, indent + "    ", depth+1, parentNode);
			}
		}
		else if (node.type().equals(YangConstants.LIST)) {
//			JSONArray array = new JSONArray();
//			object.put(node.name(), array);
			
			ArrayList<YangAttribute> uses = node.attribute(YangConstants.USES);
			
			YangHierarchyNode objectListNode = null;
			if ( (uses != null) || (node.childrenSize() > 1)) {
				PlasticGlobals.instance().trace(indent + "OBJECT list");
				objectListNode = new YangHierarchyNode(HierarchyTypeEnum.OBJECT_LIST, parentNode, node);
			}
			
			if (uses != null) {
//				YangHierarchyNode usesNode = new YangHierarchyNode(HierarchyTypeEnum.USES_LIST, parentNode, node);
				
				PlasticGlobals.instance().trace(indent + "Uses entries: " + uses.size());
				for(YangAttribute attr: uses) {
					PlasticGlobals.instance().trace(indent + "Uses: " + attr.value() + "\n");		
					
					YangStructure grouping = Helper.getGrouping(node.moduleWhereYouAreDefined(), node, attr.value());
					
					if (grouping == null) {
						throw(new IllegalStateException("Could not find grouping: " + attr.value()));
					}
					else {
//						descend(grouping, indent + "    ", depth+1, parentNode);
						descend(grouping, indent + "    ", depth+1, objectListNode);
					}
				}
			}

			if (node.childrenSize() == 1) {				
				PlasticGlobals.instance().trace(indent + "leaf list");
				YangHierarchyNode leafListNode = new YangHierarchyNode(HierarchyTypeEnum.LEAF_LIST, parentNode, node);
				
				// Some trickery here, we actually use the child as the basis for
				// the variable.
				VariableInfo info = addVariable(node.getChild(0), indent);
				
//				array.put(info.variableInsert());
				
				leafListNode.addVariable(info);
			}
			else {
				PlasticGlobals.instance().trace(indent + "OBJECT list");
//				JSONObject newobj = new JSONObject();
//				array.put(newobj);

//				YangHierarchyNode objectListNode = new YangHierarchyNode(HierarchyTypeEnum.OBJECT_LIST, parentNode, node);

				
				Iterator<YangStructure> children = node.children();
				while(children.hasNext()) {
					YangStructure child = children.next();
					descend(child, indent + "    ", depth+1, objectListNode);
				}

			}
		}
		else if (node.type().equals(YangConstants.LEAF)) {
			PlasticGlobals.instance().trace(indent + "leaf");
			
			VariableInfo info = addVariable(node, indent);

//			object.put(node.name(), info.variableInsert());
			
			parentNode.addVariable(info);
		}
		else if (node.type().equals(YangConstants.LEAF_LIST)) {
//			JSONArray array = new JSONArray();
//			object.put(node.name(), array);
			
			YangHierarchyNode leafListNode = new YangHierarchyNode(HierarchyTypeEnum.LEAF_LIST, parentNode, node);

			PlasticGlobals.instance().trace(indent + "leaf-list");
			
			VariableInfo info = addVariable(node, indent);

//			array.put(info.variableInsert());
			
			leafListNode.addVariable(info);
			
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
//				descend(child, indent + "    ", depth+1, object);
				descend(child, indent + "    ", depth+1, parentNode);
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
	private VariableInfo addVariable(YangStructure yang, String indent) {
		PlasticGlobals.instance().trace(indent + "addVariable: " + yang.getFullyQualifiedName());
		
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
