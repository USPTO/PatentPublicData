package gov.uspto.parser.keyvalue;

import java.nio.charset.Charset;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class KeyValue {
	private final String key;
	private String value;
	private String originalKey;

	public KeyValue(String key, String value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public int hashCode() {
		 HashFunction hash = Hashing.murmur3_128();
		 return hash.hashString(key+value, Charset.defaultCharset()).asInt();
		 //return key.hashCode() + value.hashCode();
	}

	public String getKey() {
		return key;
	}

	public String getKeyOriginal() {
		if (originalKey == null) {
			return key;
		}
		return originalKey;
	}

	/**
	 * Set Original Keyname; useful when renaming field name to keep track of original name.
	 * @param originalKeyName
	 */
	public KeyValue setKeyOriginal(String originalKeyName) {
		this.originalKey = originalKeyName;
		return this;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void appendValue(String value) {
		this.value = this.value + " " + value;
	}

	public void appendValueNoSpace(String value) {
		this.value = this.value + value;
	}

    @Override
    public boolean equals(Object other){
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        KeyValue kv = (KeyValue) other;
        return this.key.equals(kv.key) && value.equals(kv.value);
    }

	@Override
	public String toString() {
		return "KeyValue [key=" + key + ", value=" + value + ", originalKey=" + originalKey + "]";
	}
}
