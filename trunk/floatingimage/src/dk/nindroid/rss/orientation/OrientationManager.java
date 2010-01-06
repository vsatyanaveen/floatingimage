package dk.nindroid.rss.orientation;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class OrientationManager implements SensorEventListener {
	private final static long ROTATION_DELAY = 100l; 
	private SensorManager				mSensorManager;
	List<OrientationSubscriber> subscribers;
	int currentOrientation = -1;
	int settingOrientation;
	private static Timer timer;
	
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
		
		int orientation = -1;
		if(y > 6.0f){
			orientation = OrientationSubscriber.UP_IS_UP;
		}else if(y < -6.0f){
			orientation = OrientationSubscriber.UP_IS_DOWN;
		}else if(x > 6.0f){
			orientation = OrientationSubscriber.UP_IS_LEFT;
		}else if(x < -6.0f){
			orientation = OrientationSubscriber.UP_IS_RIGHT;
		}
		synchronized(this){
			if(orientation != -1 && orientation != settingOrientation){
				settingOrientation = orientation;
				if(timer != null){
					timer.cancel();
				}
				timer = new Timer();
				timer.schedule(new ChangeOrientation(this), ROTATION_DELAY);
			}
		}
	}
}
