package dk.nindroid.rss.data;


public class Progress {
	int percentDone;
	Object key = null;
	public synchronized void setKey(Object key){
		this.key = key;
	}
	public synchronized boolean isKey(Object key){
		return this.key == null ? false : this.key.equals(key);
	}
	public synchronized int getPercentDone(){
		return percentDone;		
	}
	public synchronized void setPercentDone(int percentDone){
		this.percentDone = percentDone;
	}
}
