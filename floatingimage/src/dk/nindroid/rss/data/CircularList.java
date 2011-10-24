package dk.nindroid.rss.data;

import android.util.Log;


public class CircularList<ImageRefeference> {
	ImageReference[] data;
	int position; 	// Where are we
	int prevLimit;  // How far back can we go?
	int prevData;   // How far back can we go, and still find data?
	int nextLimit;  // How far can we go?
	int nextData;   // How far can we go and still find data?
	
	public CircularList(int capacity){
		//Log.v("Floating List", "Creating Circular list");
		capacity += 3; // The two extremes, and ..?
		data = new ImageReference[capacity];
		position = capacity / 2;
		prevLimit = 0;
		nextLimit = capacity - 1;
		nextData = position + 1;
		prevData = position;
		//visualize(position);
	}
	
	public ImageReference next(ImageReference last){
		synchronized(data){
			while(position != dec(nextData)){
				position = inc(position);
				
				if(prevData == prevLimit){
					prevData = inc(prevData);
				}
				prevLimit = inc(prevLimit);
				nextLimit = inc(nextLimit);
				
				if(data[prevLimit] != null){
					data[prevLimit].recycleBitmap();
					data[prevLimit] = null;
				}
				
				//Log.v("Floating List", "next()");
				if(data[position] != null){
					ImageReference res = data[position];
					data[position] = last;
					//visualize(position);
					return res;
				}
				
			}
		}
		return null;
	}
	
	public ImageReference prev(ImageReference last){
		synchronized(data){
			while(position != prevData){
				
				if(nextData == nextLimit){
					nextData = dec(nextData);
				}
				prevLimit = dec(prevLimit);
				nextLimit = dec(nextLimit);
				
				if(data[nextLimit] != null){
					data[nextLimit].recycleBitmap();
					data[nextLimit] = null;
				}
				
				//Log.v("Floating List", "prev()");
				if(data[position] != null){
					ImageReference res = data[position];
					data[position] = last;
					position = dec(position);
					//visualize(position);
					return res;
				}
				position = dec(position);
			}
		}
		return null;
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
				data[nextData] = t;
				nextData = inc(nextData);

				//Log.v("Floating List", "addNext()");
				//visualize(nextData);
			}else{
				Log.v("Floating List", "Wasted work...");
				t.recycleBitmap();
			}
		}
	}
	
	public void addPrev(ImageReference t){
		synchronized(data){
			if(needPrev()){
				data[prevData] = t;
				prevData = dec(prevData);

				//Log.v("Floating List", "addPrev()");
				//visualize(prevData);
			}else{
				if(t.getBitmap() != null){
					t.recycleBitmap();
				}
				Log.v("Floating List", "Wasted work...");
			}
		}
	}
	
	public void clear(){
		synchronized(data){
			for(int i = 0; i < data.length; ++i){
				if(data[i] != null){
					data[i].recycleBitmap();
					data[i] = null;
				}
			}
			nextData = inc(position);
			prevData = position;
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
						}
					}
				}
				
				if(i == (prevData)){
					if(data[i] != null){
						sb2.append('[');
					}else{
						sb2.append('<');
					}
				}else if(i == (nextData)){
					if(data[i] != null){
						sb2.append(']');
					}else{
						sb2.append('>');
					}
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
