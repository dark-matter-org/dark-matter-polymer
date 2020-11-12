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
		YangStructure rc = null;
		
		if (gname.contains(":")) {
			YangStructure moduleToSearch = module;
			
			String prefix = gname.substring(0, gname.indexOf(':'));
			String gnamePart = gname.substring(gname.indexOf(':')+1);
			
			// Get the prefix defined within the module itself - if available
			YangAttribute modulePrefix = module.singleAttribute(YangConstants.PREFIX);
			if (modulePrefix == null) {
				moduleToSearch = module.getImportByPrefix(prefix);
			}
			else {
				if (modulePrefix.value().equals(prefix))
					moduleToSearch = module;
				else
					moduleToSearch = module.getImportByPrefix(prefix);
			}
			
			rc = moduleToSearch.findGroupingInLocalScope(gnamePart);
			
			if (rc == null) {
				Iterator<YangStructure>	groupings = moduleToSearch.childrenOfType(YangConstants.GROUPING);
				while(groupings.hasNext()) {
					YangStructure group = groupings.next();
					if (group.name().equals(gnamePart)) {
						rc = group;
						break;
					}
				}
			}

		}
		else {
			rc = node.findGroupingInLocalScope(gname);
			
			if (rc == null) {
				Iterator<YangStructure>	groupings = module.childrenOfType(YangConstants.GROUPING);
				while(groupings.hasNext()) {
					YangStructure group = groupings.next();
					if (group.name().equals(gname)) {
						rc = group;
						break;
					}
				}
			}
		}
		
		return(rc);
	}
	
}
