package com.connectifex.polymer.mdl.tools.plastic.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSource;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.dmd.util.exceptions.DebugInfo;

/**
 * The PlasticResourceUtil class is a singleton that provides convenience mechanisms to 
 * copy the reusable plastic lib classes to the generated hierarchy of plastic files.
 * This allows for easy addition of reusable classes to the lib. The class understands
 * whether it's running from a JAR or running in Eclipse and performs the file copies
 * appropriately.
 * 
 * You should add your library classes to src/main/resources/plastic-library
 * 
 * Files are found in the JAR hierarchy as follows:
 * ./BOOT-INF/classes/plastic-library
 * 
 */
public class PlasticLibraryResourceUtil {
	
	static private PlasticLibraryResourceUtil instance;

//	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private final static String PLASTIC_PATH = "/src/main/resources/plastic-library";
	
//	// Ensuring we have a completely separate path for the empty plastic structure
//	private final static String FAKE_SUBDIR = "/fake";
//
//	private static String BNC_REST_API_HANDLER_JAR	= "bnc-rest-api-handler";
	private static String RESOURCE_PREFIX			= "plastic-library/";
	
	private static String USER_GUIDE_PREFIX			= "user-guide/";
	private static String POLYMER_DIR				= "/polymer";
//	private static String TARGET_DIR 				= "/opt/connectifex/user/" + BNC_REST_API_HANDLER_JAR + "/plastic";
	
	// This is initialized, depending on whether we're running from the JAR or in Eclipse
//	private String searchPathBase;
	
	private PlasticLibraryResourceUtil() {
		
	}
	
	/**
	 * @return the instance of the PlasticResourceUtil
	 */
	static public PlasticLibraryResourceUtil instance() {
		if (instance == null) {
			instance = new PlasticLibraryResourceUtil();
//			instance.initialize();
		}
		
		return(instance);
	}
	
	/**
	 * Copies to the plastic library classes to the specified directory, whether we're running from
	 * JAR or in Eclipse.
	 */
	public void initLibraryClasses(String targetDir) {
		String[] paths = System.getProperty("java.class.path").split(File.pathSeparator);

		if (paths.length == 1) {
			try {					
		    	CodeSource src = PlasticLibraryResourceUtil.class.getProtectionDomain().getCodeSource();
		    	if (src == null) {
		    		throw(new IllegalStateException("Could not access CodeSource in order to access JAR contents!"));
		    	}
		    	else {			    		
		    		@SuppressWarnings("resource")
					JarFile jar = new JarFile(paths[0]);

					URL thejar = src.getLocation();
					ZipInputStream zip = new ZipInputStream(thejar.openStream());
					while (true) {
						ZipEntry entry = zip.getNextEntry();
						if (entry == null)
							break;
						
						if (entry.getName().startsWith(RESOURCE_PREFIX)) {
							String ename = entry.getName();
							String addOn = ename.replace(RESOURCE_PREFIX, "");
							String targetName = targetDir;
							
							if (addOn.length() > 0)
								targetName = targetName + "/" + addOn;
							else {
								// Note, we stick on the slash so we know this is the top level directory
								targetName = targetName + "/";
							}

							if (targetName.endsWith("/")){
								File dir = new File(targetName);
								if (!dir.exists()) {
									if (!dir.mkdirs()) {
										throw(new IllegalStateException("Could not create directory: " + targetName));
									}
								}
							}
							else {
								InputStream in = jar.getInputStream(entry);
								FileUtils.copyInputStreamToFile(in, new File(targetName));
								in.close();
							}
						}
					}
					
					zip.close();
					
					jar.close();
		    	}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			// We'd be here if we were running in Eclipse
			try {
				File curr = new File(".");
//				DebugInfo.debug(curr.getCanonicalPath() + PLASTIC_PATH);
				
				File libDir = new File(curr.getCanonicalPath() + PLASTIC_PATH);
				
				
				File[] files = libDir.listFiles();
				for(File srcFile: files) {
					if (srcFile.getName().endsWith(".groovy")) {
//						DebugInfo.debug("Copying " + srcFile.getName());
//						DebugInfo.debug("To " + targetDir);
						File destFile = new File(targetDir + "/" + srcFile.getName());
						FileUtils.copyFile(srcFile, destFile);
					}
				}
				
//				logger.info("plastic search path base: " + curr.getCanonicalPath() + PLASTIC_PATH);
//				searchPathBase = curr.getCanonicalPath() + PLASTIC_PATH;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Extracts the user guide to disk and passes back a fully qualified path to it.
	 * The /src/main/resources/user-guide folder should only contain the latest user guide and NOTHING ELSE!
	 */
	public String extractUserGuide() {
		String userHome = System.getProperty("user.home");
		
		if (userHome == null)
			throw(new IllegalStateException("Couldn't find user.home in System.property"));

		String targetDir = userHome + POLYMER_DIR;
		File 	tdir = new File(targetDir);
		
		if (!tdir.exists()) {
			System.out.println("\nCreating " + targetDir + "\n");
			tdir.mkdirs();
		}
		
		String[] paths = System.getProperty("java.class.path").split(File.pathSeparator);
		String rc = null;

		if (paths.length == 1) {
			try {					
		    	CodeSource src = PlasticLibraryResourceUtil.class.getProtectionDomain().getCodeSource();
		    	if (src == null) {
		    		throw(new IllegalStateException("Could not access CodeSource in order to access JAR contents!"));
		    	}
		    	else {			    		
		    		@SuppressWarnings("resource")
					JarFile jar = new JarFile(paths[0]);

					URL thejar = src.getLocation();
					ZipInputStream zip = new ZipInputStream(thejar.openStream());
					while (true) {
						ZipEntry entry = zip.getNextEntry();
						if (entry == null)
							break;
						
						if (entry.getName().startsWith(USER_GUIDE_PREFIX)) {
							String ename = entry.getName();
							String addOn = ename.replace(USER_GUIDE_PREFIX, "");
							String targetName = targetDir;
					
							// Just skip the top level directory
							if (ename.equals(USER_GUIDE_PREFIX))
								continue;
							
							if (addOn.length() > 0) {
								targetName = targetName + "/" + addOn;
								
								if (rc != null)
									throw(new IllegalStateException("Multiple files found in the: " + USER_GUIDE_PREFIX + " directory!"));
								
								rc = targetName;
							}
							else {
								// Note, we stick on the slash so we know this is the top level directory
								targetName = targetName + "/";
							}

							if (targetName.endsWith("/")){
								// Shouldn't happen
								throw(new IllegalStateException("There should be no folders beneath: " + USER_GUIDE_PREFIX));
							}
							else {
								System.out.println("\nExtracting user guide: " + targetName + "\n");
								
								InputStream in = jar.getInputStream(entry);
								FileUtils.copyInputStreamToFile(in, new File(targetName));
								in.close();
							}
						}
					}
					
					zip.close();
					
					jar.close();
		    	}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			throw(new IllegalStateException("You can't run polymer guide from Eclipse!"));
		}
		
		return(rc);
	}


}
