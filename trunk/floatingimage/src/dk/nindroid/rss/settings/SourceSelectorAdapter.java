package dk.nindroid.rss.settings;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.Gravity;
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
		return mItems.length + 1;
	}

	@Override
	public Object getItem(int position) {
		if(position == mItems.length) return null;
		return mItems[position];
	}

	@Override
	public long getItemId(int position) {
		if(position == mItems.length) return 0l;
		return mItems[position].id;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(position == mItems.length){
			// Cancel item;
			TextView text = new TextView(mContext);
			text.setText(mContext.getString(R.string.cancel));
			text.setTextSize(20);
			text.setGravity(Gravity.CENTER_HORIZONTAL);
			text.setTextColor(Color.WHITE);
			text.setBackgroundColor(Color.DKGRAY);
			return text;
		}
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
