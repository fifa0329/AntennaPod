package de.danoeh.antennapod.config;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import de.danoeh.antennapod.core.StorageCallbacks;
import de.danoeh.antennapod.core.storage.PodDBAdapter;

public class StorageCallbacksImpl implements StorageCallbacks {

    @Override
    public int getDatabaseVersion() {
        return 16;
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("DBAdapter", "Upgrading from version " + oldVersion + " to "
                + newVersion + ".");
        if (oldVersion <= 1) {
            db.execSQL("ALTER TABLE " + PodDBAdapter.TABLE_NAME_FEEDS + " ADD COLUMN "
                    + PodDBAdapter.KEY_TYPE + " TEXT");
        }
        if (oldVersion <= 2) {
            db.execSQL("ALTER TABLE " + PodDBAdapter.TABLE_NAME_SIMPLECHAPTERS
                    + " ADD COLUMN " + PodDBAdapter.KEY_LINK + " TEXT");
        }
        if (oldVersion <= 3) {
            db.execSQL("ALTER TABLE " + PodDBAdapter.TABLE_NAME_FEED_ITEMS
                    + " ADD COLUMN " + PodDBAdapter.KEY_ITEM_IDENTIFIER + " TEXT");
        }
        if (oldVersion <= 4) {
            db.execSQL("ALTER TABLE " + PodDBAdapter.TABLE_NAME_FEEDS + " ADD COLUMN "
                    + PodDBAdapter.KEY_FEED_IDENTIFIER + " TEXT");
        }
        if (oldVersion <= 5) {
            db.execSQL("ALTER TABLE " + PodDBAdapter.TABLE_NAME_DOWNLOAD_LOG
                    + " ADD COLUMN " + PodDBAdapter.KEY_REASON_DETAILED + " TEXT");
            db.execSQL("ALTER TABLE " + PodDBAdapter.TABLE_NAME_DOWNLOAD_LOG
                    + " ADD COLUMN " + PodDBAdapter.KEY_DOWNLOADSTATUS_TITLE + " TEXT");
        }
        if (oldVersion <= 6) {
            db.execSQL("ALTER TABLE " + PodDBAdapter.TABLE_NAME_SIMPLECHAPTERS
                    + " ADD COLUMN " + PodDBAdapter.KEY_CHAPTER_TYPE + " INTEGER");
        }
        if (oldVersion <= 7) {
            db.execSQL("ALTER TABLE " + PodDBAdapter.TABLE_NAME_FEED_MEDIA
                    + " ADD COLUMN " + PodDBAdapter.KEY_PLAYBACK_COMPLETION_DATE
                    + " INTEGER");
        }
        if (oldVersion <= 8) {
            final int KEY_ID_POSITION = 0;
            final int KEY_MEDIA_POSITION = 1;

            // Add feeditem column to feedmedia table
            db.execSQL("ALTER TABLE " + PodDBAdapter.TABLE_NAME_FEED_MEDIA
                    + " ADD COLUMN " + PodDBAdapter.KEY_FEEDITEM
                    + " INTEGER");
            Cursor feeditemCursor = db.query(PodDBAdapter.TABLE_NAME_FEED_ITEMS,
                    new String[]{PodDBAdapter.KEY_ID, PodDBAdapter.KEY_MEDIA}, "? > 0",
                    new String[]{PodDBAdapter.KEY_MEDIA}, null, null, null);
            if (feeditemCursor.moveToFirst()) {
                db.beginTransaction();
                ContentValues contentValues = new ContentValues();
                do {
                    long mediaId = feeditemCursor.getLong(KEY_MEDIA_POSITION);
                    contentValues.put(PodDBAdapter.KEY_FEEDITEM, feeditemCursor.getLong(KEY_ID_POSITION));
                    db.update(PodDBAdapter.TABLE_NAME_FEED_MEDIA, contentValues, PodDBAdapter.KEY_ID + "=?", new String[]{String.valueOf(mediaId)});
                    contentValues.clear();
                } while (feeditemCursor.moveToNext());
                db.setTransactionSuccessful();
                db.endTransaction();
            }
            feeditemCursor.close();
        }
        if (oldVersion <= 9) {
            db.execSQL("ALTER TABLE " + PodDBAdapter.TABLE_NAME_FEEDS
                    + " ADD COLUMN " + PodDBAdapter.KEY_AUTO_DOWNLOAD
                    + " INTEGER DEFAULT 1");
        }
        if (oldVersion <= 10) {
            db.execSQL("ALTER TABLE " + PodDBAdapter.TABLE_NAME_FEEDS
                    + " ADD COLUMN " + PodDBAdapter.KEY_FLATTR_STATUS
                    + " INTEGER");
            db.execSQL("ALTER TABLE " + PodDBAdapter.TABLE_NAME_FEED_ITEMS
                    + " ADD COLUMN " + PodDBAdapter.KEY_FLATTR_STATUS
                    + " INTEGER");
            db.execSQL("ALTER TABLE " + PodDBAdapter.TABLE_NAME_FEED_MEDIA
                    + " ADD COLUMN " + PodDBAdapter.KEY_PLAYED_DURATION
                    + " INTEGER");
        }
        if (oldVersion <= 11) {
            db.execSQL("ALTER TABLE " + PodDBAdapter.TABLE_NAME_FEEDS
                    + " ADD COLUMN " + PodDBAdapter.KEY_USERNAME
                    + " TEXT");
            db.execSQL("ALTER TABLE " + PodDBAdapter.TABLE_NAME_FEEDS
                    + " ADD COLUMN " + PodDBAdapter.KEY_PASSWORD
                    + " TEXT");
            db.execSQL("ALTER TABLE " + PodDBAdapter.TABLE_NAME_FEED_ITEMS
                    + " ADD COLUMN " + PodDBAdapter.KEY_IMAGE
                    + " INTEGER");
        }
        if (oldVersion <= 12) {
            db.execSQL("ALTER TABLE " + PodDBAdapter.TABLE_NAME_FEEDS
                    + " ADD COLUMN " + PodDBAdapter.KEY_IS_PAGED + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + PodDBAdapter.TABLE_NAME_FEEDS
                    + " ADD COLUMN " + PodDBAdapter.KEY_NEXT_PAGE_LINK + " TEXT");
        }
        if (oldVersion <= 13) {
            // remove duplicate rows in "Chapters" table that were created because of a bug.
            db.execSQL(String.format("DELETE FROM %s WHERE %s NOT IN " +
                            "(SELECT MIN(%s) as %s FROM %s GROUP BY %s,%s,%s,%s,%s)",
                    PodDBAdapter.TABLE_NAME_SIMPLECHAPTERS,
                    PodDBAdapter.KEY_ID,
                    PodDBAdapter.KEY_ID,
                    PodDBAdapter.KEY_ID,
                    PodDBAdapter.TABLE_NAME_SIMPLECHAPTERS,
                    PodDBAdapter.KEY_TITLE,
                    PodDBAdapter.KEY_START,
                    PodDBAdapter.KEY_FEEDITEM,
                    PodDBAdapter.KEY_LINK,
                    PodDBAdapter.KEY_CHAPTER_TYPE));
        }
        if(oldVersion <= 14) {
            db.execSQL("ALTER TABLE " + PodDBAdapter.TABLE_NAME_FEED_ITEMS
                    + " ADD COLUMN " + PodDBAdapter.KEY_AUTO_DOWNLOAD + " INTEGER");
            db.execSQL("UPDATE " + PodDBAdapter.TABLE_NAME_FEED_ITEMS
                    + " SET " + PodDBAdapter.KEY_AUTO_DOWNLOAD + " = "
                    + "(SELECT " + PodDBAdapter.KEY_AUTO_DOWNLOAD
                    + " FROM " + PodDBAdapter.TABLE_NAME_FEEDS
                    + " WHERE " + PodDBAdapter.TABLE_NAME_FEEDS + "." + PodDBAdapter.KEY_ID
                    + " = " + PodDBAdapter.TABLE_NAME_FEED_ITEMS + "." + PodDBAdapter.KEY_FEED + ")");

            db.execSQL("ALTER TABLE " + PodDBAdapter.TABLE_NAME_FEEDS
                    + " ADD COLUMN " + PodDBAdapter.KEY_HIDE + " TEXT");
            
            db.execSQL("ALTER TABLE " + PodDBAdapter.TABLE_NAME_FEEDS
                    + " ADD COLUMN " + PodDBAdapter.KEY_LAST_UPDATE_FAILED + " INTEGER DEFAULT 0");

            // create indexes
            db.execSQL(PodDBAdapter.CREATE_INDEX_FEEDITEMS_FEED);
            db.execSQL(PodDBAdapter.CREATE_INDEX_FEEDITEMS_IMAGE);
            db.execSQL(PodDBAdapter.CREATE_INDEX_FEEDMEDIA_FEEDITEM);
            db.execSQL(PodDBAdapter.CREATE_INDEX_QUEUE_FEEDITEM);
            db.execSQL(PodDBAdapter.CREATE_INDEX_SIMPLECHAPTERS_FEEDITEM);
        }
        if(oldVersion <= 15) {
            db.execSQL("ALTER TABLE " + PodDBAdapter.TABLE_NAME_FEED_MEDIA
                    + " ADD COLUMN " + PodDBAdapter.KEY_HAS_EMBEDDED_PICTURE + " INTEGER DEFAULT -1");
            db.execSQL("UPDATE " + PodDBAdapter.TABLE_NAME_FEED_MEDIA
                    + " SET " + PodDBAdapter.KEY_HAS_EMBEDDED_PICTURE + "=0"
                    + " WHERE " + PodDBAdapter.KEY_DOWNLOADED + "=0");
            Cursor c = db.rawQuery("SELECT " + PodDBAdapter.KEY_FILE_URL
                    + " FROM " + PodDBAdapter.TABLE_NAME_FEED_MEDIA
                    + " WHERE " + PodDBAdapter.KEY_DOWNLOADED + "=1 "
                    + " AND " + PodDBAdapter.KEY_HAS_EMBEDDED_PICTURE + "=-1", null);
            if(c.moveToFirst()) {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                do {
                    String fileUrl = c.getString(0);
                    try {
                        mmr.setDataSource(fileUrl);
                        byte[] image = mmr.getEmbeddedPicture();
                        if (image != null) {
                            db.execSQL("UPDATE " + PodDBAdapter.TABLE_NAME_FEED_MEDIA
                                    + " SET " + PodDBAdapter.KEY_HAS_EMBEDDED_PICTURE + "=1"
                                    + " WHERE " + PodDBAdapter.KEY_FILE_URL + "='"+ fileUrl + "'");
                        } else {
                            db.execSQL("UPDATE " + PodDBAdapter.TABLE_NAME_FEED_MEDIA
                                    + " SET " + PodDBAdapter.KEY_HAS_EMBEDDED_PICTURE + "=0"
                                    + " WHERE " + PodDBAdapter.KEY_FILE_URL + "='"+ fileUrl + "'");
                        }
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                } while(c.moveToNext());
            }
            c.close();
        }
    }
}
