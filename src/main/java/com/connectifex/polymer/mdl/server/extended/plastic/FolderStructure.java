package com.connectifex.polymer.mdl.server.extended.plastic;

import org.dmd.dms.ClassDefinition;                                    // Used in derived constructors - (DMWGenerator.java:284)
import org.dmd.util.exceptions.ResultException;

import com.connectifex.polymer.mdl.server.extended.plastic.util.PlasticConstants;
import com.connectifex.polymer.mdl.server.generated.dmw.FolderStructureDMW;
import com.connectifex.polymer.mdl.server.generated.dsd.MdlModuleDefinitionManager;
import com.connectifex.polymer.mdl.shared.generated.dmo.FolderStructureDMO;


public class FolderStructure extends FolderStructureDMW {

    public FolderStructure(){
        super();
    }

    public FolderStructure(FolderStructureDMO dmo, ClassDefinition cd){
        super(dmo,cd);
    }

    @Override
    public void performAdditionalValidation(MdlModuleDefinitionManager definitions) throws ResultException {
    	if ( (getInFolder3() != null) && (getInFolder2() == null)) {
    		ResultException ex = new ResultException("If you specify inFolder3, you must also specify inFolder2");
    		ex.setLocationInfo(getFile(), getLineNumber());
    		throw(ex);
    	}
    	
    	if ( (getOutFolder3() != null) && (getOutFolder2() == null)) {
    		ResultException ex = new ResultException("If you specify outFolder3, you must also specify outFolder2");
    		ex.setLocationInfo(getFile(), getLineNumber());
    		throw(ex);
    	}
    	
    }
    
    public String getInputSchemaDirName(String version) {
    	StringBuilder sb = new StringBuilder();
    	
    	sb.append(getName().getNameString() + "/");
    	
    	sb.append(PlasticConstants.SCHEMAS + "/");
    	
    	sb.append(getInFolder1().getName());
    	
    	if (getInFolder2() != null)
    		sb.append("/" + getInFolder2().getName());
    	
    	if (getInFolder3() != null)
    		sb.append("/" + getInFolder3().getName());
    	
    	sb.append("/R" + version);
    	
    	return(sb.toString());
    }
    
    /**
     * @return the prefix for input related files within this folder structure comprised of infolder1-infolder2-infolder3
     */
    public String getInFileNamePrefix() {
    	StringBuilder sb = new StringBuilder();
    	sb.append(getInFolder1().getName());
    	if (getInFolder2() != null)
    		
    		sb.append("-" + getInFolder2().getName());
    	
    	if (getInFolder3() != null)
    		sb.append("-" + getInFolder3().getName());
    	
    	return(sb.toString());
    }
    
    /**
     * @return the prefix for out related files within this folder structure comprised of outfolder1-outfolder2-outfolder3
     */
    public String getOutFileNamePrefix() {
    	StringBuilder sb = new StringBuilder();
    	sb.append(getOutFolder1().getName());
    	if (getOutFolder2() != null)
    		sb.append("-" + getOutFolder2().getName());
    	
    	if (getOutFolder3() != null)
    		sb.append("-" + getOutFolder3().getName());
    	
    	return(sb.toString());
    }
    
    public String getOutputSchemaDirName(String version) {
    	StringBuilder sb = new StringBuilder();
    	
    	sb.append(getName().getNameString() + "/");
    	
    	sb.append(PlasticConstants.SCHEMAS + "/");
    	
    	sb.append(getOutFolder1().getName());
    	
    	if (getOutFolder2() != null)
    		sb.append("/" + getOutFolder2().getName());
    	
    	if (getOutFolder3() != null)
    		sb.append("/" + getOutFolder3().getName());
    	
    	sb.append("/R" + version);
    	
    	return(sb.toString());
    }
    
    public String getMorphersVersionedDirName(String version) {
    	StringBuilder sb = new StringBuilder();
    	
    	sb.append(getName().getNameString() + "/");
    	
    	sb.append(PlasticConstants.MORPHERS + "/");
    	
    	sb.append(getOutFolder1().getName());
    	
    	if (getOutFolder2() != null)
    		sb.append("/" + getOutFolder2().getName());
    	
    	if (getOutFolder3() != null)
    		sb.append("/" + getOutFolder3().getName());
    	
    	sb.append("/R" + version);
    	
    	return(sb.toString());
    }
    
    public String getClassifiersDirName() {
    	StringBuilder sb = new StringBuilder();
    	
    	sb.append(getName().getNameString() + "/");
    	
    	sb.append(PlasticConstants.CLASSIFIERS);
    	
    	return(sb.toString());
    }
    
    public String getLibDirName() {
    	StringBuilder sb = new StringBuilder();
    	
    	sb.append(getName().getNameString() + "/");
    	
    	sb.append(PlasticConstants.LIB);
    	
    	return(sb.toString());
    }
    
    public String getMorphersBaseDirName() {
    	StringBuilder sb = new StringBuilder();
    	
    	sb.append(getName().getNameString() + "/");
    	
    	sb.append(PlasticConstants.MORPHERS);
    	
    	return(sb.toString());
    }
}

