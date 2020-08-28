package com.connectifex.polymer.tools.analyzer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import org.dmd.dmc.DmcNameClashException;
import org.dmd.dmc.DmcValueException;
import org.dmd.dmc.DmcValueExceptionSet;
import org.dmd.dmc.rules.DmcRuleExceptionSet;
import org.dmd.util.exceptions.DebugInfo;
import org.dmd.util.exceptions.ResultException;
import org.dmd.util.formatting.PrintfFormat;

import com.connectifex.polymer.mdl.server.extended.plastic.PlasticPattern;
import com.connectifex.polymer.mdl.tools.plastic.MdlConfigLoader;

public class MorpherAnalyzer {
	
	private static final String GROOVY =".groovy";
	
	private MdlConfigLoader loader;
	private PlasticPattern functionSignature;
	private TreeMap<String,ArrayList<FileInfo>>	morphersByName;
	int longest;
	
	private TreeMap<String,FunctionInfo>	functionsByName;
	

	public MorpherAnalyzer() {
		// TODO Auto-generated constructor stub
	}
	
	public void analyze(String indir, String configDir) throws IOException, ResultException, DmcValueException, DmcRuleExceptionSet, DmcNameClashException, DmcValueExceptionSet {
		loader = new MdlConfigLoader();
		loader.load(configDir, "analysis");
		functionSignature = loader.definitionManager().getPlasticPatternDefinition("functionSignature");
		
		functionsByName = new TreeMap<>();
		
		 File dir = new File(indir);
		 morphersByName = new TreeMap<String, ArrayList<FileInfo>>();
		 
		 if (dir.isDirectory()) {
			 descend(dir);
		 }
		 
		 PrintfFormat format = new PrintfFormat("%-" + longest + "s");
		 
		 int multiVersions = 0;
		 int totalMorphers = 0;
		 for(String key: morphersByName.keySet()) {
			 ArrayList<FileInfo> existing = morphersByName.get(key);
			 if (existing.size() > 1)
				 multiVersions++;
			 
			 totalMorphers += existing.size();
					 
//			 System.out.print(format.sprintf(key) + "  - ");
//			 for(FileInfo info: existing) {
//				 System.out.print(info.release() + " ");
//			 }
//			 System.out.println();
		 }
		 
		 System.out.println("         Total morphers: " + totalMorphers);
		 System.out.println("       Unique  morphers: " + morphersByName.size());
		 System.out.println(" With multiple versions: " + multiVersions);
		 System.out.println();
		 
		 int filesWithUnique = 0;
		 
		 for(String key: morphersByName.keySet()) {
			 ArrayList<FileInfo> existing = morphersByName.get(key);
			 for(FileInfo info: existing) {
				 info.extractFunctions(functionSignature);
				 if (info.hasUniqueFunctions())
					 filesWithUnique++;
				 
//				 info.showUniqueFunctions();
				 
				 Iterator<FunctionInfo> fit = info.getFunctions();
				 while(fit.hasNext()) {
					 FunctionInfo fi = fit.next();
					 FunctionInfo exFunc = functionsByName.get(fi.key());
					 
					 if (exFunc == null) {
						 exFunc = new FunctionInfo(fi);
						 functionsByName.put(exFunc.key(),exFunc);
					 }
					 else {
						 exFunc.addInstance(fi);
					 }
				 }
			 }	 
		 }		
		 
		 System.out.println("\nFiles with unique functions: " + filesWithUnique + "\n");
		 System.out.println("\nFunctions: " + functionsByName.size() + "\n");
		 for(FunctionInfo fi: functionsByName.values()) {
			 System.out.println(fi.toString());
		 }
	}
	
	private void descend(File dir) throws IOException {
		
		
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			for(File file: files) {
				if (file.isDirectory()) {
					descend(file);
				}
				else {
					if (file.getName().endsWith(GROOVY)) {
						
						FileInfo info = new FileInfo(file.getCanonicalPath());
						
						if (info.isPlastic()) {
							if (info.release().equals("classifier")) {
								DebugInfo.debug(info.fullName());
								continue;
							}
							
							
							ArrayList<FileInfo> existing = morphersByName.get(info.name());
							if (existing == null) {
								existing = new ArrayList<>();
								morphersByName.put(info.name(), existing);
							}
							existing.add(info);
							
							if (info.name().length() > longest)
								longest = info.name().length();
						}
//						DebugInfo.debug(info.toString());
					}
				}
			}
		}
	}
	
//	class FileInfo {
//		
//		String fullname;
//		String name;
//		String release;
//		boolean	isPlastic;
//		
//		public FileInfo(String fn) {
//			isPlastic = false;
//			
//			this.fullname = fn;
//			int lastSlash = fn.lastIndexOf('/');
//			String nameAndRelease = fn.substring(lastSlash+1);
//			
//			int lastDash = nameAndRelease.lastIndexOf('-');
//			int lastDot = nameAndRelease.lastIndexOf('.');
//			
//			if (lastDash == -1) {
////				DebugInfo.debug("No last dash: " + fn);
//				name = "?";
//				release = "?";
//				return;
//			}
//						
//			isPlastic = true;
//			name = nameAndRelease.substring(0,lastDash);
//			release = nameAndRelease.substring(lastDash+1,lastDot);
//			
//			if (release.equals("classifier")){
//				isPlastic = false;
//			}
//			
//			if (release.contains("hw")) {
//				isPlastic = false;
//			}
//			
//			
//		}
//		
//		public String toString() {
//			return(name + "  " + release);
//		}
//	}
}
