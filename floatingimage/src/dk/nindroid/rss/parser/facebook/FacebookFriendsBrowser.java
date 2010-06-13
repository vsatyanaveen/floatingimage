package dk.nindroid.rss.parser.facebook;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import dk.nindroid.rss.DownloadUtil;

public class FacebookFriendsBrowser extends ListActivity {
	private List<Friend> friends = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fillMenu();
	}
	
	private void fillMenu(){
		friends = getFriends();
		if(friends == null) return;
		
		Collections.sort(friends);
		String[] options = new String[friends.size()];
		for(int i = 0; i < friends.size(); ++i){
			options[i] = friends.get(i).name;
		}
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent intent = new Intent(this, FacebookFriendView.class);
		Friend friend = friends.get(position);
		intent.putExtra("ID", friend.getId());
		intent.putExtra("Name", friend.getName());
		this.startActivityForResult(intent, 0);
	}
	
	private List<Friend> getFriends(){
		String url = null;
		try {
			url = FacebookFeeder.getMyFriendsUrl();
		} catch (MalformedURLException e) {
			Log.e("Floating Image", "Internal error. URL incorrect!", e);
			Toast.makeText(this, "Internal error detected. Please contact developer!", 2).show();
		} catch (IOException e) {
			Log.w("Floating Image", "Could not get url for the photos of me.", e);
			Toast.makeText(this, "Error getting stream. Please try again.", 2).show();
		}
		if(url == null){
			finish();
			return null;
		}else{
			try {
				
				String content = DownloadUtil.readStreamToEnd(url);
				JSONObject json = new JSONObject(content);
				
				JSONArray data = json.getJSONArray(FacebookTags.DATA);
				List<Friend> albums = new ArrayList<Friend>(data.length());
				for(int i = 0; i < data.length(); ++i){
					JSONObject obj = data.getJSONObject(i);
					String name = obj.getString(FacebookTags.NAME);
					String id = obj.getString(FacebookTags.ID);
					albums.add(new Friend(name, id));
				}
				
				return albums;
			} catch (JSONException e) {
				Log.w("Floating Image", "Error reading facebook stream", e);
				Toast.makeText(this, "Error reading stream.", 2).show();
			} catch (IOException e) {
				Log.w("Floaing Image", "Error getting albums", e);
				Toast.makeText(this, "Error getting stream. Please try again.", 2).show();
			}
		}
		finish();
		return null;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			setResult(RESULT_OK, data);
			finish();
		}
	}
	
	class Friend implements Comparable<Friend> {
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
}
