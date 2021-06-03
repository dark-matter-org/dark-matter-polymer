package com.connectifex.polymer.mdl.tools.yang.util;

import java.util.ArrayList;
import java.util.TreeMap;

import org.dmd.dmc.types.IntegerVar;
import org.dmd.dmu.util.json.PrettyJSON;
import org.dmd.util.formatting.PrintfFormat;
import org.json.JSONArray;
import org.json.JSONObject;

import com.connectifex.polymer.mdl.shared.generated.enums.HierarchyTypeEnum;

/**
 * The YangHierarchyNode class represents a node in the overall Yang hierarchy.
 * This allows us to create paths to containers so that we can generate input
 * schemas and their associated path.
 */
public class YangHierarchyNode {
	
	private HierarchyTypeEnum				type;
	
	// This container's parent, or null if this is a top level container
	private YangHierarchyNode				parent;
	
	// The children from this point in the hierarchy
	private ArrayList<YangHierarchyNode>	children;
	
	// The structure that defined this container/grouping
	private YangStructure					structure;
	
	// The object at this level of the container hierarchy
	private JSONObject						object;
	
	// Or, if this a list, we'll have an array
	private JSONArray						array;
	
	// When dump the schemas, we're primarily interested in leaf containers, but
	// in the case where we have a non-leaf container in the middle of the hierarchy
	// that has leaf elements, we want those containers as well - this flag is
	// set if we add a variable to an OBJECT_LIST type
	private boolean							hasLeafOrLLeafLists;
	
	private TreeMap<String,VariableInfo>	variablesByName;
	private int								longest;

	public YangHierarchyNode(HierarchyTypeEnum type, YangHierarchyNode parent, YangStructure structure) {
		this.type		= type;
		this.parent		= parent;
		this.structure	= structure;
		
		children		= new ArrayList<>();
		variablesByName = new TreeMap<>();
		
		switch(type) {
		case CONTAINER:
		case GROUPING:
			object	= new JSONObject();
			if (parent != null)
				parent.object.put(structure.name(), object);
			array	= null;
			break;
		case USES_LIST:
		case LEAF_LIST:
			object	= null;
			array	= new JSONArray();
			if (parent != null)
				parent.object.put(structure.name(), array);
			break;
		case OBJECT_LIST:
			array	= new JSONArray();
			object	= new JSONObject();
			
			array.put(object);
			if (parent != null)
				parent.object.put(structure.name(), array);
			break;
		}
		
		if (parent != null)
			parent.children.add(this);
	}
	
	/**
	 * @return our JSON object.
	 */
	public JSONObject object() {
		if (object == null)
			throw(new IllegalStateException("This isn't a container - no object is available"));
		
		return(object);
	}
	
	/**
	 * @return our array if we're an array.
	 */
	public JSONArray array() {
		if (array == null)
			throw(new IllegalStateException("This isn't a list - no array is available"));
		
		return(array);
	}
	
	public void addVariable(VariableInfo info) {
		switch(type) {
		case CONTAINER:
			object.put(info.structure().name(), info.variableInsert());
			break;
		case LEAF_LIST:
			array.put(info.variableInsert());
			break;
		case OBJECT_LIST:
			hasLeafOrLLeafLists = true;
			object.put(info.structure().name(), info.variableInsert());
			break;
		case GROUPING:
		case USES_LIST:
			throw(new IllegalStateException("Can't add a variable to a node of type: " + type));
		}
		
		variablesByName.put(info.name(), info);
		if (info.name().length() > longest)
			longest = info.name().length();
	}
	
	public String getSchema(ModuleVersion version) {
		StringBuilder sb = new StringBuilder();
		
//		sb.append(structure.getFullyQualifiedName() + "\n\n");

		StringBuilder path = new StringBuilder();
		getPath(path);
		
		if (isLeafContainer()) {
			sb.append("// leaf container: " + isLeafContainer() + "\n");
			
			IntegerVar longestName = new IntegerVar();
			TreeMap<String,VariableInfo> variablesHere = new TreeMap<>();
			gatherVariables(variablesHere,longestName);
			longestName.set(longestName.intValue() + 4);
			
			PrintfFormat format = new PrintfFormat("%-" + longestName.intValue() + "s");
			
			sb.append("InputSchema\n");
			sb.append("name            " + structure.getFullyQualifiedName() + "\n");
			sb.append("path            " + path.toString() + "\n");
	
			for(VariableInfo var: variablesHere.values()) {
//				sb.append(var.toVariableFormat(format) + "\n");
				sb.append(var.toVariableFormatNoNote(format) + "\n");
			}
	
			sb.append("inputSchema " + PrettyJSON.instance().prettyPrint(object, true, "  ") + "\n");
			sb.append("description Generated from: " + version.nameAndRevision() + " - variables: " + variablesHere.size() + "\n");
			sb.append("\n");
		}
		else {
			if (hasLeafOrLLeafLists) {
				sb.append("// non-leaf container with leaf(s)\n");
				sb.append("InputSchema\n");
				sb.append("name            " + structure.getFullyQualifiedName() + "\n");
				sb.append("path            " + path.toString() + "\n");
				sb.append("inputSchema " + PrettyJSON.instance().prettyPrint(object, true, "  ") + "\n");
				
			}
			else {
				sb.append("// non-leaf container with no leaf value\n");
				sb.append("path            " + path.toString() + "\n");
//				sb.append("InputSchema\n");
//				sb.append("name            " + structure.getFullyQualifiedName() + "\n");
//				sb.append("path            " + path.toString() + "\n");
//				sb.append("inputSchema " + PrettyJSON.instance().prettyPrint(object, true, "  ") + "\n");
			}
		}
		
		return(sb.toString());
	}
	
	private boolean isLeafContainer() {
		boolean rc = true;
		
		if (children.size() == 0) {
			
		}
		else {
			for(YangHierarchyNode child: children) {
				if (child.type == HierarchyTypeEnum.CONTAINER) {
					rc = false;
					break;
				}
				
				if (child.isLeafContainer()) {
					
				}
				else {
					rc = false;
					break;
				}
				
			}
		}
		
		return(rc);
	}
	
	private void gatherVariables(TreeMap<String,VariableInfo> variables, IntegerVar longestName) {
		for(VariableInfo info:variablesByName.values()) {
			variables.put(info.name(), info);
			if (info.name().length() > longestName.intValue())
				longestName.set(info.name().length());
		}
		
		for(YangHierarchyNode child: children) {
			if (child.isLeafContainer()) {
				child.gatherVariables(variables,longestName);
			}
		}		
	}
	
	private void getPath(StringBuilder path) {
		switch(type) {
		case CONTAINER:
			path.insert(0, "/" + structure.name());
			break;
		case GROUPING:
			break;
		case LEAF_LIST:
			break;
		case OBJECT_LIST:
			YangAttribute key = structure.singleAttribute(YangStructure.KEY);
			if (key == null)
				path.insert(0, "/" + structure.name() + "[]");
			else
				path.insert(0, "/" + structure.name() + "[" + key.value() + "]");
				
			break;
		case USES_LIST:
			break;
		}
		if (parent != null)
			parent.getPath(path);
		
	}
}
