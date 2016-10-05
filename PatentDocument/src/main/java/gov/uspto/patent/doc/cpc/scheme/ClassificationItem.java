package gov.uspto.patent.doc.cpc.scheme;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Joiner;

/**
 * ClassificationItem becomes a tree of ClassificationItems, each ClassificationItem can have sub ClassificationItems.
 * 
 *<p><pre>
 * A
 * |--A01
 *     |-- A01A
 *     |     |--A01A/01
 *     |
 *     |-- A01B
 *<p></pre>
 *
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class ClassificationItem {

	private final String symbol;
	private List<String> titleParts = new LinkedList<String>();
	private List<ClassificationItem> subClasses = new LinkedList<ClassificationItem>();
	
	public ClassificationItem(String symbol){
		this.symbol = symbol;
	}

	public void addTitlePart(String titlePartText){
		titleParts.add(titlePartText);
	}
	
	public void addTitlePart(List<String> titlePartText){
		titleParts.addAll(titlePartText);
	}

	public void addSubClassificationItem(ClassificationItem subItem){
		subClasses.add(subItem);
	}
	
	public String getTitleText(){
		return Joiner.on("/").join(titleParts);
	}
	
	public String getSymbol(){
		return symbol;
	}

	public List<ClassificationItem> getSubClassiticationItems(){
		return subClasses;
	}
	
	@Override
	public String toString() {
		return "ClassificationItem [symbol=" + symbol + ", titleParts=" + titleParts + ", subClasses=" + subClasses	+ "]\n";
	}

	/**
	 * Iterator which walks the ClassificationItem Tree, iterating over items and each item's sub-items.
	 */
	public Iterator<ClassificationItem> getItemIterator(){
		return new ClassificationItemIterator(this);
	}
}
