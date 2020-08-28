package com.connectifex.polymer.mdl.tools.groovygen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.dmd.dms.ComplexTypeDefinition;
import org.dmd.dms.SchemaDefinition;
import org.dmd.dms.SchemaManager;
import org.dmd.dms.generated.dmw.ComplexTypeDefinitionIterableDMW;

public class GroovyGenerator {
	
	private final static String GROOVY_GEN = "GroovyGen";

	public GroovyGenerator() {
		// TODO Auto-generated constructor stub
	}
	
	public void generate(String outdir, SchemaManager manager, SchemaDefinition schema) {
		ComplexTypeDefinitionIterableDMW it = schema.getComplexTypeDefList();
		while(it.hasNext()) {
			ComplexTypeDefinition ctd = it.next();
			Iterator<String> tagIt = ctd.getTags();
			
			boolean doGen = false;
			while(tagIt.hasNext()) {
				if (tagIt.next().equals(GROOVY_GEN)) {
					doGen = true;
					break;
				}
			}
			
			if (doGen) {
				createGroovy(outdir, ctd);
			}
		}
	}
	
	private void createGroovy(String outdir, ComplexTypeDefinition ctd) {
		StringBuilder sb = new StringBuilder();

		
	}
	
    private static void createFile(String fn, String contents) {
    	
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fn));
			
			out.write(contents);
			
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    		
    }

	
}
