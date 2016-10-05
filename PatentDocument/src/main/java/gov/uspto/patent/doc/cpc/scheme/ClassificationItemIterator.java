package gov.uspto.patent.doc.cpc.scheme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ClassificationItem Tree Iterator
 *
 *<p>
 * Iterator Walks the ClassificationItem item tree, which includes each ClassificationItem
 * and all sub ClassificationItems.
 *</p>
 *
 *<p>
 * An iterator chain is used, upon retrieving each ClassificaitonItem its subitem iterator is added to the chain of iterators.
 * Each iterator is exhausted before moving onto the next.
 *</p>
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class ClassificationItemIterator implements Iterator<ClassificationItem> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationItemIterator.class);

	private List<Iterator<ClassificationItem>> iteratorChain = new ArrayList<Iterator<ClassificationItem>>();
	private Iterator<ClassificationItem> currentIterator;
	private Iterator<ClassificationItem> lastUsedIterator;
	private int currentIteratorIndex = 0;

	public ClassificationItemIterator(ClassificationItem item) {
		iteratorChain.add(item.getSubClassiticationItems().iterator());
	}

	/**
	 * Updates the current iterator field to ensure that the current Iterator is not exhausted
	 */
	protected void updateCurrentIterator() {		
		if (currentIterator == null) {
			if (iteratorChain.isEmpty()) {
				currentIterator = Collections.emptyIterator();
			} else {
				currentIterator = (Iterator<ClassificationItem>) iteratorChain.get(0);
			}

			lastUsedIterator = currentIterator;
		}

		while (currentIterator.hasNext() == false && currentIteratorIndex < iteratorChain.size() - 1) {
			currentIteratorIndex++;
			currentIterator = (Iterator<ClassificationItem>) iteratorChain.get(currentIteratorIndex);
		}
	}

	@Override
	public boolean hasNext() {
		updateCurrentIterator();
		lastUsedIterator = currentIterator;

		return currentIterator.hasNext();
	}

	@Override
	public ClassificationItem next() {
		updateCurrentIterator();
		lastUsedIterator = currentIterator;

		ClassificationItem item = currentIterator.next();
		iteratorChain.add(item.getSubClassiticationItems().iterator());
		return item;
	}

	@Override
	public void remove() {
		updateCurrentIterator();
		lastUsedIterator.remove();
	}
}
