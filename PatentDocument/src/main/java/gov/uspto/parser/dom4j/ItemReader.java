package gov.uspto.parser.dom4j;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Items are recurring Nodes which can occur in a number of other nodes / fragments. 
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 * @param <T>
 */
public abstract class ItemReader<T> implements Reader<T> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ItemReader.class);

	protected Node itemNode;

	public ItemReader(Node itemNode) {
		this.itemNode = itemNode;
	}

	public boolean hasChildren(){
		return ((Element) itemNode).nodeCount() > 0;
	}

	public ItemReader(Node itemNode, String expectedNodeName) {
		if (itemNode.getName().equals(expectedNodeName)) {
			// Check if current node matches what is exepected.
			this.itemNode = itemNode;
		} else if (itemNode.selectSingleNode(expectedNodeName) != null) {
			// Check if Child Node Matches.
			this.itemNode = itemNode.selectSingleNode(expectedNodeName);
		} else {
			this.itemNode = itemNode.selectSingleNode("//" + expectedNodeName);
		}

		// Avoid NPE by creating empty node.
		if (this.itemNode == null) {
			LOGGER.warn("Could Not Find XML Fragment: {} in parent: {}", expectedNodeName, itemNode.getName());
			this.itemNode = (Node) DocumentHelper.createElement("");
		}
	}

	public ItemReader(Node itemNode, String expectedNodeName, String elsePrefix) {
		if (itemNode.getName().equals(expectedNodeName)) {
			// Check if current node matches what is exepected.
			this.itemNode = itemNode;
		} else if (itemNode.getName().endsWith(elsePrefix)) {
			this.itemNode = itemNode;
		} else {
			// Check if Child Node Matches.
			this.itemNode = itemNode.selectSingleNode(expectedNodeName);
		}

		// Avoid NPE by creating empty node.
		if (this.itemNode == null) {
			LOGGER.warn("Could Not Find XML Fragment: {} in parent: {}", expectedNodeName, itemNode.getName());
			this.itemNode = (Node) DocumentHelper.createElement("");
		}
	}

	public void setItemNode(Node itemNode) {
		this.itemNode = itemNode;
	}
}
