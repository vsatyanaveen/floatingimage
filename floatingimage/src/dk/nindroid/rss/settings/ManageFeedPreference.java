package dk.nindroid.rss.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.preference.Preference;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import dk.nindroid.rss.R;

public class ManageFeedPreference extends Preference {
	Bitmap 		mIcon;
	boolean 	mEnabled = true;
	Context 	mContext;
	boolean 	mHideCheckBox;
	
	public ManageFeedPreference(Context context, Bitmap icon, boolean hideCheckBox) {
		super(context);
		this.mIcon = icon;
		this.mContext = context;
		this.mHideCheckBox = hideCheckBox;
		setLayoutResource(R.layout.feeds_row);
	}
	
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		final CheckBox checkbox = (CheckBox)view.findViewById(R.id.enabled);
		checkbox.setOnCheckedChangeListener(new CheckedChanged());
		
		if(checkbox != null){
			checkbox.setChecked(mEnabled);
		}
		final ImageView icon = (ImageView)view.findViewById(R.id.icon);
		if(icon != null){
			icon.setImageBitmap(this.mIcon);
		}
		
		if(mHideCheckBox){
			checkbox.setVisibility(View.GONE);
		}
	}
	
	class CheckedChanged implements OnCheckedChangeListener{
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			mEnabled = isChecked;
		}
	}
	
	public boolean isActive(){
		return mEnabled;
	}
	
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getBoolean(index, true);
	}
	
	
	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		if (restorePersistedValue) {
            // Restore state
            mEnabled = getPersistedBoolean(true);
        } else {
            // Set state
            Boolean value = (Boolean) defaultValue;
            mEnabled = value;
        }
	}
}
