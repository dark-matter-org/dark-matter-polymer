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

public class YangAttribute {
	
	private String name;
	private String value;
	
	public YangAttribute(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public String toString() {
		return(name + " = " + value);
	}
	
	public String name() {
		return(name);
	}
	
	public String value() {
		return(value);
	}
}
