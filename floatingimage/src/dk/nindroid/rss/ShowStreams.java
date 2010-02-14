package dk.nindroid.rss;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Toast;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.data.LocalImage;
import dk.nindroid.rss.menu.Settings;
import dk.nindroid.rss.orientation.OrientationManager;
import dk.nindroid.rss.settings.DirectoryBrowser;
import dk.nindroid.rss.settings.FeedsDbAdapter;

public class ShowStreams extends Activity {
	public static final int 			ABOUT_ID 		= Menu.FIRST;
	public static final int 			FULLSCREEN_ID	= Menu.FIRST + 1;
	public static final int 			SHOW_FOLDER_ID	= Menu.FIRST + 2;
	public static final int 			SETTINGS_ID 	= Menu.FIRST + 3;
	public static final int				SHOW_FOLDER_ACTIVITY = 13;
	public static final int				CONTEXT_GO_TO_SOURCE = Menu.FIRST;
	public static final int				CONTEXT_SAVE 	= Menu.FIRST + 1;
	public static final int				CONTEXT_BACKGROUND = Menu.FIRST + 2;
	public static final int				CONTEXT_SHARE 	= Menu.FIRST + 3;
	public static final int				MENU_IMAGE_CONTEXT = 13;
	public static final String 			version 		= "2.0.0";
	public static ShowStreams 			current;
	private GLSurfaceView 				mGLSurfaceView;
	private RiverRenderer 				renderer;
	private PowerManager.WakeLock 		wl;
	private OrientationManager			orientationManager;
	private MenuItem					selectedItem;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String dataFolder = getString(R.string.dataFolder);
		File sdDir = Environment.getExternalStorageDirectory();
		dataFolder = sdDir.getAbsolutePath() + dataFolder;
		File dataFile = new File(dataFolder);
		if(!sdDir.canWrite()){
			Toast error = Toast.makeText(this, "SD card not writeable (Or existant)\nCache will not work, operations might be flaky!", Toast.LENGTH_LONG);
			error.show();
		}else{
			if(!dataFile.exists() && !dataFile.mkdirs()){
				Toast error = Toast.makeText(this, "Error creating data folder (Do you have an SD card?)\nCache will not work, operations might be flaky!", Toast.LENGTH_LONG);
				error.show();
			}
		}
		try{
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);
			SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
			orientationManager = new OrientationManager(sensorManager);
			
			cleanIfOld();
			saveVersion(dataFile);
			GlowImage.init(this);
			ShadowPainter.init(this);
			BackgroundPainter.init(this);
			this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "Floating Image");
			ShowStreams.current = this;
			renderer = new RiverRenderer(true);
			orientationManager.addSubscriber(RiverRenderer.mDisplay);
			ClickHandler.init(renderer);
			//*
			setContentView(R.layout.main);
			mGLSurfaceView = new GLSurfaceView(this);
			mGLSurfaceView.setRenderer(renderer);
			setContentView(mGLSurfaceView);
		}catch(Throwable t){
			Log.e("Floating Image", "Uncaught exception caught!", t);
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
	
	public void openContextMenu(){
		this.registerForContextMenu(mGLSurfaceView);
		openContextMenu(mGLSurfaceView);
		this.unregisterForContextMenu(mGLSurfaceView);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		((Vibrator)ShowStreams.current.getSystemService(Activity.VIBRATOR_SERVICE)).vibrate(100l);
		ImageReference ir = renderer.getSelected();
		if(ir != null){
			super.onCreateContextMenu(menu, v, menuInfo);
			menu.add(0, CONTEXT_GO_TO_SOURCE, 0, R.string.go_to_source);
			if(!(ir instanceof LocalImage)){
				menu.add(0, CONTEXT_BACKGROUND, 0, R.string.set_as_background);
				menu.add(0, CONTEXT_SAVE, 0, R.string.save_image);
			}
		}else{
			Toast.makeText(this, "No image selected...", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Intent intent = null;
		ImageReference ir = null;
		switch(item.getItemId()){
			case CONTEXT_GO_TO_SOURCE:
				intent = renderer.followSelected();
				if(intent != null){
					startActivity(intent);
				}
				return true;
			case CONTEXT_BACKGROUND:
				ir = renderer.getSelected();
				Toast.makeText(this, "Setting background, please be patient...", Toast.LENGTH_LONG).show();
				ImageDownloader.setWallpaper(ir.getOriginalImageUrl(), ir.getTitle());
				return true;
			case CONTEXT_SAVE:
				ir = renderer.getSelected();
				ImageDownloader.downloadImage(ir.getOriginalImageUrl(), ir.getTitle());
				return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public FileOutputStream openFileOutput(String name, int mode)
			throws FileNotFoundException {
		return super.openFileOutput(name, mode);
	}
	
	@Override
	protected void onStop() {
		Log.v("Floating image", "Stopping!");
		super.onStop();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		dk.nindroid.rss.settings.Settings.readSettings(this);
		renderer.onResume();
		wl.acquire();
		orientationManager.onResume();
		mGLSurfaceView.onResume();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean res = super.onCreateOptionsMenu(menu);
		menu.clear();
		menu.add(0, ABOUT_ID, 0, R.string.about);
		if(RiverRenderer.mDisplay.isFullscreen()){
			menu.add(0, FULLSCREEN_ID, 0, R.string.show_details);
		}else{
			menu.add(0, FULLSCREEN_ID, 0, R.string.fullscreen);
		}
		if(dk.nindroid.rss.settings.Settings.showDirectory == null){
			menu.add(0, SHOW_FOLDER_ID, 0, R.string.show_folder);
		}else{
			menu.add(0, SHOW_FOLDER_ID, 0, R.string.cancel_show_folder);
		}
		menu.add(0, SETTINGS_ID, 0, R.string.settings);
		return res;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case ABOUT_ID:
			showAbout();
			return true;
		case FULLSCREEN_ID:
			RiverRenderer.mDisplay.toggleFullscreen();
			item.setTitle(RiverRenderer.mDisplay.isFullscreen() ? R.string.show_details : R.string.fullscreen);
			return true;
		case SHOW_FOLDER_ID:
			if(dk.nindroid.rss.settings.Settings.showDirectory == null){
				Intent showFolder = new Intent(this, DirectoryBrowser.class);
				startActivityForResult(showFolder, SHOW_FOLDER_ACTIVITY);
				selectedItem = item;
			}else{
				dk.nindroid.rss.settings.Settings.showDirectory = null;
				renderer.cancelShowFolder();
				item.setTitle(R.string.show_folder);
			}
			return true;
		case SETTINGS_ID:
			Intent showSettings = new Intent(this, Settings.class);
			startActivity(showSettings);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == SHOW_FOLDER_ACTIVITY && resultCode == RESULT_OK){
			Bundle b = data.getExtras();
			String path = (String)b.get("PATH");
			dk.nindroid.rss.settings.Settings.showDirectory = path;
			if(selectedItem != null){
				selectedItem.setTitle(R.string.cancel_show_folder);
			}
		}
	}
	
	private void showAbout(){
		Builder builder = new Builder(this);
		builder.setTitle(R.string.about);
		builder.setMessage(R.string.about_text);
		builder.setPositiveButton("Ok", null);
		builder.show();
	}
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		super.dispatchTouchEvent(ev);
		return ClickHandler.onTouchEvent(ev);
	}
	
	@Override 
	protected void onPause() {
		Log.v("Floating image", "Pausing...");
		mGLSurfaceView.onPause();
		renderer.onPause();
		wl.release();
		orientationManager.onPause();
		Log.v("Floating image", "Paused!");
		super.onPause();
	}

	void saveVersion(File dataFolder){
		File ver = new File(dataFolder.getAbsolutePath() + "/version");
		try {
			FileOutputStream fos = new FileOutputStream(ver);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			bos.write(version.getBytes());
			bos.close();
			fos.close();
		} catch (FileNotFoundException e) {
			Log.w("dk.nindroid.rss.ShowStreams", "Error writing version to sdcard");
		} catch (IOException e) {
			Log.w("dk.nindroid.rss.ShowStreams", "Error writing version to sdcard");
		}
	}
	
	private void cleanIfOld() {
		File ver = new File("/sdcard/floatingImage/version");
		String version = "0.0";
		if(ver.exists()){
			try {
				FileInputStream fis = new FileInputStream(ver);
				DataInputStream dis = new DataInputStream(fis);
				version = dis.readLine();
			}catch (IOException e) {
				Log.w("dk.nindroid.rss.ShowStreams", "Error reading old version file!");
			}
		}
		
		if(!ShowStreams.version.equals(version)){
			String oldCache = R.string.dataFolder + "/exploreCache";
			oldCache = Environment.getExternalStorageDirectory().getAbsolutePath() + oldCache;
			File dir = new File(oldCache);
			if(dir.exists()){
				ClearCache.deleteAll(dir);
			}
			addDefaultLocalPaths();			
		}
	}

	private void addDefaultLocalPaths() {
		FeedsDbAdapter mDbHelper = new FeedsDbAdapter(this);
		mDbHelper.open();
		mDbHelper.addFeed(DirectoryBrowser.ID, "/sdcard/DCIM");
		mDbHelper.addFeed(DirectoryBrowser.ID, "/sdcard/download");
		mDbHelper.close();
	}
}