package dk.nindroid.rss.settings;

import java.util.ArrayList;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class FeedsDbAdapter {

    public static final String KEY_TITLE = "title";
    public static final String KEY_URI = "uri";
    public static final String KEY_TYPE = "type";
    public static final String KEY_ROWID = "_id";
    public static final String KEY_EXTRA = "extra";
    
    public static final String KEY_USER_TITLE = "user_title";
    public static final String KEY_USER_EXTRA = "user_extra";
    public static final String KEY_SORTING = "sorting";
    
    public static final String SUBDIR_KEY = "__ID";
    public static final String KEY_DIR = "dir";
    public static final String KEY_ENABLED = "enabled";

    private static final String TAG = "FeedsDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    /**
     * Database creation sql statement
     */
    private static final String DATABASE_NAME = "floatingImage";
    private static final String FEEDS_TABLE = "feeds";
    private static final String SUBDIR_TABLE = "subdirs";
    
    private static final String FEEDS_CREATE =
            					"create table " + FEEDS_TABLE + " ("
            				  + KEY_ROWID +" integer primary key autoincrement, "
            				  + KEY_TYPE + " integer not null, " 
            				  + KEY_URI + " text not null, " 
            				  + KEY_TITLE + " text not null, "
            				  + KEY_EXTRA + " text not null, "
            				  + KEY_SORTING + " integer not null, "
            				  + KEY_USER_TITLE + " text, "
            				  + KEY_USER_EXTRA + " text);";

    private static final String SUBDIR_CREATE =
    							"create table " + SUBDIR_TABLE + " ("
    						  + SUBDIR_KEY + " integer primary key autoincrement, "
    						  + KEY_DIR + " text, "
    						  + KEY_ROWID + " integer, "
    						  + KEY_ENABLED + " bookean not null);";
    
    private static final int DATABASE_VERSION = 6;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(FEEDS_CREATE);
            db.execSQL(SUBDIR_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ". Transfering data");
            if(oldVersion == 5){
            	upgradeFrom5(db);
            }else{
	            db.execSQL("DROP TABLE IF EXISTS " + FEEDS_TABLE);
	            onCreate(db);
            }
        }
        
        void upgradeFrom5(SQLiteDatabase db){
        	Cursor c = db.query(true, FEEDS_TABLE, new String[] {KEY_ROWID,
                            KEY_TITLE, KEY_URI, KEY_TYPE, KEY_EXTRA, KEY_SORTING, KEY_USER_TITLE, KEY_USER_EXTRA}, null, null, null, null, null, null);
        	List<Feed5> feeds = null;
            if (c != null) {
            	feeds = new ArrayList<FeedsDbAdapter.DatabaseHelper.Feed5>();
                while(c.moveToNext()){
                	Feed5 feed = new Feed5();
                	feed.title = c.getString(1);
                	feed.uri = c.getString(2);
                	feed.type = c.getInt(3);
                	feed.extra = c.getString(4);
                	feed.sorting = c.getInt(5);
                	feed.userTitle= c.getString(6);
                	feed.userExtra= c.getString(7);
                	feeds.add(feed);
                }
                c.close();
            }
            db.execSQL("DROP TABLE IF EXISTS " + FEEDS_TABLE);
            onCreate(db);
            
            if(feeds != null){
            	for(Feed5 feed : feeds){
            		ContentValues initialValues = new ContentValues();
                    initialValues.put(KEY_TITLE, feed.title);
                    initialValues.put(KEY_URI, feed.uri);
                    initialValues.put(KEY_TYPE, feed.type);
                    initialValues.put(KEY_EXTRA, feed.extra);
                    initialValues.put(KEY_SORTING, feed.sorting);
                    initialValues.put(KEY_USER_TITLE, feed.userTitle);
                    initialValues.put(KEY_USER_EXTRA, feed.userExtra);
                    db.insert(FEEDS_TABLE, null, initialValues);
            	}
            }
        }
        
        private class Feed5{
        	public String title;
        	public String uri;
        	public int type;
        	public String extra;
        	public int sorting;
        	public String userTitle;
        	public String userExtra;
        }
    }
    
    public void deleteAll(){
    	mDb.execSQL("DROP TABLE IF EXISTS " + FEEDS_TABLE);
    	mDb.execSQL("DROP TABLE IF EXISTS " + SUBDIR_TABLE);
    	mDbHelper.onCreate(mDb);
    }

    public FeedsDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public FeedsDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
    	mDb.close();
        mDbHelper.close();
        mDb = null;
    }


    /**
     * Add a feed
     * 
     * @param title the title of the note
     * @param body the body of the note
     * @return rowId or -1 if failed
     */
    public long addFeed(String title, String uri, int type, String extras) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_URI, uri);
        initialValues.put(KEY_TYPE, type);
        initialValues.put(KEY_EXTRA, extras);
        initialValues.put(KEY_SORTING, 3); // Reverse date search is default!
        //initialValues.put(KEY_ENABLED, enabled ? 1 : 0);

        return mDb.insert(FEEDS_TABLE, null, initialValues);
    }

    /**
     * Delete the note with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteFeed(long rowId) {
    	mDb.delete(SUBDIR_TABLE, KEY_ROWID + "=" + rowId, null);
        return mDb.delete(FEEDS_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public long addSubDir(long rowId, String name, boolean enabled){
    	ContentValues initialValues = new ContentValues();
    	initialValues.put(KEY_DIR, name);
    	initialValues.put(KEY_ENABLED, enabled ? 1 : 0);
    	initialValues.put(KEY_ROWID, rowId);
    	return mDb.insert(SUBDIR_TABLE, null, initialValues);
    }

    public Cursor getSubDirs(long rowId){
    	return mDb.query(SUBDIR_TABLE, new String[]{KEY_DIR, KEY_ENABLED}, KEY_ROWID + "=" + rowId, null, null, null, null);
    }
    
    public boolean deleteSubdirs(long rowId){
    	return mDb.delete(SUBDIR_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public Cursor fetchAllFeeds() {

        return mDb.query(FEEDS_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
                KEY_URI, KEY_TYPE, KEY_EXTRA, KEY_SORTING, KEY_USER_TITLE, KEY_USER_EXTRA}, null, null, null, null, KEY_TYPE);
    }

    public Cursor fetchFeed(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, FEEDS_TABLE, new String[] {KEY_ROWID,
                        KEY_TITLE, KEY_URI, KEY_TYPE, KEY_EXTRA, KEY_SORTING, KEY_USER_TITLE, KEY_USER_EXTRA}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }
    
    public Cursor fetchFeed(String path) throws SQLException {
    	Cursor mCursor =

                mDb.query(true, FEEDS_TABLE, new String[] {KEY_ROWID,
                        KEY_TITLE, KEY_URI, KEY_TYPE, KEY_EXTRA, KEY_SORTING, KEY_USER_TITLE, KEY_USER_EXTRA}, KEY_URI + "='" + path + "'", null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }


    public boolean updateFeed(long rowId, String title, String body, int type, String extras, int sorting, String userTitle, String userExtra) {
        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);
        args.put(KEY_URI, body);
        args.put(KEY_TYPE, type);
        args.put(KEY_EXTRA, extras);
        args.put(KEY_SORTING, sorting);
        args.put(KEY_USER_TITLE, userTitle);
        args.put(KEY_USER_EXTRA, userExtra);

        return mDb.update(FEEDS_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public boolean updateFeed(long rowId, int sorting, String userTitle, String userExtra) {
    	ContentValues args = new ContentValues();
    	args.put(KEY_SORTING, sorting);
        args.put(KEY_USER_TITLE, userTitle);
        args.put(KEY_USER_EXTRA, userExtra);

        return mDb.update(FEEDS_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
