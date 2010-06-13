package dk.nindroid.rss;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import dk.nindroid.rss.data.Progress;

public class DownloadUtil {
	private static final int CHUNKSIZE = 8192; // size of fixed chunks
	private static final int BUFFERSIZE = 1024; // size of reading buffer

	public static byte[] fetchUrlBytes(URL url, String userAgent, Progress progress)
			throws IOException {

		HttpURLConnection connection = null;

		if(progress != null){
			progress.setPercentDone(5);
		}
		connection = (HttpURLConnection) url.openConnection();
		if (userAgent != null) {
			connection.setRequestProperty("User-Agent", userAgent);
		}
		connection.setConnectTimeout(5000);
		connection.setReadTimeout(5000);
		
		int total = connection.getContentLength();

		int bytesRead = 0;
		byte[] buffer = new byte[BUFFERSIZE]; // initialize buffer
		byte[] fixedChunk = new byte[CHUNKSIZE]; // initialize 1st chunk
		ArrayList<byte[]> BufferChunkList = new ArrayList<byte[]>(); // List of
																		// chunk
																		// data
		int spaceLeft = CHUNKSIZE;
		int chunkIndex = 0;
		int totalRead = 0;

		DataInputStream in = new DataInputStream(connection.getInputStream());

		while ((bytesRead = in.read(buffer)) != -1) { // loop until the
														// DataInputStream is
														// completed
			totalRead += bytesRead;
			if(progress != null){
				progress.setPercentDone((totalRead * 100) / total);
			}
			if (bytesRead > spaceLeft) {
				// copy to end of current chunk
				System.arraycopy(buffer, 0, fixedChunk, chunkIndex, spaceLeft);
				BufferChunkList.add(fixedChunk);

				// create a new chunk, and fill in the leftover
				fixedChunk = new byte[CHUNKSIZE];
				chunkIndex = bytesRead - spaceLeft;
				System.arraycopy(buffer, spaceLeft, fixedChunk, 0, chunkIndex);
			} else {
				// plenty of space, just copy it in
				System.arraycopy(buffer, 0, fixedChunk, chunkIndex, bytesRead);
				chunkIndex = chunkIndex + bytesRead;
			}
			spaceLeft = CHUNKSIZE - chunkIndex;
		}
		//Log.v("Download util", "Bytes read: " + totalRead);
		
		if(progress != null){
			progress.setPercentDone(100);
		}

		if (in != null) {
			in.close();
		}

		// copy it all into one big array
		int responseSize = (BufferChunkList.size() * CHUNKSIZE) + chunkIndex;

		byte[] responseBody = new byte[responseSize];
		int index = 0;
		for (byte[] b : BufferChunkList) {
			System.arraycopy(b, 0, responseBody, index, CHUNKSIZE);
			index = index + CHUNKSIZE;
		}

		System.arraycopy(fixedChunk, 0, responseBody, index, chunkIndex);

		return responseBody;
	}
	
	public static String readStreamToEnd(String url) throws IOException{
		InputStream stream = HttpTools.openHttpConnection(url);
		BufferedInputStream bis = new BufferedInputStream(stream);
		byte[] buffer = new byte[8192];
		StringBuilder sb = new StringBuilder();
		int read = 0;
		while((read = bis.read(buffer)) != -1){
			sb.append(new String(buffer, 0, read));
		}
		return sb.toString();
	}
}
