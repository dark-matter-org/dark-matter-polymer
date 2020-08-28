package com.connectifex.polymer.mdl.server.extended.validation;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.dmd.dms.ClassDefinition;                             // Used in derived constructors - (DMWGenerator.java:284)
import org.dmd.util.exceptions.DebugInfo;
import org.dmd.util.exceptions.ResultException;

import com.connectifex.polymer.mdl.server.extended.plastic.PlasticMapping;
import com.connectifex.polymer.mdl.server.generated.dmw.StringValueIterableDMW;
import com.connectifex.polymer.mdl.server.generated.dmw.ValueSetDMW;
import com.connectifex.polymer.mdl.server.generated.dsd.MdlModuleDefinitionManager;
import com.connectifex.polymer.mdl.shared.generated.dmo.ValueSetDMO;
import com.connectifex.polymer.mdl.shared.generated.types.PlasticVariable;
import com.connectifex.polymer.mdl.shared.generated.types.StringValue;
import com.connectifex.polymer.mdl.util.Manipulator;


public class ValueSet extends ValueSetDMW {
	
	private Set<String> allowed;

    public ValueSet(){
        super();
    }

    public ValueSet(ValueSetDMO dmo, ClassDefinition cd){
        super(dmo,cd);
    }
    
    @Override
    public void performAdditionalValidation(MdlModuleDefinitionManager definitions) throws ResultException {
    	// Note: because we use functionality here to validate default values, we just
    	// ensure that we only run through this code once by checking allowed
    	if (allowed == null) {
	    	allowed = new TreeSet<>();
	    	
	    	StringValueIterableDMW it = getValuesIterable();
	    	while(it.hasNext()) {
	    		StringValue sv = it.next();
	    		if (allowed.contains(sv.getValue())) {
	    			ResultException ex = new ResultException("Duplicate value in value set: " + sv.getValue());
	    			ex.setLocationInfo(getFile(), getLineNumber());
	    			throw(ex);
	    		}
	    		allowed.add(sv.getValue());
	    	}
    	}
    }
    
	@Override
	public String getInitialization(PlasticMapping mapping, PlasticVariable variable) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("    // Generated from: " + DebugInfo.getWhereWeAreNow() + "\n");
		sb.append("    static final Set " + Manipulator.fix(getName().getNameString()) + "Values = [ ");
		Iterator<String> it = allowed.iterator();
		while(it.hasNext()) {
			String value = it.next();
			sb.append("'" + value + "'");
			if (it.hasNext())
				sb.append(", ");
		}
		
		sb.append(" ] as Set\n\n");
		
		return(sb.toString());
	}
	
	@Override
	public String getConstructorInfo(PlasticMapping mapping, PlasticVariable variable) {
		return "";
	}

	@Override
	public String getCall(PlasticMapping mapping, PlasticVariable variable) {
		StringBuilder sb = new StringBuilder();
		
		// Note: if there are any validation calls, the base generation will create a
		// String validationRC = null;
		sb.append("        // Generated from: " + DebugInfo.getWhereWeAreNow() + "\n");
		sb.append("        validate" + Manipulator.capFirstChar(Manipulator.fix(getName().getNameString())) + "(\"" + variable.getName() + "\", inputs['" + variable.getName() + "'])\n");
		
		return(sb.toString());
	}

	@Override
	public String getImplementation(PlasticMapping mapping, PlasticVariable variable) {
		StringBuilder vals = new StringBuilder();
		vals.append(" - Allowed values: " );
		StringValueIterableDMW it = getValuesIterable();
		while(it.hasNext()) {
			StringValue sv = it.next();
			vals.append(sv.getValue());
			if (it.hasNext())
				vals.append(", ");
		}
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("\n");
		sb.append("    // Generated from: " + DebugInfo.getWhereWeAreNow() + "\n");
		sb.append("    private void validate" + Manipulator.capFirstChar(Manipulator.fix(getName().getNameString())) + "(String variableName, String input){\n");
		sb.append("        if (input == null)\n");
		sb.append("            return\n");
		sb.append("        \n");
		sb.append("        if (!" + Manipulator.fix(getName().getNameString()) + "Values.contains(input)){\n");
		sb.append("            abort(\"Invalid value: \" + input + \" for variable: \" + variableName, \"" + vals.toString() +"\");\n");
		sb.append("        }\n");
		sb.append("    }\n\n");
		
		return(sb.toString());
	}

	@Override
	public void checkDefault(PlasticMapping mapping, PlasticVariable variable) throws ResultException {
		if (variable.getDefault() == null)
			return;
		
		if (!allowed.contains(variable.getDefault())) {
			ResultException ex = new ResultException("Invalid default: " + variable.getDefault() + " for variable: "+ variable.getName());
			ex.moreMessages("Validated against ValueSet " + getName() + " - " + getFileAndLine());
			ex.setLocationInfo(mapping.getFile(), mapping.getLineNumber());
			throw(ex);
		}
		
	}

//	@Override
//	public String validate(String variableName, String input) {
//		if (!allowed.contains(input)) {
//			
//			return("");
//		}
//		return(null);
//	}

}

