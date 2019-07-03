package gov.uspto.common.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Simple Tree of Nodes
 * 
 * <p>
 * Useful in maintaining simple or complex hierarchies. Wither it's a simple
 * file path, or a complex multilevel classification.
 * </p>
 * 
 * @author Brian G. Feldman <brian.feldman@uspto.gov>
 * 
 */
public class Tree extends Node {

	public Tree() {
		super(null, "");
	}

	/**
	 * Parse path hierarchy to Tree
	 * 
	 * @param path with each segment delimited by backslash "/"
	 */
	public void buildFromPath(String path) {
		Node parentNode = this;
		for (String segment : path.split("/")) {
			Node child = new Node(parentNode, segment);
			parentNode.addChild(child);
			parentNode = child;
		}
	}

	public void buildFromArray(String[] segments) {
		build(this, Arrays.asList(segments), 1);
	}

	/**
	 * 
	 * @param segments
	 */
	public void buildFromList(Iterable<?> segments) {
		build(this, segments, 1);
	}

	private void build(Node parentNode, Iterable<?> segments, int level) {
		for (Object obj : segments) {
			if (obj instanceof String) {
				Node child = parentNode.addChild((String) obj);
				if (level == 1) {
					parentNode = child;
				}
			} else if (obj instanceof String[]) {
				parentNode.addChildren(Arrays.asList((String[]) obj));
			} else if (obj instanceof Map) {
				Map<String, Iterable> myMap = (Map<String, Iterable>) obj;
				level++;
				for (Entry<String, Iterable> entry : myMap.entrySet()) {
					Node child = parentNode.addChild(entry.getKey());
					build(child, entry.getValue(), level);
				}
			} else if (obj instanceof Collection) {
				System.out.println("Collection");
				parentNode.addChildren((Collection) obj);
			}
		}
	}

	public List<Node> getLeafNodes() {
		List<Node> leaveNodes = new ArrayList<Node>();
		return getLeafNodes(this, leaveNodes);
	}

	/**
	 * Get All Leaf Nodes.
	 * 
	 * @param startNode
	 * @param collectNodes
	 * @return
	 */
	public List<Node> getLeafNodes(Node startNode, List<Node> collectNodes) {
		for (Node node : startNode.getChildren()) {
			if (node.hasChildren()) {
				getLeafNodes(node, collectNodes);
			} else {
				collectNodes.add(node);
			}
		}
		return collectNodes;
	}

	/**
	 * Get All Leaf Facets
	 * 
	 * @return
	 */
	public List<String> getLeafFacets() {
		return getLeafNodes().stream().flatMap(l -> l.getPathFacets().stream()).distinct().collect(Collectors.toList());
	}

	/**
	 * Get All Leaf Facets
	 * 
	 * @return
	 */
	public List<String> getLeafFacets(String separator, boolean includeLevel) {
		return getLeafNodes().stream().flatMap(l -> l.getPathFacets(separator, includeLevel).stream()).distinct()
				.collect(Collectors.toList());
	}

	public List<String> getLeafPaths(String separator) {
		return getLeafNodes().stream().map(l -> l.getPath(separator)).collect(Collectors.toList());
	}

}
