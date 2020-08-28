package com.connectifex.polymer.mdl.tools.yang.util;

import org.dmd.util.debug.DebugCategory;

public class YangDebugChannels {

	static final public DebugCategory fileLoading 					= new DebugCategory("YangFileLoading", "Let's you see the files being loaded", "file-loading");

	static final public DebugCategory singleLineAttributeParsing 	= new DebugCategory("YangSingleLineAttributeParsing", "Single line attribute parsing", "single-line-attributes", "attribute-parsing");

	static final public DebugCategory multiLineAttributeParsing 	= new DebugCategory("YangMultiLineAttributeParsing", "Multi-line attribute parsing", "multi-line-attributes", "attribute-parsing");

	static final public DebugCategory structureDepth 				= new DebugCategory("YangStructureDepth", "Yang structure push/pop", "structure");

	static final public DebugCategory moduleImport 					= new DebugCategory("YangModuleImport", "Yang module import parsing", "module-import");

	static final public DebugCategory moduleInclude 				= new DebugCategory("YangModuleInclude", "Yang module include parsing", "module-include");

	static final public DebugCategory structureTranslation 			= new DebugCategory("YangStructureTranslation", "Translation of the Yang structure into DSL", "yang-structure");

	static final public DebugCategory structureNaming 				= new DebugCategory("YangStructureNaming", "Assignment of names to Yang structures", "yang-structure-naming");

	static final public DebugCategory factoryOmniHierarchy 			= new DebugCategory("YangFactoryOmniHierarchy", "Assignment of names to Yang structures", "factory-omni-hierarchy");

}
