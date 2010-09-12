package dk.nindroid.rss.orientation;

import java.util.ArrayList;
import java.util.List;

import dk.nindroid.rss.settings.Settings;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Surface;

public class OrientationManager implements SensorEventListener {
	private SensorManager				mSensorManager;
	List<OrientationSubscriber> subscribers;
	int currentOrientation = -1;
	int settingOrientation = -1;
	int initialRotation = 0;
	
	public OrientationManager(SensorManager sensorManager, int initialRotation) {
		subscribers = new ArrayList<OrientationSubscriber>();
		mSensorManager = sensorManager;
		this.initialRotation = initialRotation != -1 ? initialRotation : 0;
	}
	
	public void addSubscriber(OrientationSubscriber subscriber){
		synchronized(this){
			subscribers.add(subscriber);
		}
	}
	
	public void onPause(){
		mSensorManager.unregisterListener(this);
	}
	
	public void onResume(){
		currentOrientation = -1; // Resend orientation info!
		settingOrientation = -1; // Resend orientation info!
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(SensorManager.SENSOR_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);
		Log.v("Orientation manager", "Resume!");
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// I don't care.
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		
		float absX = Math.abs(x);
		float absY = Math.abs(y);
		float absZ = Math.abs(z);
		
		int orientation = -1;
		if(absY > absX && absY > absZ / 4.0f){
			if(y > 0){
				orientation = Surface.ROTATION_0;
			}else{
				orientation = Surface.ROTATION_180;
			}
		}else if(absX > absY && absX > absZ / 4.0f){
			if(x > 0){
				orientation = Surface.ROTATION_270;
			}else{
				orientation = Surface.ROTATION_90;
			}
		}
		if(orientation != -1 && orientation != settingOrientation){
			// Adjust for rotated devices
			orientation = (orientation + 4 - initialRotation + Settings.forceRotation) % 4;
			settingOrientation = orientation;
			setOrientation();
		}
	}
	
	void setOrientation(){
		if(settingOrientation != currentOrientation){
			currentOrientation = settingOrientation;
			for(OrientationSubscriber os : subscribers){
				os.setOrientation(currentOrientation);
			}
		}
	}
}
