package com.connectifex.polymer.tools.analyzer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import org.dmd.dmc.types.CheapSplitter;
import org.dmd.util.exceptions.DebugInfo;
import org.dmd.util.exceptions.ResultException;

import com.connectifex.polymer.mdl.server.extended.plastic.PlasticPattern;

public class FileInfo {
	
	private final static String TWEAK_INPUTS = "tweakInputs";
	private final static String TWEAK_VALUES = "tweakValues";
	private final static String TWEAK_PARSED = "tweakParsed";
	
	private final static String RETURN_TYPE_AND_NAME = "returnTypeAndFunctionName";
	
	// Had originally excluded functions with a void return, but these turned out to
	// be primary validation functions that abort() when values don't pass checks
//	private final static String VOID = "void";
	private final static String PRIVATE = "private";
	private final static String RETURN = "return";
	
	private boolean tweaksInputs;
	private boolean tweaksValues;
	private boolean tweaksParsed;
	
	private TreeSet<String>					uniqueFunctions;
	private TreeMap<String,FunctionInfo>	functionsByName;

	private	String 	fullName;
	private String 	name;
	private String 	release;
	private boolean	isPlastic;
	
	public FileInfo(String fn) {
		isPlastic = false;
		uniqueFunctions = new TreeSet<>();
		functionsByName	= new TreeMap<>();
		
		this.fullName = fn;
		int lastSlash = fn.lastIndexOf('/');
		String nameAndRelease = fn.substring(lastSlash+1);
		
		int lastDash = nameAndRelease.lastIndexOf('-');
		int lastDot = nameAndRelease.lastIndexOf('.');
		
		if (lastDash == -1) {
			name = "?";
			release = "?";
			return;
		}
					
		isPlastic = true;
		name = nameAndRelease.substring(0,lastDash);
		release = nameAndRelease.substring(lastDash+1,lastDot);
		
		if (release.equals("classifier")){
			isPlastic = false;
		}
		
		if (release.contains("hw")) {
			isPlastic = false;
		}
	}
	
	public boolean isPlastic() {
		return(isPlastic);
	}
	
	public String fullName() {
		return(fullName);
	}
	
	public String name() {
		return(name);
	}
	
	public String release() {
		return(release);
	}
	
	public String toString() {
		return(name + "  " + release);
	}

	public void extractFunctions(PlasticPattern functionSignature) throws ResultException, IOException {
		FileReader reader = null;
		
		try {
			reader = new FileReader(fullName);
		} catch (FileNotFoundException e) {
			ResultException ex = new ResultException("File not found: " + fullName);
			throw(ex);
		}
		parse(reader, functionSignature);
		
		reader.close();
	}
	
	public boolean hasUniqueFunctions() {
		if (uniqueFunctions.size() > 0)
			return(true);
		return(false);
	}
	
	public void showUniqueFunctions() {
		if (uniqueFunctions.size() > 0) {
			System.out.println(name);
			for(String f: uniqueFunctions)
				System.out.println("    " + f);
			
			System.out.println();
		}
	}
	
	public Iterator<FunctionInfo> getFunctions(){
		return(functionsByName.values().iterator());
	}
	
	private void parse(FileReader reader, PlasticPattern functionSignature) throws ResultException {
		LineNumberReader in = new LineNumberReader(reader);
		
        String line = null;
        try {
			while ((line = in.readLine()) != null) {
				String trimmed = line.trim();
				
				if (functionSignature.matches(trimmed)) {
					
					// Set flags to indicate if the function os one or our standards
					if (trimmed.contains(TWEAK_INPUTS))
						tweaksInputs = true;
					else if (trimmed.contains(TWEAK_VALUES))
						tweaksValues = true;
					else if (trimmed.contains(TWEAK_PARSED))
						tweaksParsed = true;
					else {
						ArrayList<String> tokens = CheapSplitter.split(functionSignature.getGroupValue(RETURN_TYPE_AND_NAME), ' ', false, true);
						if (tokens.size() == 2) {
							// Don't bother with void functions for now
//							if (tokens.get(0).equals(VOID))
//								continue;
							if (tokens.get(0).equals(PRIVATE))
								continue;
							if (tokens.get(0).equals(RETURN))
								continue;
							
							uniqueFunctions.add(trimmed);
							
							FunctionInfo info = new FunctionInfo(tokens.get(1), tokens.get(0), fullName);
							functionsByName.put(info.key(), info);
//							DebugInfo.debug(trimmed);
						}
						
						if (tokens.size() > 2)
							DebugInfo.debug(trimmed);
					}
					
				}
			}
        
		} catch (Exception e) {
			ResultException ex = new ResultException(e);
			ex.moreMessages("Problem reading from file: " + fullName + " line: " + in.getLineNumber());
			throw(ex);
		}
        
		
        try {
			in.close();
		} catch (IOException e) {
			ResultException ex = new ResultException(e);
			ex.moreMessages("Problem closing file: " + fullName);
			throw(ex);
		}
		

	}
}
