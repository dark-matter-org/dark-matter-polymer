package com.connectifex.polymer.mdl.tools.yang.util;

import java.util.Iterator;

import org.dmd.util.formatting.PrintfFormat;

import com.connectifex.polymer.mdl.server.extended.plastic.util.PlasticGlobals;

public class VariableInfo {

	private String 			name;
	private String			variableInsert;
	private YangStructure	yang;
	
	private boolean			mandatory;
	private String			defaultValue;
	private String			units;
	private String			description;
	private String			type;
	
	// If the variable is associated with a choice, it's here
	private ChoiceInfo		choice;
	
	public VariableInfo(String name, YangStructure yang) {
		this.name 	= name;
		this.yang 	= yang;
		variableInsert = "${" + name + "}";
		
		YangAttribute mandatoryAttr = yang.singleAttribute(YangConstants.MANDATORY);
		if (mandatoryAttr != null)
			mandatory = true;
			
		YangAttribute defaultV = yang.singleAttribute(YangConstants.DEFAULT);
		if (defaultV != null)
			defaultValue = defaultV.value();
		
		YangAttribute unitsAttr = yang.singleAttribute(YangConstants.UNITS);
		if (unitsAttr != null)
			units = unitsAttr.value();
		
		YangAttribute descriptionAttr = yang.singleAttribute(YangConstants.DESCRIPTION);
		if (descriptionAttr != null)
			description = descriptionAttr.value();

		// Type information is tricky. In some cases, it's just an attribute on the 
		// structure. In other cases, it's a child of the structure. So we handle these
		// cases differently.
		YangAttribute typeAttr = yang.singleAttribute(YangConstants.TYPE);
		if (typeAttr != null)
			type = typeAttr.value();
		else {
			setTypeFromTypeDef();
			
			if (type == null)
				PlasticGlobals.instance().trace("WARNING: no type for: " + yang.getFullyQualifiedName() + " structure type: " + yang.type());
		}
	}
	
	public void setChoice(ChoiceInfo info) {
		choice = info;
	}
	
	/**
	 * This is for cases where we have something like 
	 *      type leafref {
     *        path "/l3vpn-svc/vpn-profiles/"+
     *        "valid-provider-identifiers/cloud-identifier/id";
     *      }
     * Not just a simple attribute.
	 * @return a string for the type or null if we couldn't get it
	 */
	private void setTypeFromTypeDef() {
		Iterator<YangStructure> it = yang.children();
		while(it.hasNext()) {
			YangStructure child = it.next();
			if (child.type().equals(YangConstants.TYPE)) {
				if (child.name().equals(YangConstants.LEAFREF)) {
					YangAttribute path = child.singleAttribute(YangConstants.PATH);
					if (path == null) {
						PlasticGlobals.instance().trace("WARNING: could not get path  for leafref: " + yang.getFullyQualifiedName() + " structure type: " + yang.type());
					}
					else {
						type = "leafref - " + path.value();
					}
				}
				else if (child.name().equals(YangConstants.IDENTITYREF)) {
					YangAttribute base = child.singleAttribute(YangConstants.BASE);
					if (base == null) {
						PlasticGlobals.instance().trace("WARNING: could not get base  for identityref: " + yang.getFullyQualifiedName() + " structure type: " + yang.type());
					}
					else {
						type = "identityref - " + base.value();
					}
				}
				else if (child.name().equals(YangConstants.STRING)) {
					YangAttribute length = child.singleAttribute(YangConstants.LENGTH);
					YangAttribute pattern = child.singleAttribute(YangConstants.PATTERN);
					YangAttribute modifier = child.singleAttribute(YangConstants.MODIFIER);
					StringBuilder sb = new StringBuilder("string - ");
					
					if (length != null)
						sb.append("length: " + length.value() + " ");
					
					if (pattern != null)
						sb.append("pattern: " + pattern.value() + " ");
					
					if (modifier != null)
						sb.append("modifier: " + modifier.value() + " ");
					
					type = sb.toString();
					
				}
				else if (child.name().equals(YangConstants.DECIMAL64)) {
					YangAttribute fraction 	= child.singleAttribute(YangConstants.FRACTION_DIGITS);
					YangAttribute range 	= child.singleAttribute(YangConstants.RANGE);
					StringBuilder sb = new StringBuilder("decimal64 - ");
					if (range != null)
						sb.append("range: " + range.value() + " ");
					
					if (fraction != null)
						sb.append("fraction: " + fraction.value());
					
					type = sb.toString();
				}
				else if (child.name().equals(YangConstants.ENUMERATION)) {
					setTypeAsEnum(child);
				}
				else if ( (child.name().startsWith("uint")) || child.name().startsWith("int")) {
					YangAttribute range 	= child.singleAttribute(YangConstants.RANGE);
					StringBuilder sb = new StringBuilder(child.name());
					
					if (range != null)
						sb.append(" - range: " + range.value());
					
					type = sb.toString();
					
				}
			}
		}
	}
	
	private void setTypeAsEnum(YangStructure enumdef) {
		StringBuilder sb = new StringBuilder();
		sb.append("enum - ");

		Iterator<YangStructure> it = enumdef.children();
		while(it.hasNext()) {
			YangStructure value = it.next();
			sb.append(value.name());
			if (it.hasNext())
				sb.append("/");
		}
		
		type = sb.toString();
	}
	
	public boolean isMandator() {
		return(mandatory);
	}
	
	public boolean hasDefault() {
		if (defaultValue == null)
			return(false);
		return(true);
	}
	
	/**
	 * @return our unique name based on location in the hierarchy
	 */
	public String name() {
		return(name);
	}
		
	/**
	 * @return the plastic variable specification with ${ }
	 */
	public String variableInsert() {
		return(variableInsert);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(variableInsert + "\n");
		
		YangAttribute mandatory = yang.singleAttribute(YangConstants.MANDATORY);
		if (mandatory != null)
			sb.append("  mandatory\n");
			
		YangAttribute defaultV = yang.singleAttribute(YangConstants.DEFAULT);
		if (defaultV != null)
			sb.append("  default: " + defaultV.value() + "\n");
		
		YangAttribute units = yang.singleAttribute(YangConstants.UNITS);
		if (units != null)
			sb.append("  units: " + units.value() + "\n");
		
		YangAttribute description = yang.singleAttribute(YangConstants.DESCRIPTION);
		if (description != null)
			sb.append("\n  description: " + description.value() + "\n");
		
		return(sb.toString());
	}

	public String toVariableFormat(PrintfFormat format) {
		StringBuilder sb = new StringBuilder();
		sb.append("variables ");
		
		sb.append(format.sprintf(variableInsert));
		
		if (defaultValue != null)
			sb.append(" default=\"" + defaultValue + "\"");
		
		if (description!= null)
			sb.append(" note=\"" + description +"\"");
		
		if (type!= null)
			sb.append(" type=\"" + type +"\"");
		
		if (units!= null)
			sb.append(" units=\"" + units +"\"");
		
		// We don't add this is there's only a single case
		if (choice != null && choice.moreThanOneCase())
			sb.append(" choice=" + choice.uniqueName() + "");
			
		return(sb.toString());
	}

}
