package dk.nindroid.rss.parser.facebook;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FacebookFriendsBrowser extends ListActivity implements GetFriendsTask.Callback {
	private List<Friend> friends = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fillMenu();
	}
	
	private void fillMenu(){
		getFriends();
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
	
	private void getFriends(){
		new GetFriendsTask(this, this).execute();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			setResult(RESULT_OK, data);
			finish();
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
			finish();
			return;
		}
		
		friends = param;
		Collections.sort(friends);
		String[] options = new String[friends.size()];
		for(int i = 0; i < friends.size(); ++i){
			options[i] = friends.get(i).name;
		}
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options));
	}
}
