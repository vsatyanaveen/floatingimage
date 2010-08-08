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
	
	private static int 		mAction = 0; // 0: nothing, 1: move, 2: long click, 3: ??
	
	private final static int ACTION_NOTHING 	= 0;
	private final static int ACTION_MOVE    	= 1;
	private final static int ACTION_LONG_CLICK	= 2;
	
	public static void init(RiverRenderer renderer){
		ClickHandler.renderer = renderer;
		mTouchLastPos = new Vec2f();
		mTouchStartPos = new Vec2f();
	}
	
		
	@Override
	public void run() {
		mAction = ACTION_LONG_CLICK;
		ShowStreams.current.runOnUiThread(new OpenContextMenu());
	}
	
	public static boolean onTouchEvent(MotionEvent event) {
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
