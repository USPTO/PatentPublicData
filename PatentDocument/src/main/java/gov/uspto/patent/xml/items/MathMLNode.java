package gov.uspto.patent.xml.items;

import java.util.List;

import org.dom4j.Node;

import gov.uspto.parser.dom4j.ItemReader;

public class MathMLNode extends ItemReader<String> {

	public MathMLNode(Node itemNode) {
		super(itemNode);
	}

	@Override
	public String read() {

		StringBuilder sb = new StringBuilder();

		mathmlToString(sb, itemNode);

		return sb.toString();
	}

	public void mathmlToString(StringBuilder sb, Node node){

		@SuppressWarnings("unchecked")
		List<Node> children = node.selectNodes("*");
		
		if (isMrowOrMathOrMfenced(node.getName()) && children.size() <= 1) {
			if (children.size() == 1) {
				mathmlToString(sb, children.get(0));
			}
		}
		else {
			sb.append(node.getName());
		}

		if (children.size() > 1){
			sb.append("(");
            for (int i = 0; i < children.size(); i++) {
            	mathmlToString(sb, children.get(i));
            }
			sb.append(")");
		}
	}

    private static boolean isMrowOrMathOrMfenced(String nodeName) {
        return nodeName != null && (nodeName.equals("math") || nodeName.equals("mrow") || nodeName.equals("mfenced"));
    }
}
