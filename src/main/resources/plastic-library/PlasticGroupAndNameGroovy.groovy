
/**
 * This is based on the PlasticGroupAndName class that's a complex type.
 * It has been stripped down to allow for it to be used in Groovy.
 * This is a temporary hack.
 */
public class PlasticGroupAndNameGroovy {

    // The number of the pattern group
    Integer groupNumberV;

    // The name of the pattern group
    String groupNameV;

    // An optional note
    String noteV;

    /**
     * Default constructor.
     */
    public PlasticGroupAndNameGroovy(){
    }

    /**
     * All fields constructor.
     * Generated from: org.dmd.dms.util.NewComplexTypeFormatter.dumpComplexType(NewComplexTypeFormatter.java:186)
     */
    public PlasticGroupAndNameGroovy(Integer groupNumber_, String groupName_, String note_) {
        groupNumberV = groupNumber_;
        groupNameV = groupName_;
        noteV = note_;
    }

    /**
     * String form.
     * Generated from: org.dmd.dms.util.NewComplexTypeFormatter.dumpComplexType(NewComplexTypeFormatter.java:403)
     */
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append(groupNumberV.toString());
        sb.append(' ');
        sb.append(groupNameV.toString());
        if (noteV != null){
            sb.append(' ');
            sb.append("note=" + "\"" + noteV.toString() + "\"");
        }

        return(sb.toString());
    }

    public Integer getGroupNumber(){
        return(groupNumberV);
    }

    public String getGroupName(){
        return(groupNameV);
    }

    public String getNote(){
        return(noteV);
    }

}
