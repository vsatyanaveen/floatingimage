package dk.nindroid.rss;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;
import android.view.MotionEvent;
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

public class Wallpaper extends GLWallpaperService implements MainActivity{
	Settings mSettings;
	
	public Engine onCreateEngine() {
		mSettings = new Settings(WallpaperSettings.SHARED_PREFS_NAME);
		MyEngine engine = new MyEngine(this, mSettings);
		return engine;
	}
	
	// prefs and sensor interface is optional, just showing that this is where you do all of that - everything that would normally be in an activity is in here.
	class MyEngine extends GLEngine implements SharedPreferences.OnSharedPreferenceChangeListener {
		RiverRenderer renderer;
		FeedController mFeedController;
		Wallpaper mContext;
		Settings mSettings;
		TextureBank mBank;
		ImageCache mImageCache;
		OnDemandImageBank mOnDemandBank;
		
		public MyEngine(Wallpaper context, Settings settings) {
			super();
			mContext = context;
			this.mSettings = settings;
			
			ShadowPainter.init(context);
			
			ShowStreams.registerParsers();
			
			mBank = setupFeeders();			
			
			renderer = new RiverRenderer(context, false, true);
			mFeedController.setRenderer(renderer);
			OSD.init(context, renderer);
			
			SharedPreferences sp = mContext.getSharedPreferences(WallpaperSettings.SHARED_PREFS_NAME, 0);
			sp.registerOnSharedPreferenceChangeListener(this);
			onSharedPreferenceChanged(sp, null);
			
			this.setRenderer(renderer);
			this.setRenderMode(RENDERMODE_CONTINUOUSLY);
			//this.setTouchEventsEnabled(true);
		}
		
		void init(){
			if(renderer != null){
				dk.nindroid.rss.renderers.Renderer defaultRenderer = renderer.getRenderer();
				mOnDemandBank.start();
				if(mSettings.mode == dk.nindroid.rss.settings.Settings.MODE_FLOATING_IMAGE){
					if(!(defaultRenderer instanceof FloatingRenderer)){
						Log.v("Floating Image", "Switching to floating renderer");
						defaultRenderer = new FloatingRenderer(mContext, mOnDemandBank, mFeedController, renderer.mDisplay);
					}
				}else{
					if(!(defaultRenderer instanceof SlideshowRenderer)){
						Log.v("Floating Image", "Switching to slideshow renderer");
						defaultRenderer = new SlideshowRenderer(mContext, mBank, renderer.mDisplay);
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
			TextureBank bank = new TextureBank(mContext);
			mFeedController = new FeedController(mContext);
			BitmapDownloader bitmapDownloader = new BitmapDownloader(bank, mFeedController, mSettings);
			mImageCache = new ImageCache(mContext, bank);
			bank.setFeeders(bitmapDownloader, mImageCache);
			mOnDemandBank = new OnDemandImageBank(mFeedController, mContext, mImageCache);
			return bank;
		}
	
		public void onDestroy() {
			super.onDestroy();
			mBank.stop();
			if (renderer != null) {
				renderer.onPause();
			}
			renderer = null;
			mImageCache.cleanCache();
		}

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			Log.v("Floating Image", "Live wallpaper, preference changed: " + key);
			mSettings.readSettings(mContext);
			init();
		}
		
		float x, y;

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset,
				float xOffsetStep, float yOffsetStep, int xPixelOffset,
				int yPixelOffset) {
			super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep,
					xPixelOffset, yPixelOffset);
			float offset = (xOffset - 0.5f) * 2.0f; 
			if(renderer != null){
				renderer.wallpaperMove(offset);
			}
		}
		
		public void onTouchEvent(android.view.MotionEvent e) 
		{
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
		
	}

	@Override
	public Context context() {
		return this;
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
	public void showFolder() {
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
}
