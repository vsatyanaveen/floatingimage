package dk.nindroid.rss.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import dk.nindroid.rss.R;

public class ManageFeedPreference extends Preference implements OnClickListener {
	Bitmap 		mIcon;
	boolean 	mEnabled = true;
	Context 	mContext;
	
	public ManageFeedPreference(Context context, Bitmap icon) {
		super(context);
		this.mIcon = icon;
		this.mContext = context;
		setLayoutResource(R.layout.feeds_row);
	}
	
	@Override
	protected boolean callChangeListener(Object newValue) {
		mEnabled = (Boolean)newValue;
		persistBoolean(mEnabled);
		return true;
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		View frame = view.findViewById(android.R.id.widget_frame);
		frame.setLongClickable(true);
		frame.setOnClickListener(this);
		final CheckBox checkbox = (CheckBox)view.findViewById(R.id.enabled);
		if(checkbox != null){
			checkbox.setChecked(mEnabled);
		}
		final ImageView icon = (ImageView)view.findViewById(R.id.icon);
		if(icon != null){
			icon.setImageBitmap(this.mIcon);
		}
	}
	
	@Override
	protected void onClick() {
		mEnabled ^= true;
		persistBoolean(mEnabled);
		notifyChanged();
		super.onClick();
	}
	
	@Override
	public void onClick(View v) {
		this.onClick();
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
            persistBoolean(value);
        }
	}
	
	@Override
    protected Parcelable onSaveInstanceState() {
        /*
         * Suppose a client uses this preference type without persisting. We
         * must save the instance state so it is able to, for example, survive
         * orientation changes.
         */
        
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        // Save the instance state
        final SavedState myState = new SavedState(superState);
        myState.enabled = mEnabled;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }
     
        // Restore the instance state
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        mEnabled = myState.enabled;
        notifyChanged();
    }
    
    /**
     * SavedState, a subclass of {@link BaseSavedState}, will store the state
     * of MyPreference, a subclass of Preference.
     * <p>
     * It is important to always call through to super methods.
     */
    private static class SavedState extends BaseSavedState {
        boolean enabled;
        
        public SavedState(Parcel source) {
            super(source);
            
            // Restore the click counter
            enabled = source.readInt() != 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            
            // Save the click counter
            dest.writeInt(enabled ? 1 : 0);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }
}
