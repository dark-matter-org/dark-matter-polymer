
import org.dmd.util.formatting.PrintfFormat;

class PolymerTrace {

	static void traceTweakInputs(Map inputs, Object payload){
	    System.out.println("\ntweakInputs\n\n");
	    
		Set<String> keys = null;
	      
	    int longest = 0;
	      
		keys = inputs.keySet();
  		for(String key: keys){
        	if (key.length() > longest)
        	    longest = key.length();
		}
        PrintfFormat format = new PrintfFormat("%-" + longest + "s");          

		System.out.println("inputs:\n");
  		for(String key: keys){
        	System.out.println(format.sprintf(key) + "  -  " + inputs.get(key));
		}
	    System.out.println();
	                                             
	                                             
	}

	static void traceTweakValues(Map inputs, Map outputs){
	    System.out.println("\ntweakValues\n\n");

		Set<String> keys = null;
	      
	    int longest = 0;
	      
		keys = inputs.keySet();
  		for(String key: keys){
        	if (key.length() > longest)
        	    longest = key.length();
		}
        PrintfFormat format = new PrintfFormat("%-" + longest + "s");          

		System.out.println("inputs:\n");
  		for(String key: keys){
        	System.out.println(format.sprintf(key) + "  -  " + inputs.get(key));
		}
		
		keys = outputs.keySet();
		System.out.println("\noutputs:\n");
  		for(String key: keys){
        	System.out.println(format.sprintf(key) + "  -  " + outputs.get(key));
		}
	    System.out.println();
		
	                                             
	}

	static void traceTweakParsed(Object payload, Object output){
	    System.out.println("\ntweakParsed\n\n");
	                                             
	                                             
	}

}