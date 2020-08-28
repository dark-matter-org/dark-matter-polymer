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

package com.connectifex.polymer.mdl.server.extended.plastic.util;

public class PlasticConstants {

	// The start marker for a plastic variable
	public final static String START_MARKER = "${";
	
	// The end marker for a plastic variable
	public final static String END_MARKER = "}";

	public final static String FAKE 		=	"fake";

	public final static String CLASSIFIERS 	=	"classifiers";
	public final static String LIB 			=	"lib";
	public final static String MORPHERS 	=	"morphers";
	public final static String SCHEMAS 		=	"schemas";
	
	public final static String DEFAULTS_FN 	=	"-defaults-";
	
	public final static String INPUT_FN 	=	"-input-";
	public final static String INPUT 		=	"-input";
	
	public final static String OUTPUT_FN 	=	"-output-";
	public final static String OUTPUT 		=	"-output";
	
	public final static String JSON_EXT 	=	".json";
	public final static String JSON 		=	"json";
	
	public final static String OPEN_CURLY 		=	"{";
	public final static String OPEN_SQUARE 		=	"[";
	
	public final static String ARRAY_INDICATOR = "[]";
	public final static String REMOVE_OPTIONAL = "ReMoVeOpTiOnAl";

	// Used to detect that we're running tests in the build environment
	// In that case, we circumvent the individual tests that we use for 
	// testing in development.
	public final static String BUILD_ENV = "/home/odluser/workspace";

	
}
