package dk.nindroid.rss.orientation;

import java.util.ArrayList;
import java.util.List;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class OrientationManager implements SensorEventListener {
	private SensorManager				mSensorManager;
	List<OrientationSubscriber> subscribers;
	int currentOrientation = -1;
	int settingOrientation = -1;
	
	public OrientationManager(SensorManager sensorManager) {
		subscribers = new ArrayList<OrientationSubscriber>();
		mSensorManager = sensorManager;
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
				orientation = OrientationSubscriber.UP_IS_UP;
			}else{
				orientation = OrientationSubscriber.UP_IS_DOWN;
			}
		}else if(absX > absY && absX > absZ / 4.0f){
			if(x > 0){
				orientation = OrientationSubscriber.UP_IS_LEFT;
			}else{
				orientation = OrientationSubscriber.UP_IS_RIGHT;
			}
		}
		if(orientation != -1 && orientation != settingOrientation){
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
