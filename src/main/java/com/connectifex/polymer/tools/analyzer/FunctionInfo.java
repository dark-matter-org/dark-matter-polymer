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

package com.connectifex.polymer.tools.analyzer;

import java.util.ArrayList;

public class FunctionInfo {

	private ArrayList<String>	files;
	
	private String file;
	private String key;
	private String name;
	private String returnType;
	
	public FunctionInfo(String name, String returnType, String file) {
		this.name = name;
		this.returnType = returnType;
		
		this.key = name + "-" + returnType;
		
		this.file = file;
	}
	
	public FunctionInfo(FunctionInfo info) {
		files = new ArrayList<>();
		files.add(info.file);
		
		name = info.name;
		returnType = info.returnType;
		key = info.key;
	}
	
	public void addInstance(FunctionInfo info) {
		files.add(info.file);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(name + "  -  returns:" + returnType + "\n");
		for(String f: files)
			sb.append(f + "\n");
		sb.append("\n");
		
		return(sb.toString());
	}
	
	public String key() {
		return(key);
	}
	
	public String name() {
		return(name);
	}
	
}
