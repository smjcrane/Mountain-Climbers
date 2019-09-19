package com.example.mountainclimbers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DataBaseHandler extends SQLiteOpenHelper {

    //database
    public static final String DATABASE_NAME = "userprogress.db";

    //tables
    public static final String TABLE_SCORES = "tablescores";

    //columns
    public static String COLUMN_ID = "columnid";
    public static String COLUMN_COMPLETED = "columncompleted";
    public static String COLUMN_LOCKED = "columnlocked";
    public static String COLUMN_BEST_MOVES = "columnbestmoves";
    public static String COLUMN_BEST_TIME = "columnbesttime";

    public DataBaseHandler(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SCORES + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_COMPLETED + " INTEGER, " +
                COLUMN_LOCKED + " INTEGER, " +
                COLUMN_BEST_MOVES + " INTEGER, " +
                COLUMN_BEST_TIME + " INTEGER " +
                ")");
        for (int id : LevelSelectActivity.levelIDs){
            Cursor res = db.rawQuery("SELECT * FROM " + TABLE_SCORES +
                    " WHERE " + COLUMN_ID + "=" + Integer.toString(id),
                    null);
            if (res != null && res.getCount() == 0){
                ContentValues row = new ContentValues();
                row.put(COLUMN_ID, id);
                row.put(COLUMN_COMPLETED, 0);
                row.put(COLUMN_LOCKED, (id == LevelSelectActivity.levelIDs[0] ? 0 : 1));
                row.put(COLUMN_BEST_MOVES, -1);
                row.put(COLUMN_BEST_TIME, -1);
                db.insert(TABLE_SCORES, null, row);
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public boolean getCompleted(int id){
        SQLiteDatabase db = getReadableDatabase();
        Cursor res = db.rawQuery(
                "SELECT * FROM " + TABLE_SCORES + " WHERE " + COLUMN_ID + "=" + id,
                null);
        if (res == null || res.getCount() == 0){
            return false;
        }
        res.moveToFirst();
        boolean completed = res.getInt(res.getColumnIndex(COLUMN_COMPLETED)) == 1;
        res.close();
        db.close();
        return completed;
    }

    public void markCompleted(int id) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues row = new ContentValues();
        row.put(COLUMN_COMPLETED, 1);
        db.update(TABLE_SCORES, row, COLUMN_ID + "=" + id, null);
        db.close();
    }

    public boolean isLocked(int id){
        SQLiteDatabase db = getReadableDatabase();
        Cursor res = db.rawQuery(
                "SELECT * FROM " + TABLE_SCORES + " WHERE " + COLUMN_ID + "=" + id,
                null);
        if (res == null || res.getCount() == 0){
            return false;
        }
        res.moveToFirst();
        boolean locked = res.getInt(res.getColumnIndex(COLUMN_LOCKED)) == 1;
        res.close();
        db.close();
        return locked;
    }

    public void unlock(int id) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues row = new ContentValues();
        row.put(COLUMN_LOCKED, 0);
        db.update(TABLE_SCORES, row, COLUMN_ID + "=" + id, null);
        db.close();
    }

    public int getBestMoves(int id){
        SQLiteDatabase db = getReadableDatabase();
        Cursor res = db.rawQuery(
                "SELECT * FROM " + TABLE_SCORES + " WHERE " + COLUMN_ID + "=" + id,
                null);
        if (res == null || res.getCount() == 0){
            return -1;
        }
        res.moveToFirst();
        int bestMoves = res.getInt(res.getColumnIndex(COLUMN_BEST_MOVES));
        res.close();
        db.close();
        return bestMoves;
    }

    public void setBestMoves(int id, int bestMoves) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues row = new ContentValues();
        row.put(COLUMN_BEST_MOVES, bestMoves);
        db.update(TABLE_SCORES, row, COLUMN_ID + "=" + id, null);
        db.close();
    }

    public int getBestTimeMilliseconds(int id){
        SQLiteDatabase db = getReadableDatabase();
        Cursor res = db.rawQuery(
                "SELECT * FROM " + TABLE_SCORES + " WHERE " + COLUMN_ID + "=" + id,
                null);
        if (res == null || res.getCount() == 0){
            return -1;
        }
        res.moveToFirst();
        int bestTime = res.getInt(res.getColumnIndex(COLUMN_BEST_TIME));
        res.close();
        db.close();
        return bestTime;
    }

    public void setBestTimeMilliseconds(int id, int bestTime) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues row = new ContentValues();
        row.put(COLUMN_BEST_TIME, bestTime);
        db.update(TABLE_SCORES, row, COLUMN_ID + "=" + id, null);
        db.close();
    }

}