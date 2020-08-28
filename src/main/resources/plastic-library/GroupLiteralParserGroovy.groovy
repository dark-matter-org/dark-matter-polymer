
import java.util.ArrayList;

/**
 * The GroupLiteralParserGroovy class is the groovisized version of the GroupLiteralParser that
 * can be used to support pattern matching functionality in our morphers. All sanity checking
 * has been stripped because we wouldn't get to using this if problems had been caught in
 * full Java versions of the classes.
 * 
 * We hand copy this code into our src/main/resources/plastic-library and drop the package indication.
 */
public class GroupLiteralParserGroovy {

	public static ArrayList<GroupOrLiteralGroovy> parse(PlasticPatternGroovy np, String expression) {
		ArrayList<GroupOrLiteralGroovy> rc = new ArrayList<>();
		
    		boolean inGroup = false;
    		StringBuilder currentPart = null;
    		 
    		for(int i=0; i<expression.length(); i++){
    			if (expression.charAt(i) == '%'){
    				if (inGroup){
    					PlasticGroupAndNameGroovy gan = np.getGroupByName(currentPart.toString());
    					rc.add(new GroupOrLiteralGroovy(gan, null));
    					inGroup = false;
    					currentPart = null;
    				}
    				else{
    					if (currentPart != null)
    						rc.add(new GroupOrLiteralGroovy(null,currentPart.toString()));
    					
    					currentPart = new StringBuilder();
    					inGroup = true;
    				}
    			}
    			else{
    				if (inGroup){
	    				currentPart.append(expression.charAt(i));
    				}
    				else{
	    				if (currentPart == null)
	    					currentPart = new StringBuilder();
	    				currentPart.append(expression.charAt(i));
    				}
    			}
    		}
    		    		
    		if (currentPart != null)
				rc.add(new GroupOrLiteralGroovy(null, currentPart.toString()));
		
		return(rc);
	}
}
