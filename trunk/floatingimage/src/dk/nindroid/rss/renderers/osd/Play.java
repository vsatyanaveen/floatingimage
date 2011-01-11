package dk.nindroid.rss.renderers.osd;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import dk.nindroid.rss.R;

public class Play extends Button {
	Bitmap 	mPlay;
	Bitmap 	mPause;
	int		mPlayTex;
	int 	mPauseTex;
	
	boolean playing = true;
	
	int		mCurrentTex;
	
	List<EventHandler> listeners;
	
	public Play(Context context) {
		InputStream is = context.getResources().openRawResource(R.drawable.osd_play);
		mPlay = BitmapFactory.decodeStream(is);
		is = context.getResources().openRawResource(R.drawable.osd_pause);
		mPause = BitmapFactory.decodeStream(is);
	}

	@Override
	public void click(long time) {
		if(playing){
			playing = false;
			mCurrentTex = mPlayTex;
			onEvent();
		}else{
			playing = true;
			mCurrentTex = mPauseTex;
			onEvent();
		}
	}
	
	public void onEvent(){
		if(listeners  != null){
			if(playing){
				for(EventHandler listener : listeners){
					listener.Play();
				}
			}else{
				for(EventHandler listener : listeners){
					listener.Pause();
				}
			}
		}
	}
	
	@Override
	public int getTextureID() {
		return mCurrentTex;
	}

	public void init(GL10 gl) {
		int[] textures = new int[2];
		gl.glGenTextures(2, textures, 0);
		mPlayTex = textures[0];	
		mPauseTex = textures[1];
		setTexture(gl, mPlay, mPlayTex);	
		setTexture(gl, mPause, mPauseTex);
		mCurrentTex = mPauseTex;
	}
	
	public void registerEventListener(EventHandler listener){
		if(listeners == null){
			listeners = new ArrayList<EventHandler>();
		}
		listeners.add(listener);
	}
	
	public interface EventHandler{
		void Play();
		void Pause();
	}
}

