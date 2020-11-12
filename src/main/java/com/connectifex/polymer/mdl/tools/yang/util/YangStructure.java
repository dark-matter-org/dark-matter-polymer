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

package com.connectifex.polymer.mdl.tools.yang.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;

import org.dmd.dmc.types.CheapSplitter;
import org.dmd.dmc.types.IntegerVar;
import org.dmd.util.exceptions.DebugInfo;
import org.dmd.util.exceptions.ResultException;


/**
 * The YangStructure provides a simple mechanism to store a hierarchy of YANG structures
 * that can be translated into YANG DSL classes. The root YangStructure represents a YANG module.
 * 
 * However, YANG modules can include submodules which, if they are found, are parsed and stored
 * here as submodules. 
 */
public class YangStructure {
	
	private static String NACM = "nacm";
	private static String INCLUDE = "include";
	private static String IMPORT = "import";
	private static String GROUPING = "grouping";

	private String type;
	private String name;
	
	
	// This is base module/submodule structure - it's only initialized in substructures
	private YangStructure								module;
	
	private YangStructure								parent;
	private ArrayList<YangStructure> 					children;
	
	// Key: The name of the submodule 
	// Value: Its content as a YangStructure
	private TreeMap<String, YangStructure>				includes;
	
	// Key: The name of the module 
	// Value: Its content as a YangStructure
	private TreeMap<String, YangStructure>				imports;
	
	// Key: the prefix attribute of an import
	// Value: Its content as a YangStructure
	private TreeMap<String, YangStructure>				importsByPrefix;
	
	private ArrayList<YangAttribute>						attributes;
	private TreeMap<String, ArrayList<YangAttribute>>	 	attributesByName;
	
	// This is only defined on the root structure
	private String	fileName;
	
	// The line number at which this structure started - this is unique for
	// all structures in a module
	private int 											lineNumber;
	
	// When we translate the YangStructures into the Yang DSL, some structures
	// will have names generated for them - it will stored here - this allows
	// for references to structures that haven't yet been instantiated as
	// we descend the hierarchy of structures in YangFactory. The YangModule
	// class is in charge of assigning unique identifiers and will set this
	// on the structure when its getUniqueName() method is called. 
	private String										assignedName;
	
	// A fully qualified name for this structure. The getFullyQualifiedName() method
	// will initialize this. It's created by proceeding up the structure hierarchy 
	// and concatenating the names of structures together e.g. name--name--name
	//
	// We also check to see if we've had to use an assigned name for a structure e.g.
	// in cases where the name is something like a reference:
	// uses /oc-if:interfaces/oc-if:interface
	private String										fqn;
	
	// When we translate from Yang to DSL we wind up flattening the Yang structures. Because
	// of this we often require unique names for the DSL object instances e.g. you might have
	// two leafs with the same name in different parts of the definition - so this structure maintains
	// a unique id scheme for each type of substructure. Sub structures will call getUniqueName()
	// when they require unique names.
	// Key: structure type name e.g. leaf, container, list
	// Value: the next available integer identifier
	protected TreeMap<String,IntegerVar>	uniqueIdsByStructName;
	
	// Typedefs can be defined and used within the scope of a structure e.g. within a container
	// In those cases, leaves within the container need to refer to the type within the scope and
	// thus need to use the fully qualified name of the typedef in the reference 
	// When typedefs are found below level 1, we add them here so that we can look up the tree
	// to see if the type is defined in local scope.
	private TreeMap<String,YangStructure>	typeDefsByName;
	
	// Groupings, likewise, can be defined and used within the scope of a structure.
	// We do a similar thing as for types - we maintain a listing for those Groupings
	// defined beneath level 1.
	// See: ietf-netconf-monitoring and the lock-info grouping.
	private TreeMap<String,YangStructure>	groupingDefsByName;
	
	private int depth;
	
	/**
	 * Constructs a new module/submodule structure
	 * @param type the type
	 * @param name the name of the module
	 * @param parent null
	 * @param fileName the file we were parsed from
	 */
	public YangStructure(String type, String name, YangStructure parent, String fileName) {
		this.type = type;
		this.name = name;
		this.parent = parent;
		if (parent != null)
			parent.addChild(this);
		this.includes = null;
		this.imports = null;
		
		this.fileName = fileName;
		this.lineNumber = 1;
		this.depth = 0;
		
		uniqueIdsByStructName = new TreeMap<>();
	}
	
	public YangStructure getModule() {
		if (parent == null)
			return(this);
		return(parent.getModule());
	}
	
	public YangStructure(String type, String name, YangStructure parent, int lineNumber, int depth, YangStructure module) {
//	public YangStructure(String type, String name, YangStructure parent, int lineNumber, int depth) {
		this.type = type;
		this.name = name;
		this.module = module;
		
		this.parent = parent;
		if (parent != null)
			parent.addChild(this);
		this.includes = null;
		this.imports = null;
		
		this.fileName = null;
		this.lineNumber = lineNumber;
		
		this.depth = depth;
		
		this.module.assignUniqueName(this, module);
		
		// If a typedef is defined beneath level 1, store it in the parent element for later lookup
		if ((depth > 1) && type.equals(YangConstants.TYPEDEF)) {
			parent.storeLocalType(this);
		}
		
		// If a grouping is defined beneath level 1, store it in the parent element for later lookup
		if ((depth > 1) && type.equals(YangConstants.GROUPING)) {
			parent.storeLocalGrouping(this);
		}
	}
	
	/**
	 * Can be used on substructures to get their containing module.
	 * @return
	 */
	public YangStructure moduleWhereYouAreDefined() {
		return(module);
	}
	
	public YangStructure findTypeInLocalScope(String typeName) {
		if (depth == 1)
			return(null);
		
		if (typeDefsByName == null) {
			return(parent.findTypeInLocalScope(typeName));
		}
		else {
			YangStructure rc = typeDefsByName.get(typeName);
			if (rc == null) {
				return(parent.findTypeInLocalScope(typeName));
			}
			else
				return(rc);
		}
	}
	
	private void storeLocalType(YangStructure typeDef) {
		if (typeDefsByName == null) {
			typeDefsByName = new TreeMap<>();
		}
		
		if (typeDefsByName.get(typeDef.name) != null)
			throw(new IllegalStateException("Duplicate typedef name: " + typeDef.name));
		
		typeDefsByName.put(typeDef.name, typeDef);
	}
	
	public YangStructure findGroupingInLocalScope(String groupingName) {
//		if (depth == 1)
//			return(null);
		
		if (groupingDefsByName == null) {
			if (parent == null)
				return(null);
			
			return(parent.findGroupingInLocalScope(groupingName));
		}
		else {
			YangStructure rc = groupingDefsByName.get(groupingName);
			if (rc == null) {
				return(parent.findGroupingInLocalScope(groupingName));
			}
			else
				return(rc);
		}
	}
	
	private void storeLocalGrouping(YangStructure groupingDef) {
		if (groupingDefsByName == null) {
			groupingDefsByName = new TreeMap<>();
		}
		
		if (groupingDefsByName.get(groupingDef.name) != null)
			throw(new IllegalStateException("Duplicate grouping name: " + groupingDef.name));
		
		groupingDefsByName.put(groupingDef.name, groupingDef);
	}
	
	/**
	 * @return the fully qualified name of this structure
	 */
	public String getFullyQualifiedName() {
		if (parent == null) {
			// This is the root of the module, so the fqn is just its name
			return(name);
		}
		if (fqn == null) {
			StringBuilder sb = new StringBuilder();
			createFQN(sb);
			fqn = sb.toString();
		}
		
		return(fqn);
	}
	
	private void createFQN(StringBuilder sb) {
		if (parent == null) {
			// This is the root node, but don't add that to the name
		}
		else {
			parent.createFQN(sb);
			if (sb.length() > 0) {
				if (assignedName == null)
					sb.append("--" + name);
				else
					sb.append("--" + assignedName);
			}
			else {
				if (assignedName == null)
					sb.append(name);
				else
					sb.append(assignedName);
			}
		}
	}
	
	public String assignedName() {
		return(assignedName);
	}
	
	public void assignedName(String assignedName) {
		this.assignedName = assignedName;
	}
	
//	public void scanForDuplicateTopLevelNames() {
//		TreeMap<String,YangStructure> byName = new TreeMap<>();
//		for(YangStructure child: children) {
//			YangStructure existing = byName.get(child.name);
//			if (existing != null) {
//				if (existing.type.equals(GROUPING)) {
//					existing.assignedName(existing.name + "_gr");
//				}
//				else if (child.type.equals(GROUPING)) {
//					child.assignedName(child.name + "_gr");
//				}
//				else {
//					DebugInfo.debug("Clash at top level: " + child.name + " " + existing.type + " " + child.type);
//				}
//			}
//			else
//				byName.put(child.name, child);
//		}
//	}
	
	public void loadImportsAndIncludes(YangContext context, boolean trace) throws ResultException, IOException {
		for(YangAttribute attr: attributes) {
			if (attr.name().equals(INCLUDE)) {
				ModuleVersion version = context.getModule(attr.value(), null);
				
				if (version == null) 
					throw(new IllegalStateException("Could not find included submodule: " + attr.value() + " included from: " + fileName));
				
				if (context.isLoaded(version)) {
					YangDebugChannels.moduleInclude.getChannel().publish("Include already loaded: " + version.nameAndRevision());
					addInclude(attr.value(),context.getLoaded(version));
				}
				else {
					YangDebugChannels.moduleInclude.getChannel().publish("Loading include: " + version.nameAndRevision());
					
					if (trace)
						DebugInfo.debug("\n\nLoading include: " + version.nameAndRevision());
					
					YangParser parser = new YangParser(context);
					parser.trace(trace);
					
					YangStructure included = parser.parse(version.location().getDirectory(), version.nameAndRevision() + ".yang");
					addInclude(attr.value(), included);
					
					YangDebugChannels.moduleInclude.getChannel().publish("Include loaded: " + version.nameAndRevision() + " " + version.location().getDirectory());
					context.loaded(version, included);
				}
			}
		}
		
		if (children != null) {
			for(YangStructure child: children) {
				if (child.type().equals(IMPORT)) {
					ModuleVersion version = context.getModule(child.name(), null);
					
					if (version == null) 
						throw(new IllegalStateException("Could not find imported module: " + child.name()));
					
					if (context.isLoaded(version)) {
						YangDebugChannels.moduleImport.getChannel().publish("Import already loaded: " + version.nameAndRevision());
						addImport(fileName, child.singleAttribute(YangConstants.PREFIX), context.getLoaded(version));
					}
					else {
					
						if (trace)
							DebugInfo.debug("\n\nLoading import: " + version.nameAndRevision());
						
						YangParser parser = new YangParser(context);
						parser.trace(trace);
						
						YangStructure module = parser.parse(version.location().getDirectory(), version.nameAndRevision() + ".yang");
						addImport(fileName, child.singleAttribute(YangConstants.PREFIX), module);
						
						YangDebugChannels.moduleImport.getChannel().publish("Import loaded: " + version.nameAndRevision() + " " + version.location().getDirectory());
						context.loaded(version, module);
					}
				}
			}
		}
	}
	
	public int lineNumber() {
		return(lineNumber);
	}
	
	private void addInclude(String name, YangStructure submodule) {
		if (includes == null)
			includes = new TreeMap<>();
		
		includes.put(name, submodule);
		
	}
	
	private void addImport(String name, YangAttribute prefix, YangStructure module) {
		if (imports == null) {
			imports = new TreeMap<>();
			importsByPrefix = new TreeMap<>();
		}
		
		imports.put(name, module);
		
		if (prefix != null) {
//			DebugInfo.debug("IMPORT " + prefix.value());
			importsByPrefix.put(prefix.value(), module);
		}
		
	}
	
	public YangStructure getImportByPrefix(String prefix) {
		if (importsByPrefix == null)
			return(null);
		return(importsByPrefix.get(prefix));
	}
	
	public YangStructure getImport(String name) {
		if (imports == null)
			return(null);
		
		return(imports.get(name));
	}
	
	/**
	 * We only store the filename on the root (module), this will recurse up the stack to get the filename
	 * @return the file where this structure resides
	 */
	public String fileName() {
		if (parent == null)
			return(fileName);
		else
			return(parent.fileName());
	}
	
	public String name() {
		return(name);
	}
	
	public String type() {
		return(type);
	}
	
	public YangStructure parent() {
		return(parent);
	}
	
	public Iterator<YangStructure> children(){
		if (children == null)
			return(Collections.emptyIterator());
		return(children.iterator());
	}
	
	/**
	 * @param index the index of the child you want
	 * @return the child structure.
	 */
	public YangStructure getChild(int index) {
		if (children == null)
			throw(new IllegalStateException("No children are avilable on this YangStructure."));
		
		if ( (index+1) > children.size())
			throw(new IllegalStateException("The are only " + children.size() + " children available."));
		
		return(children.get(index));
	}
	
	/**
	 * @return true if the current structure is contained within a list anywhere up the tree.
	 */
	public boolean isInList() {
		if (type.equals(YangConstants.LIST) || type.equals(YangConstants.LEAF_LIST))
			return(true);
		
		if (parent == null)
			return(false);
		
		return(parent.isInList());

	}
	
	/**
	 * @param type the type from YangConstants
	 * @return the children of this node of the specified type
	 */
	public Iterator<YangStructure> childrenOfType(String type){
		if (children == null)
			return(Collections.emptyIterator());
		
		ArrayList<YangStructure> byType = new ArrayList<>();
		Iterator<YangStructure> it = children.iterator();
		while(it.hasNext()) {
			YangStructure ys = it.next();
			if (ys.type.equals(type))
				byType.add(ys);
		}
		
		return(byType.iterator());
	}
	
	public int childrenSize() {
		if (children == null)
			return(0);
		return(children.size());
	}
	
	/**
	 * Adds an attribute to the structure. This will come in a couple of forms, either
	 * a name value pair, or a name and quoted value.
	 * @param singleAttr the single representation of the attribute
	 * @return the YangAttribute that's created
	 */
	public YangAttribute addAttribute(String singleAttr) {
		
//		if (type.equals(YangConstants.USES)) {
//			if (YangConstants.nameIsValid(singleAttr)) {
//				DebugInfo.debug("Simple USES: " + singleAttr);
//			}
//			if(!YangConstants.hasSpecialCharacters(singleAttr)) {
//				DebugInfo.debug("Simple USES: " + singleAttr);
//			}
			
//		}
//		DebugInfo.debug(singleAttr);
		if (attributes == null) {
			attributes = new ArrayList<>();
			attributesByName = new TreeMap<>();
		}
		
		YangAttribute attr = null;
		String tmp = singleAttr.replaceAll(";", "");
		
		ArrayList<String> tokens = CheapSplitter.split(tmp, ' ', false, true);
		
		if (tokens.size() == 1) {
			// Could be something like:
			// tailf:cli-operational-mode;
			// Not sure what this is indicating at this point - seems to be in an action definition
			// There is no "value"
			attr = new YangAttribute(tokens.get(0), "NONE");
			
		}
		else if (singleAttr.contains("\"")) {
			int firstQuote = singleAttr.indexOf("\"");
			int lastQuote = singleAttr.lastIndexOf("\"");
			attr = new YangAttribute(tokens.get(0), singleAttr.substring(firstQuote+1, lastQuote));
		}
		else {
			if (tokens.size() == 1) {
				// Could be an nacm: entry
				// Example from ietf-system:       nacm:default-deny-write;
				if (tokens.get(0).startsWith(NACM)) {
					ArrayList<String> altTokens = CheapSplitter.split(tmp, ':', false, true);
					if (altTokens.size() == 2) {
						attr = new YangAttribute(altTokens.get(0), altTokens.get(1));
					}
					else {
						DebugInfo.debug("Can't handle NACM attribute: " + singleAttr);
						DebugInfo.debug("File: " + fileName() + " Line: " + lineNumber);
					}
				}
			}
			else {
				attr = new YangAttribute(tokens.get(0), tokens.get(1));
			}
		}
		
		attributes.add(attr);
		ArrayList<YangAttribute> existing = attributesByName.get(attr.name());
		
		if (existing == null) {
			existing = new ArrayList<>();
			attributesByName.put(attr.name(), existing);
		}
		
		existing.add(attr);
//		else {
//			DebugInfo.debug("Multiple attributes with name: " + attr.name());
//			DebugInfo.debug("File: " + fileName() + " Line: " + lineNumber);
//		}
		
		return(attr);
	}
	
	public ArrayList<YangAttribute>	getAttributes(){
		return(attributes);
	}
	
	private void addChild(YangStructure child) {
		if (children == null)
			children = new ArrayList<>();
		children.add(child);
	}
	
	public YangAttribute singleAttribute(String name) {
		if (attributesByName == null)
			return(null);
		
		ArrayList<YangAttribute> existing = attributesByName.get(name);
		if (existing == null)
			return(null);
		
		if (existing.size() == 1)
			return(existing.get(0));
		
		throw(new IllegalStateException("singleAttribute() called on multi-valued attribute: " + name));
	}
	
	public ArrayList<YangAttribute> attribute(String name) {
		if (attributesByName == null)
			return(null);
		return(attributesByName.get(name));
	}
	
	public String getDependencies() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("Dependencies for: " + fileName + "\n\n");
		
		getDependencies(sb, "");
		
		return(sb.toString());
	}
	
	private void getDependencies(StringBuilder sb, String indent) {
		if (imports != null) {
			for(String key: imports.keySet()) {
				YangStructure ys = imports.get(key);
				sb.append(indent + "    Import: " + key + "\n");
				ys.getDependencies(sb, indent + "    ");
			}
		}
		if (includes != null) {
			for(String key: includes.keySet()) {
				YangStructure ys = includes.get(key);
				sb.append(indent + "    Include: " + key + "\n");
				ys.getDependencies(sb, indent + "    ");
			}
		}
	}
		
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
//		sb.append(type + "  " + name + "\n");
//		if (attributes != null) {
//			for(YangAttribute attr: attributes) {
//				sb.append("  " + attr + "\n");
//			}
//		}
		
		toString(sb, "");

		
		return(sb.toString());
	}
	
	private void toString(StringBuffer sb, String indent) {
		sb.append(indent + type + "  " + name + "\n");
		if (parent != null)
			sb.append(indent + "  fqn: " + getFullyQualifiedName() + "\n");
		
		if (attributes != null) {
			for(YangAttribute attr: attributes) {
				sb.append(indent + "  +" + attr + "\n");
			}
		}
		 if (children == null)
			 return;
		 
		 for(YangStructure child: children) {
			 child.toString(sb, indent + "    ");
		 }
	}
	
    /**
     * Some constructs don't have name per se, so we allow for names to be generated so that
     * they are unique within any given module. When this method is called, it will set the assignedName on the
     * YangStructure so that subsequent references to the structure will have the correct unique name.
     * </p>
     * Note: we generally assign unique names to structures that have things like references in their "name" e.g.
     * uses /oc-if:interfaces/oc-if:interface
     * This mechanism is used in conjunction with the fully qualified name mechanism - when constructing
     * the FQN we'll use the assignedName if it's available - otherwise we use the name we parsed out.
     * </p>
     * We also assign unique names to particular structures since they can clash with other top level
     * definitions in the same module:
     * YangUses
     * @param structure The YangStructure for which we need a unique name
     * @param module the module being parsed
     * @return a unique name for a Yang DSL class within a module.
     */
    private void assignUniqueName(YangStructure structure, YangStructure module) {
    		
    		// This logic should be coordinated with YangDefinition.initializeAttributes()

    		boolean topLevelClash = false;
		// This is a generalized mechanism to deal with places where someone has defined a toplevel
		// structure that has the same name as module in which it's defined - most, if not all of
    		// the SNMP-*-MIB modules do this nonsense!
    		if (  (structure.depth == 1) && structure.name.equals(module.name())) {
    			topLevelClash = true;
//    			DebugInfo.debug("Toplevel clash for: " + structure.type() + "  " + structure.name());
    		}
    		
    		boolean clashingUnion = parentIsUnionWithClashingTypes(structure);
    		
//    		if ( YangConstants.hasSpecialCharacters(structure.name()) ||
    		if ( !YangConstants.nameIsValid(structure.name()) ||
    				(structure.type().equals(YangConstants.USES)) ||
    				(structure.type().equals(YangConstants.GROUPING)) ||
				(structure.type().equals(YangConstants.RPC)) ||
				
				// Note, the need for this may go away when types are properly flattened
				(structure.type().equals(YangConstants.TYPE)) && structure.name().equals(YangConstants.IDENTITYREF)  ||
				
				topLevelClash ||
				
				clashingUnion
				
			) {
					
	    		YangDebugChannels.structureNaming.getChannel().publish("Assigning name for: " + structure.type() + "  " + structure.name());
	
	//			DebugInfo.debug("Assigning name for: " + structure.type() + "  " + structure.name());
				
				if (structure.type.equals(YangConstants.TYPEDEF) && (structure.depth==1)) {
					// If we have a top level typedef with a weird name e.g. embedded periods
					// as in ietf-yang-type.yang xpath1.0 - we'll try and recover by replacing
					// periods with underscores. This comes into play when creating references
					// to this type from other modules - e.g. ietf-netconf-acm refers to xpath
					// but, if we assign a unique name, we don't have an easy way to come up
					// with the reference.
					String tmp = structure.name().replace(".", "_");
					
					if (!YangConstants.nameIsValid(tmp)) {
						throw(new IllegalStateException("Can't properly adjust typedef name: " + structure.name + 
								" From module: " + module.name + " Line: " + structure.lineNumber));
					}
					
					structure.assignedName(tmp);
				}
				else if (structure.type.equals(YangConstants.GROUPING)){
					// For groupings, we always append _gr to prevent top level
					// clashes for morons who define top level containers (or
					// other definitions) with the same name!
					structure.assignedName(structure.name() + YangConstants.GROUPING_SUFFIX);
					
					// This means that uses statements in the following constructs
					// must be adjusted to append _gr to the grouping name:
					// container
					// list
					// cases
					// grouping
					// input
					// output
					// notification
					// augment
				}
				else {
			    		if (uniqueIdsByStructName == null)
			    			uniqueIdsByStructName = new TreeMap<>();
			    		
			    		IntegerVar id = uniqueIdsByStructName.get(structure.type());
			    		
			    		if (id == null) {
			    			id = new IntegerVar(1);
			    			uniqueIdsByStructName.put(structure.type(), id);
			    		}
			    		else {
			    			id.set(id.intValue() + 1);
			    		}
			    		
			    		structure.assignedName(structure.type() + id.intValue());
				}
			}
		
    }
    
    private boolean parentIsUnionWithClashingTypes(YangStructure structure) {
    		if (structure.parent == null)
    			return(false);
    		
    		if (structure.parent.name.equals(YangConstants.UNION)) {
    			HashSet<String> names = new HashSet<>();
    			for(YangStructure child: structure.parent.children) {
    				if (names.contains(child.name))
    					return(true);
    				names.add(child.name);
    			}
    		}
    			
    		return(false);
    }

	public int depth() {
		return(depth);
	}
    
    

}
