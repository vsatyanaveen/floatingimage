package dk.nindroid.rss;

import java.util.Timer;
import java.util.TimerTask;

import android.view.MotionEvent;
import dk.nindroid.rss.gfx.Vec2f;
import dk.nindroid.rss.uiActivities.OpenContextMenu;

public class ClickHandler extends TimerTask {
	private static RiverRenderer renderer;
	private static Vec2f 	mTouchLastPos;
	private static Vec2f	mTouchStartPos;
	private static Timer	mClickTimer;
	private static final int LONGCLICKTIME = 1000;
	private static long 	mMoveTime;
	private static float[]	mLastSpeedX = new float[2];
	private static float[]	mLastSpeedY = new float[2];
	private static boolean 	mIsMultitouch = false;
	private static Vec2f	mPointerBStart;
	private static int		mPointerA;
	private static int		mPointerB;
	
	private static int 		mAction = 0; // 0: nothing, 1: move, 2: long click, 3: ??
	
	private final static int ACTION_NOTHING 	= 0;
	private final static int ACTION_MOVE    	= 1;
	private final static int ACTION_LONG_CLICK	= 2;
	
	public static void init(RiverRenderer renderer){
		ClickHandler.renderer = renderer;
		mTouchLastPos = new Vec2f();
		mTouchStartPos = new Vec2f();
		mPointerBStart = new Vec2f();
	}
	
		
	@Override
	public void run() {
		mAction = ACTION_LONG_CLICK;
		ShowStreams.current.runOnUiThread(new OpenContextMenu());
	}
	
	public static boolean onTouchEvent(MotionEvent event) {
		/*
		// Multitouch, 2.1-update1+ only!
		if(event.getPointerCount() > 1 || mIsMultitouch){
			handleMultitouch(event);
			return true;
		}
		*/
		
		int action = event.getAction();
		float x = event.getX(); float y = event.getY();
		float lastX = mTouchLastPos.getX();
		float lastY = mTouchLastPos.getY();
		if(action ==  MotionEvent.ACTION_DOWN){
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
				renderer.move(x, y, (mLastSpeedX[0] + mLastSpeedX[1]) / 2, (mLastSpeedY[0] + mLastSpeedY[1]) / 2);
			}
		}
		if(action == MotionEvent.ACTION_UP){
			renderer.moveRelease();
			for(int i = 0; i < 2; ++i){
				mLastSpeedX[i] = 0;
				mLastSpeedY[i] = 0;
			}
		}
		mTouchLastPos.setX(x);
		mTouchLastPos.setY(y);
		return true;
	}
	/*
	private static void handleMultitouch(MotionEvent event) {
		// We only get one "up" for when the user stops touching the screen!
		if(event.getAction() == MotionEvent.ACTION_UP){
			mIsMultitouch = false;
			return;
		}
		float ax = 0, ay = 0, bx = 0, by = 0;
		if(!mIsMultitouch){
			mClickTimer.cancel();
			mIsMultitouch = true;
			mPointerA = event.getPointerId(0);
			mPointerB = event.getPointerId(1);
			ax = event.getX(0);
			ay = event.getY(0);
			bx = event.getX(1);
			by = event.getY(1);
			mTouchStartPos.set(ax, ay);
			mPointerBStart.set(bx, by);
			return;
		}else{
			for(int i = 0; i < event.getPointerCount(); ++i){
				if(mPointerA == event.getPointerId(i)){
					ax = event.getX(i);
					ay = event.getY(i);
				} else if(mPointerB == event.getPointerId(i)){
					bx = event.getX(i);
					by = event.getY(i);
				}
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
		
		//Log.v("Floating Image", "Multitouch move: (" + moveX +  "," + moveY + ")");
		//Log.v("Floating Image", "Multitouch rotation: " + rotation * 180.0 / Math.PI + ", z: " + z);
		//Log.v("Floating Image", "Multitouch scale: " + scale);
	}
*/

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
}
