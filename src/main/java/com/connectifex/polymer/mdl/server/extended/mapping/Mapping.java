package com.connectifex.polymer.mdl.server.extended.mapping;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.dmd.dmc.DmcValueException;
import org.dmd.dmc.types.CheapSplitter;
import org.dmd.dms.ClassDefinition;                             // Used in derived constructors - (DMWGenerator.java:284)
import org.dmd.dmu.util.json.PrettyJSON;
import org.dmd.util.exceptions.DebugInfo;
import org.dmd.util.exceptions.ResultException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.connectifex.polymer.mdl.server.extended.mapping.util.SchemaBuilder;
import com.connectifex.polymer.mdl.server.generated.dmw.DeviceSupportInfoIterableDMW;
import com.connectifex.polymer.mdl.server.generated.dmw.MappingDMW;
import com.connectifex.polymer.mdl.server.generated.dmw.MappingDirectiveIterableDMW;
import com.connectifex.polymer.mdl.server.generated.dmw.RESTOperationTypeEnumIterableDMW;
import com.connectifex.polymer.mdl.shared.generated.dmo.MappingDMO;
import com.connectifex.polymer.mdl.shared.generated.enums.RESTOperationTypeEnum;
import com.connectifex.polymer.mdl.shared.generated.types.DeviceSupportInfo;
import com.connectifex.polymer.mdl.shared.generated.types.YangModelInfo;


public class Mapping extends MappingDMW {
	
	private static int count = 0;
	protected final static 	String 	OPEN_SQUARE = "[";

	protected final static 	String 	MORPHERS		= "morphers";
	protected final static 	String 	SCHEMAS			= "/schemas";
	
	protected final static 	String 	VENDOR 			= "/vendor";
	protected final static 	String 	OS				= "/os";
	protected final static 	String 	VERSION 		= "/version";

	protected final static 	String 	PUT_INPUT 		= "-put-input-";
	protected final static 	String 	GET_INPUT 		= "-get-input-";
	protected final static 	String 	DELETE_INPUT 	= "-delete-input-";

	protected final static 	String 	PUT_OUTPUT 		= "-put-output-";
	protected final static 	String 	GET_OUPUT 		= "-get-output-";
	protected final static 	String 	DELETE_OUPUT 	= "-delete-output-";

	protected final static 	String 	DEVICE_PREFIX 	= "/device-os-";
	protected final static 	String 	DEVICE_VERSION 	= "1.0";
	
	private SchemaBuilder putSchema;
	private SchemaBuilder getSchema;
	private SchemaBuilder deleteSchema;

    public Mapping(){
        super();
    }

    public Mapping(MappingDMO dmo, ClassDefinition cd){
        super(dmo,cd);
    }

    public Mapping(String file, String[] columns) throws DmcValueException{
        super();
        setTitle(columns[1]);
        setFile(file);
        
        if (columns.length == 2) {
        	// They weren't providing mapping names - handle this for now
        	setName("mapping_" + count);
        	count++;
        }
        else
        	setName(columns[2]);
        
    }
    
    public void generate(String apiname, String outdir) throws ResultException {
    	String fn = null;
    	
    	putSchema = new SchemaBuilder();
    	putSchema.initialize(this);
    	
    	if (getDeviceSupportHasValue()) {
    		DeviceSupportInfoIterableDMW it = getDeviceSupportIterable();
    		while(it.hasNext()) {
    			DeviceSupportInfo dsi = it.next();
    			generateForDevice(apiname, outdir, dsi);
    		}
    	}
    	else {
    		try {
				DeviceSupportInfo dsi = new DeviceSupportInfo(VENDOR, OS, VERSION);
				generateForDevice(apiname, outdir, dsi);
			} catch (DmcValueException e) {
				throw(new ResultException(e));
			}
    	}
    }
    
    private void generateForDevice(String apiname, String outdir, DeviceSupportInfo dsi) throws ResultException {
    	String fn = null;
    	String apidir = outdir + "/" + apiname + SCHEMAS + "/R" + getPlasticVersion();
		
    	RESTOperationTypeEnumIterableDMW it = getOperationsIterable();
    	while(it.hasNext()) {
    		RESTOperationTypeEnum op = it.next();
    		switch(op) {
    		case DELETE:
    			fn = apidir + "/" + apiname + "-" + getPlasticPrefix() + DELETE_INPUT + getPlasticVersion();
    			DebugInfo.debug(fn);
    			break;
    		case GET:
    			fn = apidir + "/" + apiname + "-" + getPlasticPrefix() + GET_INPUT + getPlasticVersion();
    			DebugInfo.debug(fn);
    			break;
    		case PUT:
    			fn = apidir + "/" + apiname + "-" + getPlasticPrefix() + PUT_INPUT + getPlasticVersion();
    			DebugInfo.debug(fn);
    			
    			fn = outdir + "/" + apiname + SCHEMAS + "/" + dsi.getVendor() + "/" + dsi.getDeviceType() + "/R" + dsi.getOsVersion() + "/" + dsi.getVendor() + "-" + dsi.getDeviceType() + "-" + getPlasticPrefix() + PUT_OUTPUT + dsi.getOsVersion();
    			DebugInfo.debug(fn);
    			
    			break;
    		}
    	}
    }
    
	private void writeSchemaFile(String fn, JSONObject example) {
		System.out.println("Writing: " + fn);

		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fn));

			out.write(PrettyJSON.instance().prettyPrint(example, true) + "\n");

			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void addDeviceSupport(String[] columns, int lineNumber) throws DmcValueException, ResultException {
		DeviceSupportInfo dsi = new DeviceSupportInfo(columns[1].toLowerCase(), columns[2].toLowerCase(), columns[3].toLowerCase());
		if (deviceSupportContains(dsi)) {
			ResultException ex = new ResultException("Duplicate value for DEVICE_SUPPORT: " + dsi.toString());
			ex.setLocationInfo(getFile(), lineNumber);
			throw(ex);
		}
		addDeviceSupport(dsi);
	}
    
    /**
     * Adds the supported operations to the mapping.
     * @param columns the second column should contain a space separated list of REST operations.
     */
    public void addOperations(String[] columns, int lineNumber) throws ResultException {
    	if (columns.length == 2) {
    		ArrayList<String> ops = CheapSplitter.split(columns[1], ' ', false, true);
    		for(String op: ops) {
    			RESTOperationTypeEnum opType = RESTOperationTypeEnum.get(op);
    			if (opType == null) {
    	    		ResultException ex = new ResultException("Invalid OPERATIONS type: " + op);
    	    		ex.setLocationInfo(getFile(), lineNumber);
    	    		throw(ex);
    			}
    			if (operationsContains(opType)){
    	    		ResultException ex = new ResultException("Duplicate OPERATIONS type: " + op);
    	    		ex.setLocationInfo(getFile(), lineNumber);
    	    		throw(ex);
    			}
    			addOperations(opType);
    		}
    	}
    	else {
    		ResultException ex = new ResultException("An OPERATIONS row must have 2 columns");
    		ex.setLocationInfo(getFile(), lineNumber);
    		throw(ex);
    	}
    }
    
    public void initJsonStructures() {
    	JSONObject	fromSchema = new JSONObject();
    	
    	MappingDirectiveIterableDMW it = getDirectivesIterable();
    	while(it.hasNext()) {
    		MappingDirective md = it.next();
    		
    		ArrayList<String>	pathParts = md.getFromPath();
    		
    		updateObject(fromSchema, pathParts);
    	}
    }
    
    private void updateObject(JSONObject object, ArrayList<String>	pathParts) {
    	JSONObject current = object;
    	
    	Iterator<String> it = pathParts.iterator();
		while(it.hasNext()){
			String part = it.next();
			
			boolean array = false;
			String elementName = part;
			if (part.contains(OPEN_SQUARE)) {
				array = true;
				int osPos = part.indexOf(OPEN_SQUARE);
				elementName = part.substring(osPos);
			}
			
			try {
				current.get(elementName);
			}
			catch(JSONException e) {
				if (it.hasNext()) {
					if (array) {
						JSONArray a = new JSONArray();
						
					}
					else {
						
					}
				}
				else {
					
				}
			}
		}
    	
    }

    public void addFromModel(String[] columns) throws DmcValueException {
    	if (columns.length == 2)
        	setFromModel(new YangModelInfo(columns[1], "unknown", "unknown"));
    	else if (columns.length == 3)
        	setFromModel(new YangModelInfo(columns[1], columns[2], "unknown"));
    	else
    		setFromModel(new YangModelInfo(columns[1], columns[2], columns[3]));
    }

    public void addToModel(String[] columns) throws DmcValueException {
    	if (columns.length == 2)
    		addToModels(new YangModelInfo(columns[1], "unknown", "unknown"));
    	else if (columns.length == 3)
    		addToModels(new YangModelInfo(columns[1], columns[2], "unknown"));
    	else
    		addToModels(new YangModelInfo(columns[1], columns[2], columns[3]));
    }

    public void addTypeModel(String[] columns) throws DmcValueException {
    	if (columns.length == 1)
    		return;

    	if (columns.length == 2)
    		addTypeModels(new YangModelInfo(columns[1], "unknown", "unknown"));
    	else if (columns.length == 3)
    		addTypeModels(new YangModelInfo(columns[1], columns[2], "unknown"));
    	else
    		addTypeModels(new YangModelInfo(columns[1], columns[2], columns[3]));
    }
}

