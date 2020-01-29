package gov.uspto.parser.keyvalue;
/**
 * BRS Key Value format
 *
 *<p>Fields names are 3-4 characters and text is wrapped on a word break and indented</p>
 *
 *<pre>
 * WKU  039305848
 * APN  4584481
 * APT  1
 * ART  316
 * TTL Method for performing chip level electromagnetic interference reduction,
 *       and associated apparatus
 * URPN 2003/0169838
 *</pre>
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class SimpleKvReader extends KvReader {

	public SimpleKvReader(){
		super(3, 4, 5);
	}

	@Override
	public String keyTransform(String key) {
		return key.toUpperCase();
	}

	@Override
	public boolean isKeyValid(String key) {
		// Key length 3 or 4.
		if (key.length() > 5 || key.length() < 2) {
			return false;
		}

		char ch = key.charAt(0);
		if (!(ch >= 'A' && ch <= 'Z')) {
			return false;
		}

		for (int i = 1; i < key.length(); i++) {
			ch = key.charAt(i);
			if (!(ch >= '0' && ch <= '9') && !(ch >= 'A' && ch <= 'Z')) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String valueTransform(String key, String value) {
		return value;
	}

}
