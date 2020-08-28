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

import org.dmd.util.parsing.ConfigLocation;

/**
 * The ModuleVersion is used to maintain a YANG module with along with its version and tie it
 * back to the ConfigLocation where it can be found.
 */
public class ModuleVersion implements Comparable<Object>{
	
	public static String NO_VERSION = "";
	
	public static String AT = "@";

	private String 			name;
	
	// Note: this may be blank if the Config doesn't include a version
	private String 			version;
	
	private ConfigLocation	location;
	
	public ModuleVersion(ConfigLocation location) {
		this.location = location;
		
		int index = location.getConfigName().indexOf(AT);
		if (index == -1) {
			name = location.getConfigName();
			version = NO_VERSION;
		}
		else {
			name = location.getConfigName().substring(0, index);
			version = location.getConfigName().substring(index+1);
		}
	}
	
	public String toString() {
		return(name + " -- " + version);
	}
	
	public String nameAndRevision() {
		if (version.equals(NO_VERSION))
			return(name);
		
		return(name + AT + version);
	}
	
	public String name() {
		return(name);
	}
	
	public String version() {
		return(version);
	}
	
	public ConfigLocation location() {
		return(location);
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof ModuleVersion) {
			ModuleVersion other = (ModuleVersion) o;
			
			int rc = nameAndRevision().compareTo(other.nameAndRevision());
			
			if (rc == 0)
				return(location().getDirectory().compareTo(other.location().getDirectory()));
			
			return(rc);
		}
		
		return -1;
	}
	
	/**
	 * Returns true if the object is a ModuleVersion and its name and revision and directory location
	 * match this ModuleVersion's info.
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof ModuleVersion) {
			ModuleVersion other = (ModuleVersion) o;
			if (nameAndRevision().compareTo(other.nameAndRevision()) == 0) {
				if (location.getDirectory().equals(other.location().getDirectory()))
					return(true);
			}
		}
		
		return(false);
	}
}
