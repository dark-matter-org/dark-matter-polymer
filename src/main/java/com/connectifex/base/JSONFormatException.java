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

package com.connectifex.base;

import java.util.ArrayList;

import org.dmd.dmc.types.CheapSplitter;
import org.dmd.util.exceptions.ResultException;
import org.json.JSONException;

import com.connectifex.polymer.mdl.server.extended.MdlDefinition;


@SuppressWarnings("serial")
public class JSONFormatException extends ResultException {
	
	private String[] lines;
	

	/**
	 * Creates a new exception that indicates problems with a filled JSON template
	 * @param def the definition that contained the template.
	 * @param jsonError The JSONException with the error location info
	 * @param json the filled template that contains the error.
	 */
	public JSONFormatException(MdlDefinition def, JSONException jsonError, String json) {
		
		super("Malformed JSON");
		setLocationInfo(def.getFile(), def.getLineNumber());
		
		StringBuilder sb = new StringBuilder();
		sb.append(jsonError.getMessage() + ":\n");
		
		ArrayList<String> tokens = CheapSplitter.split(json, '\n', false, false);
		
		int lineNum = 1;
		for(String line: tokens) {
			if (lineNum < 10)
				sb.append("00" + lineNum + "  ");
			else if (lineNum < 100)
				sb.append("0" + lineNum + "  ");
			else
				sb.append(lineNum + "  ");
			
			sb.append(line + "\n");
			lineNum++;
		}
		
		moreMessages(sb.toString());
	}
	
	/**
	 * @return the error message broken down into separate lines.
	 */
	public String[] getLines() {
		return(lines);
	}
}
