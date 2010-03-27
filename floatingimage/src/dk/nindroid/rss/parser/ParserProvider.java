package dk.nindroid.rss.parser;

import java.util.HashMap;

import android.util.Log;

public class ParserProvider {
	private static HashMap<Integer, Class<? extends FeedParser>> parsers = new HashMap<Integer, Class<? extends FeedParser>>(); 
	
	public static void registerParser(int identifier, Class<? extends FeedParser> parser){
		parsers.put(identifier, parser);
	}
	
	public static FeedParser getParser(int type){
		if(parsers.containsKey(type)){
			try {
				return parsers.get(type).newInstance();
			} catch (IllegalAccessException e) {
				Log.e("ParserProvider", "Cannot create parser instance", e);
			} catch (InstantiationException e) {
				Log.e("ParserProvider", "Cannot create parser instance", e);
			}
		}
		return null;
	}
}
