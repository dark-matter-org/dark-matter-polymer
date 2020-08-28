package com.connectifex.polymer.mdl.server.extended.plastic.util;

import java.util.ArrayList;

import org.dmd.util.exceptions.ResultException;

import com.connectifex.polymer.mdl.server.extended.plastic.PlasticPattern;
import com.connectifex.polymer.mdl.shared.generated.types.PlasticGroupAndName;



public class GroupLiteralParser {

	public static ArrayList<GroupOrLiteral> parse(PlasticPattern np, String expression) throws ResultException {
		ArrayList<GroupOrLiteral> rc = new ArrayList<>();
		
    		boolean inGroup = false;
    		StringBuilder currentPart = null;
    		 
    		for(int i=0; i<expression.length(); i++){
    			if (expression.charAt(i) == '%'){
    				if (inGroup){
    					PlasticGroupAndName gan = np.getGroupByName(currentPart.toString());
    					if (gan == null){
			    			ResultException ex = new ResultException("Unknown pattern group name: " + currentPart.toString());
			    			ex.moreMessages("In: normalizedForm " + expression);
			    			ex.setLocationInfo(np.getFile(), np.getLineNumber());
			    			throw(ex);
    					}
    					rc.add(new GroupOrLiteral(gan, null));
    					inGroup = false;
    					currentPart = null;
    				}
    				else{
    					if (currentPart != null)
    						rc.add(new GroupOrLiteral(null,currentPart.toString()));
    					
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
    		
    		if (inGroup){
    			ResultException ex = new ResultException("Missing % to close pattern group name insertion: " + expression);
    			ex.moreMessages("In: normalizedForm " + expression);
    			ex.setLocationInfo(np.getFile(), np.getLineNumber());
    			throw(ex);
    		}
    		
    		if (currentPart != null)
				rc.add(new GroupOrLiteral(null, currentPart.toString()));
		
		return(rc);
	}
}
