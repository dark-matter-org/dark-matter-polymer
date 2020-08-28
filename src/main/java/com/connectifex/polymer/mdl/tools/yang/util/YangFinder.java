// Copyright 2020 connectifex
// 
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
// 
//      http://www.apache.org/licenses/LICENSE-2.0
// 
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//

package com.connectifex.polymer.mdl.tools.yang.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.TreeMap;

import org.dmd.util.exceptions.DebugInfo;
import org.dmd.util.exceptions.ResultException;
import org.dmd.util.parsing.ConfigFinder;
import org.dmd.util.parsing.ConfigLocation;

/**
 * The YangFinder extends the base ConfigFinder to find .yang modules and provides some
 * useful mechanisms to allow for resolving import statements.
 */
public class YangFinder extends ConfigFinder {
	
	// Key: module name without version
	// Value:  Key: version
	//         Value: the module version with ConfigLocation
	private TreeMap<String,TreeMap<String,ModuleVersion>>	 byName;

	public YangFinder() {
		super(".yang");
	}
	
	public TreeMap<String,TreeMap<String,ModuleVersion>>	getYangModules(){
		return(byName);
	}
	
	/**
	 * @param moduleName the name of YANG module without revision indication.
	 * @return the revisions of the module that have been found, or null if none are available.
	 */
	public TreeMap<String,ModuleVersion> getVersions(String moduleName){
		if (byName == null)
			throw(new IllegalStateException("The findConfigs() method has not been called yet!"));
		
		return(byName.get(moduleName));
	}
	
	@Override
	public void findConfigs() throws ResultException, IOException {
		// If we haven't already searched for configs, proceed - otherwise,
		// we just use what we already have
		if (byName == null) {
			byName = new TreeMap<>();
			
			super.findConfigs();
			
			Iterator<ConfigLocation> it = getLocations();
			while(it.hasNext()) {
				ConfigLocation c = it.next();
				
				ModuleVersion mv = new ModuleVersion(c);
				
				TreeMap<String,ModuleVersion> existing = byName.get(mv.name());
				if (existing == null) {
					existing = new TreeMap<>();
					existing.put(mv.version(), mv);
					byName.put(mv.name(), existing);
//					DebugInfo.debug("Adding initial version for: " + c.getConfigName() + "  version: " + mv.version());
				}
				else {
					existing.put(mv.version(), mv);
//					DebugInfo.debug("Adding additional version for: " + c.getConfigName() + "  version: " + mv.version());
				}
				
			}		
		}
	}
		
	/**
	 * Let's you retrieve a module by name and, potentially, by version.
	 * @param name the name of the module
	 * @param version the version or null if no version was specified
	 * @return the ModuleVersion with a handle to the ConfigLocation
	 */
	public ModuleVersion getModule(String name, String version) {
		ModuleVersion rc = null;
		
		TreeMap<String,ModuleVersion> existing = byName.get(name);
		if (existing == null)
			return(rc);
		
		if (version == null) {
			// no version was specified, try to get the NO_VERSION version
			rc = existing.get(ModuleVersion.NO_VERSION);
			
			if (rc == null) {
				// We don't have a NO_VERSION version, so just get the latest version
				rc = existing.lastEntry().getValue();
			}
		}
		else
			rc = existing.get(version);
		
		return(rc);
	}
}
