package dk.nindroid.rss;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.net.Uri;
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
import dk.nindroid.rss.compatibility.ButtonBrightness;
import dk.nindroid.rss.compatibility.Honeycomb;
import dk.nindroid.rss.compatibility.SetWallpaper;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.data.LocalImage;
import dk.nindroid.rss.helpers.MultisampleConfigChooser;
import dk.nindroid.rss.launchers.ReadFeeds;
import dk.nindroid.rss.menu.Settings;
import dk.nindroid.rss.orientation.InitialOritentationReflector;
import dk.nindroid.rss.orientation.OrientationManager;
import dk.nindroid.rss.parser.ParserProvider;
import dk.nindroid.rss.parser.facebook.FacebookParser;
import dk.nindroid.rss.parser.flickr.FlickrParser;
import dk.nindroid.rss.parser.picasa.PicasaParser;
import dk.nindroid.rss.renderers.OSD;
import dk.nindroid.rss.renderers.Renderer;
import dk.nindroid.rss.renderers.floating.FloatingRenderer;
import dk.nindroid.rss.renderers.floating.GlowImage;
import dk.nindroid.rss.renderers.floating.ShadowPainter;
import dk.nindroid.rss.renderers.slideshow.SlideshowRenderer;
import dk.nindroid.rss.settings.FeedsDbAdapter;
import dk.nindroid.rss.settings.ManageFeeds;

public class ShowStreams extends Activity implements MainActivity {
	public static final int 			ABOUT_ID 		= Menu.FIRST;
	public static final int 			FULLSCREEN_ID	= Menu.FIRST + 1;
	public static final int 			SHOW_FOLDER_ID	= Menu.FIRST + 2;
	public static final int 			SETTINGS_ID 	= Menu.FIRST + 3;
	public static final int				SHOW_ACTIVITY 	= 13;
	public static final int				CONTEXT_GO_TO_SOURCE = Menu.FIRST;
	public static final int				CONTEXT_SAVE 	= Menu.FIRST + 1;
	public static final int				CONTEXT_BACKGROUND = Menu.FIRST + 2;
	public static final int				CONTEXT_SHARE 	= Menu.FIRST + 3;
	public static final int				CONTEXT_DELETE 	= Menu.FIRST + 4;
	public static final int				MENU_IMAGE_CONTEXT = 13;
	public static final int				MISC_ROW_ID		= 201;
	public static final String 			version 		= "2.5.1";
	public static final int				CACHE_SIZE		= 15;
	private GLSurfaceView 				mGLSurfaceView;
	private RiverRenderer 				renderer;
	private PowerManager.WakeLock 		wl;
	private OrientationManager			orientationManager;
	private FeedController				mFeedController;
	private ImageCache 					mImageCache;
	private TextureBank					mTextureBank;
	private dk.nindroid.rss.settings.Settings		mSettings;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		registerParsers();
		String dataFolder = getString(R.string.dataFolder);
		File sdDir = Environment.getExternalStorageDirectory();
		dataFolder = sdDir.getAbsolutePath() + dataFolder;
		File dataFile = new File(dataFolder);
		this.mSettings = new dk.nindroid.rss.settings.Settings("dk.nindroid.rss_preferences");
		if(!dataFile.exists() && !dataFile.mkdirs()){
			Toast error = Toast.makeText(this, "Error creating data folder (Do you have an SD card?)\nCache will not work, operations might be flaky!", Toast.LENGTH_LONG);
			error.show();
		}
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		int rotation = InitialOritentationReflector.getRotation(getWindowManager().getDefaultDisplay());
		orientationManager = new OrientationManager(mSettings, sensorManager, rotation);
		saveVersion(dataFile);
		GlowImage.init(this);
		ShadowPainter.init(this);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "Floating Image");
		//ShowStreams.current = this;
		mTextureBank = setupFeeders();
		cleanIfOld();
		renderer = new RiverRenderer(this, true, mTextureBank, false);
		mFeedController.setRenderer(renderer);
		OSD.init(this, renderer);
		orientationManager.addSubscriber(renderer.mDisplay);
		ClickHandler.init(this, renderer);
		setContentView(R.layout.main); 
		mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setRenderer(renderer);
		
		setContentView(mGLSurfaceView);
	}
	
	static void registerParsers(){
		ParserProvider.registerParser(dk.nindroid.rss.settings.Settings.TYPE_FLICKR, FlickrParser.class);
		ParserProvider.registerParser(dk.nindroid.rss.settings.Settings.TYPE_PICASA, PicasaParser.class);
		ParserProvider.registerParser(dk.nindroid.rss.settings.Settings.TYPE_FACEBOOK, FacebookParser.class);
	}
	
	TextureBank setupFeeders(){
		TextureBank bank = new TextureBank();
		mFeedController = new FeedController(this);
		BitmapDownloader bitmapDownloader = new BitmapDownloader(bank, mFeedController, mSettings);
		mImageCache = new ImageCache(this, bank);
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
		((Vibrator)getSystemService(Activity.VIBRATOR_SERVICE)).vibrate(100l);
		ImageReference ir = renderer.getSelected();
		if(ir != null){
			super.onCreateContextMenu(menu, v, menuInfo);
			menu.add(0, CONTEXT_GO_TO_SOURCE, 0, R.string.go_to_source);
			menu.add(0, CONTEXT_BACKGROUND, 0, R.string.set_as_background);
			if(!(ir instanceof LocalImage)){
				menu.add(0, CONTEXT_SAVE, 0, R.string.save_image);
			}
			menu.add(0, CONTEXT_SHARE, 0, R.string.share_image);
			if(ir instanceof LocalImage){
				menu.add(0, CONTEXT_DELETE, 0, R.string.delete_image);
			}
		}else{
			boolean paused = renderer.pause();
			Toast.makeText(this, paused ? R.string.pause : R.string.resume, Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if(item == null){
			return super.onContextItemSelected(item);
		}
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
				//renderer.setBackground();
				ir = renderer.getSelected();
				if(ir == null) {
					Toast.makeText(this, "Something strange happened, please try again...", Toast.LENGTH_LONG).show();
					return super.onContextItemSelected(item);
				}
				Toast.makeText(this, "Setting background, please be patient...", Toast.LENGTH_LONG).show();
				ImageDownloader.setWallpaper(ir.getOriginalImageUrl(), ir.getTitle(), ir instanceof LocalImage, this);
				return true;
			case CONTEXT_SAVE:
				ir = renderer.getSelected();
				ImageDownloader.downloadImage(ir.getOriginalImageUrl(), ir.getTitle(), this);
				return true;
			case CONTEXT_SHARE:
				Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
				ir = renderer.getSelected();
				String shareString = "Share image";
				if(ir instanceof LocalImage){
					shareIntent.setType("image/jpeg");
					shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(((LocalImage) ir).getFile()));
					shareString = getString(R.string.share_image);
				}else{
					shareIntent.setType("text/plain");
					shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, ir.getImagePageUrl());
					shareString = getString(R.string.share_url);
				}
				shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
				startActivity(Intent.createChooser(shareIntent, shareString));
				return true;
			case CONTEXT_DELETE:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				String question = this.getString(R.string.delete_image_are_you_sure);
				final LocalImage li = (LocalImage)renderer.getSelected();
				final File file = li.getFile();
				question += " " + file.getName() + "?";
				builder.setMessage(question)
				       .setCancelable(false)
				       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   file.delete();
				        	   dialog.dismiss();
				        	   renderer.deleteSelected();
				           }
				       })
				       .setNegativeButton("No", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                dialog.cancel();
				           }
				       });
				AlertDialog alert = builder.create();
				
				alert.show();
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
	protected void onPause() {
		Log.v("Floating image", "Pausing...");
		//Debug.stopMethodTracing();
		mGLSurfaceView.onPause();
		renderer.onPause();
		wl.release();
		orientationManager.onPause();
		Log.v("Floating image", "Paused!");
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		Log.v("Floating Image", "Resuming main activity");
		super.onResume();
		//String loading = this.getString(dk.nindroid.rss.R.string.please_wait);
		//ProgressDialog dialog = ProgressDialog.show(this, "", loading, true);
		mSettings.readSettings(this);
		try{
			ButtonBrightness.setButtonBrightness(getWindow().getAttributes(), 0.0f);
			if(android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.HONEYCOMB) {
				Honeycomb.HideButtons(mGLSurfaceView);
			}else{
				Log.v("Floating Image", "Not Honeycomb!");
			}
		}catch (Throwable t){
			Log.v("Floating Image", "Compatibility check", t);
		}
		Log.v("Floating Image", "Begin resume...");
		Renderer defaultRenderer = renderer.getRenderer();
		if(mSettings.mode == dk.nindroid.rss.settings.Settings.MODE_FLOATING_IMAGE){
			if(!(defaultRenderer instanceof FloatingRenderer)){
				Log.v("Floating Image", "Switching to floating renderer");
				defaultRenderer = new FloatingRenderer(this, mTextureBank, renderer.mDisplay);
			}
		}else{
			if(!(defaultRenderer instanceof SlideshowRenderer)){
				Log.v("Floating Image", "Switching to slideshow renderer");
				defaultRenderer = new SlideshowRenderer(this, mTextureBank, renderer.mDisplay);
			}
		}
		Log.v("Floating Image", "Resume texture bank done...");
		
		renderer.setRenderer(defaultRenderer);
		mTextureBank.initCache(CACHE_SIZE, defaultRenderer.totalImages());
		renderer.onResume();
		
		wl.acquire();
		orientationManager.onResume();

		mGLSurfaceView.onResume();
		ReadFeeds.runAsync(mFeedController, defaultRenderer.totalImages() + CACHE_SIZE);
		//dialog.dismiss();
		//Debug.startMethodTracing("floatingimage");
		Log.v("Floating Image", "End resume...");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		renderer.toggleMenu();
		return false;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case ABOUT_ID:
			showAbout();
			return true;
		case FULLSCREEN_ID:
			renderer.mDisplay.toggleFullscreen();
			item.setTitle(renderer.mDisplay.isFullscreen() ? R.string.show_details : R.string.fullscreen);
			mSettings.setFullscreen(renderer.mDisplay.isFullscreen());
			return true;
		case SHOW_FOLDER_ID:
			showFolder();
			return true;
		case SETTINGS_ID:
			showSettings();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void showSettings(){
		Intent showSettings = new Intent(this, Settings.class);
		startActivity(showSettings);
	}
	
	public void showFolder(){
		Intent showFolder = new Intent(this, ManageFeeds.class);
		showFolder.putExtra(ManageFeeds.SHARED_PREFS_NAME, Settings.SHARED_PREFS_NAME);
		startActivityForResult(showFolder, SHOW_ACTIVITY);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == SHOW_ACTIVITY && resultCode == RESULT_OK){
			// Yay
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
	
	public void showAbout(){
		Builder builder = new Builder(this);
		builder.setTitle(R.string.about);
		//builder.setMessage(R.string.about_text);
		//builder.setPositiveButton("Ok", null);
		//builder.show();
	}
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		super.dispatchTouchEvent(ev);
		return ClickHandler.onTouchEvent(ev);
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
		if(isDeprecated(oldVersion)){ // upgrade
			mImageCache.cleanCache();
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
		File phonePhotos = new File("/emmc");
		String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
		File defaultDir = new File(sdcard + "/DCIM");
		if(!defaultDir.exists()){
			defaultDir = new File(sdcard + "/photos"); // Something
			if(!defaultDir.exists()){
				defaultDir = new File(sdcard + "/external_sd/DCIM"); // Samsung
			}
		}
		
		FeedsDbAdapter mDbHelper = new FeedsDbAdapter(this);
		mDbHelper.open();
		mDbHelper.addFeed(getString(R.string.cameraPictures), defaultDir.getAbsolutePath(), dk.nindroid.rss.settings.Settings.TYPE_LOCAL, "");
		if(phonePhotos.exists()){
			mDbHelper.addFeed(getString(R.string.moreCameraPictures), "/emmc/DCIM", dk.nindroid.rss.settings.Settings.TYPE_LOCAL, ""); // Droid
		}
		mDbHelper.addFeed(getString(R.string.Downloads), Environment.getExternalStorageDirectory().getAbsolutePath() + "/download", dk.nindroid.rss.settings.Settings.TYPE_LOCAL, "");
		// mDbHelper.addFeed(getString(R.string.flickrExplore), FlickrFeeder.getExplore(), dk.nindroid.rss.settings.Settings.TYPE_FLICKR, "");
		mDbHelper.close();
	}

	@Override
	public Context context() {
		return this;
	}
	
	@Override
	public void setWallpaper(Bitmap bitmap) throws IOException {
		try{
			SetWallpaper.setWallpaper(bitmap, this);
		}catch(Throwable e){
			super.setWallpaper(bitmap);
		}
	}
	
	@Override
	public dk.nindroid.rss.settings.Settings getSettings() {
		return mSettings;
	}
	
	@Override
	public void setWallpaper(InputStream data) throws IOException {
		try{
			SetWallpaper.setWallpaper(data, this);
		}catch(Throwable e){
			super.setWallpaper(data);
		}
	}

	@Override
	public String getSettingsKey() {
		return Settings.SHARED_PREFS_NAME;
	}

	@Override
	public View getView() {
		return mGLSurfaceView;
	}
}