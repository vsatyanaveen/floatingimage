package dk.nindroid.rss;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;
import android.view.MotionEvent;
import dk.nindroid.rss.gfx.Vec2f;
import dk.nindroid.rss.uiActivities.OpenContextMenu;

public class ClickHandler extends TimerTask {
	private static MultitouchHandler mtHandler;
	
	
	private static RiverRenderer renderer;
	private static Vec2f 	mTouchLastPos;
	private static Vec2f	mTouchStartPos;
	private static Timer	mClickTimer;
	private static final int LONGCLICKTIME = 1000;
	private static long 	mMoveTime;
	private static float[]	mLastSpeedX = new float[2];
	private static float[]	mLastSpeedY = new float[2];
	private static boolean 	mIsMultitouch = false;
	
	private static int 		mAction = 0;
	
	private final static int ACTION_NOTHING 	= 0;
	private final static int ACTION_MOVE    	= 1;
	private final static int ACTION_LONG_CLICK	= 2;
	
	public static void init(RiverRenderer renderer){
		ClickHandler.renderer = renderer;
		mTouchLastPos = new Vec2f();
		mTouchStartPos = new Vec2f();
		mtHandler = new MultitouchHandler();
	}
	
		
	@Override
	public void run() {
		mAction = ACTION_LONG_CLICK;
		ShowStreams.current.runOnUiThread(new OpenContextMenu());
	}
	
	public static boolean onTouchEvent(MotionEvent event) {
		if(mtHandler.handleMultitouch(event)){
			return true;
		}
		
		int action = event.getAction();
		float x = event.getX(); float y = event.getY();
		float lastX = mTouchLastPos.getX();
		float lastY = mTouchLastPos.getY();
		if(action ==  MotionEvent.ACTION_DOWN){
			renderer.moveInit();
			mMoveTime = -1;
			mAction = ACTION_NOTHING;
			mTouchStartPos.set(x, y);
			mTouchLastPos.set(mTouchStartPos);
			mClickTimer = new Timer();
			mClickTimer.schedule(new ClickHandler(), LONGCLICKTIME);
		}else if(action ==  MotionEvent.ACTION_UP && mAction == ACTION_NOTHING){
			// Click
			mClickTimer.cancel();
			renderer.onClick(x,y);
		}else if(action == MotionEvent.ACTION_MOVE){
			if(Math.abs(mTouchStartPos.getX() - x) > 40.0f || Math.abs(mTouchStartPos.getY() - y) > 40.0f){
				mClickTimer.cancel();
				mAction = ACTION_MOVE;
			}
			if(mAction == ACTION_MOVE){
				float diffX = x - lastX;
				float diffY = y - lastY;
				saveSpeed(diffX, diffY);
				renderer.move(x - mTouchStartPos.getX(), y - mTouchStartPos.getY(), mLastSpeedX[0], mLastSpeedY[0]);
			}
		}
		if(action == MotionEvent.ACTION_UP){
			for(int i = 0; i < 2; ++i){
				mLastSpeedX[i] = 0;
				mLastSpeedY[i] = 0;
			}
			if(mAction == ACTION_MOVE){
				renderer.moveEnd(mLastSpeedX[0], mLastSpeedY[0]);
			}
		}
		mTouchLastPos.setX(x);
		mTouchLastPos.setY(y);
		return true;
	}

	private static void saveSpeed(float diffX, float diffY){
		long timeDiff = System.currentTimeMillis() - mMoveTime;
		if(timeDiff < 10) return; // Ignore too small updates, it makes the stream go wonky!
		mLastSpeedX[1] = mLastSpeedX[0];
		float speed = diffX / timeDiff * 10.0f;
		mLastSpeedX[0] = speed;
		
		mLastSpeedY[1] = mLastSpeedY[0];
		speed = diffY / timeDiff * 10.0f;
		mLastSpeedY[0] = speed;
		mMoveTime = System.currentTimeMillis();
	}
	
	private static class MultitouchHandler{
		private static Method getPointerCount;
		private static Method getPointerId;
		private static Method getX;
		private static Method getY;
		
		static {
			initCompatibility();
			mPointerBStart = new Vec2f();
		}
		
		private static void initCompatibility() {
			try{
				getPointerCount = MotionEvent.class.getMethod("getPointerCount");
				getPointerId = MotionEvent.class.getMethod("getPointerId", int.class);
				getX = MotionEvent.class.getMethod("getX", int.class);
				getY = MotionEvent.class.getMethod("getY", int.class);
			}catch (NoSuchMethodException e){
				// No can do
			}
		}
		
		private static int runGetPointerCount(MotionEvent event) throws IOException{
			try {
				return (Integer)getPointerCount.invoke(event);
			} catch (IllegalArgumentException e) {
				Log.e("Floating Image", "Illegal argument exception caught!", e);
			} catch (IllegalAccessException e) {
				Log.e("Floating Image", "Unexpected Exception caught!", e);
			} catch (InvocationTargetException e) {
				Throwable cause = e.getCause();
		           if (cause instanceof IOException) {
		               throw (IOException) cause;
		           } else if (cause instanceof RuntimeException) {
		               throw (RuntimeException) cause;
		           } else if (cause instanceof Error) {
		               throw (Error) cause;
		           } else {
		               /* unexpected checked exception; wrap and re-throw */
		               throw new RuntimeException(e);
		           }
			}
			return -1;
		}
		
		private static int runGetPointerId(MotionEvent event, int index) throws IOException{
			try {
				return (Integer)getPointerId.invoke(event, index);
			} catch (IllegalArgumentException e) {
				Log.e("Floating Image", "Illegal argument exception caught!", e);
			} catch (IllegalAccessException e) {
				Log.e("Floating Image", "Unexpected Exception caught!", e);
			} catch (InvocationTargetException e) {
				Throwable cause = e.getCause();
		           if (cause instanceof IOException) {
		               throw (IOException) cause;
		           } else if (cause instanceof RuntimeException) {
		               throw (RuntimeException) cause;
		           } else if (cause instanceof Error) {
		               throw (Error) cause;
		           } else {
		               /* unexpected checked exception; wrap and re-throw */
		               throw new RuntimeException(e);
		           }
			}
			return -1;
		}
		
		private static float runGetX(MotionEvent event, int index) throws IOException{
			try {
				return (Float)getX.invoke(event, index);
			} catch (IllegalArgumentException e) {
				Log.e("Floating Image", "Illegal argument exception caught!", e);
			} catch (IllegalAccessException e) {
				Log.e("Floating Image", "Unexpected Exception caught!", e);
			} catch (InvocationTargetException e) {
				Throwable cause = e.getCause();
		           if (cause instanceof IOException) {
		               throw (IOException) cause;
		           } else if (cause instanceof RuntimeException) {
		               throw (RuntimeException) cause;
		           } else if (cause instanceof Error) {
		               throw (Error) cause;
		           } else {
		               /* unexpected checked exception; wrap and re-throw */
		               throw new RuntimeException(e);
		           }
			}
			return -1;
		}
		
		private static float runGetY(MotionEvent event, int index) throws IOException{
			try {
				return (Float)getY.invoke(event, index);
			} catch (IllegalArgumentException e) {
				Log.e("Floating Image", "Illegal argument exception caught!", e);
			} catch (IllegalAccessException e) {
				Log.e("Floating Image", "Unexpected Exception caught!", e);
			} catch (InvocationTargetException e) {
				Throwable cause = e.getCause();
		           if (cause instanceof IOException) {
		               throw (IOException) cause;
		           } else if (cause instanceof RuntimeException) {
		               throw (RuntimeException) cause;
		           } else if (cause instanceof Error) {
		               throw (Error) cause;
		           } else {
		               /* unexpected checked exception; wrap and re-throw */
		               throw new RuntimeException(e);
		           }
			}
			return -1;
		}
		
		public boolean handleMultitouch(MotionEvent event) {
			if(getPointerCount == null) return false; // Old API, no multitouch
			int pointerCount = 0;
			try {
				pointerCount = runGetPointerCount(event);
			} catch (IOException e) {
				Log.e("Floating Image", "Error getting pointer count");
				return false;
			}
			if(pointerCount > 1 || mIsMultitouch){
				actOnMultitouch(event, pointerCount);
				return true;
			}
			return false;
		}
		
		private static Vec2f	mPointerBStart;
		private static int		mPointerA;
		private static int		mPointerB;
		
		private void actOnMultitouch(MotionEvent event, int pointerCount) {
			// We only get one "up" for when the user stops touching the screen!
			if(event.getAction() == MotionEvent.ACTION_UP){
				mIsMultitouch = false;
				renderer.transformEnd();
				return;
			}
			if(pointerCount < 2){
				return;
			}
			float ax = 0, ay = 0, bx = 0, by = 0;
			if(!mIsMultitouch){
				mClickTimer.cancel();
				mIsMultitouch = true;
				try{
					mPointerA = runGetPointerId(event, 0);
					mPointerB = runGetPointerId(event, 1);
					ax = runGetX(event, 0);
					ay = runGetY(event, 0);
					bx = runGetX(event, 1);
					by = runGetY(event, 1);
				}catch(IOException e){
					Log.e("Floating Image", "Unexpected exception caught", e);
					return;
				}
				mTouchStartPos.set(ax, ay);
				mPointerBStart.set(bx, by);
				renderer.moveInit();
				return;
			}else{
				try{
					for(int i = 0; i < pointerCount; ++i){
						int pointerID = runGetPointerId(event, i);
						if(mPointerA == pointerID){
							ax = runGetX(event, i);
							ay = runGetY(event, i);
						} else if(mPointerB == pointerID){
							bx = runGetX(event, i);
							by = runGetY(event, i);
						}
					}
				}catch(IOException e){
					Log.e("Floating Image", "Unexpected exception caught", e);
					return;
				}
			}
			float orgCenterX = mTouchStartPos.getX() + (mPointerBStart.getX() - mTouchStartPos.getX()) / 2.0f;
			float orgCenterY = mTouchStartPos.getY() + (mPointerBStart.getY() - mTouchStartPos.getY()) / 2.0f;
			float centerX = ax + (bx - ax) / 2.0f;
			float centerY = ay + (by - ay) / 2.0f;
			float orgXDiff = mTouchStartPos.getX() - mPointerBStart.getX();
			float orgYDiff = mTouchStartPos.getY() - mPointerBStart.getY();
			float orgDist = (float)Math.sqrt(orgXDiff * orgXDiff + orgYDiff * orgYDiff);
			float xDiff = ax - bx;
			float yDiff = ay - by;
			float dist = (float)Math.sqrt(xDiff * xDiff + yDiff * yDiff);
			float scale = dist / orgDist;
			float moveX = centerX - orgCenterX;
			float moveY = centerY - orgCenterY;
			float oldLength = (float)Math.sqrt(orgXDiff * orgXDiff + orgYDiff * orgYDiff);
			float newLength = (float)Math.sqrt(xDiff * xDiff + yDiff * yDiff);
			float oldXDir = orgXDiff / oldLength;
			float oldYDir = orgYDiff / oldLength;
			float newXDir = xDiff / newLength;
			float newYDir = yDiff / newLength;
			
			float rotation = (float)Math.acos(Vec2f.dot(oldXDir, oldYDir, newXDir, newYDir));
			float z = oldXDir * newYDir - oldYDir * newXDir; // Z-part of a cross
			if(z < 0) rotation = -rotation;
			
			renderer.transform(centerX, centerY, moveX, moveY, rotation, scale);
		}
	}
}
