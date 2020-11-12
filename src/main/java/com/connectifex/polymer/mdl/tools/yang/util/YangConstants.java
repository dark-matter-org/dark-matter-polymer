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

public class YangConstants {

	// Constructs
	public static String AUGMENT 		= "augment";
	public static String CASE 			= "case";
	public static String CHOICE 		= "choice";
	public static String CONTAINER 		= "container";
	public static String LEAF 			= "leaf";
	public static String GROUPING 		= "grouping";
	public static String LIST 			= "list";
	public static String LEAF_LIST 		= "leaf-list";
	public static String MODULE 		= "module";
	public static String REVISION 		= "revision";
	public static String SUBMODULE 		= "submodule";
	public static String RPC 			= "rpc";
	public static String TYPEDEF 		= "typedef";
	public static String ENUMERATION 	= "enumeration";
	public static String UNION 			= "union";
	public static String USES 			= "uses";
	public static String WHEN 			= "when";
	
	public static String IDENTITYREF 		= "identityref";
	public static String SNMP_COMMUNITY_MIB = "SNMP-COMMUNITY-MIB";
	public static String SNMP_TARGET_MIB 	= "SNMP-TARGET-MIB";
	public static String LEAFREF 			= "leafref";

	// Attributes
	public static String BASE 				= "base";
	public static String CONFIG 			= "config";
	public static String CONTACT 			= "contact";
	public static String DEFAULT 			= "default";
	public static String DESCRIPTION 		= "description";
	public static String FRACTION_DIGITS 	= "fraction-digits";
	public static String IF_FEATURE 		= "if-feature";
	public static String IMPORT 			= "import";
	public static String INCLUDE 			= "include";
	public static String KEY 				= "key";
	public static String LENGTH 			= "length";
	public static String MANDATORY 			= "mandatory";
	public static String MODIFIER 			= "modifier";
	public static String NAMESPACE 			= "namespace";
	public static String ORGANIZATION 		= "organization";
	public static String PATH 				= "path";
	public static String PATTERN 			= "pattern";
	public static String PREFIX 			= "prefix";
	public static String PRESENCE 			= "presence";
	public static String RANGE 				= "range";
	public static String REFERENCE 			= "reference";
	public static String REVISION_DATE 		= "revision-date";
	public static String STATUS 			= "status";
	public static String TYPE 				= "type";
	public static String UNITS 				= "units";
	
	public static String STRING 			= "string";
	public static String DECIMAL64 			= "decimal64";
	
	
	
	public static String YANG 			= ".yang";
	
	// Used as a suffuix on all grouping names to ensure uniqueness of
	// top level names (but is applied to ALL groupings)
	public static String GROUPING_SUFFIX 	= "_gr";

	
	public static String PRESENCE_WARNING = "Warning - no presence description specified.";

	public final static String camelCase = "[a-zA-Z0-9][a-zA-Z0-9_.\\s/-]*";

	/**
	 * @param name the name section of a construct i.e. the second token on a line.
	 * @return true if there are special characters embedded
	 */
	public static boolean hasSpecialCharacters(String name) {
		if (name.contains(":") ||
				name.contains("/") ||
				name.contains(" ") ||
				name.contains("{") ||
				name.contains("$") ||
				name.contains("+") ||
				name.contains("'") )
			return(true);
				
		return(false);
			
	}
	
	/**
	 * Uses the same mechanism as the dark-matter DefinitionName to determine if a name is valid.
	 * @param name the potential name to be checked
	 * @return true if the form is okay and false otherwise
	 */
	public static boolean nameIsValid(String name) {
		// For actual definition names, a period is okay, but not when this is the raw
		// name of something
		if (name.contains("."))
			return(false);
		
		if (name.matches(camelCase))
			return(true);
		return(false);
	}
	
//	public static String convertReference(String reference, YangModuleBase module) throws DmcValueException {
//		if (reference.contains(":")) {
//			ArrayList<String> tokens = CheapSplitter.split(reference, ':', false, true);
//			
//			if (tokens.size() != 2)
//				throw(new IllegalStateException("Expecting 2 tokens in a reference: " + reference));
//			
//			String fullName = module.getFullNameForPrefix(tokens.get(0));
//			
//			if (fullName == null) {
//				String modulePrefix = module.theModulesPrefix();
//				
//				if (modulePrefix == null) {
//					throw(new IllegalStateException("Could not find module prefix in reference: " + reference + " in module: " + module.getName()));
//				}
//				else {
//					if (modulePrefix.equals(tokens.get(0))) {
//						// The prefix is for the current module, so just indicate the referenced structure name, it's local
//						return(tokens.get(1));
//					}
//					else {
//						throw(new IllegalStateException("Could not find module prefix in reference: " + reference + " in module: " + module.getName()));						
//					}
//				}
//			}
//			
//			if (fullName.length() > 0) {
//				String refName = tokens.get(1);
//				
//				if (refName.contains(".")) {
//					refName = refName.replace(".", "_");
//				}
//				
//				return(fullName + "-module." + refName);
//			}
//			else
//				return(tokens.get(1));
//		}
//		else {
//			return(reference);
//		}
//	}
	
//	/**
//	 * Allows for the conversion of augment target specifications to the appropriate reference to
//	 * a container, list, choice, case, input, output, or notification node within the appropriate module.
//	 * For now, we assume that all of the prefixes will be the same as in the note in the refernce parameter.
//	 * We find that prefix, get a mapping for the module name and then break the structure down into parts
//	 * based on slashes.
//	 * <p/>
//	 * If the reference is like /a/b/c we just return null for now. This stuff gets very complex from the
//	 * point of view of figuring out what is being referred to - see arrcus-copp-classifier for some of this
//	 * insanity, where the augmentation is referring to a container within itself that's using a container
//	 * from arrcus-classifier. This junk is crazy!
//	 * <p/>
//	 * And also, for now, we're skipping situtations where the module prefixes change - for example:
//	 * /oc-bgp:bgp/oc-bgp:global/oc-bgp:afi-safis/oc-bgp:afi-safi/oc-bgp:state
//	 * from arrcus-openconfig-bgp
//	 * <p/>
//	 * And situations where there's a mix of prefixes and no prefixes...
//	 * /oc-rib-bgp:bgp-rib/oc-rib-bgp:afi-safis/oc-rib-bgp:afi-safi/ipv6-label-unicast/neighbors/neighbor
//	 * from arrcus-openconfig-rib-bgp-augment
//	 * @param reference a string that looks like: /jc:configuration or /jc:configuration/jc:groups, depending
//	 * on the depth of the reference
//	 * @param module the module we're converting
//	 * @return a fully qualified reference to a module and a definition within it or null if the reference
//	 * is local reference like /a/b/c.
//	 * @throws DmcValueException 
//	 */
//	public static String convertAugmentTarget(YangStructure structure, String reference, YangModuleBase module) throws DmcValueException {
//		String rc = null;
//		ArrayList<String>	parts = new ArrayList<String>();
//		
//		// This should be the same for multipart references				
//		String 				basePrefix = null;
//		String				fullModuleName = null;
//		
//		if (!reference.contains(":")) {
//			DebugInfo.debug("Skipping local reference for augment target: " + reference + " file: " + module.getName() + " line: " + structure.lineNumber());
//			return(null);
//		}
//		
//		ArrayList<String> tokens = CheapSplitter.split(reference, '/', false, true);
//		for(String token: tokens) {
//			ArrayList<String> moduleAndRef = CheapSplitter.split(token, ':', false, true);
//			
//			if (moduleAndRef.size() != 2) {
//				return(null);
////				throw(new IllegalStateException("Expecting 2 tokens in a reference: " + reference+ " in module: " + module.getName()  + " line: " + structure.lineNumber()));
//			}
//			
//			if (basePrefix == null) {
//				String fullName = module.getFullNameForPrefix(moduleAndRef.get(0));
//				
//				if (fullName == null) {
//					String modulePrefix = module.theModulesPrefix();
//					
//					if (modulePrefix == null) {
//						throw(new IllegalStateException("Could not find module prefix in augment target: " + reference + " in module: " + module.getName() + " line: " + structure.lineNumber()));
//					}
//					else {
//						if (modulePrefix.equals(moduleAndRef.get(0))) {
//							// The prefix is for the current module, so just indicate the referenced structure name, it's local
//							parts.add(moduleAndRef.get(1));
//						}
//						else {
//							throw(new IllegalStateException("Could not find module prefix in augment target: " + reference + " in module: " + module.getName() + " line: " + structure.lineNumber()));						
//						}
//					}
//					
//				}
//				else {
//					basePrefix = moduleAndRef.get(0);
//					fullModuleName = fullName;
//					parts.add(moduleAndRef.get(1));
//				}
//			}
//			else {
//				// We have multiple parts to the reference
//				if (moduleAndRef.get(0).equals(basePrefix))
//					parts.add(moduleAndRef.get(1));
//				else {
//					return(null);
////					throw(new IllegalStateException("Different module prefixes in augment target: " + basePrefix + " - " + moduleAndRef.get(0) + " in module: " + module.getName()  + " line: " + structure.lineNumber()));
//				}
//			}
//		}
//		
//		rc = fullModuleName + "-module.";
//		
//		Iterator<String> it = parts.iterator();
//		while(it.hasNext()) {
//			String part = it.next();
//			if (part.contains(".")) {
//				part = part.replace(".", "_");
//			}
//			rc = rc + part;
//			
//			if (it.hasNext())
//				rc = rc + "--";
//		}
//		
//		return(rc);
//	}
}
