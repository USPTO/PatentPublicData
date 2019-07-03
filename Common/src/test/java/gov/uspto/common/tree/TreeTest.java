package gov.uspto.common.tree;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class TreeTest {

	@Test
	public void buildFromPath_LeafNodes() {
		Tree tree = new Tree();
		tree.buildFromPath("one/two/three/four/five/six");
		List<Node> leafNodes = tree.getLeafNodes();
		assertEquals("six", leafNodes.get(0).getName());
	}

	@Test
	public void buildFromList() {
		Tree tree = new Tree();
		List<Object> paths = new ArrayList<Object>();
		paths.add("one");
		paths.add(new String[] { "two", "three" });
		tree.buildFromList(paths);
		List<Node> leafNodes = tree.getLeafNodes();

		// leafNodes.stream().map(Node::getPath).forEach(System.out::println);

		assertEquals("one/two", leafNodes.get(0).getPath());
		assertEquals("one/three", leafNodes.get(1).getPath());
	}

	@Test
	public void buildFromListMap() {
		Tree tree = new Tree();
		List<Object> paths = new ArrayList<Object>();

		Map<String, List<String>> myMap = new LinkedHashMap<String, List<String>>();
		myMap.put("one", Arrays.asList(new String[] { "two", "three" }));
		myMap.put("four", Arrays.asList(new String[] { "five", "six" }));
		paths.add(myMap);

		tree.buildFromList(paths);
		List<Node> leafNodes = tree.getLeafNodes();

		// leafNodes.stream().map(Node::getPath).forEach(System.out::println);

		assertEquals("two", leafNodes.get(0).getName());
		assertEquals("three", leafNodes.get(1).getName());
	}

}
