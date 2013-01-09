package dk.nindroid.rss.renderers.osd;

import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public abstract class PlayPauseAbstractButton extends Button {
	int		mPlayTex;
	int 	mPauseTex;
	
	int		mPlayRes;
	int		mPauseRes;
	
	boolean playing = true;
	
	int		mCurrentTex;
	
	PlayPauseEventHandler listener;
	
	public PlayPauseAbstractButton(int play, int pause) {
		mPlayRes = play;
		mPauseRes = pause;
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
	
	public boolean isPlaying(){
		return playing;
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

	public void init(GL10 gl, Context context) {
		InputStream is = context.getResources().openRawResource(mPlayRes);
		Bitmap play = BitmapFactory.decodeStream(is);
		is = context.getResources().openRawResource(mPauseRes);
		Bitmap pause = BitmapFactory.decodeStream(is);
		
		int[] textures = new int[2];
		gl.glGenTextures(2, textures, 0);
		mPlayTex = textures[0];	
		mPauseTex = textures[1];
		setTexture(gl, play, mPlayTex);	
		setTexture(gl, pause, mPauseTex);
		mCurrentTex = mPauseTex;
		
		play.recycle();
		pause.recycle();
	}
	
	public void registerEventListener(PlayPauseEventHandler listener){
		this.listener = listener;
	}
}

