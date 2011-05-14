package dk.nindroid.rss.settings;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import dk.nindroid.rss.R;
import dk.nindroid.rss.gfx.ImageUtil;
import dk.nindroid.rss.settings.SourceSelectorAdapter.Source;

public class SourceSelector extends ListActivity {
	public static final int 	LOCAL_ACTIVITY  = 13;
	public static final int 	FLICKR_ACTIVITY = 14;
	public static final int 	PICASA_ACTIVITY = 15;
	public static final int 	FACEBOOK_ACTIVITY = 16;
	public static final int		PHOTOBUCKET_ACTIVITY = 17;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		fillMenu();
	}
	
	protected void fillMenu(){
		String local = this.getString(R.string.local);
		Bitmap localBmp = ImageUtil.readBitmap(this, R.drawable.phone_icon);
		SourceSelectorAdapter.Source localS = new Source(local, localBmp, LOCAL_ACTIVITY);
		
		String flickr = this.getString(R.string.flickr);
		Bitmap flickrBmp = ImageUtil.readBitmap(this, R.drawable.flickr_icon);
		SourceSelectorAdapter.Source flickrS = new Source(flickr, flickrBmp, FLICKR_ACTIVITY);
		
		String picasa = this.getString(R.string.picasa);
		Bitmap picasaBmp = ImageUtil.readBitmap(this, R.drawable.picasa_icon);
		SourceSelectorAdapter.Source picasaS = new Source(picasa, picasaBmp, PICASA_ACTIVITY);
		
		String facebook = this.getString(R.string.facebook);
		Bitmap facebookBmp = ImageUtil.readBitmap(this, R.drawable.facebook_icon);
		SourceSelectorAdapter.Source facebookS = new Source(facebook, facebookBmp, FACEBOOK_ACTIVITY);
		
		String photobucket = this.getString(R.string.photobucket);
		Bitmap photobucketBmp = ImageUtil.readBitmap(this, R.drawable.photobucket_icon);
		SourceSelectorAdapter.Source photobucketS = new Source(photobucket, photobucketBmp, PHOTOBUCKET_ACTIVITY);		
		
		SourceSelectorAdapter.Source[] options = new Source[] {localS, flickrS, picasaS, facebookS, photobucketS};
		setListAdapter(new SourceSelectorAdapter(this, options));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {	
		super.onListItemClick(l, v, position, id);
		switch(position){
		case 0: // Local
			Intent showFolder = new Intent(this, DirectoryBrowser.class);
			startActivityForResult(showFolder, LOCAL_ACTIVITY);
			break;
		case 1: // Flickr
			Intent showFlickr = new Intent(this, FlickrBrowser.class);
			startActivityForResult(showFlickr, FLICKR_ACTIVITY);
			break;
		case 2: // Picasa
			Intent showPicasa = new Intent(this, PicasaBrowser.class);
			startActivityForResult(showPicasa, PICASA_ACTIVITY);
			break;
		case 3: // Facebook
			Intent showFacebook = new Intent(this, FacebookBrowser.class);
			startActivityForResult(showFacebook, FACEBOOK_ACTIVITY);
			break;
		case 4: // Photobucket
			Intent showPhotobucket = new Intent(this, PhotobucketBrowser.class);
			startActivityForResult(showPhotobucket, PHOTOBUCKET_ACTIVITY);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
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
			setResult(RESULT_OK, intent);
			finish();
		}
	}
}
