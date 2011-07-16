package dk.nindroid.rss.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import dk.nindroid.rss.R;
import dk.nindroid.rss.gfx.ImageUtil;
import dk.nindroid.rss.settings.SourceSelectorAdapter.Source;

public class SourceSelector extends ListFragment {
	public static final int 	LOCAL_ACTIVITY  = 13;
	public static final int 	FLICKR_ACTIVITY = 14;
	public static final int 	PICASA_ACTIVITY = 15;
	public static final int 	FACEBOOK_ACTIVITY = 16;
	public static final int		PHOTOBUCKET_ACTIVITY = 17;
	
	boolean mDualPane;
	int mSelected = 0;
		
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		View sourceFrame = getActivity().findViewById(R.id.source);
        mDualPane = sourceFrame != null && sourceFrame.getVisibility() == View.VISIBLE;
		
        if(savedInstanceState != null){
        	mSelected = savedInstanceState.getInt("selected");
        }
        
        if (mDualPane) {
            // In dual-pane mode, the list view highlights the selected item.
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            // Make sure our UI is in the correct state.
            showSource(mSelected);
        }

		fillMenu();
	}
		
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("selected", mSelected);
		Thread t;
	}
	
	protected void fillMenu(){
		Activity activity = this.getActivity();
		String local = this.getString(R.string.local);
		Bitmap localBmp = ImageUtil.readBitmap(activity, R.drawable.phone_icon);
		SourceSelectorAdapter.Source localS = new Source(local, localBmp, LOCAL_ACTIVITY);
		
		String flickr = this.getString(R.string.flickr);
		Bitmap flickrBmp = ImageUtil.readBitmap(activity, R.drawable.flickr_icon);
		SourceSelectorAdapter.Source flickrS = new Source(flickr, flickrBmp, FLICKR_ACTIVITY);
		
		String picasa = this.getString(R.string.picasa);
		Bitmap picasaBmp = ImageUtil.readBitmap(activity, R.drawable.picasa_icon);
		SourceSelectorAdapter.Source picasaS = new Source(picasa, picasaBmp, PICASA_ACTIVITY);
		
		String facebook = this.getString(R.string.facebook);
		Bitmap facebookBmp = ImageUtil.readBitmap(activity, R.drawable.facebook_icon);
		SourceSelectorAdapter.Source facebookS = new Source(facebook, facebookBmp, FACEBOOK_ACTIVITY);
		
		String photobucket = this.getString(R.string.photobucket);
		Bitmap photobucketBmp = ImageUtil.readBitmap(activity, R.drawable.photobucket_icon);
		SourceSelectorAdapter.Source photobucketS = new Source(photobucket, photobucketBmp, PHOTOBUCKET_ACTIVITY);		
		
		SourceSelectorAdapter.Source[] options = new Source[] {localS, flickrS, picasaS, facebookS};
		setListAdapter(new SourceSelectorAdapter(activity, options));
	}
		
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {	
		super.onListItemClick(l, v, position, id);
		
		showSource(position);
	}
	
	void showSource(int source){
		this.mSelected = source;
		if(mDualPane){
			getListView().setItemChecked(source, true);
			Fragment f = getFragmentManager().findFragmentById(R.id.source);
			
			if (f == null || !(f instanceof SourceFragment) || ((SourceFragment)f).getSource() != source) {
				SourceFragment sf = getSource(source);
		        
		        if(sf != null){
			        // Execute a transaction, replacing any existing fragment
			        // with this one inside the frame.
			        FragmentTransaction ft = getFragmentManager().beginTransaction();
			        ft.replace(R.id.source, sf);
			        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			        ft.commit();
		        }
			}
			
		}else{
			Intent intent = new Intent();
			intent.setClass(getActivity(), SourceActivity.class);
			intent.putExtra("source", source);
			switch(source){
			case 0: // Local
                startActivityForResult(intent, LOCAL_ACTIVITY);
				break;
			case 1: // Flickr
                startActivityForResult(intent, FLICKR_ACTIVITY);
				break;
			case 2: // Picasa
				startActivityForResult(intent, PICASA_ACTIVITY);
				break;
			case 3: // Facebook
				startActivityForResult(intent, FACEBOOK_ACTIVITY);
				break;
			case 4: // Photobucket
				startActivityForResult(intent, PHOTOBUCKET_ACTIVITY);
			}
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK){
			Intent intent = new Intent();
			Bundle b = data.getExtras();
			switch(requestCode){
			case LOCAL_ACTIVITY:
				b.putInt("TYPE", Settings.TYPE_LOCAL);
				break;
			case FLICKR_ACTIVITY:
				b.putInt("TYPE", Settings.TYPE_FLICKR);
				break;
			case PICASA_ACTIVITY:
				b.putInt("TYPE", Settings.TYPE_PICASA);
				break;
			case FACEBOOK_ACTIVITY:
				b.putInt("TYPE", Settings.TYPE_FACEBOOK);
				break;
			case PHOTOBUCKET_ACTIVITY:
				b.putInt("TYPE", Settings.TYPE_PHOTOBUCKET);
				break;
			}
			intent.putExtras(b);
			getActivity().setResult(Activity.RESULT_OK, intent);
			getActivity().finish();
		}
	}
	
	static SourceFragment getSource(int sourceCode){
		switch(sourceCode){
    	case 0:
    		return new DirectoryBrowser();
    	case 1:
    		return new FlickrBrowser();
    	case 2:
    		return new PicasaBrowser();
    	case 3:
    		return new FacebookBrowser();
    	case 4: 
    		return new PhotobucketBrowser();
    	}
		return null;
	}
	
	public static class SourceActivity extends FragmentActivity{
		 @Override
	        protected void onCreate(Bundle savedInstanceState) {
	            super.onCreate(savedInstanceState);

	            if (getResources().getConfiguration().orientation
	                    == Configuration.ORIENTATION_LANDSCAPE) {
	                // If the screen is now in landscape mode, we can show the
	                // dialog in-line with the list so we don't need this activity.
	                finish();
	                return;
	            }

	            if (savedInstanceState == null) {
	                // During initial setup, plug in the details fragment.
	            	int sourceCode = getIntent().getIntExtra("source", -1);
	            	SourceFragment source = getSource(sourceCode);
	            	if(source == null){
	            		finish();
	            	}else{	                
		                source.setArguments(getIntent().getExtras());
		                getSupportFragmentManager().beginTransaction().add(android.R.id.content, source).commit();
	            	}
	            }
	        }
	}
	
	public static abstract class SourceFragment extends ListFragment{
		public SourceFragment(int source) {
			super();
            Bundle args = new Bundle();
            args.putInt("source", source);
            this.setArguments(args);
        }
		
		public int getSource(){
			return getArguments().getInt("source", 0);
		}
	}
}
