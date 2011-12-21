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

public class CategoryBrowser extends SourceSelector.SourceFragment {
	public static final int CATEGORY_ABSTRACT = 0;
	public static final int CATEGORY_ANIMALS = 1;
	public static final int CATEGORY_BLACK_AND_WHITE = 2;
	public static final int CATEGORY_CELEBRITIES = 3;
	public static final int CATEGORY_CITY_AND_ARCHITECTURE = 4;
	public static final int CATEGORY_COMMERCIAL = 5;
	public static final int CATEGORY_CONCERT = 6;
	public static final int CATEGORY_FAMILY = 7;
	public static final int CATEGORY_FASHION = 8;
	public static final int CATEGORY_FILM = 9;
	public static final int CATEGORY_FINE_ART = 10;
	public static final int CATEGORY_FOOD = 11;
	public static final int CATEGORY_JOURNALISM = 12;
	public static final int CATEGORY_LANDSCAPES = 13;
	public static final int CATEGORY_MACRO = 14;
	public static final int CATEGORY_NATURE = 15;
	public static final int CATEGORY_NUDE = 16;
	public static final int CATEGORY_PEOPLE = 17;
	public static final int CATEGORY_PERFORMING_ARTS = 18;
	public static final int CATEGORY_SPORT = 19;
	public static final int CATEGORY_STILL_LIFE = 20;
	public static final int CATEGORY_STREET = 21;
	public static final int CATEGORY_TRAVEL = 22;
	public static final int CATEGORY_UNDERWATER = 23;
	
	public CategoryBrowser() {
		super(5);
	}
	
	boolean nudity;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		nudity = getActivity().getIntent().getBooleanExtra("nudity", false);
		fillMenu();
		super.onActivityCreated(savedInstanceState);
	}
	
	void fillMenu(){
		String sAbstract = this.getResources().getString(R.string.fivehundredpxCategoryAbstract);
		String animals = this.getResources().getString(R.string.fivehundredpxCategoryAnimals);
		String blackandwhite = this.getResources().getString(R.string.fivehundredpxCategoryBlackAndWhite);
		String celebrities = this.getResources().getString(R.string.fivehundredpxCategoryCelebrities);
		String cityandarchitecture = this.getResources().getString(R.string.fivehundredpxCategoryCityAndArchitecture);
		String commercial = this.getResources().getString(R.string.fivehundredpxCategoryCommercial);
		String concert = this.getResources().getString(R.string.fivehundredpxCategoryConcert);
		String family = this.getResources().getString(R.string.fivehundredpxCategoryFamily);
		String fashion = this.getResources().getString(R.string.fivehundredpxCategoryFashion);
		String film = this.getResources().getString(R.string.fivehundredpxCategoryFilm);
		String fineart = this.getResources().getString(R.string.fivehundredpxCategoryFineArt);
		String food = this.getResources().getString(R.string.fivehundredpxCategoryFood);
		String journalism = this.getResources().getString(R.string.fivehundredpxCategoryJournalism);
		String landscapes = this.getResources().getString(R.string.fivehundredpxCategoryLandscapes);
		String macro = this.getResources().getString(R.string.fivehundredpxCategoryMacro);
		String nature = this.getResources().getString(R.string.fivehundredpxCategoryNature);
		String nude = this.getResources().getString(R.string.fivehundredpxCategoryNude);
		String people = this.getResources().getString(R.string.fivehundredpxCategoryPeople);
		String performingarts = this.getResources().getString(R.string.fivehundredpxCategoryPerformingArts);
		String sport = this.getResources().getString(R.string.fivehundredpxCategorySport);
		String stillife = this.getResources().getString(R.string.fivehundredpxCategoryStillLife);
		String street = this.getResources().getString(R.string.fivehundredpxCategoryStreet);
		String travel = this.getResources().getString(R.string.fivehundredpxCategoryTravel);
		String underwater = this.getResources().getString(R.string.fivehundredpxCategoryUnderwater);
		String[] options;
		if(nudity){
			options = new String[]{sAbstract, animals, blackandwhite, celebrities, cityandarchitecture, 
					commercial, concert, family, fashion, film, fineart, food, journalism, landscapes, macro, nature, nude,
					people, performingarts, sport, stillife, street, travel, underwater};
		}else{
			options = new String[]{sAbstract, animals, blackandwhite, celebrities, cityandarchitecture, 
					commercial, concert, family, fashion, film, fineart, food, journalism, landscapes, macro, nature,  
					people, performingarts, sport, stillife, street, travel, underwater};	
		}
		setListAdapter(new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, options));
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if(!nudity && position >= CATEGORY_NUDE){
			++position;
		}
		switch(position){
		case CATEGORY_ABSTRACT:
			returnUrl(FiveHundredPxFeeder.getCategory(FiveHundredPxFeeder.CATEGORY_ABSTRACT), getString(R.string.fivehundredpxCategoryAbstract), "");
			break;
		case CATEGORY_ANIMALS:
			returnUrl(FiveHundredPxFeeder.getCategory(FiveHundredPxFeeder.CATEGORY_ANIMALS), getString(R.string.fivehundredpxCategoryAnimals), "");
			break;
		case CATEGORY_BLACK_AND_WHITE:
			returnUrl(FiveHundredPxFeeder.getCategory(FiveHundredPxFeeder.CATEGORY_BLACK_AND_WHITE), getString(R.string.fivehundredpxCategoryBlackAndWhite), "");
			break;
		case CATEGORY_CELEBRITIES :
			returnUrl(FiveHundredPxFeeder.getCategory(FiveHundredPxFeeder.CATEGORY_CELEBRITIES), getString(R.string.fivehundredpxCategoryCelebrities), "");
			break;
		case CATEGORY_CITY_AND_ARCHITECTURE:
			returnUrl(FiveHundredPxFeeder.getCategory(FiveHundredPxFeeder.CATEGORY_CITY_AND_ARCHITECTURE), getString(R.string.fivehundredpxCategoryCityAndArchitecture), "");
			break;
		case CATEGORY_COMMERCIAL:
			returnUrl(FiveHundredPxFeeder.getCategory(FiveHundredPxFeeder.CATEGORY_COMMERCIAL), getString(R.string.fivehundredpxCategoryCommercial), "");
			break;
		case CATEGORY_CONCERT:
			returnUrl(FiveHundredPxFeeder.getCategory(FiveHundredPxFeeder.CATEGORY_CONCERT), getString(R.string.fivehundredpxCategoryConcert), "");
			break;
		case CATEGORY_FAMILY:
			returnUrl(FiveHundredPxFeeder.getCategory(FiveHundredPxFeeder.CATEGORY_FAMILY), getString(R.string.fivehundredpxCategoryFamily), "");
			break;
		case CATEGORY_FASHION:
			returnUrl(FiveHundredPxFeeder.getCategory(FiveHundredPxFeeder.CATEGORY_FASHION), getString(R.string.fivehundredpxCategoryFashion), "");
			break;
		case CATEGORY_FILM:
			returnUrl(FiveHundredPxFeeder.getCategory(FiveHundredPxFeeder.CATEGORY_FILM), getString(R.string.fivehundredpxCategoryFilm), "");
			break;
		case CATEGORY_FINE_ART:
			returnUrl(FiveHundredPxFeeder.getCategory(FiveHundredPxFeeder.CATEGORY_FINE_ART), getString(R.string.fivehundredpxCategoryFineArt), "");
			break;
		case CATEGORY_FOOD:
			returnUrl(FiveHundredPxFeeder.getCategory(FiveHundredPxFeeder.CATEGORY_FOOD), getString(R.string.fivehundredpxCategoryFood), "");
			break;
		case CATEGORY_JOURNALISM:
			returnUrl(FiveHundredPxFeeder.getCategory(FiveHundredPxFeeder.CATEGORY_JOURNALISM), getString(R.string.fivehundredpxCategoryJournalism), "");
			break;
		case CATEGORY_LANDSCAPES:
			returnUrl(FiveHundredPxFeeder.getCategory(FiveHundredPxFeeder.CATEGORY_LANDSCAPES), getString(R.string.fivehundredpxCategoryLandscapes), "");
			break;
		case CATEGORY_MACRO:
			returnUrl(FiveHundredPxFeeder.getCategory(FiveHundredPxFeeder.CATEGORY_MACRO), getString(R.string.fivehundredpxCategoryMacro), "");
			break;
		case CATEGORY_NATURE:
			returnUrl(FiveHundredPxFeeder.getCategory(FiveHundredPxFeeder.CATEGORY_NATURE), getString(R.string.fivehundredpxCategoryNature), "");
			break;
		case CATEGORY_NUDE:
			returnUrl(FiveHundredPxFeeder.getCategory(FiveHundredPxFeeder.CATEGORY_NUDE), getString(R.string.fivehundredpxCategoryNude), "");
			break;
		case CATEGORY_PEOPLE:
			returnUrl(FiveHundredPxFeeder.getCategory(FiveHundredPxFeeder.CATEGORY_PEOPLE), getString(R.string.fivehundredpxCategoryPeople), "");
			break;
		case CATEGORY_PERFORMING_ARTS:
			returnUrl(FiveHundredPxFeeder.getCategory(FiveHundredPxFeeder.CATEGORY_PERFORMING_ARTS), getString(R.string.fivehundredpxCategoryPerformingArts), "");
			break;
		case CATEGORY_SPORT:
			returnUrl(FiveHundredPxFeeder.getCategory(FiveHundredPxFeeder.CATEGORY_SPORT), getString(R.string.fivehundredpxCategorySport), "");
			break;
		case CATEGORY_STILL_LIFE:
			returnUrl(FiveHundredPxFeeder.getCategory(FiveHundredPxFeeder.CATEGORY_STILL_LIFE), getString(R.string.fivehundredpxCategoryStillLife), "");
			break;
		case CATEGORY_STREET:
			returnUrl(FiveHundredPxFeeder.getCategory(FiveHundredPxFeeder.CATEGORY_STREET), getString(R.string.fivehundredpxCategoryStreet), "");
			break;
		case CATEGORY_TRAVEL:
			returnUrl(FiveHundredPxFeeder.getCategory(FiveHundredPxFeeder.CATEGORY_TRAVEL), getString(R.string.fivehundredpxCategoryTravel), "");
			break;
		case CATEGORY_UNDERWATER:
			returnUrl(FiveHundredPxFeeder.getCategory(FiveHundredPxFeeder.CATEGORY_UNDERWATER), getString(R.string.fivehundredpxCategoryUnderwater), "");
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
