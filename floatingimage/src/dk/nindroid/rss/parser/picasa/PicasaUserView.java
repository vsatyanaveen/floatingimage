package dk.nindroid.rss.parser.picasa;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import dk.nindroid.rss.R;
import dk.nindroid.rss.settings.PicasaBrowser;
import dk.nindroid.rss.settings.Settings;
import dk.nindroid.rss.settings.SettingsFragment;
import dk.nindroid.rss.settings.PicasaBrowser.AlbumActivity;

public class PicasaUserView extends ListFragment implements SettingsFragment {
	private static final int	STREAM		 		= 0;
	private static final int	ALBUMS				= 1;
	
	String id;
	boolean mDualPane;
	
	public static PicasaUserView getInstance(String id){
		PicasaUserView puv = new PicasaUserView();
		
		Bundle b = new Bundle();
		b.putString("ID", id);
		puv.setArguments(b);
		
		return puv;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		id = getArguments().getString("ID");
		View sourceFrame = getActivity().findViewById(R.id.source);
		mDualPane = sourceFrame != null && sourceFrame.getVisibility() == View.VISIBLE;
		fillMenu();
		
	}
	
	private void fillMenu(){
		String photosOf = this.getResources().getString(R.string.picasaShowStream);
		String albums = this.getResources().getString(R.string.picasaShowAlbums);
		String[] options = new String[]{photosOf, albums};
		setListAdapter(new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, options));
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		FrameLayout fl = new FrameLayout(this.getActivity());
		final EditText input = new EditText(this.getActivity());

		fl.addView(input, FrameLayout.LayoutParams.FILL_PARENT);
		input.setGravity(Gravity.CENTER);
		switch(position){
		case STREAM:
			returnStream();
			break;
		case ALBUMS:
			showAlbums();
			break;
		}
	}

	private void returnStream() {
		String url = null;
		url = PicasaFeeder.getRecent(id);
		if(url != null){
			returnResult(url, "Photos of " + id);
		}
	}
	
	private void showAlbums() {
		if(mDualPane){
			FragmentTransaction ft = getFragmentManager().beginTransaction();
	        ft.replace(R.id.source, PicasaAlbumBrowser.getInstance(id));
	        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
	        ft.commit();
		}else{
			Intent intent = new Intent(this.getActivity(), AlbumActivity.class);
			intent.putExtra(PicasaAlbumBrowser.OWNER, id);
			startActivityForResult(intent, ALBUMS);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK){
			switch(requestCode){
			case ALBUMS:
				this.getActivity().setResult(Activity.RESULT_OK, data);
				this.getActivity().finish();
				break;
			}
		}
	}
	
	private void returnResult(String url, String name){
		Intent intent = new Intent();
		Bundle b = new Bundle();
		
		b.putString("PATH", url);
		b.putString("NAME", name);
		b.putInt("TYPE", Settings.TYPE_PICASA);
		intent.putExtras(b);
		this.getActivity().setResult(Activity.RESULT_OK, intent);		
		this.getActivity().finish();
	}

	@Override
	public boolean back() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.source, new PicasaBrowser(), "content");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
        return true;
	}
	
	public static class AlbumActivity extends FragmentActivity{
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

				Fragment f = PicasaAlbumBrowser.getInstance(this.getIntent().getStringExtra(PicasaAlbumBrowser.OWNER));
				f.setArguments(getIntent().getExtras());
				getSupportFragmentManager().beginTransaction().add(android.R.id.content, f, "content").commit();
			}
		}
	}
}
