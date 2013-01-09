package dk.nindroid.rss;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Process;
import android.util.Log;
import dk.nindroid.rss.compatibility.Exif;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.data.LocalImage;
import dk.nindroid.rss.data.Progress;
import dk.nindroid.rss.settings.Settings;

public class BitmapDownloader implements Runnable {
	TextureBank bank;
	FeedController mFeedController;
	static boolean exifAvailable = false;
	Settings mSettings;

	// Do we have access to the Exif class?
	static {
		try {
			Log.v("Floating Image", "Checking if Exif tool is available...");
			Exif.checkAvailable();
			exifAvailable = true;
			Log.v("Floating Image", "Enabling Exif reading!");
		} catch (Throwable t) {
			exifAvailable = false;
			Log.v("Floating Image", "Exif tool is not available");
		}
	}

	public BitmapDownloader(TextureBank bank, FeedController feedController,
			Settings settings) {
		this.bank = bank;
		this.mFeedController = feedController;
		this.mSettings = settings;
	}

	@Override
	public void run() {
		Log.v("Floating Image", "*** Starting asynchronous downloader thread");
		Process.setThreadPriority(15);
		while (true) {
			try {
				if (bank.stopThreads)
					return;
				for (int i = 0; i < 2; ++i) { // 0 = next, 1 = prev
					while (i == 0 ? bank.images.needNext() : bank.images
							.needPrev()) {
						if (bank.stopThreads) {
							Log.i("Floating Image",
									"*** Stopping asynchronous downloader thread per request");
							return;
						}
						ImageReference ir = i == 0 ? mFeedController
								.getNextImageReference() : mFeedController
								.getPrevImageReference();
						if (ir == null) {
							// No data
							Thread.sleep(100);
							break;
						} else {
							if (ir.getBitmap() != null
									&& !ir.getBitmap().isRecycled()) {
								continue; // Threading issue?
							}
							if (bank.doDownload(ir.getID())) {
								if (ir.getBitmap() != null
										&& !ir.getBitmap().isRecycled()) { // Image
																			// is
																			// being
																			// shown,
																			// ignore!
									break;
								} else if (ir instanceof LocalImage) {
									addLocalImage((LocalImage) ir, i == 0);
								} else {
									addExternalImage(ir, i == 0);
								}
							} else {
								bank.addFromCache(ir, i == 0);
							}
						}
					}
				}
				synchronized (bank.images) {
					if (bank.stopThreads) {
						Log.v("Floating Image",
								"*** Stopping asynchronous downloader thread per request");
						return;
					}
					try {
						bank.images.wait();
					} catch (InterruptedException e) {
						Log.v("Floating Image",
								"*** Stopping asynchronous downloader thread",
								e);
						return;
					}
				}
			} catch (Exception e) {
				Log.e("Floating Image", "Unexpected exception caught...", e);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
				}
			}
		}
	}

	public void addExternalImage(ImageReference ir, boolean next) {
		String url = mSettings.highResThumbs ? ir.get256ImageUrl() : ir
				.get128ImageUrl();
		ir.getExtended();
		Bitmap bmp = downloadImage(url, null, mSettings.bitmapConfig);
		if (bmp == null) {
			return;
		}
		if (mSettings.highResThumbs) {
			ir.set256Bitmap(bmp);
		} else {
			ir.set128Bitmap(bmp);
		}
		bank.addBitmap(ir, true, next);
	}

	public void addLocalImage(LocalImage image, boolean next) {

		File file = image.getFile();
		int size = mSettings.highResThumbs ? 256 : 128;
		Bitmap bmp = ImageFileReader.readImage(file, size, null,
				mSettings.bitmapConfig);
		if (bmp != null) {
			if (exifAvailable) {
				try {
					Exif exif = new Exif(file.getAbsolutePath());
					int rotation = exif.getAttributeInt(Exif.TAG_ORIENTATION,
							-1);
					switch (rotation) {
					case Exif.ORIENTATION_NORMAL:
					case -1:
						break;
					case Exif.ORIENTATION_ROTATE_90:
						image.setRotation(270);
						break;
					case Exif.ORIENTATION_ROTATE_180:
						image.setRotation(180);
						break;
					case Exif.ORIENTATION_ROTATE_270:
						image.setRotation(90);
						break;
					}
				} catch (IOException e) {
					Log.w("Floating Image", "Error reading exif info for file",
							e);
				} catch (Throwable t) {
					exifAvailable = false; // Some devices sort of know
											// ExifInterface...
					Log.w("Floating Image",
							"Disabling Exif Interface, the device lied!");
				}
			}
			try {
				if (mSettings.highResThumbs) {
					image.set256Bitmap(bmp);
				} else {
					image.set128Bitmap(bmp);
				}
			} catch (Throwable t) {
				Log.w("Floating Image", "OOM", t);
				return;
			}
			bank.addBitmap(image, true, next);
		}
	}

	public static Bitmap downloadImage(String URL, Progress progress,
			Config config) {
		URL url;
		byte[] bitmapByteArray;
		try {
			url = new URL(URL);
			bitmapByteArray = DownloadUtil.fetchUrlBytes(url,
					"Floating image/Android", progress);
			Options opts = new Options();
			opts.inPreferredConfig = config;
			Bitmap bm = BitmapFactory.decodeByteArray(bitmapByteArray, 0,
					bitmapByteArray.length, opts);
			return bm;
		} catch (Exception e) {
			Log.w("Floating Image", "Error handling URL \"" + URL + "\"", e);
		} catch (Throwable t) {
			Log.e("Floating Image", "Horrible error handling URL \"" + URL
					+ "\"", t);
		}
		return null;
	}

	public static Bitmap downloadImageDonut(String URL) {
		Bitmap bitmap = null;
		InputStream in = null;
		try {
			in = HttpTools.openHttpConnection(URL);
			bitmap = BitmapFactory.decodeStream(in);
			in.close();
		} catch (IOException e) {
			Log.e("Floating Image", "Error downloading bitmap", e);
		} catch (NullPointerException e) {
			Log.e("Floating Image", "Error downloading bitmap", e);
		}

		return bitmap;
	}
}
