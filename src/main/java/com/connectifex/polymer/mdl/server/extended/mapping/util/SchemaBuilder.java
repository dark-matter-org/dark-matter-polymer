package com.connectifex.polymer.mdl.server.extended.mapping.util;

import java.util.ArrayList;
import java.util.TreeMap;

import org.dmd.dmc.types.CheapSplitter;
import org.dmd.util.exceptions.DebugInfo;
import org.dmd.util.exceptions.ResultException;

import com.connectifex.polymer.mdl.server.extended.mapping.Map;
import com.connectifex.polymer.mdl.server.extended.mapping.MapAny;
import com.connectifex.polymer.mdl.server.extended.mapping.MapComplex;
import com.connectifex.polymer.mdl.server.extended.mapping.Mapping;
import com.connectifex.polymer.mdl.server.extended.mapping.MappingDirective;
import com.connectifex.polymer.mdl.server.generated.dmw.MappingDirectiveIterableDMW;
import com.connectifex.polymer.mdl.shared.generated.enums.PrimitiveTypeEnum;

/**
 * The SchemaBuilder takes a Mapping instance and creates the schemas for the from/to 
 * sides of the translation.
 */
public class SchemaBuilder {

	private final static char SLASH = '/';

	private final static String STRING = "STRING";
	private final static String BOOLEAN = "BOOLEAN";

	private final static String INT = "int";
	private final static String UINT = "uint";
	private final static String DECIMAL = "dec";

	JsonElement	fromSchema;
	
	TreeMap<String,JsonElement> toSchemas;

	public SchemaBuilder() {
		// TODO Auto-generated constructor stub
	}
	
	public void initialize(Mapping mapping) throws ResultException {
		fromSchema = new JsonElement();
		toSchemas = new TreeMap<>();
		
    	MappingDirectiveIterableDMW it = mapping.getDirectivesIterable();
    	while(it.hasNext()) {
    		MappingDirective md = it.next();
    		
    		if (md instanceof Map) {
    			Map map = (Map) md;
    			
//    			DebugInfo.debug("\n" + map.toOIF());
    			ArrayList<String> pathTokens = CheapSplitter.split(map.getFromAttrPath(), SLASH, false, true);
    			
    			// The insert returns the primitive value at the deepest level of the path
    			JsonElement fromElement = fromSchema.insert(md, pathTokens);
    			fromElement.valueType(type(map.getFromAttrType()));
    			
    			pathTokens = CheapSplitter.split(map.getToAttrPath(), SLASH, false, true);
    			
    			JsonElement toSchema = toSchemas.get(pathTokens.get(0));
    			JsonElement toElement = null;
    			
    			if (toSchema == null) {
    				toSchema = new JsonElement();
    				toElement = toSchema.insert(md, pathTokens);
    				
    				toSchemas.put(toSchema.name(), toSchema);
    			}
    			else {
    				toElement = toSchema.insert(md, pathTokens);
    			}
    			toElement.valueType(type(map.getToAttrType()));
    			
    			String variableName = fromElement.name() + "--" + toElement.name();
    			
    			fromElement.variableName(variableName);
    			toElement.variableName(variableName);
    		}
    		else if (md instanceof MapAny) {
    			DebugInfo.debug("MAP ANY NOT IMPLEMENTED");
    		}
    		else if (md instanceof MapComplex) {
    			DebugInfo.debug("MAP COMPLEX NOT IMPLEMENTED");
    		}
    	}
    	
    	System.out.println("FROM:");
    	System.out.println(fromSchema.getSchema());
    	
    	System.out.println("TO:");
    	for(JsonElement to: toSchemas.values()) {
    		System.out.println(to.getSchema());
    	}
    	
	}
	
	private PrimitiveTypeEnum type(String type) {
		if (type.equalsIgnoreCase(STRING))
			return(PrimitiveTypeEnum.STRING);
		if (type.equalsIgnoreCase(BOOLEAN))
			return(PrimitiveTypeEnum.BOOLEAN);
		
		if (type.startsWith(INT))
			return(PrimitiveTypeEnum.NUMBER);
		if (type.startsWith(UINT))
			return(PrimitiveTypeEnum.NUMBER);
		if (type.startsWith(DECIMAL))
			return(PrimitiveTypeEnum.NUMBER);
		
		return(PrimitiveTypeEnum.STRING);
	}
}
