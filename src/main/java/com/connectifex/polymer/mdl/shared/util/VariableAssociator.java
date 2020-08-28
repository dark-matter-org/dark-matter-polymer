package com.connectifex.polymer.mdl.shared.util;

import java.util.ArrayList;
import java.util.TreeMap;

import org.dmd.util.exceptions.ResultException;

import com.connectifex.polymer.mdl.server.extended.plastic.PlasticMapping;
import com.connectifex.polymer.mdl.shared.generated.types.PlasticVariable;

/**
 * The VariableAssociator allows for establishing an association between PlasticVariables
 * from an input schema to those in an output schema. This is useful in support of code
 * generation mechanisms.
 */
public class VariableAssociator {
	
	private TreeMap<String, InAndOut>	byName;
	
	// We need this so that we can get the PlasticVariable info to go with the PlasticVariableInfo based
	// on the name of the variable. 
	private PlasticMapping mapping;

	public VariableAssociator(PlasticMapping mapping) {
		byName = new TreeMap<>();
		this.mapping = mapping;
	}
	
	public void addInVariables(TreeMap<String,PlasticVariableInfo> in) throws ResultException {
		for(PlasticVariableInfo pvi: in.values()) {
			InAndOut iao = byName.get(pvi.name());
			if (iao == null) {
				PlasticVariable pv = mapping.getVariableByName(pvi.name());
				if (pv == null) {
					ResultException ex = new ResultException("Could not find variable definition for input schema variable: " + pvi.name());
					ex.setLocationInfo(mapping.getFile(), mapping.getLineNumber());
					throw(ex);
				}
				byName.put(pvi.name(), new InAndOut(pvi.name(), pvi, null, pv));
			}
			else {
				if (iao.input != null)
					throw(new IllegalStateException("You're re-adding the input schema info!"));
				iao.input = pvi;
			}
		}
	}
	
	public void addOutVariables(TreeMap<String,PlasticVariableInfo> out) throws ResultException {
		for(PlasticVariableInfo pvi: out.values()) {
			InAndOut iao = byName.get(pvi.name());
			if (iao == null) {
				PlasticVariable pv = mapping.getVariableByName(pvi.name());
				if (pv == null) {
					ResultException ex = new ResultException("Could not find variable definition for output schema variable: " + pvi.name());
					ex.setLocationInfo(mapping.getFile(), mapping.getLineNumber());
					throw(ex);
				}

				byName.put(pvi.name(), new InAndOut(pvi.name(), null, pvi, pv));
			}
			else {
				if (iao.output != null)
					throw(new IllegalStateException("You're re-adding the output schema info!"));
				iao.output = pvi;
			}
		}
	}
	
	public void validate() throws ResultException {
		for(InAndOut iao: byName.values()) {
			if (iao.input == null) {
				ResultException ex = new ResultException("Missing input schema variable info for: " + iao.name);
				throw(ex);
			}
			if (iao.output == null) {
				ResultException ex = new ResultException("Missing output schema variable info for: " + iao.name);
				throw(ex);
			}
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for(InAndOut iao: byName.values()) {
			sb.append(iao.name);
			if ( (iao.variable.getOptional() != null) && iao.variable.getOptional())
				sb.append("  optional");
			sb.append("\n\n");
			
			sb.append("IN\n");
			iao.input.appendPaths(sb);
			sb.append("OUT\n");
			iao.output.appendPaths(sb);
			sb.append("\n");
		}
		
		return(sb.toString());
	}
	
	class InAndOut {
		String 				name;
		PlasticVariable		variable;
		PlasticVariableInfo input;
		PlasticVariableInfo output;
		
		public InAndOut(String name, PlasticVariableInfo input, PlasticVariableInfo output, PlasticVariable variable) {
			this.name 		= name;
			this.input 		= input;
			this.output 	= output;
			this.variable	= variable;
		}
		
		boolean isOptional() {
			if ((variable.getOptional() != null) && variable.getOptional())
				return(true);
			return(false);
		}
	}
	
	public String getTweaksForOptional() {
		StringBuilder sb = new StringBuilder();
		
		for(InAndOut iao: byName.values()) {
			if (iao.isOptional()) {
				
			}
		}
		
		return(sb.toString());
	}
}
