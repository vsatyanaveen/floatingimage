package dk.nindroid.rss;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;
import android.view.Window;
import dk.nindroid.rss.helpers.GLWallpaperService;
import dk.nindroid.rss.launchers.ReadFeeds;
import dk.nindroid.rss.renderers.OSD;
import dk.nindroid.rss.renderers.floating.BackgroundPainter;
import dk.nindroid.rss.renderers.floating.FloatingRenderer;
import dk.nindroid.rss.renderers.floating.GlowImage;
import dk.nindroid.rss.renderers.floating.ShadowPainter;
import dk.nindroid.rss.renderers.slideshow.SlideshowRenderer;
import dk.nindroid.rss.settings.Settings;

public class Wallpaper extends GLWallpaperService implements MainActivity{
	public Engine onCreateEngine() {
		MyEngine engine = new MyEngine(this);
		return engine;
	}
	
	// prefs and sensor interface is optional, just showing that this is where you do all of that - everything that would normally be in an activity is in here.
	class MyEngine extends GLEngine implements SharedPreferences.OnSharedPreferenceChangeListener {
		RiverRenderer renderer;
		FeedController mFeedController;
		Wallpaper mContext;
		
		public MyEngine(Wallpaper context) {
			super();
			mContext = context;
			GlowImage.init(context);
			ShadowPainter.init(context);
			BackgroundPainter.init();
			
			ShowStreams.registerParsers();
			
			TextureBank bank = setupFeeders();			
			
			renderer = new RiverRenderer(context, false, bank, true);
			mFeedController.setRenderer(renderer);
			OSD.init(context, renderer);
			
			dk.nindroid.rss.settings.Settings.readSettings(context);
			dk.nindroid.rss.renderers.Renderer defaultRenderer = renderer.getRenderer();
			if(dk.nindroid.rss.settings.Settings.mode == dk.nindroid.rss.settings.Settings.MODE_FLOATING_IMAGE){
				if(!(defaultRenderer instanceof FloatingRenderer)){
					Log.v("Floating Image", "Switching to floating renderer");
					defaultRenderer = new FloatingRenderer(context, bank, renderer.mDisplay);
				}
			}else{
				if(!(defaultRenderer instanceof SlideshowRenderer)){
					Log.v("Floating Image", "Switching to slideshow renderer");
					defaultRenderer = new SlideshowRenderer(context, bank, renderer.mDisplay);
				}
			}
			renderer.setRenderer(defaultRenderer);
			bank.initCache(ShowStreams.CACHE_SIZE, defaultRenderer.totalImages());
			renderer.onResume();
			
			this.setRenderer(renderer);
			setRenderMode(RENDERMODE_CONTINUOUSLY);
			ReadFeeds.runAsync(mFeedController, defaultRenderer.totalImages() + ShowStreams.CACHE_SIZE);
		}
		
		TextureBank setupFeeders(){
			TextureBank bank = new TextureBank();
			mFeedController = new FeedController(mContext);
			BitmapDownloader bitmapDownloader = new BitmapDownloader(bank, mFeedController);
			ImageCache imageCache = new ImageCache(mContext, bank);
			bank.setFeeders(bitmapDownloader, imageCache);
			return bank;
		}
	
		public void onDestroy() {
			super.onDestroy();
			if (renderer != null) {
				renderer.onPause();
			}
			renderer = null;
		}

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			Log.v("Floating Image", "Live wallpaper, preference changed: " + key);
			Settings.readSettings(mContext);
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
}
