package dk.nindroid.rss;

import java.io.IOException;
import java.io.InputStream;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.Window;
import dk.nindroid.rss.helpers.GLWallpaperService;
import dk.nindroid.rss.launchers.ReadFeeds;
import dk.nindroid.rss.menu.WallpaperSettings;
import dk.nindroid.rss.renderers.OSD;
import dk.nindroid.rss.renderers.floating.FloatingRenderer;
import dk.nindroid.rss.renderers.floating.ShadowPainter;
import dk.nindroid.rss.renderers.slideshow.SlideshowRenderer;
import dk.nindroid.rss.settings.Settings;

@TargetApi(Build.VERSION_CODES.ECLAIR_MR1)
public class Wallpaper extends GLWallpaperService {
	int idCounter = 0;
	
	public Engine onCreateEngine() {
		GLEngine engine = new MyEngine(this);
		return engine;
	}
	
	// prefs and sensor interface is optional, just showing that this is where you do all of that - everything that would normally be in an activity is in here.
	class MyEngine extends GLEngine implements SharedPreferences.OnSharedPreferenceChangeListener, MainActivity {
		RiverRenderer renderer;
		Settings mSettings;
		FeedController mFeedController;
		Wallpaper mContext;
		TextureBank mBank;
		ImageCache mImageCache;
		OnDemandImageBank mOnDemandBank;
		int id;
		
		public MyEngine(Wallpaper context) {
			super();
			mContext = context;
			this.mSettings = new Settings(WallpaperSettings.SHARED_PREFS_NAME);
			Log.v("Floating Image", "Created engine: " + idCounter++);
			
			ShadowPainter.init(context);
			
			ShowStreams.registerParsers();
			
			mBank = setupFeeders();			
			
			renderer = new RiverRenderer(this, false, true);
			mFeedController.setRenderer(renderer);
			OSD.init(this, renderer);
			
			SharedPreferences sp = mContext.getSharedPreferences(WallpaperSettings.SHARED_PREFS_NAME, 0);
			sp.registerOnSharedPreferenceChangeListener(this);
			onSharedPreferenceChanged(sp, null);
			
			this.setRenderer(renderer);
			ClickHandler.init();
			this.setRenderMode(RENDERMODE_CONTINUOUSLY);
			this.setTouchEventsEnabled(true);
		}
		
		void init(){
			if(renderer != null){
				dk.nindroid.rss.renderers.Renderer defaultRenderer = renderer.getRenderer();
				mOnDemandBank.start();
				if(mSettings.mode == dk.nindroid.rss.settings.Settings.MODE_FLOATING_IMAGE){
					if(!(defaultRenderer instanceof FloatingRenderer)){
						Log.v("Floating Image", "Switching to floating renderer");
						defaultRenderer = new FloatingRenderer(this, mOnDemandBank, mFeedController, renderer.mDisplay);
					}
				}else{
					if(!(defaultRenderer instanceof SlideshowRenderer)){
						Log.v("Floating Image", "Switching to slideshow renderer");
						defaultRenderer = new SlideshowRenderer(this, mBank, renderer.mDisplay);
						mBank.initCache(ShowStreams.CACHE_SIZE, defaultRenderer.totalImages());
						mBank.start();
					}
				}
				renderer.setRenderer(defaultRenderer);
				renderer.onResume();
				
				ReadFeeds.runAsync(mFeedController, defaultRenderer.totalImages() + ShowStreams.CACHE_SIZE);
			}
		}
		
		TextureBank setupFeeders(){
			TextureBank bank = new TextureBank(this);
			mFeedController = new FeedController(this);
			BitmapDownloader bitmapDownloader = new BitmapDownloader(bank, mFeedController, mSettings);
			mImageCache = new ImageCache(this, bank);
			bank.setFeeders(bitmapDownloader, mImageCache);
			mOnDemandBank = new OnDemandImageBank(mFeedController, this, mImageCache);
			return bank;
		}
	
		public void onDestroy() {
			super.onDestroy();
			Log.v("Floating Image", "Desroying engine: " + id);
			mBank.stop();
			if (renderer != null) {
				renderer.onPause();
			}
			renderer = null;
			this.setTouchEventsEnabled(false);
			mImageCache.cleanCache();
		}

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			Log.v("Floating Image", "Live wallpaper, preference changed: " + key);
			if(!"fullscreen".equalsIgnoreCase(key)){
				mSettings.readSettings(mContext);
				mSettings.fullscreen = true;
				mSettings.fullscreenBlack = false;
				init();
			}
		}
		
		float x, y;

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset,
				float xOffsetStep, float yOffsetStep, int xPixelOffset,
				int yPixelOffset) {
			super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep,
					xPixelOffset, yPixelOffset);
			Log.v("Floating Image", "Offset changed for: " + id);
			float offset = (xOffset - 0.5f) * 2.0f; 
			if(renderer != null){
				renderer.wallpaperMove(offset);
			}
		}
		
		@Override
		public void onTouchEvent(android.view.MotionEvent e) 
		{
			Log.v("Floating Image", "Touched: " + id);
			RiverRenderer renderer = getRenderer();
			if(renderer != null){
				ClickHandler.onTouchEvent(e, getRenderer(), this);
			}
		}
		
			/*
			if(e.getAction() == MotionEvent.ACTION_MOVE){
				if(x != 0 || y != 0){
					//renderer.wallpaperMove(e.getX() - x, e.getY() - y);
				}
				x = e.getX();
				y = e.getY();
			}else{
				x = 0;
				y = 0;
			}
		}
		*/
		
		@Override
		public Context context() {
			return Wallpaper.this;
		}

		@Override
		public Window getWindow() {
			// Not used with wallpaper
			return null;
		}

		@Override
		public void openContextMenu() {
			// Not used with wallpaper
		}

		@Override
		public void runOnUiThread(Runnable action) {
			// Not used with wallpaper
		}

		@Override
		public void manageFeeds() {
			// Not used with wallpaper
		}

		@Override
		public void showSettings() {
			// Not used with wallpaper
		}

		@Override
		public void stopManagingCursor(Cursor c) {
			// Not used with wallpaper
		}

		@Override
		public Settings getSettings() {
			return mSettings;
		}

		@Override
		public String getSettingsKey() {
			return WallpaperSettings.SHARED_PREFS_NAME;
		}

		@Override
		public View getView() {
			return null;
		}

		@Override
		public void showNoImagesWarning() {
			// Do nothing, this is disruptive, 
			// and maybe we're just waiting for the sdcard to mount 
		}

		@Override
		public RiverRenderer getRenderer() {
			return this.renderer;
		}

		@Override
		public boolean canShowOSD() {
			return false;
		}

		@Override
		public void setWallpaper(InputStream is) throws IOException {
			Wallpaper.this.setWallpaper(is);
		}

		@Override
		public void setWallpaper(Bitmap bmp) throws IOException {
			Wallpaper.this.setWallpaper(bmp);
		}
	}
}
