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
import java.util.LinkedHashMap;
import java.util.Set;

public class Analyzer {

	public static void analyze(LinkedHashMap<?,?> map) {
		descend("", map);
	}
	
	private static void descend(String indent, Object obj) {
		if (obj instanceof LinkedHashMap) {
			LinkedHashMap<?,?> map  = (LinkedHashMap<?, ?>) obj;
			
			Set<Object> keys = (Set<Object>) map.keySet();
			for(Object key: keys) {
				Object value = map.get(key);
				System.out.println(indent + "key: " + key + " value type: " + value.getClass().getName());
				descend(indent + "  ", value);
			}
			
		}
		else if (obj instanceof ArrayList){
			ArrayList<Object> list = (ArrayList<Object>) obj;
			
			for(Object element: list) {
				System.out.println(indent + "element type: " + element.getClass().getName());
				descend(indent + "  ", element);
			}
		}
		else {
			System.out.println(indent + " object type: " + obj.getClass().getName());
		}
	}
}
