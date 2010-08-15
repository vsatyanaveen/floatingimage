package dk.nindroid.rss.settings;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import dk.nindroid.rss.R;

public class SourceSelectorAdapter extends BaseAdapter {
	Source[] 	mItems;
	private Context mContext;
	
	public SourceSelectorAdapter(Context context, Source[] items){
		this.mContext = context;
		this.mItems = items;
	}
	
	@Override
	public int getCount() {
		return mItems.length;
	}

	@Override
	public Object getItem(int position) {
		return mItems[position];
	}

	@Override
	public long getItemId(int position) {
		return mItems[position].id;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout itemLayout;
		Source s = mItems[position];
		if(convertView != null && convertView instanceof LinearLayout){
			itemLayout = (LinearLayout)convertView;
		}else{
			itemLayout = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.source_row, parent, false);
		}
		TextView text = (TextView) itemLayout.findViewById(android.R.id.title);
		ImageView icon = (ImageView) itemLayout.findViewById(R.id.icon);
		text.setText(s.name);
		icon.setImageBitmap(s.icon);
		return itemLayout;
	}

	public static class Source{
		String name;
		Bitmap icon;
		long id;
		public Source(String name, Bitmap icon, long id){
			this.name = name;
			this.icon = icon;
			this.id = id;
		}
	}
}
