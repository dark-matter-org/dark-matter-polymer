
public class OptionalValueRemover {

	public static void removeOptional(LinkedHashMap<?,?> map, boolean trace) {
		descend(map, trace);
	}
	
	private static void descend(Object obj, boolean trace) {
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
					descend(value, trace);
				}
			}
			
			if (toRemove != null) {
				for(Object key: toRemove) {
				    if (trace)
						System.out.println("Removing: " + key);
					map.remove(key);
				}
			}
			
		}
		else if (obj instanceof ArrayList){
			ArrayList<Object> list = (ArrayList<Object>) obj;
			
			for(Object element: list) {
				descend(element, trace);
			}
		}
		else {
			// Nothing to do - this is just a String
		}
	}
}
