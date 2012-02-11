package dk.nindroid.rss.parser.contentUri;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Images.ImageColumns;
import dk.nindroid.rss.data.ContentUriImage;
import dk.nindroid.rss.data.FeedReference;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.parser.FeedParser;
import dk.nindroid.rss.settings.Settings;

public class ContentUriParser implements FeedParser {
	@Override
	public List<ImageReference> parseFeed(FeedReference feed, Context context)
			throws ParserConfigurationException, SAXException,
			FactoryConfigurationError, IOException {
		String stringUri = feed.getFeedLocation();
		Uri uri = Uri.parse(stringUri);
		Cursor cursor = context.getContentResolver().query(uri, new String[]{ImageColumns.DISPLAY_NAME, ImageColumns.ORIENTATION, ImageColumns._ID}, null, null, null);
		List<ImageReference> irs = new ArrayList<ImageReference>();
		while(cursor.moveToNext()){
			String title = cursor.getString(0);
			int orientation = cursor.getInt(1);
			long id = cursor.getLong(2);
			ContentUriImage cui = new ContentUriImage(id, title, uri);
			cui.setRotation(orientation);
			irs.add(cui);
		}
		return irs;		
	}

	@Override
	public void init(Settings settings) {
		
	}

}
