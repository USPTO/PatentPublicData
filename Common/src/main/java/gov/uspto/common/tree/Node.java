package gov.uspto.common.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/**
 * Tree Node
 *
 * @author Brian G. Feldman <brian.feldman@uspto.gov>
 *
 */
public class Node {
	private final Node parent;
	private final String name;
	private final LinkedHashMap<String, Comparable<?>> attributes = new LinkedHashMap<String, Comparable<?>>();
	private final List<Node> children = new ArrayList<Node>();

	public Node(Node parent, final String name) {
		this.parent = parent;
		this.name = name;
	}

	public boolean addChild(Node node) {
		return children.add(node);
	}

	public Node addChild(String nodeName) {
		Node newNode = new Node(this, nodeName);
		addChild(newNode);
		return newNode;
	}

	public void addChildren(Iterable<String> nodeNames) {
		for (String name : nodeNames) {
			addChild(name);
		}
	}

	public void addAttrib(String name, Comparable<?> value) {
		attributes.put(name, value);
	}

	public boolean hasAttrib(String name) {
		return attributes.containsKey(name);
	}

	public boolean hasParent() {
		return parent != null;
	}

	public boolean isRoot() {
		return !hasParent();
	}

	public boolean hasChildren() {
		return !children.isEmpty();
	}

	public boolean isLeaf() {
		return !hasChildren();
	}

	public String getName() {
		return this.name;
	}

	public Node getParent() {
		return parent;
	}

	public Node getChild(int num) {
		return children.get(num);
	}

	public List<Node> getChildren() {
		return children;
	}

	public Comparable<?> getAttrib(String name) {
		return attributes.get(name);
	}

	public int getDepth() {
		return (int) getPathSegments().stream().filter(p -> Objects.nonNull(p)).count();
	}

	/**
	 * Get Path Hierarchy using default slash '/' separator
	 * 
	 * @param separator
	 * @return
	 */
	public String getPath() {
		return getPath("/");
	}

	/**
	 * Get Path Hierarchy using provided separator to delineate parts of the path
	 * 
	 * @param separator
	 * @return
	 */
	public String getPath(final String separator) {
		return String.join(separator, getPathSegments());
	}

	public List<String> getPathSegments() {
		List<String> segments = new ArrayList<String>();
		segments.add(this.getName());
		if (parent != null) {
			Node currentParent = parent;
			while (currentParent.getParent() != null) {
				segments.add(currentParent.getName());
				currentParent = currentParent.getParent();
			}
		}
		Collections.reverse(segments);
		return segments;
	}

	/**
	 * Get Path Facets
	 * 
	 * <pre>
	 * D/07/B/22/01/2051 =>
	 * 
	 * 5/D/07/B/2201/2051 4/D/07/B/2201 3/D/07/B 2/D/07 1/D
	 * 
	 */
	public List<String> getPathFacets() {
		return getPathFacets("/", true);
	}

	/**
	 * Get Path Facet using provided separator to delineate parts of the path
	 * 
	 * <pre>
	 * Example to create "text tree" using space for separator and includeLevel=false
	 *  
	 * D/07/B/22/01/2051 =>
	 * 
	 * D 07 B 22 01 2051
	 * D 07 B 22 01
	 * D 07 B 22
	 * D 07 B
	 * D 07
	 * D
	 * </pre>
	 * 
	 * @param separator
	 * @param includeLevel
	 * @return
	 */
	public List<String> getPathFacets(final String separator, boolean includeLevel) {
		List<String> segments = getPathSegments();
		List<String> facets = new ArrayList<String>();
		StringBuilder currentPath = new StringBuilder();
		for (int i = 0; i < segments.size();) {
			currentPath.append(separator).append(segments.get(i));
			if (includeLevel) {
				facets.add(i++ + currentPath.toString());
			} else {
				facets.add(currentPath.toString().trim());
				i++;
			}
		}

		return facets;
	}

	@Override
	public String toString() {
		return "Node [name=" + name + ", attributes=" + attributes + ", children=" + children + ", getDepth()="
				+ getDepth() + ", getPath()=" + getPath() + "]";
	}

}
