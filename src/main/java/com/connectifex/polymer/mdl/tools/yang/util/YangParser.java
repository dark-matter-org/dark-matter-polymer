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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dmd.dmc.types.CheapSplitter;
import org.dmd.util.exceptions.DebugInfo;
import org.dmd.util.exceptions.ResultException;
import org.dmd.util.parsing.StringArrayList;


public class YangParser {

	private static String NO_SPACE_BEFORE_OPEN_CURLY = "[^\\s]+[{]$";
	private static Pattern pattern = Pattern.compile(NO_SPACE_BEFORE_OPEN_CURLY);
	private static Matcher MALFORM_MATCHER = pattern.matcher("");
	
	// Handle to our reader
	private LineNumberReader				in;
	private String						currentFile;
	
	private Stack<YangStructure>			constructStack;
	private	YangStructure				root;
	
	private int							currentIndent;
	
	private boolean						trace;
	
	private final static String CLOSE_CURLY = "}";
	private final static String OPEN_CURLY = "{";
	private final static String COMMENT = "//";
	private final static String CONCATENATION = "+";
	private final static String SEMICOLON = ";";
	private final static String OPEN_COMMENT = "/*";
	private final static String CLOSE_COMMENT = "*/";
	private final static String PATTERN = "pattern";
	private final static String DEVIATION = "deviation";
	
//	private YangFinder		finder;
	private YangContext		context;
	
	// Keywords we find
	private HashSet<String>	keywords;
	
	public YangParser() {

	}
	
	public void trace(boolean flag) {
		trace = flag;
	}
	
	private void trace(String msg) {
		if (trace) {
			if (in == null)
				System.out.println(msg + " -- " + DebugInfo.getShortWhereWeWereCalledFrom());
			else {
				StringBuffer sb = new  StringBuffer();
				if (constructStack.size() > 0) {
					for(int i=0; i<constructStack.size(); i++)
						sb.append("    ");
				}
				System.out.println("line: " + in.getLineNumber() + " -- depth: " + constructStack.size() + " -- " + sb.toString() + msg + " -- " + DebugInfo.getShortWhereWeWereCalledFrom());
			}
		}
	}
	
	private String traceWithLineAndDepth(String msg) {
		if (in == null)
			return(msg + " -- " + DebugInfo.getShortWhereWeWereCalledFrom());
		else {
			StringBuffer sb = new  StringBuffer();
			if (constructStack.size() > 0) {
				for(int i=0; i<constructStack.size(); i++)
					sb.append("    ");
			}
			return("line: " + in.getLineNumber() + " -- depth: " + constructStack.size() + " -- " + sb.toString() + msg + " -- " + DebugInfo.getShortWhereWeWereCalledFrom());
		}
	}
	
	public YangParser(YangContext context) {
		this.context = context;
	}
	
	public YangStructure parse(String dn, String fn) throws ResultException, IOException {
		Reader reader = null;
		
		root = null;
		constructStack = new Stack<>();
		keywords = new HashSet<>();
		
		String fullName = dn + File.separator + fn;
		
		YangDebugChannels.fileLoading.getChannel().publish("Loading: " + fullName);
		
		trace("Parsing: " + fullName);
		currentFile = fullName;
		try {
			reader = new FileReader(fullName);
		} catch (FileNotFoundException e) {
			ResultException ex = new ResultException(e);
			ex.result.lastResult().fileName(fn);
			throw(ex);
		}
		parse(reader, fn);
		
		reader.close();
		
		root.loadImportsAndIncludes(context, trace);
//		System.out.println(root.toString());
		
		return(root);
	}
	
	public ArrayList<YangStructure>	parseAll(String dn) throws ResultException, IOException{
		StringArrayList ignore = new StringArrayList();
		return(parseAll(dn, ignore));
	}
		
	public ArrayList<YangStructure>	parseAll(String dn, StringArrayList ignore) throws ResultException, IOException{
		ArrayList<YangStructure>		rc = new ArrayList<>();
		
		File dir = new File(dn);
		if (dir.exists()) {
			String[] files = dir.list();
			for(String fn: files) {
				File f = new File(fn);
				if (f.isHidden()	)
					continue;
				
				if (!fn.endsWith(YangConstants.YANG))
					continue;
				
				if (ignore.size() > 0) {
					boolean ignoring = false;
					for(String s: ignore) {
						if (fn.contains(s)) {
							YangDebugChannels.fileLoading.getChannel().publish("Ignoring: " + dn + "/" + fn);
							ignoring = true;
							break;
						}
					}
					if (ignoring)
						continue;
				}
				
				
//				YangDebugChannels.fileLoading.getChannel().publish("Loading: " + dn + "/" + fn);
//				DebugInfo.debug("Reading: " + dn + "/" + fn);
				YangStructure ys = parse(dn, fn);
				
				rc.add(ys);
			}
		}
		else {
			throw(new IllegalStateException("Base directory not found: " + dn));
		}
		
		return(rc);
	}
	
	private String getNextLine() throws IOException {
		String line = in.readLine();
				
		if (line == null)
			return(null);
		
		if (MALFORM_MATCHER.reset(line.trim()).find()) {
//			DebugInfo.debug("File: " + currentFile + "\n" + "Line: " + in.getLineNumber() + "\nMalformed: " + line );
			
			line = line.replace("{", " {");
//			DebugInfo.debug("Adjusted: " + line + "\n");
		}
		
		return(line);
	}
	
	private void parse(Reader reader, String fileName) throws ResultException {
		in = new LineNumberReader(reader);
		StringBuilder	concatenated = null;
		
		// Used to handle lines where we have a start double quote but no end quote in
		// situations like:
		// from openconfig-network-instances
		// when "../../config/type = 'L2VSI' or ../../config/type = 'L2P2P'
		//         or ../../config/type = 'L2L3'" {
		StringBuilder	splitQuote = null;
		 
        String line = null;
        try {
//			while ((line = in.readLine()) != null) {
			while ((line = getNextLine()) != null) {
				String trimmed = line.trim();
				
				if (trimmed.startsWith(COMMENT))
					continue;
				
				// Have to trim any trailing comments 
				int commentStart = trimmed.indexOf(COMMENT);
				if (commentStart != -1) {
					int semiPos = trimmed.lastIndexOf(SEMICOLON);
					
					if ( (semiPos != -1) && (commentStart > semiPos)){
						trace("Trimming trailing comment");
						
						trimmed = trimmed.substring(0, commentStart).trim();
					}
					
				}
				
				if (trimmed.length() == 0)
					continue;
				
				///////////////////////////////////////////////////////////////
				// See openconfig-if-ip.yang for example of concatenated lines
				
				if (trimmed.endsWith(CONCATENATION)) {
					if (concatenated == null)
						concatenated = new StringBuilder();
					
					concatenated.append(line);
					continue;
				}
				
				if (concatenated != null) {
					concatenated.append(line);
					trimmed = collapseConcatenateParts(concatenated.toString()).trim();
//					DebugInfo.debug("CONCATENATED: " + trimmed);
					
//					collapseConcatenateParts(trimmed);
					
					concatenated = null;
				}
												
				///////////////////////////////////////////////////////////////
				// Split quotes - see openconfig-network-instances near
				// l2ni-encapsulation-config for example of the when clause
				
				if (charCount('\"', trimmed) == 1) {
					if (splitQuote == null) {
						splitQuote = new StringBuilder();
						splitQuote.append(trimmed);
						trace("*** Start split quote");
						continue;
					}
					else {
						splitQuote.append(" " + trimmed);
						trimmed = splitQuote.toString();
						splitQuote = null;
						
						trace("*** End split quote: " + trimmed);
					}
				}
				
				if (splitQuote != null) {
					splitQuote.append(" " + trimmed);
					continue;
				}
				
				
				///////////////////////////////////////////////////////////////
				
				if (trimmed.startsWith(OPEN_COMMENT)) {
					parseComment(trimmed);
					continue;
				}
				
				///////////////////////////////////////////////////////////////

				ArrayList<String> tokens = CheapSplitter.split(trimmed, ' ', false, true);
			
				if (tokens.size() > 0) {
					
					if (isSingleLineStructure(trimmed)) {
						YangStructure parent = null;
						if (!constructStack.isEmpty())
							parent = constructStack.peek();
						
						// Hack for now - to handle patterns
						if (trimmed.startsWith(PATTERN)) {
							parseAttribute(trimmed);
//							DebugInfo.debug("");
						}
						else {
							YangStructure newConstruct = new YangStructure(tokens.get(0), tokens.get(1), parent, in.getLineNumber(),constructStack.size(), root);
	
//							if (root == null) 
//								root = newConstruct;
							
							constructStack.push(newConstruct);
							trace("Pushed: " + tokens.get(0) + " with name: " + tokens.get(1));
							
							parseSingleLineStructAttributes(trimmed, newConstruct);
	
							YangStructure construct = constructStack.pop();
							trace("Popped: " + construct.name() + "  -- line: " + in.getLineNumber());
						}
							
					}
					else if (tokens.get(0).equals(CLOSE_CURLY)) {
						if (constructStack.size() == 0) {
							// This should be the last curly in the file
							continue;
						}
						
						YangStructure construct = constructStack.pop();
						trace("Popped: " + construct.name());
//						DebugInfo.debug("Popping: " + construct.name());
					}
					else if (trimmed.endsWith(OPEN_CURLY)) {
						trace("open curly");
//						DebugInfo.debug("Depth: " + constructStack.size());
						YangStructure parent = null;
						YangStructure newConstruct = null;
						String constructName = tokens.get(1);
						
						if (charCount('\"', trimmed) == 2) {
							// In some cases, the name part is quoted e.g. in a 'when' clause, so we have to
							// separate it out
							constructName = getQuotedSection(trimmed);
						}
						
						if (constructStack.isEmpty()) {
							newConstruct = new YangStructure(tokens.get(0), constructName, parent, fileName);
						}
						else {
							parent = constructStack.peek();
							newConstruct = new YangStructure(tokens.get(0), constructName, parent, in.getLineNumber(),constructStack.size(), root);
						}
						
						
//						YangStructure newConstruct = new YangStructure(tokens.get(0), tokens.get(1), parent, in.getLineNumber());
						if (root == null)
							root = newConstruct;
						
						constructStack.push(newConstruct);
						trace("Pushed: " + tokens.get(0) + " with name: " + constructName);
					}
					else {
						// We're not starting or stopping a structure so we could be dealing with an attribute
						// in the current structure, or we could have a situation where we're dealing with
						// a concatenation character starting the next line - I hate this frigged up grammar.
						
						// So far, the only place I've seen the CONCATENATION at the beginning of a line is 
						// when dealing with a deviation - so there's a hack for that
						if (trimmed.startsWith(DEVIATION)) {
							parseDeviation(trimmed);
						}
						else {
							// This could be all on one line, or it could span many lines.
							parseAttributeOrStructure(trimmed);
						}
					}
					
				}
			
				
			}
		} catch (Exception e) {
			ResultException ex = new ResultException(e);
			ex.moreMessages("Problem reading from file: " + fileName + " line: " + in.getLineNumber());
			throw(ex);
		}
        
        try {
			in.close();
		} catch (IOException e) {
			ResultException ex = new ResultException(e);
			ex.moreMessages("Problem closing file: " + fileName);
			throw(ex);
		}
	        
	}
	
	/**
	 * The handle this nonsense:
	 *   deviation "/oc-ni:network-instances/oc-ni:network-instance/oc-ni:interfaces"
     *     + "/oc-ni:interface/oc-ni:state/oc-ni:associated-address-families" {
     *
	 * @param initial
	 * @throws IOException 
	 */
	private void parseDeviation(String initial) throws IOException {
		StringBuilder sb = new StringBuilder(initial);
		
		String line = null;
//		while ((line = in.readLine()) != null) {
		while ((line = getNextLine()) != null) {
			String trimmed = line.trim();
			if (trimmed.startsWith(CONCATENATION)) {
				sb.append(trimmed.replace(CONCATENATION, ""));
			}
			if (sb.toString().endsWith(CLOSE_CURLY))
				break;
		}
		
		int quotes = charCount('"',sb.toString());
		if ( (quotes%2) == 0) {
			String stripped = sb.toString().replaceAll("\"", "");
			ArrayList<String> tokens = CheapSplitter.split(stripped, ' ', false, true);
			
			YangStructure parent = null;
			if (!constructStack.isEmpty())
				parent = constructStack.peek();
			
			YangStructure newConstruct = new YangStructure(tokens.get(0), tokens.get(1), parent, in.getLineNumber(),constructStack.size(), root);
			constructStack.push(newConstruct);
			
			trace("Pushed: " + tokens.get(0) + " with name: " + tokens.get(1));

		}
		else {
			throw(new IllegalStateException("Unbalanced quotes in a deviation specification"));
		}
		
	}
	
	private void parseSingleLineStructAttributes(String line, YangStructure structure) throws IOException {
		int open = line.indexOf(OPEN_CURLY);
		int close = line.indexOf(CLOSE_CURLY);
		
		String values = line.substring(open+1, close).trim();
		
		if (charCount(';', values) == 1) {
			// Assume this is an attribute of the current construct e.g. something like:
			// import openconfig-extensions { prefix oc-ext; }
			// Where we're dealing with this part: prefix oc-ext;
			parseAttribute(values);
		}
		else {
		
			DebugInfo.debug("File: " + currentFile + " : " + in.getLineNumber());
			throw(new IllegalStateException("Not implemented"));
		}
	}
	
	/**
	 * Handles sections bounded by standard Java/C comment delimiters.
	 * @param initial the initial line
	 * @throws IOException
	 */
	private void parseComment(String initial) throws IOException {
		if (initial.contains(CLOSE_COMMENT))
			return;
	
		String line = null;
//		while ((line = in.readLine()) != null) {
		while ((line = getNextLine()) != null) {
			if (line.trim().endsWith(CLOSE_COMMENT))
				break;
		}
	}
	
	private void parseAttribute(String initial) throws IOException {
		
		if (initial.endsWith(SEMICOLON)) {
			if (initial.startsWith(YangConstants.USES)) {
				if (initial.contains(":"))
					DebugInfo.debug("Here");
			}
//			DebugInfo.debug("ATTRIBUTE: " + initial);
			
			YangAttribute ya = constructStack.peek().addAttribute(initial);
			
			if (ya != null)
				trace("    single-line attribute: " + ya.name() + " \"" + ya.value() + "\"");
		}
		else {
			StringBuilder sb = new StringBuilder(initial);
			// Keep reading until we get to a line ending with a semicolon
			String line = null;
			boolean quoted = false;
			boolean balanced = false;
			
			if (initial.contains("\""))
				quoted = true;
			
//			while ((line = in.readLine()) != null) {
			while ((line = getNextLine()) != null) {
				if (line.contains("\"")) {
					if (quoted) {
						balanced = true;
					}
					else {
						quoted = true;
						if (charCount('"',line) == 2)
							balanced = true;
					}
				}
				String trimmed = line.trim();
				sb.append(" " + trimmed);
				
				if (quoted) {
					if (balanced && trimmed.endsWith(SEMICOLON)) {
						break;
					}
				}
				else {
					if (trimmed.endsWith(SEMICOLON))
						break;					
				}
			}
			trace("    multi-line attribute: " + sb.toString());
			
//			DebugInfo.debug("ML ATTRIBUTE: " + sb.toString());
			
			constructStack.peek().addAttribute(sb.toString());
		}
	}
	
	private void parseAttributeOrStructure(String initial) throws IOException {
		boolean popConstruct = false;
		
		if (initial.endsWith(SEMICOLON)) {
//			if (initial.startsWith(YangConstants.USES)) {
//				if (initial.contains(":"))
//					DebugInfo.debug("Here");
//			}
//			DebugInfo.debug("ATTRIBUTE: " + initial);
			YangAttribute ya = constructStack.peek().addAttribute(initial);
			if (ya != null)
				trace("    single-line attribute: " + ya.name() + " \"" + ya.value() + "\"");
		}
		else {
			StringBuilder sb = new StringBuilder(initial);
			// Keep reading until we get to a line ending with a semicolon
			String line = null;
			boolean quoted = false;
			boolean balanced = false;
			
			String quoteChar = getQuoteChar(initial);
			
			if ((quoteChar != null) && initial.contains(quoteChar))
				quoted = true;
			
//			while ((line = in.readLine()) != null) {
			while ((line = getNextLine()) != null) {
				if (quoteChar == null)
					quoteChar = getQuoteChar(line);
				
				// And, of course, there can sometimes be blank lines just dropped in there to
				// makes things more interesting - so we still won't know the quote char
				if ((line.trim().length() == 0) && quoteChar == null)
					continue;
				
				// Or, the friggin line could start with a comment
				if (line.trim().startsWith(COMMENT))
					continue;
				
				String escapedQuote = "\\" + quoteChar; 
				
				// And, of course, it gets worse, there can also be escaped quote chars in the 
				// quoted section - so we have to strip them before we do our test
				String stripped = line.replace(escapedQuote, "");
				
//				if (line.contains(quoteChar)) {
				if (stripped.contains(quoteChar)) {
					if (quoted) {
						balanced = true;
					}
					else {
						quoted = true;
//						if (charCount(quoteChar.charAt(0),line) == 2)
						if (charCount(quoteChar.charAt(0),stripped) == 2)
							balanced = true;
					}
				}
				
				String trimmed = line.trim();
				
				if (balanced && trimmed.endsWith(CLOSE_CURLY)) {
					trace("    MISPLACED } being handled");
					// More nastiness - have come across cases like this in openconfig-aft-pf
					//       reference
			        //         "RFC5462: Multiprotocol Label Switching (MPLS) Label Stack
			        //         Entry: 'EXP' Field Renamed to 'Traffic Class' Field"; }
					// Where the close curly is at the end of the line - when it should
					// be on the next line down
					// We're going to have pop the construct at this level - yuck!
					trimmed = trimmed.substring(0, trimmed.length()-2).trim();
					popConstruct = true;
				}

				sb.append(" " + trimmed);
				
				if (quoted) {
					if (balanced && trimmed.endsWith(SEMICOLON)) {
						break;
					}
				}
				else {
					if (trimmed.endsWith(SEMICOLON))
						break;					
				}
			}
			trace("    multi-line attribute: " + sb.toString());
			
//			DebugInfo.debug("ML ATTRIBUTE: " + sb.toString());
			
			constructStack.peek().addAttribute(sb.toString());
			
			if (popConstruct) {
				YangStructure construct = constructStack.pop();
				trace("Popped: " + construct.name() + "  -- line: " + in.getLineNumber());

			}
		}
	}
	
	private String getQuoteChar(String text) {
		// Frickin tricky - by default, we assume quoted sections start and end with double quotes,
		// but - in some cases where someone wants to include double quotes in the quoted section, they
		// can start that section with a single quote - in which case we have to change things up
		// This is the case in:
		// tailf-cli-extension@2018-09-15.yang - line 1067
		// Again, have I mentioned I hate this grammar!
		String quoteChar = null;
		
		int doubleQuoteLoc = text.indexOf('"');
		int singleQuoteLoc = text.indexOf('\'');
		
		if (doubleQuoteLoc != -1) {
			quoteChar = "\"";
			if (singleQuoteLoc != -1) {
				if (singleQuoteLoc < doubleQuoteLoc)
					quoteChar = "'";
			}
		}
		else if (singleQuoteLoc != -1) {
			quoteChar = "'";
			if (doubleQuoteLoc != -1) {
				if (doubleQuoteLoc < singleQuoteLoc)
					quoteChar = "\"";
			}
		}
		
		if (quoteChar == null)
			trace("Quote char not determined yet");
		else
			trace("Quote char: " + quoteChar);
		
		return(quoteChar);
	}
	
	private int charCount(char theChar, String line) {
		int rc = 0;
		
		for(int i=0; i<line.length(); i++) {
			if (line.charAt(i) == theChar)
				rc++;
		}
		
		return(rc);
	}
	
	private String getQuotedSection(String line) {
		int open = line.indexOf("\"");
		int close = line.lastIndexOf("\"");
		
		return(line.substring(open+1, close));
	}
	
	/**
	 * Takes the concatenated parts of an augment statement and collapses all of the
	 * quoted sections into a single section.
	 * @param line the concatenated lines with embedded pluses
	 * @return a string with all the cruft removed
	 */
	private String collapseConcatenateParts(String line) {
		trace(line);
		
		if (line.indexOf("\"") != -1) {
			int firstQuote = line.indexOf("\"");
			int lastQuote = line.lastIndexOf("\"");
			
			String leading = line.substring(0, firstQuote);
			String trailing = line.substring(lastQuote + 1);
			
			String allQuoted = line.substring(firstQuote, lastQuote);
			allQuoted = allQuoted.replaceAll("\"", "");
			allQuoted = allQuoted.replaceAll("\\+", "");
			allQuoted = allQuoted.replaceAll("\\s*", "");
			
			return(leading + allQuoted + trailing);
		}
		else if (line.indexOf("\'") != -1) {
			int firstQuote = line.indexOf("\'");
			int lastQuote = line.lastIndexOf("\'");
			
			String leading = line.substring(0, firstQuote);
			String trailing = line.substring(lastQuote + 1);
			
			String allQuoted = line.substring(firstQuote, lastQuote);
			allQuoted = allQuoted.replaceAll("\'", "");
			allQuoted = allQuoted.replaceAll("\\+", "");
			allQuoted = allQuoted.replaceAll("\\s*", "");
			
			return(leading + allQuoted + trailing);
		}
		else {
			throw new IllegalStateException("Unquoted sections in a concantenation");
		}
	}
	 
	/**
	 * This is intended to handle things like:
	 * import openconfig-inet-types { prefix oc-inet; }
	 * 
	 * However, we have to be sure it isn't actually a quoted section with embedded curlies like:
	 * description "FTP destination URL (allows {text} macros)";
	 * 
	 * Another case is pattern specifiers e.g. from ietf-yang-types:
	 * pattern '\d*(\.\d*){1,127}';
	 * 
	 * @param line
	 * @return
	 */
	private boolean isSingleLineStructure(String line) {
		boolean rc = false;
		
		if (line.contains(OPEN_CURLY) && line.contains(CLOSE_CURLY)) {
			
			if (line.contains("\"")) {
				if (charCount('"', line) == 2) {
					int firstQ = line.indexOf('"');
					int secondQ = line.indexOf('"', firstQ+1);
					int open = line.indexOf(OPEN_CURLY);
					int close = line.indexOf(CLOSE_CURLY);
					
					if ((firstQ < open) && (secondQ > close))
						return(rc);
				}
			}
			else if (line.contains("'")){
				if (charCount('\'', line) == 2) {
					int firstQ = line.indexOf("'");
					int secondQ = line.indexOf("'", firstQ+1);
					int open = line.indexOf(OPEN_CURLY);
					int close = line.indexOf(CLOSE_CURLY);
					
					if ((firstQ < open) && (secondQ > close))
						return(rc);
				}
			}
			rc = true;			
		}
		
		return(rc);
	}
}
