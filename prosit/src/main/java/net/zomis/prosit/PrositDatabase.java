package net.zomis.prosit;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Zomis on 2015-01-06.
 */
public class PrositDatabase extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "prosit";

    private static final String TABLE_POINTS = "points";

    private static final String KEY_ID = "id";
    private static final String KEY_WHO = "who";
    private static final String KEY_TIME = "sneeze_time";

    public PrositDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_POINTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_WHO + " TEXT,"
                + KEY_TIME + " timestamp not null default current_timestamp)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POINTS);
        onCreate(db);
    }
}
