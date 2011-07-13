package dk.nindroid.rss.settings;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import dk.nindroid.rss.R;

public class DirectoryAdapter extends BaseAdapter implements OnClickListener{
	List<String> 		mFiles;
	DirectoryBrowser	mContext;
	
	public DirectoryAdapter(DirectoryBrowser context, List<String> files){
		this.mFiles = files;
		this.mContext = context;
	}
	
	@Override
	public int getCount() {
		return mFiles.size();
	}

	@Override
	public Object getItem(int position) {
		return mFiles.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout itemLayout;
		String file = mFiles.get(position);
		if(convertView != null && convertView instanceof LinearLayout){
			itemLayout = (LinearLayout)convertView;
		}else{
			itemLayout = (LinearLayout) LayoutInflater.from(mContext.getActivity()).inflate(R.layout.directory_browser, parent, false);
		}
		TextView text = (TextView) itemLayout.findViewById(android.R.id.title);
		ImageView icon = (ImageView) itemLayout.findViewById(R.id.icon);
		if(position == 0){
			icon.setVisibility(ImageView.INVISIBLE);
		}else{
			icon.setVisibility(ImageView.VISIBLE);
			icon.setOnClickListener(this);
		}
		text.setText(file);
		return itemLayout;
	}

	@Override
	public void onClick(View v) {
		if(v instanceof ImageView){
			LinearLayout parent = (LinearLayout)(v.getParent());
			TextView textView = (TextView)(parent.findViewById(android.R.id.title));
			mContext.returnResult(mContext.currentDirectory.getAbsolutePath() + "/" + textView.getText());
		}
	}
}
