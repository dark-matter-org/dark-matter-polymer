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

import java.util.Iterator;

public class Helper {

	static public YangStructure getGrouping(YangStructure module, YangStructure node, String gname) {
		YangStructure rc = node.findGroupingInLocalScope(gname);
		
		if (rc == null) {
			Iterator<YangStructure> children = module.children();
			while(children.hasNext()) {
				YangStructure child = children.next();
				if (child.type().equals(YangConstants.GROUPING)) {
					if (child.name().equals(gname)) {
						rc = child;
						break;
					}
				}
			}
		}
		
		return(rc);
	}
	
}
