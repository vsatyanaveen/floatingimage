package dk.nindroid.rss.data;

import android.util.Log;


public class CircularList<ImageRefeference> {
	ImageReference[] data;
	int position; 	// Where are we
	int prevOffset; // What's the offset to get to the previous non-active picture 
	int nextOffset; // What's the offset to get to the next non-active picture
	int prevLimit;  // How far back can we go?
	int prevData;   // How far back can we go, and still find data?
	int nextLimit;  // How far can we go?
	int nextData;   // How far can we go and still find data?
	
	public CircularList(int capacity, int active){
		//Log.v("Floating Image", "Creating Circular list");
		capacity += active;
		data = new ImageReference[capacity];
		position = capacity / 2;
		prevLimit = 0;
		nextLimit = capacity - 1;
		prevOffset = active / 2;
		nextOffset = active - prevOffset - 1;
		nextData = position + nextOffset;
		prevData = position - prevOffset;
		//visualize(position);
	}
	
	public ImageReference next(){
		synchronized(data){
			if(hasNext()){
				position = inc(position);
				if(data[prevLimit] != null){
					if(data[prevLimit].getBitmap() != null){
						data[prevLimit].getBitmap().recycle();
					}
					data[prevLimit] = null;
				}
				if(prevData == prevLimit){
					prevData = inc(prevData);
				}
				prevLimit = inc(prevLimit);
				nextLimit = inc(nextLimit);
				
				//Log.v("Floating List", "next()");
				//visualize((position + nextOffset) % data.length);
				return data[(position + nextOffset) % data.length];
			}else{
				return null;
			}
		}
	}
	
	public ImageReference prev(){
		synchronized(data){
			if(hasPrev()){
				position = dec(position);
				if(data[nextLimit] != null){
					if(data[nextLimit].getBitmap() != null){
						data[nextLimit].getBitmap().recycle();
					}
					data[nextLimit] = null;
				}
				if(nextData == nextLimit){
					nextData = dec(nextData);
				}
				prevLimit = dec(prevLimit);
				nextLimit = dec(nextLimit);
				
				//Log.v("Floating List", "prev()");
				//visualize((position + data.length - prevOffset) % data.length);
				return data[(position + data.length - prevOffset) % data.length];
			}else{
				return null;
			}
		}
	}
	
	private boolean hasNext(){
		return nextData != (position + nextOffset) % data.length;
	}
	
	private boolean hasPrev(){
		return prevData != (position - prevOffset + data.length) % data.length;
	}
	
	public boolean needPrev(){
		return prevLimit != prevData;
	}
	
	public boolean needNext(){
		return nextLimit != nextData;		
	}
	
	public void addNext(ImageReference t){
		synchronized(data){
			if(needNext()){
				nextData = inc(nextData);
				data[nextData] = t;

				//visualize(nextData);
			}else{
				//Log.v("Floating List", "Wasted work...");
				t.getBitmap().recycle();
			}
		}
	}
	
	public void addPrev(ImageReference t){
		synchronized(data){
			if(needPrev()){
				prevData = dec(prevData);
				data[prevData] = t;

				//visualize(prevData);
			}else{
				if(t.getBitmap() != null){
					t.getBitmap().recycle();
				}
				//Log.v("Floating List", "Wasted work...");
			}
		}
	}
	
	public void clear(){
		synchronized(data){
			for(int i = 0; i < data.length; ++i){
				if(data[i] != null){
					if(data[i].getBitmap() != null){
						data[i].getBitmap().recycle();
					}
					data[i] = null;
				}
			}
			nextData = position + nextOffset;
			prevData = position - prevOffset;
			//Log.v("Floating List", "clear()");
			//visualize(position);
		}
	}
	
	int inc(int num){
		return (num + 1) % data.length;
	}
	
	int dec(int num){
		return (num - 1 + data.length) % data.length;
	}
	
	/** DEBUG **/
	
	public boolean contains(ImageReference ir){
		for(int i = 0; i < data.length; ++i){
			if(ir == data[i]){
				return true;
			}
		}
		return false;
	}
	
	private void visualize(int pos){
		synchronized (data) {
			StringBuilder sb1 = new StringBuilder();
			StringBuilder sb2 = new StringBuilder();
			for(int i = 0; i < data.length; ++i){
				if(i == position){
					sb1.append('!');
				}
				else if(i == (position - prevOffset + data.length) % data.length){
					sb1.append('[');
				}
				else if(i == (position + nextOffset) % data.length){
					sb1.append(']');
				}
				else if(i == (prevLimit)){
					sb1.append('p');
				}
				else if(i == (nextLimit)){
					sb1.append('n');
				}
				else{
					sb1.append('-');
				}
				
				ImageReference ir = data[i];
				char noneChar = '-';
				if(ir != null){
					for(int j = 0; j < data.length; ++j){
						if(j != i && data[j] != null && data[j].getBitmap().equals(ir.getBitmap())){
							noneChar = 'D';
							Log.v("Floating Image", "Same ref? " + (ir == (data[j])));
						}
					}
				}
				
				if(i == (prevData)){
					sb2.append('<');
				}else if(i == (nextData)){
					sb2.append('>');
				}else if(data[i] == null){
					sb2.append('0');
				}else if(data[i].getBitmap().isRecycled()){
					sb2.append('*');
				}else{
					sb2.append(noneChar);
				}
			}
			Log.v("Floating List", sb1.toString());
			Log.v("Floating List", sb2.toString());
		}
	}
	
	public void dumpList(){
		synchronized(data){
			for(int i = 0; i < data.length; ++i){
				if(data[i] == null){
					Log.e("Floating List", i + " NULL");
				}else{
					Log.e("Floating List", i + " " + data[i].getImageID());
				}
			}
		}
	}
}
