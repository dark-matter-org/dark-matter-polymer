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
