package com.connectifex.polymer.mdl.tools.mapping;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.dmd.dmc.DmcValueException;
import org.dmd.util.BooleanVar;
import org.dmd.util.exceptions.DebugInfo;
import org.dmd.util.exceptions.ResultException;
import org.dmd.util.exceptions.ResultSet;
import org.dmd.util.parsing.CommandLine;
import org.dmd.util.parsing.StringArrayList;

import com.connectifex.polymer.mdl.server.extended.mapping.Mapping;



public class TemplateConverter {
	
	private static String CSV = ".csv";
	private static String ___ = "\n";

	private CommandLine		commandLine 	= new CommandLine();
	private BooleanVar		helpFlag		= new BooleanVar();
	private BooleanVar		traceFlag		= new BooleanVar();
	private BooleanVar		lineTraceFlag	= new BooleanVar();
	private BooleanVar		warningsFlag	= new BooleanVar();
	private	StringArrayList	srcdirs			= new StringArrayList();
	private	StringArrayList	filterStrings		= new StringArrayList();
	
	private	StringBuffer	outdir			= new StringBuffer();
	private	StringBuffer	apiname			= new StringBuffer();
	
	private TemplateParser	parser			= new TemplateParser();

	public TemplateConverter() {
		commandLine.addOption("-h", 		helpFlag, 		"Dumps the help message");
		commandLine.addOption("-srcdirs", 	srcdirs, 		"Specifies the source directories to search for mapping files.");
		commandLine.addOption("-trace", 	traceFlag, 		"Turns on detailed tracing.");
		commandLine.addOption("-lineTrace", lineTraceFlag, 	"Turns on line level tracing.");
		commandLine.addOption("-warnings", 	warningsFlag, 	"Turns on warning display.");
		commandLine.addOption("-filter", 	filterStrings, 	"Only parse files that contain these strings");
		commandLine.addOption("-outdir", 	outdir, 		"The output directory");
		commandLine.addOption("-apiname", 	apiname, 		"The name of the api schema output directory");
	}
	
	public void run(String[] args) throws ResultException, IOException, DmcValueException {
    	ResultSet rs = new ResultSet();

    	if (!commandLine.parseArgs(rs,args)) {
        	throw(new ResultException(rs));
        }

        if ((args.length == 0) || helpFlag.booleanValue()){
            displayHelp();
            return;
        }
        
        if (lineTraceFlag.booleanValue())
        	parser.lineTrace(true);
        
        if (traceFlag.booleanValue())
        	parser.trace(true);
        
        if (warningsFlag.booleanValue())
        	parser.warnings(true);
        
        if (srcdirs.size() == 0) {
        	System.err.println("You must specify the -srcdirs option followed a list of one or more folders to be processed.");
        	return;
        }
        
        if (outdir.length() == 0) {
        	System.err.println("You must specify the -outdir option to indicate where the mapping files will be written.");
        	return;
        }
        
        if (apiname.length() == 0) {
        	System.err.println("You must specify the -apiname option to indicate where the api schema files will be written.");
        	return;
        }
        
        for(String dir: srcdirs) {
        	File d = new File(dir);
        	if (d.isDirectory()) {
        		File[] files = d.listFiles();
        		for(File file: files) {
        			if (file.isFile()) {
        				if (file.getName().endsWith(CSV)) {
        					boolean parseIt = true;
        					if (filterStrings.size() > 0) {
        						parseIt = false;
        						for(String filter: filterStrings) {
        							if (file.getName().contains(filter)) {
        								parseIt = true;
        								break;
        							}
        						}
        					}
        					
        					if (parseIt) {
        						trace("To parse: " + file.getCanonicalPath());	        					
	        					ArrayList<Mapping> mappings = parser.parseCSV(file.getCanonicalPath());
	        					for(Mapping mapping: mappings) {
	        						mapping.generate(apiname.toString(), outdir.toString());
	        					}
	        					
        					}
        					else {
        						trace("Skipping: " + file.getCanonicalPath());
        					}
        				}
        			}
        		}
        	}
        	else {
            	System.err.println("This is not a directory: " + dir);
            	return;
        	}
        }
		
	}
	
	private void trace(String message) {
		if (traceFlag.booleanValue())
			System.out.println(message);
	}
	
	
	public void displayHelp() {
		StringBuffer help = new StringBuffer();
        help.append(___);
        help.append("logformatter -h -f <logfile names> \n");
        help.append(___);
        help.append("The logformatter utility is used to extract and provide human readable summaries\n");
        help.append("of REST and NETCONF operations performed against the controller.\n");
        help.append(___);
        help.append("You must enable karaf logging for detailed NETCONF tracing:\n");
        help.append("log:set TRACE org.apache.sshd\n");
        help.append(___);
        help.append("You must turn on logging of REST requests/responses:\n");
        help.append("sudo vi /opt/connectifex/configuration/customer/lsc/etc/org.opendaylight.aaa.filterchain.cfg\n");
        help.append("Add the following line:\n");
        help.append("customFilterList=com.connectifexnetworks.lsc.core.filters.SplitRequestResponseLoggingFilter\n");
        help.append(___);
        help.append("-f  Indicates the log files to parse.\n");
        help.append(___);
        help.append("    ");

        help.append(___);
        
        System.out.println(help.toString());
	}
	
	
}
