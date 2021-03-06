package io.github.mthli.Tweetin.Database.Mention;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import io.github.mthli.Tweetin.R;
import io.github.mthli.Tweetin.Unit.Tweet.Tweet;

import java.util.ArrayList;
import java.util.List;

public class MentionAction {
    private SQLiteDatabase database;
    private MentionHelper helper;
    private Context context;

    public MentionAction(Context context) {
        this.helper = new MentionHelper(context);
        this.context = context;
    }

    public void openDatabase(boolean rw) {
        if (rw) {
            database = helper.getWritableDatabase();
        } else {
            database = helper.getReadableDatabase();
        }
    }

    public void closeDatabase() {
        helper.close();
    }

    public void addRecord(MentionRecord record) {
        ContentValues values = new ContentValues();
        values.put(MentionRecord.STATUS_ID, record.getStatusId());
        values.put(MentionRecord.REPLY_TO_STATUS_ID, record.getReplyToStatusId());
        values.put(MentionRecord.USER_ID, record.getUserId());
        values.put(MentionRecord.RETWEETED_BY_USER_ID, record.getRetweetedByUserId());
        values.put(MentionRecord.AVATAR_URL, record.getAvatarURL());
        values.put(MentionRecord.CREATED_AT, record.getCreatedAt());
        values.put(MentionRecord.NAME, record.getName());
        values.put(MentionRecord.SCREEN_NAME, record.getScreenName());
        if (record.isProtect()) {
            values.put(MentionRecord.PROTECT, "true");
        } else {
            values.put(MentionRecord.PROTECT, "false");
        }
        values.put(MentionRecord.CHECK_IN, record.getCheckIn());
        values.put(MentionRecord.PICTURE_URL, record.getPictureURL());
        values.put(MentionRecord.TEXT, record.getText());
        if (record.isRetweet()) {
            values.put(MentionRecord.RETWEET, "true");
        } else {
            values.put(MentionRecord.RETWEET, "false");
        }
        values.put(MentionRecord.RETWEETED_BY_USER_NAME, record.getRetweetedByUserName());
        if (record.isFavorite()) {
            values.put(MentionRecord.FAVORITE, "true");
        } else {
            values.put(MentionRecord.FAVORITE, "false");
        }
        database.insert(MentionRecord.TABLE, null, values);
    }

    public void updatedByRetweet(Tweet oldTweet) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sp_name),
                Context.MODE_PRIVATE
        );
        long useId = sharedPreferences.getLong(
                context.getString(R.string.sp_use_id),
                -1
        );

        ContentValues values = new ContentValues();
        values.put(
                MentionRecord.RETWEETED_BY_USER_ID,
                useId
        );
        values.put(
                MentionRecord.RETWEET,
                "true"
        );
        values.put(
                MentionRecord.RETWEETED_BY_USER_NAME,
                context.getString(R.string.tweet_info_retweeted_by_me)
        );

        database.update(
                MentionRecord.TABLE,
                values,
                MentionRecord.STATUS_ID + "=?",
                new String[] {String.valueOf(oldTweet.getStatusId())}
        );
    }

    public void updatedByFavorite(Tweet oldTweet) {
        ContentValues values = new ContentValues();
        if (oldTweet.isFavorite()) {
            values.put(
                    MentionRecord.FAVORITE,
                    "true"
            );
        } else {
            values.put(
                    MentionRecord.FAVORITE,
                    "false"
            );
        }
        database.update(
                MentionRecord.TABLE,
                values,
                MentionRecord.STATUS_ID + "=?",
                new String[] {String.valueOf(oldTweet.getStatusId())}
        );
    }

    public void deleteRecord(Tweet oldTweet) {
        database.execSQL("DELETE FROM "
                        + MentionRecord.TABLE
                        + " WHERE "
                        + MentionRecord.STATUS_ID
                        + " like \""
                        + String.valueOf(oldTweet.getStatusId())
                        + "\""
        );
    }

    public void deleteAll() {
        database.execSQL("DELETE FROM " + MentionRecord.TABLE);
    }

    private MentionRecord getMentionRecord(Cursor cursor) {
        MentionRecord record = new MentionRecord();
        record.setStatusId(cursor.getLong(0));
        record.setReplyToStatusId(cursor.getLong(1));
        record.setUserId(cursor.getLong(2));
        record.setRetweetedByUserId(cursor.getLong(3));
        record.setAvatarURL(cursor.getString(4));
        record.setCreatedAt(cursor.getString(5));
        record.setName(cursor.getString(6));
        record.setScreenName(cursor.getString(7));
        record.setProtect(
                cursor.getString(8).equals("true")
        );
        record.setCheckIn(cursor.getString(9));
        record.setPictureURL(cursor.getString(10));
        record.setText(cursor.getString(11));
        record.setRetweet(
                cursor.getString(12).equals("true")
        );
        record.setRetweetedByUserName(cursor.getString(13));
        record.setFavorite(
                cursor.getString(14).equals("true")
        );

        return record;
    }
    public List<MentionRecord> getMentionRecordList() {
        List<MentionRecord> mentionRecordList = new ArrayList<MentionRecord>();
        Cursor cursor = database.query(
                MentionRecord.TABLE,
                new String[] {
                        MentionRecord.STATUS_ID,
                        MentionRecord.REPLY_TO_STATUS_ID,
                        MentionRecord.USER_ID,
                        MentionRecord.RETWEETED_BY_USER_ID,
                        MentionRecord.AVATAR_URL,
                        MentionRecord.CREATED_AT,
                        MentionRecord.NAME,
                        MentionRecord.SCREEN_NAME,
                        MentionRecord.PROTECT,
                        MentionRecord.CHECK_IN,
                        MentionRecord.PICTURE_URL,
                        MentionRecord.TEXT,
                        MentionRecord.RETWEET,
                        MentionRecord.RETWEETED_BY_USER_NAME,
                        MentionRecord.FAVORITE
                },
                null,
                null,
                null,
                null,
                null
        );

        if (cursor == null) {
            return mentionRecordList;
        }
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            MentionRecord record = getMentionRecord(cursor);
            mentionRecordList.add(record);
            cursor.moveToNext();
        }
        cursor.close();

        return mentionRecordList;
    }
}
