
import java.util.HashMap;

/**
 * The GroupOrLiteralGroovy class is the groovisized version of the GroupOrLiteral class.
 * It makes use of the PlasticGroupAndNameGroovy class.
 * 
 * We hand copy this code into our src/main/resources/plastic-library and drop the package indication.
 */
public class GroupOrLiteralGroovy {
	private PlasticGroupAndNameGroovy group;
	private String literal;
	
	public GroupOrLiteralGroovy(PlasticGroupAndNameGroovy group, String literal) {
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
