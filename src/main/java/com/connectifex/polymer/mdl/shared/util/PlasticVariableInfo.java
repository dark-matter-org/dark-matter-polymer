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

package com.connectifex.polymer.mdl.shared.util;

import java.util.ArrayList;

import org.dmd.dmc.types.CheapSplitter;

/**
 * The PlasticVariableInfo class provides access to information about the variables
 * embedded in a plastic schema. This includes not just the name of the variable, but
 * also its location within the particular schema. This location or path information
 * is used when creating morphers to support various use cases e.g. optional values.
 */
public class PlasticVariableInfo {

	private String 				name;
	
	// For an input schema, there will only be one path to a variable, but, for
	// an output schema, a variable may be located in multiple locations.
	private ArrayList<String>	paths;
	
	public PlasticVariableInfo(String name) {
		this.name = name;
		paths = new ArrayList<>();
	}
	
	public void addPath(String path) {
		paths.add(path);
	}
	
	public String name() {
		return(name);
	}
	
	public ArrayList<String> paths(){
		return(paths);
	}
	
	public void appendPaths(StringBuilder sb) {
		for(String path: paths)
			sb.append("    " + path + "\n");
	}
	
	public String getOptionalAdjustment() {
		StringBuilder sb = new StringBuilder();
		
//		for(String path:paths){
//			ArrayList<String> parts = CheapSplitter.split(path, '/', false, true);
//			
//			for(String part:parts) {
//				if (part.contains(s))
//			}
//			
//		}
		
		return(sb.toString());
	}
	
}
