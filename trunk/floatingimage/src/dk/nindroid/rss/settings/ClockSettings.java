package dk.nindroid.rss.settings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import dk.nindroid.rss.R;

public class ClockSettings extends Activity {
	public static final String SHARED_PREFS_NAME = "SHARED_PREFS_NAME";
	public static final String X_POSITION = "clock_placement_x";
	public static final String Y_POSITION = "clock_placement_y";
	public static final String VISIBLE	  = "clock_visible";
	
	BoxPlacement mBoxPlacement;
	CheckBox mVisible;
	String mSettings;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSettings = this.getIntent().getExtras().getString(SHARED_PREFS_NAME);
		setContentView(R.layout.clock_placement);
		findViewById(R.id.centerX).setOnClickListener(new OnCenterListener(true));
		findViewById(R.id.centerY).setOnClickListener(new OnCenterListener(false));
		mVisible = (CheckBox)findViewById(R.id.visible);
		mBoxPlacement = (BoxPlacement)findViewById(R.id.box);
		mVisible.setOnCheckedChangeListener(new VisibleCheckedChanged());
	}
	
	private class VisibleCheckedChanged implements OnCheckedChangeListener{
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			mBoxPlacement.setVisible(isChecked);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences sp = getSharedPreferences(mSettings, 0);
		float x = sp.getFloat(X_POSITION, 0.75f);
		float y = -sp.getFloat(Y_POSITION, 0.8f);
		boolean visible = sp.getBoolean(VISIBLE, true);
		mBoxPlacement.setPosX(x);
		mBoxPlacement.setPosY(y);
		mBoxPlacement.setVisible(visible);
		mVisible.setChecked(visible);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Editor e = getSharedPreferences(mSettings, 0).edit();
		e.putFloat(X_POSITION, mBoxPlacement.getPosX());
		e.putFloat(Y_POSITION, -mBoxPlacement.getPosY());
		e.putBoolean(VISIBLE, mVisible.isChecked());
		e.commit();
	}
	
	private class OnCenterListener implements android.view.View.OnClickListener{
		boolean isX;
		
		public OnCenterListener(boolean isX){
			this.isX = isX;
		}

		@Override
		public void onClick(View v) {
			mBoxPlacement.center(isX);
		}
		
		
	}
}
