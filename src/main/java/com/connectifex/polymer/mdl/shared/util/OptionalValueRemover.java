package com.connectifex.polymer.mdl.shared.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

public class OptionalValueRemover {

	public static void removeOptional(LinkedHashMap<?,?> map) {
		descend(map);
	}
	
	private static void descend(Object obj) {
		if (obj instanceof LinkedHashMap) {
			LinkedHashMap<?,?> 	map  		= (LinkedHashMap<?, ?>) obj;
			ArrayList<Object>	toRemove 	= null;
			
			Set<Object> keys = (Set<Object>) map.keySet();
			for(Object key: keys) {
				Object value = map.get(key);
				
				if (value instanceof String) {
					if (value.toString().equals("ReMoVeOpTiOnAl")) {
						if (toRemove == null)
							toRemove = new ArrayList<Object>();
						toRemove.add(key);
					}
				}
				else {
					descend(value);
				}
			}
			
			if (toRemove != null) {
				for(Object key: toRemove) {
					System.out.println("Removing: " + key);
					map.remove(key);
				}
			}
			
		}
		else if (obj instanceof ArrayList){
			ArrayList<Object> list = (ArrayList<Object>) obj;
			
			for(Object element: list) {
				descend(element);
			}
		}
		else {
			// Nothing to do - this is just a String
		}
	}
}
