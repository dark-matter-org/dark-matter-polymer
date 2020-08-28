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
