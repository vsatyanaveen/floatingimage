package dk.nindroid.rss.parser.fivehundredpx;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import dk.nindroid.rss.R;
import dk.nindroid.rss.settings.FiveHundredPxBrowser;
import dk.nindroid.rss.settings.Settings;
import dk.nindroid.rss.settings.SourceSelector;

public class DiscoverBrowser extends SourceSelector.SourceFragment {
	public static final int POPULAR = 0;
	public static final int UPCOMING = 1;
	public static final int EDITORS = 2;
	public static final int TODAY = 3;
	public static final int YESTERDAY = 4;
	public static final int WEEK = 5;
	
	public DiscoverBrowser() {
		super(5);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		fillMenu();
		super.onActivityCreated(savedInstanceState);
	}
	
	void fillMenu(){
		String popular = this.getResources().getString(R.string.fivehundredpxPopular);
		String upcoming = this.getResources().getString(R.string.fivehundredpxUpcoming);
		String editors = this.getResources().getString(R.string.fivehundredpxEditors);
		String today = this.getResources().getString(R.string.fivehundredpxToday);
		String yesterday = this.getResources().getString(R.string.fivehundredpxYesterday);
		String week = this.getResources().getString(R.string.fivehundredpxWeek);
		String[] options = new String[]{popular, upcoming, editors, today, yesterday, week};
		setListAdapter(new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, options));
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		switch(position){
		case POPULAR:
			returnUrl(FiveHundredPxFeeder.getPopular(), getString(R.string.fivehundredpxPopular), getString(R.string.fivehundredpxPopularSummary));
			break;
		case UPCOMING:
			returnUrl(FiveHundredPxFeeder.getUpcoming(), getString(R.string.fivehundredpxUpcoming), getString(R.string.fivehundredpxUpcomingSummary));
			break;
		case EDITORS:
			returnUrl(FiveHundredPxFeeder.getEditors(), getString(R.string.fivehundredpxEditors), getString(R.string.fivehundredpxEditorsSummary));
			break;
		case TODAY:
			returnUrl(FiveHundredPxFeeder.getToday(), getString(R.string.fivehundredpxToday), getString(R.string.fivehundredpxTodaySummary));
			break;
		case YESTERDAY:
			returnUrl(FiveHundredPxFeeder.getYesterday(), getString(R.string.fivehundredpxYesterday), getString(R.string.fivehundredpxYesterdaySummary));
			break;
		case WEEK:
			returnUrl(FiveHundredPxFeeder.getWeek(), getString(R.string.fivehundredpxWeek), getString(R.string.fivehundredpxWeekSummary));
			break;
		}
		super.onListItemClick(l, v, position, id);
	}
	
	void returnUrl(String url, String title, String extras){
		Intent intent = new Intent();
		Bundle b = new Bundle();
		b.putString("PATH", url);
		b.putString("NAME", title);
		b.putString("EXTRAS", extras);
		b.putInt("TYPE", Settings.TYPE_FIVEHUNDREDPX);
		intent.putExtras(b);
		this.getActivity().setResult(Activity.RESULT_OK, intent);		
		this.getActivity().finish();
	}

	@Override
	public boolean back() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.source, new FiveHundredPxBrowser(), "content");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        ft.commit();
        return true;
	}
}
