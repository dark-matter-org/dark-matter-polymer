package com.connectifex.polymer.mdl.tools.yang.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.TreeMap;

import org.dmd.util.exceptions.ResultException;

/**
 * The YangContext provides central management of where to search for Yang files as well
 * the Yang modules or submodules that have already been loaded. This allows for single
 * loading of Yang include/import files.
 */
public class YangContext {

	private YangFinder finder;
	
	// Set to true once we've triggered the findConfigs()
	private boolean		searchComplete;
	
	private boolean		haveSourceDirs;
	
	// Key: The ModuleVersion of the loaded module - 
	private TreeMap<ModuleVersion, YangStructure>	loaded;
	
	public YangContext() {
		finder = new YangFinder();
		loaded = new TreeMap<>();
	}
	
	/**
	 * @param moduleName the name of YANG module without revision indication.
	 * @return the revisions of the module that have been found, or null if none are available.
	 * @throws IOException 
	 * @throws ResultException 
	 */
	public TreeMap<String,ModuleVersion> getVersions(String moduleName) throws ResultException, IOException{
		if (!haveSourceDirs)
			throw(new IllegalStateException("You need to addYangDirectory() prior to looking for modules!"));
		
		if (!searchComplete) {
			finder.findConfigs();
			searchComplete = true;
		}
		
		return(finder.getVersions(moduleName));
	}	
	
	/**
	 * Adds a root directory to search for Yang definitions. We will descend
	 * from this directory recursively to find .yang files.
	 * @param directoryName the fully qualified name of a directory.
	 */
	public void addYangDirectory(String directoryName) {
		finder.addSourceDirectory(directoryName);
		haveSourceDirs = true;
	}
	
	/**
	 * Let's you retrieve a module by name and, potentially, by version.
	 * @param name the name of the module
	 * @param version the version or null if no version was specified
	 * @return the ModuleVersion with a handle to the ConfigLocation
	 * @throws IOException 
	 * @throws ResultException 
	 */
	public ModuleVersion getModule(String name, String version) throws ResultException, IOException {
		if (!searchComplete) {
			finder.findConfigs();
			searchComplete = true;
		}
		
		return(finder.getModule(name, version));
	}
	
	
	
	/**
	 * Save the fact that the specified module has been loaded.
	 * @param version the version of the module
	 * @param ys the YangStructure that was parsed from it.
	 */
	public void loaded(ModuleVersion version, YangStructure ys) {
		loaded.put(version, ys);
	}
	
	public boolean isLoaded(ModuleVersion mv) {
		if (loaded.containsKey(mv))
			return(true);
		return(false);
	}
	
	/**
	 * @return all modules that have been loaded.
	 */
	public Iterator<YangStructure> getAllLoadedModules() {
		return(loaded.values().iterator());
	}
	
	public YangStructure getLoaded(ModuleVersion version) {
		return(loaded.get(version));
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for(ModuleVersion version: loaded.keySet()) {
			
			sb.append(version + "\n");
		}
		
		return(sb.toString());
	}
	
}
