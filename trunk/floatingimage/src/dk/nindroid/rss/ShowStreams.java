package dk.nindroid.rss;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.AlertDialog.Builder;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import dk.nindroid.rss.menu.Settings;

public class ShowStreams extends Activity {
	public static final int ABOUT_ID = Menu.FIRST;
	public static final int SETTINGS_ID = Menu.FIRST + 1;
	public static final String version = "1.2.1";
	public static ShowStreams current;
	private GLSurfaceView mGLSurfaceView;
	private RiverRenderer renderer;
	private PowerManager.WakeLock wl;
	private KeyguardLock lock;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String dataFolder = getString(R.string.dataFolder);
		new File(dataFolder).mkdirs();
		File error = new File(dataFolder + "main.err");
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(error);
		} catch (FileNotFoundException e) {
			Log.e("Floating Image", "Cannot create error stream.", e);
		}
		try{
			/** Test keyguard func. **/
			KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE);  
			lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);  
			lock.disableKeyguard();  
			/** END Test keyguard func. **/
			cleanIfOld();
			saveVersion();
			GlowImage.init(this);
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);
			this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "Floating Image");
			ShowStreams.current = this;
			renderer = new RiverRenderer(true);
			ClickHandler.init(renderer);
			//*
			setContentView(R.layout.main);
			mGLSurfaceView = new GLSurfaceView(this);
			mGLSurfaceView.setRenderer(renderer);
			setContentView(mGLSurfaceView);
		}catch(Throwable t){
			try {
				Log.e("Floating Image", "Uncaught exception caught!", t);
				fos.write(t.getMessage().getBytes());
				fos.flush();
				fos.close();
			} catch (IOException e) {
			}
		}
		//*/
		/*
		mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setGLWrapper(new GLSurfaceView.GLWrapper() {
            public GL wrap(GL gl) {
                return new MatrixTrackingGL(gl);
            }});
        mGLSurfaceView.setRenderer(new SpriteTextRenderer(	));
        setContentView(mGLSurfaceView);
        //*/
		/*
		String feed = "http://api.flickr.com/services/feeds/photos_public.gne?id=73523270@N00&lang=en-us&format=rss_200";
		TextureBank bank = new TextureBank(60, 5);
		bank.addStream(feed);
		//*/
		/*
		mGLSurfaceView = new GLSurfaceView(this);
		mGLSurfaceView.setEGLConfigChooser(false);
		mGLSurfaceView.setRenderer(new TriangleRenderer(this));
		setContentView(mGLSurfaceView);
		//*/
		/*
	 	mGLSurfaceView = new GLSurfaceView(this);
		mGLSurfaceView.setEGLConfigChooser(false);
		mGLSurfaceView.setRenderer(new Lesson06(this));
		setContentView(mGLSurfaceView);
		//*/
	}

	@Override
	public FileOutputStream openFileOutput(String name, int mode)
			throws FileNotFoundException {
		return super.openFileOutput(name, mode);
	}
	
	@Override
	protected void onStop() {
		/** Test keyguard func. **/
		if(lock != null){  
			lock.reenableKeyguard();
		}
		/** END Test keyguard func. **/
		Log.v("Floating image", "Stopping!");
		super.onStop();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		dk.nindroid.rss.settings.Settings.readSettings(this);
		renderer.onResume();
		wl.acquire();
		mGLSurfaceView.onResume();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean res = super.onCreateOptionsMenu(menu);
		menu.add(0, ABOUT_ID, 0, R.string.about);
		menu.add(0, SETTINGS_ID, 0, R.string.settings);
		return res;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case ABOUT_ID:
			showAbout();
			return true;
		case SETTINGS_ID:
			Intent showSettings = new Intent(this, Settings.class);
			startActivity(showSettings);
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void showAbout(){
		Builder builder = new Builder(this);
		builder.setTitle(R.string.about);
		builder.setMessage(R.string.about_text);
		builder.setPositiveButton("Ok", null);
		builder.show();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return ClickHandler.onTouchEvent(event);
	}
	@Override 
	protected void onPause() {
		super.onPause();
		mGLSurfaceView.onPause(); // I really should do this...
		renderer.onPause();
		wl.release();
	}

	void fillData() {

	}
	void saveVersion(){
		File ver = new File("/sdcard/floatingImage/version");
		try {
			new File("/sdcard/floatingImage").mkdirs();
			FileOutputStream fos = new FileOutputStream(ver);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			bos.write(version.getBytes());
		} catch (FileNotFoundException e) {
			Log.w("dk.nindroid.rss.ShowStreams", "Error writing version to sdcard");
		} catch (IOException e) {
			Log.w("dk.nindroid.rss.ShowStreams", "Error writing version to sdcard");
		}
	}
	
	private void cleanIfOld() {
		File ver = new File("/sdcard/floatingImage/version");
		try {
			FileInputStream fis = new FileInputStream(ver);
			
			DataInputStream dis = new DataInputStream(fis);
			String version = dis.readLine();
			if(!ShowStreams.version.equals(version)){
				File oldCache = new File(getString(R.string.dataFolder) + "/exploreCache");
				if(oldCache.exists()){
					ClearCache.deleteAll(oldCache);
				}
			}
		}catch (IOException e) {
			Log.w("dk.nindroid.rss.ShowStreams", "Error reading old version file!");
		}
	}
}