package com.connectifex.util.json;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import org.dmd.dmc.DmcValueException;
import org.dmd.dmu.util.json.PrettyJSON;
import org.dmd.util.exceptions.DebugInfo;
import org.dmd.util.formatting.PrintfFormat;
import org.json.JSONObject;

import com.connectifex.polymer.mdl.shared.generated.types.PlasticVariable;


/**
 * The Parameterizer takes a JSON object and generates a set of the shortest, unique 
 * variable names for each key. This only works in situations where we have an unbroken
 * chain of keys with either no intervening arrays or if we have arrays with a single object.
 * 
 * If we're able to determine variables, we can create a PlasticSchema with the variables
 * defined, the list of variables and a PlasticTest instance parameters and their current values.
 * 
 */
public class Parameterizer {

	// The root our the JSON structure
	private JNode root;
	
	// We build a map of all keys, and all nodes that share them
	// Key:   key
	// Value: the node with that key
	private TreeMap<String, ArrayList<JNode>> nodesByKey;
	
	// A set of Nodes for which we can generate PlasticVariables
	private ArrayList<JNode>	parameterNodes;

	// This flag is set if we're able to create a set of unique keys
	private boolean uniqueKeysAvailable;

	private int longestParamName;

	private ArrayList<PlasticVariable>	PlasticVariables;
	
	private	boolean debug;
	
	private JSONObject object;

	public Parameterizer() {
		// TODO Auto-generated constructor stub
	}
	
	public void debug(boolean debug) {
		this.debug = debug;
	}
	
	public void initialize(String json) throws DmcValueException {
		object 					= new JSONObject(json);
		root 					= new JNode(object, null, null);
		nodesByKey				= new TreeMap<>();
		parameterNodes			= new ArrayList<>();
		uniqueKeysAvailable		= false;
		longestParamName		= 0;
		PlasticVariables	= new ArrayList<PlasticVariable>();
		
		// Create the JNode hierarchy
		gatherNodesByKey(root);
		
		if (debug)
			System.out.println("JNode hierarchy:\n" + root.hierarchy() + "\n\n");
		
		// Traverse the hierarchy and see if we can find places for insertable parameters
		generateUniqueParamNames();

		if (uniqueKeysAvailable) {
			if (debug)
				System.out.println("\n\nPlasticVariables\n");
			
			if (parameterNodes.size() > 0) {
				for(JNode node: parameterNodes) {
					if (node.uniqueKey.length() > longestParamName)
						longestParamName = node.uniqueKey.length();
				}
				
				for(JNode node: parameterNodes) {
					if (!node.isPrimitive())
						continue;
					
					PlasticVariable ip = node.getParameter();
					PlasticVariables.add(ip);
					if (debug)
						System.out.println(ip.toString());
				}
			}
		}

	}
	
	public boolean hasParameters() {
		if (root == null)
			throw(new IllegalStateException("You must call initialize() first!"));
			
		return(uniqueKeysAvailable);
	}
	
	/**
	 * @return a YangConfiguration in string form, suitable for pasting into a .omni module.
	 */
	public String getPlasticMapping() {
		if (!hasParameters())
			throw(new IllegalStateException("There are no unique parameters - no point calling this!"));
		
		StringBuilder sb = new StringBuilder();

		PrintfFormat format = new PrintfFormat("%-" + longestParamName + "s");
		
		sb.append("PlasticMapping\n");
		sb.append("name            insert-mapping-name\n");
		sb.append("folderStructure tutorials-structure\n");
//		sb.append("inputVersion 1.0\n");
//		sb.append("inputType    JSON\n");
		boolean first = true;
		for(PlasticVariable ip: PlasticVariables) {
			sb.append("variables    " + format.sprintf(ip.getName()) + " example=\"" + ip.getExample() + "\" ");
			if (first) {
				sb.append("default=\"" + ip.getExample() + "\" ");
				sb.append("note=\"Add notes here\"");
				first = false;
			}
			sb.append("\n");
		}
		sb.append("inputSchema");
		root.prettyPrint(sb, "  ", "", 0);
		sb.append("\n");
		sb.append("outputSchema {\n");
		sb.append("     \"add-your-schema\": \"here\"\n");
		sb.append(" }\n");
		sb.append("description  Document your mapping here...\n");

		return(sb.toString());
	}
	
	public String getOutputInfo() {
		if (!hasParameters())
			throw(new IllegalStateException("There are no unique variables - no point calling this!"));
		
		StringBuilder sb = new StringBuilder();

		PrintfFormat format = new PrintfFormat("%-" + longestParamName + "s");

		sb.append("outputVersion 1.0\n");
		sb.append("outputType    JSON\n");
		boolean first = true;
		for(PlasticVariable ip: PlasticVariables) {
			sb.append("variables    " + format.sprintf(ip.getName()) + " example=\"" + ip.getExample() + "\" ");
			if (first) {
				sb.append("default=\"" + ip.getExample() + "\" ");
				sb.append("note=\"Add notes here\"");
				first = false;
			}
			sb.append("\n");
		}
		sb.append("\n");

		sb.append("outputSchema ");
		root.prettyPrint(sb, "  ", "", 0);
		sb.append("\n");

		return(sb.toString());
	}
	
	public String getPlasticTest() {
		if (!hasParameters())
			throw(new IllegalStateException("There are no unique variables - no point calling this!"));

		StringBuilder sb = new StringBuilder();
		PrintfFormat format = new PrintfFormat("%-" + longestParamName + "s");
		
		sb.append("PlasticTest\n");
		sb.append("name            insert-mapping-name-test1\n");
		sb.append("mapping         insert-mapping-name\n");
		sb.append("inputPayload    " + PrettyJSON.instance().prettyPrint(object, true, "  ") + "\n");
		
		return(sb.toString());
	}
	
	/**
	 * Descends the JNode structure and populates the nodesByKey map.
	 * @param node the node for which we're gathering keys
	 */
	private void gatherNodesByKey(JNode node) {
		if (node.rawkey() != null)
			storeNodeByKey(node);
			
		if (node.children != null) {
			for (JNode child: node.children) {
				gatherNodesByKey(child);
			}
		}
		
	}
	
	private void storeNodeByKey(JNode node) {
		ArrayList<JNode> nodes = nodesByKey.get(node.rawkey());
		if (nodes == null) {
			nodes = new ArrayList<>();
			nodesByKey.put(node.rawkey(), nodes);
		}
		nodes.add(node);
		
	}
	
	private void generateUniqueParamNames() {
		
		for(String key: nodesByKey.keySet()) {
			if (debug)
				DebugInfo.debug("Key: " + key);
			
			ArrayList<JNode> nodes = nodesByKey.get(key);
			
			if (nodes.size() == 1) {
				if (debug)
					DebugInfo.debug("  1 node");
				// This key is unique, so if the node is a single
				// object array or a primitive, we're interested
				if (nodes.get(0).isSingleObjectArray() || nodes.get(0).isPrimitive()) {
					nodes.get(0).uniqueKey = key;
					parameterNodes.add(nodes.get(0));
					uniqueKeysAvailable = true;
					if (debug)
						DebugInfo.debug("  single object array or primitive");
				}
				else {
					if (debug)
						DebugInfo.debug("  NOT single object array or primitive");

				}
			}
			else {
				// A little more tricky - now we have to ask the Nodes
				// with the same key to attempt to construct a unique key
				// by ascending their parent hierarchy.
				
				// We will try to a max "depth" of 2 (up the hierarchy)
				if (debug)
					DebugInfo.debug("  Multiple nodes with key");

				
				for(int depth=1; depth<=2; depth++) {
					boolean uniqueAtThisDepth = true;
					
					TreeSet<String> uk = new TreeSet<String>();
					for(JNode node: nodes) {
						
						if ( (node.type == JType.OBJECT) || (node.isSingleObjectArray())) {
							if (debug)
								DebugInfo.debug("    node " + node.type + " skipping");
							continue;
						}
						if (debug)
							DebugInfo.debug("    node " + node.type);

						// We don't set the key until we're sure we have uniqueness
						String nk = node.getUniqueKeyBasedOnDepth(depth, false);
						if (uk.contains(nk)) {
							uniqueAtThisDepth = false;
//							DebugInfo.debug("Still clashing: " + nk);
							break;
						}
						uk.add(nk);
					}
					
					if (uniqueAtThisDepth) {
//						for(String k: uk) {
//							DebugInfo.debug("Composite key: " + k);
//						}
						
						// Now, set the unique key
						for(JNode node: nodes) {
							if ( (node.type == JType.OBJECT) || (node.isSingleObjectArray()))
								continue;
							node.getUniqueKeyBasedOnDepth(depth, true);
							parameterNodes.add(node);
						}
						
						uniqueKeysAvailable = true;
						break;
					}
					
				}
				
				

				
			}
		}
	}

	
}
