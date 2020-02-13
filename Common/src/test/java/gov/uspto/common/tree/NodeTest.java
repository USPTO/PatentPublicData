package gov.uspto.common.tree;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class NodeTest {

	@Test
	public void depth() {
		Tree tree = new Tree();
		tree.buildFromPath("one/two/three/four/five/six");
		Node node = tree.getChild(0).getChild(0);
		assertEquals(2, node.getDepth());
	}

	@Test
	public void getPath() {
		Tree tree = new Tree();
		tree.buildFromPath("one/two/three/four/five/six");
		Node node = tree.getChild(0).getChild(0);
		assertEquals("one/two", node.getPath());
	}

	@Test
	public void getPathFacets() {
		Tree tree = new Tree();
		tree.buildFromPath("one/two/three/four/five/six");
		Node node = tree.getChild(0).getChild(0);
		List<String> facets = node.getPathFacets();
		assertEquals("1/one/two", facets.get(1));
	}

	@Test
	public void getPathTree() {
		Tree tree = new Tree();
		tree.buildFromPath("one/two/three/four/five/six");
		Node node = tree.getChild(0).getChild(0);

		List<String> facets = node.getPathFacets(" ", false);

		//facets.stream().forEach(System.out::println);

		assertEquals("one two", facets.get(1));
	}

}
