package dk.nindroid.rss;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Toast;
import dk.nindroid.rss.data.FeedReference;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.data.LocalImage;
import dk.nindroid.rss.launchers.ReadFeeds;
import dk.nindroid.rss.menu.Settings;
import dk.nindroid.rss.orientation.OrientationManager;
import dk.nindroid.rss.parser.ParserProvider;
import dk.nindroid.rss.parser.flickr.FlickrParser;
import dk.nindroid.rss.renderers.FloatingRenderer;
import dk.nindroid.rss.renderers.Renderer;
import dk.nindroid.rss.renderers.floating.BackgroundPainter;
import dk.nindroid.rss.renderers.floating.GlowImage;
import dk.nindroid.rss.renderers.floating.ShadowPainter;
import dk.nindroid.rss.renderers.slideshow.SlideshowRenderer;
import dk.nindroid.rss.settings.FeedsDbAdapter;
import dk.nindroid.rss.settings.SourceSelector;

public class ShowStreams extends Activity {
	public static final int 			ABOUT_ID 		= Menu.FIRST;
	public static final int 			FULLSCREEN_ID	= Menu.FIRST + 1;
	public static final int 			SHOW_FOLDER_ID	= Menu.FIRST + 2;
	public static final int 			SETTINGS_ID 	= Menu.FIRST + 3;
	public static final int				SHOW_ACTIVITY 	= 13;
	public static final int				CONTEXT_GO_TO_SOURCE = Menu.FIRST;
	public static final int				CONTEXT_SAVE 	= Menu.FIRST + 1;
	public static final int				CONTEXT_BACKGROUND = Menu.FIRST + 2;
	public static final int				CONTEXT_SHARE 	= Menu.FIRST + 3;
	public static final int				MENU_IMAGE_CONTEXT = 13;
	public static final int				MISC_ROW_ID		= 201;
	public static final String 			version 		= "2.2.5";
	public static ShowStreams 			current;
	private boolean 					mShowing = false;
	private GLSurfaceView 				mGLSurfaceView;
	private RiverRenderer 				renderer;
	private PowerManager.WakeLock 		wl;
	private OrientationManager			orientationManager;
	private MenuItem					selectedItem;
	private FeedController				mFeedController;
	private ImageCache 					mImageCache;
	private TextureBank					mTextureBank;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		registerParsers();
		String dataFolder = getString(R.string.dataFolder);
		File sdDir = Environment.getExternalStorageDirectory();
		dataFolder = sdDir.getAbsolutePath() + dataFolder;
		File dataFile = new File(dataFolder);
		if(!dataFile.exists() && !dataFile.mkdirs()){
			Toast error = Toast.makeText(this, "Error creating data folder (Do you have an SD card?)\nCache will not work, operations might be flaky!", Toast.LENGTH_LONG);
			error.show();
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
			mTextureBank = setupFeeders();
			renderer = new RiverRenderer(true, mTextureBank);
			orientationManager.addSubscriber(RiverRenderer.mDisplay);
			ClickHandler.init(renderer);
			setContentView(R.layout.main);
			mGLSurfaceView = new GLSurfaceView(this);
			mGLSurfaceView.setRenderer(renderer);
			setContentView(mGLSurfaceView);
		}catch(Throwable t){
			Log.e("Floating Image", "Uncaught exception caught!", t);
		}
	}
	
	void registerParsers(){
		ParserProvider.registerParser(dk.nindroid.rss.settings.Settings.TYPE_FLICKR, FlickrParser.class);
	}
	
	TextureBank setupFeeders(){
		TextureBank bank = new TextureBank(15);
		mFeedController = new FeedController();
		BitmapDownloader bitmapDownloader = new BitmapDownloader(bank, mFeedController);
		mImageCache = new ImageCache(bank);
		bank.setFeeders(bitmapDownloader, mImageCache);
		return bank;
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
		Renderer defaultRenderer = null;
		if(dk.nindroid.rss.settings.Settings.mode == dk.nindroid.rss.settings.Settings.MODE_FLOATING_IMAGE){
			defaultRenderer = new FloatingRenderer(mTextureBank);
		}else{
			defaultRenderer = new SlideshowRenderer(mTextureBank);
		}
		renderer.setRenderer(defaultRenderer);
		renderer.onResume();
		wl.acquire();
		orientationManager.onResume();
		mGLSurfaceView.onResume();
		if(!mShowing){ // Feed is already loaded!
			ReadFeeds.runAsync(mFeedController);
		}
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
		if(!mShowing){
			menu.add(0, SHOW_FOLDER_ID, 0, R.string.show);
		}else{
			menu.add(0, SHOW_FOLDER_ID, 0, R.string.cancel_show);
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
			dk.nindroid.rss.settings.Settings.setFullscreen(RiverRenderer.mDisplay.isFullscreen());
			return true;
		case SHOW_FOLDER_ID:
			if(!mShowing){
				Intent showFolder = new Intent(this, SourceSelector.class);
				startActivityForResult(showFolder, SHOW_ACTIVITY);
				selectedItem = item;
			}else{
				item.setTitle(R.string.show_folder);
				mShowing = false;
				mImageCache.resume();
				ReadFeeds.runAsync(mFeedController);
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
		if(requestCode == SHOW_ACTIVITY && resultCode == RESULT_OK){
			Bundle b = data.getExtras();
			int type = b.getInt("TYPE");
			String path = (String)b.get("PATH");
			String name = (String)b.get("NAME");
			
			FeedReference feed = mFeedController.getFeedReference(path, type, name);
			if(!mFeedController.showFeed(feed)){
				String error = this.getResources().getString(R.string.empty_feed) + feed.getName();
				Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
			}else{
				mShowing = true;
				mImageCache.pause();
				Toast.makeText(this, "Showing " + mFeedController.getShowing() + " images from " + path, Toast.LENGTH_SHORT).show();
				if(selectedItem != null){
					selectedItem.setTitle(R.string.cancel_show);
				}
			}
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode){
		case KeyEvent.KEYCODE_BACK:
			if(renderer.unselect()) return true;
		default: return super.onKeyDown(keyCode, event);
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
		SharedPreferences sp = getSharedPreferences("version", 0);
		String oldVersion = sp.getString("version", "0.0.0");
		if(isDeprecated(oldVersion)){
			String oldCache = this.getResources().getString(R.string.dataFolder) + "/exploreCache";
			oldCache = Environment.getExternalStorageDirectory().getAbsolutePath() + oldCache;
			File dir = new File(oldCache);
			if(dir.exists()){
				ClearCache.deleteAll(dir);
			}
			addDefaultLocalPaths();			
		}
		SharedPreferences.Editor editor = sp.edit(); 
		editor.putString("version", version);
		editor.commit();
		Log.v("Floating Image", "Old version: " + oldVersion + ", current version: " + version);
	}
	
	boolean isDeprecated(String ver) {
		if(!version.equals(ver)) return true;
		return false;
	}

	private void addDefaultLocalPaths() {
		FeedsDbAdapter mDbHelper = new FeedsDbAdapter(this);
		mDbHelper.open();
		mDbHelper.addFeed("Camera pictures", "/sdcard/DCIM", dk.nindroid.rss.settings.Settings.TYPE_LOCAL);
		mDbHelper.addFeed("Downloads", "/sdcard/download", dk.nindroid.rss.settings.Settings.TYPE_LOCAL);
		mDbHelper.close();
	}
}