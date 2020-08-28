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
