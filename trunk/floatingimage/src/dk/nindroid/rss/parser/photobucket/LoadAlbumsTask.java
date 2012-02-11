package dk.nindroid.rss.parser.photobucket;

import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.util.Log;
import dk.nindroid.rss.R;
import dk.nindroid.rss.uiActivities.BlockingTask;

public class LoadAlbumsTask extends BlockingTask<Boolean, List<String>>{
	String mUrl;
	PhotobucketShowUser callback;
	
	public LoadAlbumsTask(Context context, String url, PhotobucketShowUser callback) {
		super(context, R.string.photobucketLoadingAlbums);
		this.mUrl = url;
		this.callback = callback;
	}

	@Override
	protected List<String> doInBackground(Boolean... params) {
		try {
			String response = PhotobucketFeeder.getNoImages(mUrl);
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			XMLReader xmlReader = parser.getXMLReader();
			AlbumParser handler = new AlbumParser();
			xmlReader.setContentHandler(handler);
			xmlReader.parse(new InputSource(new StringReader(response)));
			return handler.albums;
		} catch (Exception e) {
			Log.w("Floating Image", "Error fetching photobucket albums", e);
		}
		return null;
	}

	@Override
	protected void onPostExecute(List<String> result) {
		super.onPostExecute(result);
		callback.albumsLoaded(result);
	}
}
