package gov.uspto.parser.keyvalue;

import java.nio.charset.Charset;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class KeyValue {
	private final String key;
	private String value;

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
		return "KeyValue [key=" + key + ", value=" + value + "]";
	}
}
