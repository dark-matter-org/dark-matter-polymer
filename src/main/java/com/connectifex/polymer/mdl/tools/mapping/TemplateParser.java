package com.connectifex.polymer.mdl.tools.mapping;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.TreeMap;

import org.dmd.dmc.DmcValueException;
import org.dmd.util.exceptions.ResultException;

import com.connectifex.polymer.mdl.server.extended.mapping.EnumMapping;
import com.connectifex.polymer.mdl.server.extended.mapping.Map;
import com.connectifex.polymer.mdl.server.extended.mapping.MapAny;
import com.connectifex.polymer.mdl.server.extended.mapping.MapComplex;
import com.connectifex.polymer.mdl.server.extended.mapping.Mapping;
import com.connectifex.polymer.mdl.server.extended.mapping.MappingDirective;
import com.connectifex.polymer.mdl.shared.generated.enums.MappingKeywordEnum;

public class TemplateParser {
	
	// Names for the various columns - this depends on the keyword
	private final static String H_KEYWORD 		= "Keyword";
	private final static String H_URL 			= "URL";
	private final static String H_MODEL 		= "Model Name";
	private final static String H_REV 			= "Revision";
	private final static String H_USECASE 		= "Usecase Name";
	private final static String H_VERSION 		= "Version";
	private final static String H_TITLE			= "Title";
	private final static String H_MNAME			= "Mapping Name";
	private final static String H_PPREFIX		= "Plastic Prefix";
	private final static String H_PVERSION		= "Plastic Version";
	private final static String H_OPERATIONS	= "Rest Operations";
	private final static String H_URL_PREFIX	= "URL Prefix";
	private final static String H_ENAME			= "Enum Name";
	private final static String H_CONVERSION	= "Conversion Notes	";
	private final static String H_FPATH			= "From Path";
	private final static String H_TPATH			= "To Path";
	private final static String H_FPARAM		= "From Param";
	private final static String H_TPARAM		= "To Param";
	private final static String H_FTYPE			= "From Type";
	private final static String H_TTYPE			= "To Type";
	private final static String H_SEPARATOR		= "Separator";
	private final static String H_NOTE			= "Note";
	private final static String H_FVALUE		= "From Value";
	private final static String H_TVALUE		= "To Value";
	
	private final static String H_VENDOR		= "Vendor";
	private final static String H_DEVICE_TYPE	= "Device Type";
	private final static String H_OS_VERSION	= "OS Version";
	
	private final static String NOT_FOUND_STRING	= "Not Found";
	private final static String NOT_FOUND			= "NOT_FOUND";
	
	private final static String EMPTY				= "";
	
	private TreeMap<MappingKeywordEnum,RequiredColumns>	columnsByKeyword;
	
	private LineNumberReader 	in;
	private String				currentFile;
	private ArrayList<Mapping>	mappings;
	
	private MapComplex			currentComplex	= null;
	private MapAny				currentAny		= null;

	private int					directiveNumber;
	
	// Traces at the line level
	private boolean				lineTrace;
	
	// Traces at the object level
	private boolean				trace;
	
	private boolean				warnings;
	
	private	boolean				firstWarning;
	
	public TemplateParser() {
		
		columnsByKeyword = new TreeMap<>();
		columnsByKeyword.put(MappingKeywordEnum.CONVERSION, 
				new RequiredColumns(MappingKeywordEnum.CONVERSION, 2, 				H_KEYWORD, H_CONVERSION));
		
		columnsByKeyword.put(MappingKeywordEnum.DOC_VERSION, 
				new RequiredColumns(MappingKeywordEnum.DOC_VERSION, 2, 				H_KEYWORD, H_VERSION));
		
		columnsByKeyword.put(MappingKeywordEnum.ENUM_MAPPING, 
				new RequiredColumns(MappingKeywordEnum.ENUM_MAPPING, 2, 			H_KEYWORD, H_ENAME));
		
		columnsByKeyword.put(MappingKeywordEnum.FROM_MODEL, 
				new RequiredColumns(MappingKeywordEnum.FROM_MODEL, "You should specify the model name and version", 2, 4, 				H_KEYWORD, H_URL, H_MODEL, H_REV));
		
		// Has no specific headers
		columnsByKeyword.put(MappingKeywordEnum.HEADER, 
				new RequiredColumns(MappingKeywordEnum.HEADER, 2, 8));
		
		columnsByKeyword.put(MappingKeywordEnum.MAP, 
				new RequiredColumns(MappingKeywordEnum.MAP, 7, 8, 					H_KEYWORD, H_FPATH, H_FPARAM, H_FTYPE, H_TPATH, H_TPARAM, H_TTYPE, H_ENAME));
		
		columnsByKeyword.put(MappingKeywordEnum.MAP_ANY, 
				new RequiredColumns(MappingKeywordEnum.MAP_ANY, 4, 8, 				H_KEYWORD, H_FPATH, H_FPARAM, H_FTYPE, H_TPATH, H_TPARAM, H_TTYPE, H_ENAME));
		
		// NOTE: Only the first row of 2 or more MAP_COMPLEX rows will have all values - subsequent rows only have 4
		columnsByKeyword.put(MappingKeywordEnum.MAP_COMPLEX, 
				new RequiredColumns(MappingKeywordEnum.MAP_COMPLEX, 4, 7, 			H_KEYWORD, H_FPATH, H_FPARAM, H_FTYPE, H_TPATH, H_TPARAM, H_TTYPE, H_ENAME));
		
		columnsByKeyword.put(MappingKeywordEnum.MAP_COMPLEX_SEPARATOR, 
				new RequiredColumns(MappingKeywordEnum.MAP_COMPLEX_SEPARATOR, 2, 	H_KEYWORD, H_SEPARATOR));
		
		columnsByKeyword.put(MappingKeywordEnum.MAPPING, 
				new RequiredColumns(MappingKeywordEnum.MAPPING, "You should provide a mapping name", 2, 3, 			H_KEYWORD, H_TITLE, H_MNAME));
		
		columnsByKeyword.put(MappingKeywordEnum.PLASTIC_PREFIX, 
				new RequiredColumns(MappingKeywordEnum.PLASTIC_PREFIX, 2, H_KEYWORD, H_PPREFIX));
		
		columnsByKeyword.put(MappingKeywordEnum.PLASTIC_VERSION, 
				new RequiredColumns(MappingKeywordEnum.PLASTIC_VERSION, 2, H_KEYWORD, H_PVERSION));
		
		columnsByKeyword.put(MappingKeywordEnum.OPERATIONS, 
				new RequiredColumns(MappingKeywordEnum.OPERATIONS, 2, H_KEYWORD, H_OPERATIONS));
		
		columnsByKeyword.put(MappingKeywordEnum.URL_PREFIX, 
				new RequiredColumns(MappingKeywordEnum.URL_PREFIX, 2, H_KEYWORD, H_URL_PREFIX));
		
		columnsByKeyword.put(MappingKeywordEnum.NOTE, 
				new RequiredColumns(MappingKeywordEnum.NOTE, 2, 					H_KEYWORD, H_NOTE));
		
		columnsByKeyword.put(MappingKeywordEnum.TO_MODEL, 
				new RequiredColumns(MappingKeywordEnum.TO_MODEL, "You should specify the model name and version", 2, 4, 				H_KEYWORD, H_URL, H_MODEL, H_REV));
		
		columnsByKeyword.put(MappingKeywordEnum.TYPE_MODEL, 
				new RequiredColumns(MappingKeywordEnum.TYPE_MODEL, "You should specify the URL, model name and version", 1, 4, 				H_KEYWORD, H_URL, H_MODEL, H_REV));
		
		columnsByKeyword.put(MappingKeywordEnum.UNKNOWN, 
				new RequiredColumns(MappingKeywordEnum.UNKNOWN, 2));
		
		columnsByKeyword.put(MappingKeywordEnum.USE_CASE, 
				new RequiredColumns(MappingKeywordEnum.USE_CASE, 2, 				H_KEYWORD, H_NOTE));
		
		columnsByKeyword.put(MappingKeywordEnum.VALUE, 
				new RequiredColumns(MappingKeywordEnum.VALUE, 3,					H_KEYWORD, H_FVALUE, H_TVALUE));
		
		columnsByKeyword.put(MappingKeywordEnum.DEVICE_SUPPORT, 
				new RequiredColumns(MappingKeywordEnum.DEVICE_SUPPORT, 4,			H_KEYWORD, H_VENDOR, H_DEVICE_TYPE, H_OS_VERSION));
		
		
	}
	
	public void lineTrace(boolean flag) {
		lineTrace = flag;
	}
	
	public void trace(boolean flag) {
		trace = flag;
	}
	
	public void warnings(boolean flag) {
		warnings = flag;
	}
	
	/**
	 * Parses the CSV form of the template. Doing this so that we can provide error indications
	 * with line/row number indications; the XLSX POI implementation skips blank rows.
	 * @param fn the name of the file to parse.
	 * @throws IOException
	 * @throws ResultException 
	 * @throws DmcValueException 
	 */
	public ArrayList<Mapping> parseCSV(String fn) throws IOException, ResultException, DmcValueException {
		firstWarning	= true;
		currentFile 	= fn;
		in = new LineNumberReader(new FileReader(fn));
		
		mappings = new ArrayList<>();
		directiveNumber	= 1;
		
		Mapping 					currentMapping 		= null;
		MappingDirective			currentDirective	= null;
		MapComplex					currentComplex		= null;
		EnumMapping					currentEnum			= null;
		
		ArrayList<MappingDirective>	directives			= null;
		String[]					useCaseInfo;
		String[]					docVersionInfo;
		
		String line = null;
        while ((line = in.readLine()) != null) {
        	line = line.trim();
        	lineTrace("Line: " + in.getLineNumber());
        	if (line.length() == 0)
        		continue;
        	
        	String[] columns = line.split(",");
        	for(int i=0; i<columns.length; i++) {
        		lineTrace("    " + columns[i]);
        	}
        	
        	if (columns.length == 0)
        		continue;
        	
        	MappingKeywordEnum keyword = MappingKeywordEnum.UNKNOWN;
        	try {
        		keyword = MappingKeywordEnum.get(columns[0]);
        	} catch(Exception ex) {
        		throw(ex);
        	}
        	
        	if (keyword == null) {
        		if (columns[0].equals("KEY_WORD")) {
        			warning("Unknown keyword: " + columns[0]);
        			continue;
        		}
        		if (columns[0].equals(EMPTY)) {
        			warning("Missing keyword in first column.");
        			continue;
        		}
        		exception("Unknown keyword: " + columns[0]);
        	}
        	
        	if (keyword == MappingKeywordEnum.UNKNOWN)
        		exception("The keyword UNKNOWN is meant for internal use only.");
        	
        	RequiredColumns reqCols = columnsByKeyword.get(keyword);
        	
        	if (columns.length < reqCols.min) {
        		warning("Too few columns for keyword:" + keyword);
        		continue;
        	}

        	
        	if (columns.length > reqCols.max) {
        		warning("Too many columns for keyword:" + keyword + " - ignoring additional columns");
        	}
        	
        	if (warnings)
        		reqCols.showWarningIfNeeded(columns);
        	
        	switch(keyword) {
        	case CONVERSION:
        		if (currentDirective == null)
        			exception("A CONVERSION note should come after a MAP, MAP_ANY or MAP_COMPLEX line.");
        		currentDirective.addConversionNotes(columns[1]);
        		break;
        	case DOC_VERSION:
        		docVersionInfo = columns;
        		break;
        	case ENUM_MAPPING:
        		currentEnum = new EnumMapping();
        		try {
        			currentEnum.setName(columns[1]);
        		}
        		catch(DmcValueException ex) {
        			warning("The name of an enum must be a single token starting with an alpha character.");
        			currentEnum.setName("enum" + in.getLineNumber());
        			continue;
        		}
        		break;
        	case FROM_MODEL:
        		if (currentMapping == null)
        			exception("A FROM_MODEL must be specified within the context of a MAPPING.");
        		currentMapping.addFromModel(columns);
        		break;
        	case HEADER:
        		// Nothing to do
        		break;
        	case MAP:
        		if (currentMapping == null)
        			exception("A " + columns[0] + " keyword must follow a preceeding MAPPING definition.");
        		
        		Map map = new Map(columns, currentFile, in.getLineNumber());
        		currentDirective = map;
        		directives.add(currentDirective);
        		
        		currentMapping.addDirectives(currentDirective);
        		break;
        	case MAP_ANY:
        		if (currentMapping == null)
        			exception("A " + columns[0] + " keyword must follow a preceeding MAPPING definition.");
        		
        		MapAny mapany = new MapAny(columns, currentFile, in.getLineNumber());
        		currentDirective = mapany;
        		directives.add(currentDirective);
        		
        		currentMapping.addDirectives(currentDirective);
        		break;
        	case MAP_COMPLEX:
        		// A MAP_COMPLEX will end with either a separator or a MAP_COMPLEX key word followed by only 3 
        		// columns.
        		// The first statement of a new MAP_COMPLEX will have 7 columns
        		if (currentMapping == null)
        			exception("A " + columns[0] + " keyword must follow a preceeding MAPPING definition.");
        		
        		if (columns.length == 4) {
        			if (currentComplex == null)
        				exception("The first row of a MAP_COMPLEX specification must have 7 columns. This row has on 4 columns.");
        			
        			currentComplex.addField(columns);
        		}
        		else {
        			// 7 columns - this is a new complex mapping
        			currentComplex = new MapComplex(columns, currentFile, in.getLineNumber());
            		currentDirective = currentComplex;
            		directives.add(currentDirective);
            		
            		currentMapping.addDirectives(currentDirective);
        		}
        		break;
        	case MAP_COMPLEX_SEPARATOR:
        		if (currentComplex == null)
        			exception("The MAP_COMPLEX_SEPARATOR must come at the end of a series of MAP_COMPLEX rows");
        		
        		currentComplex.setComplexSeparator(columns[1]);
        		
        		// This terminates the current complex mapping
        		currentComplex = null;
        		break;
        	case MAPPING:
        		currentMapping = new Mapping(fn, columns);
        		mappings.add(currentMapping);
        		directives = new ArrayList<>();
        		break;
        	case PLASTIC_PREFIX:
        		if (currentMapping == null)
        			exception("A " + columns[0] + " keyword must follow a preceeding MAPPING definition.");
        		currentMapping.setPlasticPrefix(columns[1]);
        		break;
        	case PLASTIC_VERSION:
        		if (currentMapping == null)
        			exception("A " + columns[0] + " keyword must follow a preceeding MAPPING definition.");
        		currentMapping.setPlasticVersion(columns[1]);
        		break;
        	case DEVICE_SUPPORT:
        		if (currentMapping == null)
        			exception("A " + columns[0] + " keyword must follow a preceeding MAPPING definition.");
        		currentMapping.addDeviceSupport(columns, in.getLineNumber());
        		break;
        	case OPERATIONS:
        		if (currentMapping == null)
        			exception("A " + columns[0] + " keyword must follow a preceeding MAPPING definition.");
        		currentMapping.addOperations(columns, in.getLineNumber());
        		break;
        	case URL_PREFIX:
        		if (currentMapping == null)
        			exception("A " + columns[0] + " keyword must follow a preceeding MAPPING definition.");
        		currentMapping.setUrlPrefix(columns[1]);
        		break;
        	case NOTE:
        		break;
        	case TO_MODEL:
        		if (currentMapping == null)
        			exception("A TO_MODEL must be specified within the context of a MAPPING.");
        		currentMapping.addToModel(columns);
        		break;
        	case TYPE_MODEL:
        		if (currentMapping == null)
        			exception("A TYPE_MODEL must be specified within the context of a MAPPING.");
        		currentMapping.addTypeModel(columns);
        		break;
        	case UNKNOWN:
        		// Caught above
        		break;
        	case USE_CASE:
        		useCaseInfo = columns;
        		break;
        	case VALUE:
        		if (currentEnum == null)
        			exception("You've specified a VALUE with no prior ENUM_MAPPING");
        		currentEnum.addEnumValue(columns);
        		break;
        	}
        }
		
		in.close();
		
		for(Mapping m: mappings) {
			trace(m.toOIF() + "\n");
		}
		
		for(MappingDirective d: directives) {
			trace(d.toOIF() + "\n");
		}
		
		return(mappings);
	}
	
	/** 
	 * Convenience function to throw exception with location info.
	 * @param message the messages for the exception
	 * @throws ResultException
	 */
	private void exception(String message) throws ResultException {
		ResultException ex = new ResultException(message);
		ex.setLocationInfo(currentFile, in.getLineNumber());
		throw(ex);
	}
	
	private void lineTrace(String message) {
		if (lineTrace)
			System.out.println(message);
	}
	
	private void trace(String message) {
		if (trace)
			System.out.println(message);
	}
	
	/** 
	 * Convenience function to throw exception with location info.
	 * @param message the messages for the exception
	 * @throws ResultException
	 */
	private void warning(String message){
		if (!warnings)
			return; 
		
		if (firstWarning) {
			int lastSlash = currentFile.lastIndexOf('/');
			System.out.println("File: " + currentFile.substring(lastSlash+1));
			firstWarning = false;
		}
		System.out.println("WARNING - Line: " + in.getLineNumber() + " - " + message);		
	}
	
	class RequiredColumns {
		MappingKeywordEnum keyword;
		
		// Minimum columns
		int min;
		
		// Maximum columns
		int max;
		
		// This could be null - the HEADER keyword won't have specific header names
		String[] headers;
		
		// If we don't hit max columns, this warning is displayed
		// This handles short term problems with people not following the template, but we can get by
		String	warning;
		
		public RequiredColumns(MappingKeywordEnum keyword, int min, int max, String... headers) {
			this.keyword = keyword;
			this.min = min;
			this.max = max;
			this.headers = headers;
		}
		
		public RequiredColumns(MappingKeywordEnum keyword, String warning, int min, int max, String... headers) {
			this.keyword 	= keyword;
			this.min 		= min;
			this.max 		= max;
			this.headers 	= headers;
			this.warning	= warning;
		}
		
		public RequiredColumns(MappingKeywordEnum keyword, int min, String... headers) {
			this.keyword = keyword;
			this.min = min;
			this.max = min;
			this.headers = headers;
		}
		
		public void showWarningIfNeeded(String[] columns) {
			if ((warning != null) && (columns.length < max))
				System.out.println("WARNING - Line: " + in.getLineNumber() + " - " + warning);
		}
	}
}
