package com.connectifex.polymer.mdl.server.extended.plastic.util;

import java.util.HashMap;

import com.connectifex.polymer.mdl.shared.generated.types.PlasticGroupAndName;



public class GroupOrLiteral {
	private PlasticGroupAndName group;
	private String literal;
	
	public GroupOrLiteral(PlasticGroupAndName group, String literal) {
		this.group = group;
		this.literal = literal;
	}
	
	/**
	 * @param map the values of the tuples from a matched pattern
	 * @return the literal if this is a literal or the value 
	 */
	public String toString(HashMap<String,String> map){
		if (group == null)
			return(literal);
		
		return(map.get(group.getGroupName()));
	}
}
