package dk.nindroid.rss.renderers.osd;

import java.io.InputStream;

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
	
	EventHandler listener;
	
	public Play(Context context) {
		InputStream is = context.getResources().openRawResource(R.drawable.osd_play);
		mPlay = BitmapFactory.decodeStream(is);
		is = context.getResources().openRawResource(R.drawable.osd_pause);
		mPause = BitmapFactory.decodeStream(is);
	}

	public void play(){
		if(!playing){
			click(0);
		}
	}
	
	public void pause(){
		if(playing){
			click(0);
		}
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
		if(listener  != null){
			if(playing){
				listener.Play();
			}else{
				listener.Pause();
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
		this.listener = listener;
	}
	
	public interface EventHandler{
		void Play();
		void Pause();
	}
}

