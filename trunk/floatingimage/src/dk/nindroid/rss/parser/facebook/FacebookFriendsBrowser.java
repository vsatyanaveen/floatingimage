package dk.nindroid.rss.parser.facebook;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import dk.nindroid.rss.R;
import dk.nindroid.rss.settings.FacebookBrowser;
import dk.nindroid.rss.settings.SettingsFragment;

public class FacebookFriendsBrowser extends ListFragment implements GetFriendsTask.Callback, SettingsFragment {
	private List<Friend> friends = null;
	
	boolean mDualPane;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		fillMenu();
		View sourceFrame = getActivity().findViewById(R.id.source);
        mDualPane = sourceFrame != null && sourceFrame.getVisibility() == View.VISIBLE;
	}
	
	private void fillMenu(){
		getFriends();
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		//Intent intent = new Intent(this, FacebookFriendView.class);
		Friend friend = friends.get(position);
		//intent.putExtra("ID", friend.getId());
		//intent.putExtra("Name", friend.getName());
		//this.startActivityForResult(intent, 0);
		if(mDualPane){
			FragmentTransaction ft = getFragmentManager().beginTransaction();
	        ft.replace(R.id.source, FacebookFriendView.getInstance(friend.getId(), friend.getName()));
	        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
	        ft.commit();
		}else{
			Intent intent = new Intent(this.getActivity(), SubActivity.class);
			intent.putExtra("ID", friend.getId());
			intent.putExtra("Name", friend.getName());
			this.startActivityForResult(intent, 0);
		}
	}
	
	private void getFriends(){
		new GetFriendsTask(getActivity(), this).execute();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK){
			getActivity().setResult(Activity.RESULT_OK, data);
			getActivity().finish();
		}
	}
	
	static class Friend implements Comparable<Friend> {
		private String name;
		private String id;
		
		public class FriendComparator implements Comparator<Friend>{
			@Override
			public int compare(Friend f1, Friend f2) {
				return f1.compareTo(f2);
			}
		}
		
		Friend(String name, String id){
			this.name = name;
			this.id = id;
		}
		
		public String getName(){
			return name;
		}
		
		public String getId(){
			return id;
		}

		@Override
		public int compareTo(Friend another) {
			return name.compareTo(another.name);
		}
	}

	@Override
	public void friendsFetched(List<Friend> param) {
		if(param == null) {
			getActivity().finish();
			return;
		}
		
		friends = param;
		Collections.sort(friends);
		String[] options = new String[friends.size()];
		for(int i = 0; i < friends.size(); ++i){
			options[i] = friends.get(i).name;
		}
		setListAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, options));
	}
	
	public static class SubActivity extends FragmentActivity{
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
				Fragment f = FacebookFriendView.getInstance(this.getIntent().getStringExtra("ID"), this.getIntent().getStringExtra("Name"));
				
				f.setArguments(getIntent().getExtras());
				getSupportFragmentManager().beginTransaction().add(android.R.id.content, f, "content").commit();
			}
		}
	}

	@Override
	public boolean back() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.source, new FacebookBrowser(), "content");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        ft.commit();
        return true;
	}
}
