package dk.nindroid.rss.settings;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import dk.nindroid.rss.R;

public class DirectoryAdapter extends BaseAdapter{
	List<String> 		mFiles;
	DirectoryBrowser	mContext;
	int 				mImages;
	String				mCurrent;
	
	public DirectoryAdapter(DirectoryBrowser context, String current, List<String> files, int images){
		this.mFiles = files;
		this.mContext = context;
		this.mCurrent = current;
		this.mImages = images;
	}
	
	@Override
	public int getCount() {
		return mFiles.size() + 2;
	}

	@Override
	public Object getItem(int position) {
		return mFiles.get(position + 2);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout itemLayout;
		if(convertView != null && convertView instanceof LinearLayout){
			itemLayout = (LinearLayout)convertView;
		}else{
			itemLayout = (LinearLayout) LayoutInflater.from(mContext.getActivity()).inflate(R.layout.directory_browser, parent, false);
		}
		TextView text = (TextView) itemLayout.findViewById(android.R.id.title);
		ImageView icon = (ImageView) itemLayout.findViewById(R.id.icon);
		TextView summary = (TextView)itemLayout.findViewById(android.R.id.summary);
		
		if(position == 0){
			icon.setVisibility(ImageView.GONE);
			text.setText("..");
			summary.setVisibility(View.GONE);
		}else if(position == 1){
			String add = mContext.getString(R.string.add_this_directory, mCurrent);
			text.setText(add);
			icon.setVisibility(ImageView.VISIBLE);
			String amount = mContext.getResources().getQuantityString(R.plurals.n_images, mImages, mImages);
			summary.setText(amount);
			summary.setVisibility(View.VISIBLE);
		}else{
			summary.setVisibility(View.GONE);
			icon.setVisibility(ImageView.GONE);
			String file = mFiles.get(position - 2);
			text.setText(file);
		}
		
		return itemLayout;
	}
}
