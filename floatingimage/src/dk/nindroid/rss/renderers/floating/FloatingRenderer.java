package dk.nindroid.rss.renderers.floating;

import java.io.InputStream;
import java.util.Arrays;

import javax.microedition.khronos.opengles.GL10;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import dk.nindroid.rss.Display;
import dk.nindroid.rss.FeedController;
import dk.nindroid.rss.FeedController.EventSubscriber;
import dk.nindroid.rss.MainActivity;
import dk.nindroid.rss.OnDemandImageBank;
import dk.nindroid.rss.R;
import dk.nindroid.rss.TextureSelector;
import dk.nindroid.rss.TextureSelector.PrepareCallback;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.data.Ray;
import dk.nindroid.rss.data.Texture;
import dk.nindroid.rss.gfx.Vec3f;
import dk.nindroid.rss.renderers.Dimmer;
import dk.nindroid.rss.renderers.OSD;
import dk.nindroid.rss.renderers.Renderer;
import dk.nindroid.rss.renderers.floating.positionControllers.FloatDown;
import dk.nindroid.rss.renderers.floating.positionControllers.FloatLeft;
import dk.nindroid.rss.renderers.floating.positionControllers.FloatRight;
import dk.nindroid.rss.renderers.floating.positionControllers.FloatUp;
import dk.nindroid.rss.renderers.floating.positionControllers.Gallery;
import dk.nindroid.rss.renderers.floating.positionControllers.Mixup;
import dk.nindroid.rss.renderers.floating.positionControllers.PositionController.FeedDataProvider;
import dk.nindroid.rss.renderers.floating.positionControllers.Stack;
import dk.nindroid.rss.renderers.floating.positionControllers.StarSpeed;
import dk.nindroid.rss.renderers.floating.positionControllers.TableTop;
import dk.nindroid.rss.renderers.osd.Play.EventHandler;

public class FloatingRenderer extends Renderer implements EventSubscriber, PrepareCallback, EventHandler, Image.ImageDataProvider, FeedDataProvider{
	public static final long 	mFocusDuration = 300;
	public static long			mSelectedTime;
	public static final float  	mFocusX = 0.0f;
	public static final float  	mFocusY = 0.0f;
	public static final float 	mFocusZ = -1.0f;
	public static final float  	mJitterX = 0.8f;
	public static final float  	mJitterY = 0.5f;
	public static final float  	mJitterZ = 1.5f;
	
	public static final int		FLOATING_TYPE_LEFT = 0;
	public static final int		FLOATING_TYPE_RIGHT = 1;
	public static final int		FLOATING_TYPE_DOWN = 2;
	public static final int		FLOATING_TYPE_UP = 3;
	public static final int		FLOATING_TYPE_STARSPEED = 4;
	public static final int		FLOATING_TYPE_TABLETOP = 5;
	public static final int		FLOATING_TYPE_STACK = 6;	
	public static final int		FLOATING_TYPE_GALLERY = 15;
	public static final int		FLOATING_TYPE_MIXUP = 20;
	
	private Image[] 		mImgs;
	private Image[] 		mImgDepths;
	private OnDemandImageBank mOnDemandBank;
	public 	TextureSelector mTextureSelector;
	private long			mStartTime;
	private int 			mTotalImgRows = 0;
	private int 			mImgCnt = 0;
	private boolean			mDoUnselect = false;
	private int				mDoSelect = -1;
	private boolean			mResetImages = false;
	private Display 		mDisplay;
	private MainActivity	mActivity;
	private InfoBar			mInfoBar;
	private BackgroundPainter mBackgroundPainter;
	private float			mStreamRotation;
	private float			mRequestedStreamRotation;
	private float			mStreamOffsetX;
	private float			mStreamOffsetY;
	private float			mStreamOffsetZ;
	private Vec3f			mRequestedStreamOffset;
	private long			mUpTime;
	private Texture 		mLargeTexture;
	private long			mLastFrameTime;
	private FeedController	mFeedController;
	private OSD				mOsd;
	
	private static final Vec3f 		mCamPos = new Vec3f(0,0,0);
	private Image					mSelected = null;
	private int						mSelectedIndex;
	private Image					mSplashImg;
	private boolean					mSelectingNext;
	private boolean					mSelectingPrev;
	private ImageDepthComparator 	mDepthComparator;
	private boolean					mDoAdjustImagePositions = false;
	private boolean					mFeedsUpdated = false;
	
	private boolean					mSlideshow = false;
	private long					mSlideshowImageShowTime = 10000;
	private long					mSlideshowSlideWaitTime	= 10000;
	private long					mSlideshowImageShownAt;
	private long					mSlideshowSlideImageDismissedAt;
	private int 					mSlideshowLastImage = 0;
	
	private long					mSetOffset = -1;
	private int						mShowImage = -1;
	private long 					mLastShowImageRotations = -1;
	private String					mPendingShowImage;
	private ImageReference			mForcedImage;
	
	
	private boolean settingsEnabled = true;
	private boolean imagesEnabled = true;
	
	
	public void disableSettings(){
		this.settingsEnabled = false;
	}
	
	public void disableImages(){
		this.imagesEnabled = false;
	}
	
	public FloatingRenderer(MainActivity activity, OnDemandImageBank onDemandBank, FeedController feedController, Display display){
		this.mActivity = activity;
		mTextureSelector = new TextureSelector(display, mActivity.getSettings().bitmapConfig, mActivity);
		mRequestedStreamOffset = new Vec3f();
		mInfoBar = new InfoBar(Math.max(display.getHeightPixels(), display.getWidthPixels()));
		mBackgroundPainter = new BackgroundPainter();
		this.mDisplay = display;
		this.mOnDemandBank = onDemandBank;
		this.mLargeTexture = new Texture();
		this.mFeedController = feedController;
		mDepthComparator = new ImageDepthComparator();
		createImageArray();
		
		setPositionController(mActivity.getSettings().floatingType);
		
		mStartTime = System.currentTimeMillis();
		mSlideshowSlideImageDismissedAt = mStartTime;
	}
	
	int mCurPositionController = -1;
	
	void createImageArray(){
		// If amount of images have changed, rehash
		int settingsRows = mActivity.getSettings().tsunami ? 18 : 6;
		if(mActivity.getSettings().galleryMode){
			settingsRows = 8;
		}
		if(settingsRows != mTotalImgRows){
			mTotalImgRows = settingsRows;
			mCurPositionController = -1;
			InputStream smoothIS = mActivity.context().getResources().openRawResource(R.drawable.smooth_circle);
			Bitmap smoothImage = BitmapFactory.decodeStream(smoothIS);
			mImgCnt = 0;

			mImgs = new Image[mTotalImgRows * 3 / 2];
			for(int i = 0; i < mImgs.length; ++i){
				mImgs[mImgCnt++] = new Image(mActivity, mDisplay, mInfoBar, mLargeTexture, mTextureSelector, smoothImage, mOnDemandBank, this, i);
			}
			
			mImgDepths = new Image[mImgs.length];
			for(int i = 0; i < mImgs.length; ++i){
				mImgDepths[i] = mImgs[i];
			}
			Arrays.sort(mImgDepths, mDepthComparator);
		}
	}
	
	public void setPositionController(int type){
		if(mActivity.getSettings().galleryMode){
			type = FLOATING_TYPE_GALLERY;
		}
		if(mCurPositionController != type){
			mCurPositionController = type;
			
			switch(type){
			case FLOATING_TYPE_LEFT:
				for(int i = 0; i < mImgs.length; ++i){
					mImgs[i].setPositionController(new FloatLeft(mActivity, mDisplay, i, this));
				}
			break;
			case FLOATING_TYPE_RIGHT:
				for(int i = 0; i < mImgs.length; ++i){
					mImgs[i].setPositionController(new FloatRight(mActivity, mDisplay, i, this));
				}
			break;
			case FLOATING_TYPE_DOWN:
				for(int i = 0; i < mImgs.length; ++i){
					mImgs[i].setPositionController(new FloatDown(mActivity, mDisplay, i, this));
				}
				break;
			case FLOATING_TYPE_UP:
				for(int i = 0; i < mImgs.length; ++i){
					mImgs[i].setPositionController(new FloatUp(mActivity, mDisplay, i, this));
				}
				break;
			case FLOATING_TYPE_STARSPEED:
				for(int i = 0; i < mImgs.length; ++i){
					mImgs[i].setPositionController(new StarSpeed(mActivity, mDisplay, i, this));
				}
				break;
			case FLOATING_TYPE_TABLETOP:
				for(int i = 0; i < mImgs.length; ++i){
					mImgs[i].setPositionController(new TableTop(mActivity, mDisplay, i, this));
				}
				break;
			case FLOATING_TYPE_STACK:
				for(int i = 0; i < mImgs.length; ++i){
					mImgs[i].setPositionController(new Stack(mActivity, mDisplay, i, this));
				}
				break;
			case FLOATING_TYPE_MIXUP:
				for(int i = 0; i < mImgs.length; ++i){
					mImgs[i].setPositionController(new Mixup(mActivity, mDisplay, this, i, this));
				}
				break;
			case FLOATING_TYPE_GALLERY:
				for(int i = 0; i < mImgs.length; ++i){
					mImgs[i].setPositionController(new Gallery(mActivity, mDisplay, i, this));
				}
				break;
			}
		}
	}
		
	public boolean click(GL10 gl, float x, float y, long frameTime, long realTime){
		mOsd.pause();
		Vec3f rayDir = new Vec3f(x, y, -1);
    	rayDir.normalize();
    	Ray r = new Ray(mCamPos, rayDir);
    	
		if(mSelected != null){
			if(mActivity.getSettings().singleClickDeselect){
				deselect(gl, frameTime, realTime);
				return true;
			}else{
				if(mSelected.intersect(r) <= 0){
					deselect(gl, frameTime, realTime);
					return true;
				}
				return false; // Single click for menu when has selection
			}
		}else{
			// Ignore click if we're at the splash
	    	if(mSplashImg == null){
	        	// Only works for camera looking directly at (0, 0, -1)...
	        	int i = mImgCnt - 1;
	        	Image selected = null;
	        	float closest = Float.MAX_VALUE;
	        	float t;
	        	for(; i > -1; --i){
	        		t = mImgDepths[i].intersect(r); 
	            	if(t > 0 && t < closest){
	            		closest = t;
	            		selected = mImgDepths[i];
	            		break; // Images are depth sorted.
	            	}
	            }
	        	if(selected != null && selected.canSelect()){
	        		selectImage(gl, frameTime, realTime, selected);
	        		mSlideshowLastImage = selected.mId;
	        		return true;
	        	}else{
	        		return false;
	        	}
	        }else{
	        	return false;
	        }
		}
	}
	
	private void selectImage(GL10 gl, long frameTime, long realTime, Image selected){
		mSelectedTime = realTime;
		mSelected = selected;
		selected.select(gl, frameTime, realTime);
		for(int i = mImgs.length; i > 0; --i){
			if(mImgs[i - 1] == selected)
				mSelectedIndex = i - 1;
		}
	}
	
	@Override
	public boolean doubleClick(GL10 gl, float x, float y, long frameTime,
			long realTime) {
		if(mActivity.getSettings().singleClickDeselect){
			return false;
		}else{
			if(mSelected != null){
				deselect(gl, frameTime, realTime);
				return true;
			}
			return false;
		}
	}
	
	private void deselect(GL10 gl, long frameTime, long realTime){
		if(!mSelected.click(realTime)){
			if(mSelected.stateInFocus()){
	    		mSelectedTime = realTime;
	        	mSelected.select(gl, frameTime, realTime); // Deselect!
	    	}
		}else{
			mSelectingNext = mSelectingPrev = false;
		}
	}
	
	@Override 
	public long editOffset(long offset, long realTime, boolean isPaused){
		if(isPaused && mSelected != null){
			float interval = mSelected.getInterval(realTime + offset) - 0.5f;
			long adjustment = (long)(200.0f * interval);
			offset -= adjustment;
		}
		
		if(this.mCurPositionController == FLOATING_TYPE_GALLERY){
			int count = mFeedController.getFeedSize();
			long elapsed = realTime + offset;
			long traversal = mActivity.getSettings().floatingTraversal;
			if(elapsed < -traversal * 0.4f){
				float interval = (elapsed + traversal * 0.4f) / (traversal / 16.0f);
				interval *= interval;
				offset += interval * 40;
			}
			else{
				long overshoot = elapsed - traversal * count / mImgs.length;
				if(overshoot > -traversal / 3 * 2){
					float interval = (overshoot + traversal / 3 * 2) / (traversal / 16.0f);
					interval *= interval;
					offset -= interval * 40;
				}
			}
		}
		
		if(mSetOffset != -1){
			Log.v("Floating Image", "Set offset");
			offset = mSetOffset;
			mSetOffset = -1;
		}
		
		return offset;
	}
	
	public void update(GL10 gl, long frameTime, long realTime){
		boolean sortArray = false;
		if(mFeedsUpdated){
			mFeedsUpdated = false;
			feedsUpdated(frameTime);
		}
		if(mResetImages){
			resetImages(gl, frameTime);
		}
		if(mDoAdjustImagePositions){
			adjustImagePositions(frameTime); // If image speed has changed, image positions need to be reset!
			mDoAdjustImagePositions = false;
		}
		
		if(mDoUnselect){
			mDoUnselect = false;
			deselect(gl, frameTime, realTime);
		}
		
		if(mDoSelect != -1){
			selectImage(gl, frameTime, realTime, mImgs[mDoSelect]);
			mDoSelect = -1;
		}
		
		// Deselect selected when it is floating.
        if(mSelected != null){
        	if(mSelected.stateInFocus()){
        		if(mSelectingNext || mSelectingPrev){
        			deselect(gl, frameTime, realTime);
        		}
        	}
        	if(mSelected.stateFloating()){
        		if(mSelectingNext || mSelectingPrev){
        			int imageCount = mImgDepths.length;
        			mSelectedIndex += (mSelectingNext ? imageCount - 1 : 1);
        			mSelectedIndex %= imageCount;
        			mSelected = mImgs[mSelectedIndex];
        			if(mSelected.canSelect() && mSelected.isShowing()){
	        			mSelectedTime = realTime;
	        			mSelectingNext = false;
	        			mSelectingPrev = false;
	        			mSelected.select(gl, frameTime, realTime);
        			}else{
        				mSelected = null;
        				mSelectingNext = false;
	        			mSelectingPrev = false;
        			}
        		}else{
        			mSelected = null;
        		}
        		sortArray = true;
        	}
        }
        
        if(mLastFrameTime < frameTime){
        	for(int i = 0; i < mImgCnt; ++i){
            	if(mImgDepths[i].update(gl, frameTime, realTime)){
            		sortArray = true;
            	}
            }
        }else{ // Rewind, update images reverse
        	for(int i = mImgCnt - 1; i >= 0; --i){
            	if(mImgDepths[i].update(gl, frameTime, realTime)){
            		sortArray = true;
            	}
            }
        }
        
        updateSlideshow(gl, frameTime, realTime);
        
        if(sortArray)
        {
        	updateDepths();	
        }
        mLastFrameTime = frameTime;
        //updateRotation(realTime);
        updateTranslation(realTime);
        
        if(mShowImage != -1 && mImgs[mShowImage].getShowing() != null){
			int curRotations = mImgs[mShowImage].getRotations();
			if(mLastShowImageRotations == curRotations){
				this.selectImage(gl, frameTime, realTime, mImgs[mShowImage]);
				mShowImage = -1;
				mLastShowImageRotations = -1;
			}else{
				mLastShowImageRotations = curRotations;
			}
		}
	}
	
	public void updateSlideshow(GL10 gl, long frameTime, long realTime){
		if(mSlideshow){
			if(mSelected != null){
				if(realTime - mSlideshowImageShownAt > mSlideshowImageShowTime){
					deselect(gl, frameTime, realTime);
					mSlideshowImageShownAt = -1;
					mSlideshowSlideImageDismissedAt = realTime;
				}
			}else{
				if(realTime - mSlideshowSlideImageDismissedAt > mSlideshowSlideWaitTime){
					if(mSlideshowSlideImageDismissedAt != -1){
						mSlideshowSlideImageDismissedAt = -1;
						prepareNext();
					}
				}
			}
		}
	}
	
	public void prepareNext(){
		
		int next = (mSlideshowLastImage + 1) % mImgs.length;
		Image img = mImgs[next];
		if(!(img.canSelect() && img.isShowing())){
			mOsd.pause();
			return;
		}
		img.lockTexture(true);
		mTextureSelector.PrepareImage(img, this, img.getShowing());
		mSlideshowLastImage = next;
	}
	
	@Override
	public void TexturePrepared(String id) {
		mImgs[mSlideshowLastImage].lockTexture(false);
		if(mSelected == null){
			mDoSelect = mSlideshowLastImage;
			mSlideshowImageShownAt = System.currentTimeMillis();
		}
	}
	
	public void updateDepths(){
		Arrays.sort(mImgDepths, mDepthComparator);
	}
	
	void initRender(GL10 gl){
		gl.glDepthMask(false);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
		gl.glFrontFace(GL10.GL_CCW);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D,GL10.GL_TEXTURE_MAG_FILTER,GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,GL10.GL_CLAMP_TO_EDGE);
	}
	
	public void render(GL10 gl, long time, long realTime){
		initRender(gl);
		
		// Background first, this is backmost
		mBackgroundPainter.draw(gl, mDisplay, mActivity.getSettings().backgroundColor);
        //Image.setState(gl);
		gl.glPushMatrix();
		gl.glTranslatef(mStreamOffsetX, mStreamOffsetY, mStreamOffsetZ);
		gl.glRotatef(mStreamRotation, 0.0f, 1.0f, 0.0f);
        for(int i = 0; i < mImgCnt; ++i){
        	if(mImgDepths[i] != mSelected){
        		mImgDepths[i].draw(gl, time, realTime);
        	}
        }
        gl.glPopMatrix();
        
        Image.unsetState(gl);
        float fraction = getFraction(realTime);
        if(mSelected != null){
        	float dark = mActivity.getSettings().fullscreenBlack ? mDisplay.getFill() * mDisplay.getFill() : 0.8f;
        	Dimmer.setColor(0.0f, 0.0f, 0.0f);
        	if(mSelected.stateInFocus()){
        		Dimmer.draw(gl, 1.0f, dark, mDisplay);
        		fraction = 1.0f;
        	}else if (mSelected.stateFocusing()){
        		Dimmer.draw(gl, fraction, dark, mDisplay);
        		// Fraction is right!
        	}else{
        		Dimmer.draw(gl, 1.0f - fraction, dark, mDisplay);
        		fraction = 1.0f - fraction;
        	}
        	if(!mDisplay.isTurning()){
        		mInfoBar.setState(gl);
        		mInfoBar.draw(gl, mDisplay, fraction);
        		mInfoBar.unsetState(gl);
    		}
        	
        	gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        	Image.setState(gl);
        	mSelected.draw(gl, time, realTime);
        	Image.unsetState(gl);
        }
	}
	
	public void updateRotation(long realTime){
		float timeFactor = (4000 - (realTime - mUpTime)) / 4000.0f;
		if(timeFactor > 0){
			float damping = timeFactor * timeFactor;
			mStreamRotation = mRequestedStreamRotation * damping;
		}else{
			mRequestedStreamRotation = 0;
		}
	}
	
	public void updateTranslation(long realTime){
		float timeFactor = (2000 - (realTime - mUpTime)) / 2000.0f;
		if(timeFactor > 0){
			float damping = timeFactor * timeFactor;
			mStreamOffsetX = mRequestedStreamOffset.getX() * damping;
			mStreamOffsetY = mRequestedStreamOffset.getY() * damping;
			mStreamOffsetZ = mRequestedStreamOffset.getZ() * damping;
		}else{
			mRequestedStreamOffset.setX(0);
			mRequestedStreamOffset.setY(0);
			mRequestedStreamOffset.setZ(0);
		}
	}
	
	public static float getFraction(long realTime){
		return Math.min(((float)(realTime - mSelectedTime)) / mFocusDuration, 1.1f);
	}
	
	public void init(GL10 gl, long time, OSD osd){
		mBackgroundPainter.initTexture(gl, mActivity, mActivity.getSettings().backgroundColor);
		osd.setEnabled(true, true, true, settingsEnabled, imagesEnabled);
		for(int i = 0; i < mImgCnt; ++i){
			mImgs[i].init(gl, time);
		}
		if(mSelected != null){
			mInfoBar.select(gl, mDisplay, mSelected.getShowing());
		}
		
		if(mFeedController.getShowing() == 0){
			mFeedController.addSubscriber(this);
		}
		mOsd = osd;
		osd.registerPlayListener(this);
		osd.pause();
		
		feedsUpdated();
	}
	
	@Override
	public void Play() {
		mSlideshowSlideImageDismissedAt = 0;
		int startTest = mSlideshowLastImage; 
		while(!(mImgs[mSlideshowLastImage].canSelect() && mImgs[mSlideshowLastImage].isShowing())){
			mSlideshowLastImage = (mSlideshowLastImage + 1) % mImgs.length;
			if(startTest == mSlideshowLastImage){
				mOsd.pause();
				return; // No images to show
			}
		}
		
		this.mSlideshow = true;
	}

	@Override
	public void Pause() {
		this.mSlideshow = false;
	}
		
	@Override
	public void onPause(){
		mTextureSelector.stopThread();
	}
	
	@Override
	public void onResume(){
		mTextureSelector.startThread();
		createImageArray();
		if(mCurPositionController != -1){
			mDoAdjustImagePositions = true;
		}
		setPositionController(mActivity.getSettings().floatingType);
	}
	
	void adjustImagePositions(long time){
		for(Image img : mImgs){
			img.traversalChanged(time);
		}
	}

	@Override
	public boolean back() {
		if(mSelected != null && mSelected.stateInFocus()){
			mDoUnselect = true;
			return true;
		}
		return false;
	}
	
	public void zoomIn(){
		if(mSelected != null){
			mSelected.initTransform();
			mSelected.transform(0, 0, 0, 0, 0, 1.1f);
			mSelected.transformEnd();
		}
	}
	
	public void zoomOut(){
		if(mSelected != null){
			mSelected.initTransform();
			mSelected.transform(0, 0, 0, 0, 0, 0.9f);
			mSelected.transformEnd();
		}
	}

	@Override
	public Intent followCurrent() {
		if(mSelected != null){
			return mSelected.getShowing().follow();
		}
		return null;
	}

	@Override
	public ImageReference getCurrent() {
		return mSelected != null ? mSelected.getShowing() : null;
	}

	@Override
	public boolean slideLeft(long realTime) {
		if(mSelected != null){
			boolean reverse = mSelected.getPositionController().isReversed();
			mSelectingNext = reverse; // Normal is false
			mSelectingPrev = !reverse; // Normal is true
			Log.v("Floating Image", "SlideLeft");
			return true;
		}
		return false;
	}

	@Override
	public boolean slideRight(long realTime) {
		if(mSelected != null){
			boolean reverse = mSelected.getPositionController().isReversed();
			mSelectingNext = !reverse; // Normal is true
			mSelectingPrev = reverse; // Normal is false
			Log.v("Floating Image", "SlideRight");
			return true;
		}
		return false;
	}
	
	@Override
	public void setBackground(){
		if(mSelected != null){
			mSelected.setBackground();
		}
	}
	
	@Override
	public void resetImages() {
		mResetImages = true;
	}
	
	public void resetImages(GL10 gl, long time) {
		mResetImages = false;
		mFeedController.resetCounter();
		for(Image i : mImgs){
			i.reset(gl, time);
		}
		feedsUpdated();
	}

	@Override
	public void transform(float centerX, float centerY, float x, float y, float rotate, float scale) {
		if(mSelected != null){
			mSelected.transform(centerX, centerY, x, y, rotate, scale);
		}
	}
	
	@Override
	public void initTransform() {
		if(mSelected != null){
			mSelected.initTransform();
		}
	}

	@Override
	public boolean freeMove() {
		if(mSelected != null){
			return mSelected.freeMove();
		}
		return false;
	}

	@Override
	public void move(float x, float y) {
		if(mSelected != null){
			mSelected.move(x, y);
		}
	}

	@Override
	public void transformEnd() {
		if(mSelected != null){
			mSelected.transformEnd();
		}
	}

	@Override
	public int totalImages() {
		return mImgs.length;
	}
	
	Vec3f tmpVar = new Vec3f();
	
	@Override
	public void streamMoved(float x, float y) {
		mUpTime = System.currentTimeMillis();
		mRequestedStreamOffset.setX(mStreamOffsetX);
		mRequestedStreamOffset.setY(mStreamOffsetY);
		mRequestedStreamOffset.setZ(mStreamOffsetZ);
		tmpVar.setX(0);
		tmpVar.setY(0);
		tmpVar.setZ(0);
		mImgs[0].getPositionController().getGlobalOffset(x, y, tmpVar);
		mRequestedStreamOffset.minus(tmpVar, mRequestedStreamOffset);
		mRequestedStreamOffset.setX(Math.max(Math.min(1.0f, mRequestedStreamOffset.getX()), -1.0f));
		mRequestedStreamOffset.setY(Math.max(Math.min(1.0f, mRequestedStreamOffset.getY()), -1.0f));
		mRequestedStreamOffset.setZ(Math.max(Math.min(1.0f, mRequestedStreamOffset.getZ()), -1.0f));
	}
	
	public void wallpaperMove(float fraction){
		mStreamRotation = -fraction * 10.0f;
		mStreamOffsetX = -fraction * 1.0f;
	}

	@Override
	public float adjustOffset(float speedX, float speedY) {
		float offset = mImgs[0].getPositionController().getTimeAdjustment(speedX, speedY);
		offset = offset * mActivity.getSettings().floatingTraversal / 1000;
		return offset;
	}

	@Override
	public void deleteCurrent() {
		if(this.mSelected != null){
			this.mSelected.delete();
			this.mDoUnselect = true;
		}
	}

	@Override
	public void feedsUpdated() {
		if(mFeedController.getFeedSize() != 0){
			mFeedsUpdated = true;
		}
	}
	
	public void showImagePosition(int pos){
		mShowImage = pos;
	}
	
	public void showImage(String id){
		int index = mFeedController.findImageIndex(id);
		if(index == -1){
			mPendingShowImage = id;
		}else{
			mSetOffset = mActivity.getSettings().floatingTraversal / mImgs.length * index - mActivity.getSettings().floatingTraversal / 2;
			mShowImage = index % mImgs.length;
		}
	}
	
	private void feedsUpdated(long frameTime){
		float lastImagePosition = Float.MAX_VALUE;
		// Get reverse, if a later image is shown before an earlier one.
		for(Image i : mImgs){
			if(i != null){
				float interval = i.getInterval(frameTime);
				if(interval > lastImagePosition){
					i.getImageIfEmpty(false);
				}else{
					i.getImageIfEmpty(true);
					lastImagePosition = interval;
				}
			}
		}
		if(mPendingShowImage != null){
			showImage(mPendingShowImage);
		}
	}

	@Override
	public ImageReference getImageReference(int id, int rotations) {
		if(mForcedImage == null){
			long imageNumber = mImgs.length * (long)rotations + id;
			return mFeedController.getImageReference(imageNumber);
		}else{
			if(id == 0){
				return mForcedImage;
			}
		}
		return null;
	}

	@Override
	public int getNumberOfImages() {
		return mImgs.length;
	}
}