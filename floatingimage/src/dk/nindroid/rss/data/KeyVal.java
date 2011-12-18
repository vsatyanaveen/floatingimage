package dk.nindroid.rss.data;

public class KeyVal<T, U> {
	T key;
	U val;
	public KeyVal(T key, U val){
		this.key = key;
		this.val = val;
	}
	public T getKey() {
		return key;
	}
	public void setKey(T key) {
		this.key = key;
	}
	public U getVal() {
		return val;
	}
	public void setVal(U val) {
		this.val = val;
	}
}
