package com.gmail.mountainapp.scrane.mountainclimbers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class DataBaseHandler extends SQLiteOpenHelper {

    //database
    public static final String DATABASE_NAME = "userprogress.db";

    //tables
    public static final String TABLE_SCORES = "tablescores";
    public static final String TABLE_TUTORIAL = "tabletutorial";
    public static final String TABLE_ACHIEVEMENTS = "tableachievements";

    //columns
    public static final String COLUMN_ID = "columnid";
    public static final String COLUMN_COMPLETED = "columncompleted";
    public static final String COLUMN_BEST_MOVES = "columnbestmoves";
    public static final String COLUMN_BEST_TIME = "columnbesttime";
    public static final String COLUMN_OPTIMAL_MOVES = "columnoptimalmoves";
    public static final String COLUMN_INCREMENTS = "columnincrements";

    public DataBaseHandler(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    public int getId(int packpos, int levelpos){
        return packpos * 1000 + levelpos;
    }

    public boolean isFirstInPack(int id){
        return id % 1000 == 0;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SCORES + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_COMPLETED + " INTEGER, " +
                COLUMN_BEST_MOVES + " INTEGER, " +
                COLUMN_BEST_TIME + " INTEGER, " +
                COLUMN_OPTIMAL_MOVES + " INTEGER " +
                ")");
        for (int i = 0; i < Levels.packs.length; i++){
            Levels.Pack pack = Levels.packs[i];
            for (int j = 0; j < pack.getLevelIDs().length; j++){
                int id = getId(i, j);
                Cursor res = db.rawQuery("SELECT * FROM " + TABLE_SCORES +
                                " WHERE " + COLUMN_ID + "=" + Integer.toString(id),
                        null);
                if (res == null || res.getCount() == 0){
                    ContentValues row = new ContentValues();
                    row.put(COLUMN_ID, id);
                    row.put(COLUMN_COMPLETED, 0);
                    row.put(COLUMN_BEST_MOVES, -1);
                    row.put(COLUMN_BEST_TIME, -1);
                    row.put(COLUMN_OPTIMAL_MOVES, -1);
                    db.insert(TABLE_SCORES, null, row);
                }
            }
        }
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TUTORIAL + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_COMPLETED + " INTEGER " + ")");
        for (int i = 0; i < Levels.packs.length; i++){
            Levels.Pack pack = Levels.packs[i];
            for (int j = 0; j < pack.getNumTutorials(); j++){
                int id = getId(i, j);
                Cursor res = db.rawQuery("SELECT * FROM " + TABLE_SCORES +
                                " WHERE " + COLUMN_ID + "=" + Integer.toString(id),
                        null);
                if (res == null || res.getCount() == 0) {
                    ContentValues row = new ContentValues();
                    row.put(COLUMN_ID, id);
                    row.put(COLUMN_COMPLETED, 0);
                    db.insert(TABLE_TUTORIAL, null, row);
                }
            }
        }
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ACHIEVEMENTS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_INCREMENTS + " INTEGER " + ")");
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

    public void saveAchievementProgress(int id, int progress){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ACHIEVEMENTS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_INCREMENTS + " INTEGER " + ")");
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_INCREMENTS, progress);
        db.update(TABLE_ACHIEVEMENTS, cv, COLUMN_ID + "=" + id, null);
        db.close();
    }

    public int getAchievementProgress(int id){
        SQLiteDatabase db = getReadableDatabase();
        Cursor res = db.rawQuery(
                "SELECT * FROM " + TABLE_ACHIEVEMENTS + " WHERE " + COLUMN_ID + "=" + id,
                null);
        if (res == null || res.getCount() == 0){
            return 0;
        }
        res.moveToFirst();
        int progress = res.getInt(res.getColumnIndex(COLUMN_INCREMENTS));
        res.close();
        db.close();
        return progress;
    }

    public boolean isCompleted(int id){
        SQLiteDatabase db = getReadableDatabase();
        Cursor res = db.rawQuery(
                "SELECT * FROM " + TABLE_SCORES + " WHERE " + COLUMN_ID + "=" + id,
                null);
        if (res == null || res.getCount() == 0){
            ContentValues row = new ContentValues();
            row.put(COLUMN_ID, id);
            row.put(COLUMN_COMPLETED, 0);
            row.put(COLUMN_BEST_MOVES, -1);
            row.put(COLUMN_BEST_TIME, -1);
            row.put(COLUMN_OPTIMAL_MOVES, -1);
            db.insert(TABLE_SCORES, null, row);
            return false;
        }
        res.moveToFirst();
        boolean completed = res.getInt(res.getColumnIndex(COLUMN_COMPLETED)) == 1;
        res.close();
        db.close();
        return completed;
    }

    public boolean isCompletedTutorial(int id){
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TUTORIAL + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_COMPLETED + " INTEGER " + ")");
        Cursor res = db.rawQuery(
                "SELECT * FROM " + TABLE_TUTORIAL + " WHERE " + COLUMN_ID + "=" + id,
                null);
        if (res == null || res.getCount() == 0){
            ContentValues row = new ContentValues();
            row.put(COLUMN_ID, id);
            row.put(COLUMN_COMPLETED, 0);
            db.insert(TABLE_TUTORIAL, null, row);
            return false;
        }
        res.moveToFirst();
        boolean completed = res.getInt(res.getColumnIndex(COLUMN_COMPLETED)) == 1;
        res.close();
        db.close();
        return completed;
    }

    public void markCompleted(int id) {
        Log.d("DB", "completed level " + id);
        SQLiteDatabase db = getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_SCORES +
                        " WHERE " + COLUMN_ID + "=" + id,
                null);
        if (res == null || res.getCount() == 0){
            ContentValues row = new ContentValues();
            row.put(COLUMN_ID, id);
            row.put(COLUMN_COMPLETED, 1);
            row.put(COLUMN_BEST_MOVES, -1);
            row.put(COLUMN_BEST_TIME, -1);
            row.put(COLUMN_OPTIMAL_MOVES, -1);
            db.insert(TABLE_SCORES, null, row);
        } else {
            ContentValues row = new ContentValues();
            row.put(COLUMN_COMPLETED, 1);
            db.update(TABLE_SCORES, row, COLUMN_ID + "=" + id, null);
        }
        db.close();
    }

    public void markCompletedTutorial(int id){
        SQLiteDatabase db = getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_TUTORIAL +
                        " WHERE " + COLUMN_ID + "=" + id,
                null);
        if (res == null || res.getCount() == 0){
            ContentValues row = new ContentValues();
            row.put(COLUMN_ID, id);
            row.put(COLUMN_COMPLETED, 1);
            db.insert(TABLE_TUTORIAL, null, row);
        } else {
            ContentValues row = new ContentValues();
            row.put(COLUMN_COMPLETED, 1);
            db.update(TABLE_TUTORIAL, row, COLUMN_ID + "=" + id, null);
        }
        db.close();
    }

    public int howManyTutorialCompletedInPack(int packpos){
        SQLiteDatabase db = getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_TUTORIAL +
                        " WHERE " + COLUMN_COMPLETED + "=1 AND " +
                COLUMN_ID + " BETWEEN " + (1000 * packpos) + " AND " + (1000 * (packpos + 1) - 1),
                null );
        int ans;
        if (res == null){
            ans = 0;
        } else {
            ans = res.getCount();
        }
        res.close();
        db.close();
        return ans;
    }

    public int howManyLevelsCompleted(){
        SQLiteDatabase db = getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_SCORES +
                        " WHERE " + COLUMN_COMPLETED + "=1", null );
        int ans;
        if (res == null){
            ans = 0;
        } else {
            ans = res.getCount();
        }
        res.close();
        db.close();
        return ans;
    }

    public boolean isLocked(int id){
        if (isFirstInPack(id)){
            return false;
        }
        return !isCompleted(id - 1);
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

    public int getBestTimeSeconds(int id){
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

    public void setBestTimeSeconds(int id, int bestTime) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues row = new ContentValues();
        row.put(COLUMN_BEST_TIME, bestTime);
        db.update(TABLE_SCORES, row, COLUMN_ID + "=" + id, null);
        db.close();
    }

    public int countStars(int packpos){
        SQLiteDatabase db = getReadableDatabase();
        int ans = 0;
        Cursor res = db.rawQuery("SELECT * FROM " +  TABLE_SCORES + " WHERE " +
                COLUMN_ID + " BETWEEN " + (1000 * packpos) + " AND " + (1000 * (packpos + 1) - 1), null );
        res.moveToFirst();
        int bestMovesIndex = res.getColumnIndex(COLUMN_BEST_MOVES);
        int optimalMovesIndex = res.getColumnIndex(COLUMN_OPTIMAL_MOVES);
        while (!res.isAfterLast()){
            if (res.getInt(bestMovesIndex) != -1){
                ans += LevelListAdapter.howManyStars(res.getInt(bestMovesIndex), res.getInt(optimalMovesIndex));
            }
            res.moveToNext();
        }
        return ans;
    }

    void setOptimalMoves(int id, int optimal) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues row = new ContentValues();
        row.put(COLUMN_OPTIMAL_MOVES, optimal);
        db.update(TABLE_SCORES, row, COLUMN_ID + "=" + id, null);
        db.close();
    }

    public Future<Integer> getOptimalMoves(final int id, final Context context){
        Log.d("DB", "getting optimal moves for " + id);
        SQLiteDatabase db = getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_SCORES + " WHERE " + COLUMN_ID + "=" + id,
                null);
        int optimalMoves = -1;
        if (res.getColumnIndex(COLUMN_OPTIMAL_MOVES) == -1){
            db.execSQL("ALTER TABLE " + TABLE_SCORES + " ADD COLUMN " +
                    COLUMN_OPTIMAL_MOVES + " INTEGER DEFAULT -1");
        }
        if (res == null || res.getCount() == 0){
            ContentValues row = new ContentValues();
            row.put(COLUMN_ID, id);
            row.put(COLUMN_COMPLETED, 1);
            row.put(COLUMN_BEST_MOVES, -1);
            row.put(COLUMN_BEST_TIME, -1);
            row.put(COLUMN_OPTIMAL_MOVES, -1);
            db.insert(TABLE_SCORES, null, row);
            res.close();
            db.close();
            return Executors.newSingleThreadExecutor().submit(new Callable<Integer>() {
                @Override
                public Integer call(){
                    int packpos = id / 1000;
                    int levelpos = id % 1000;
                    int resourceID = Levels.packs[packpos].getLevelIDs()[levelpos];
                    int optimalMoves = Solver.solveFromResourceID(context, resourceID);
                    Log.d("DB", "Solved optimal moves");
                    ContentValues cv = new ContentValues();
                    cv.put(COLUMN_OPTIMAL_MOVES, optimalMoves);
                    SQLiteDatabase td = getWritableDatabase();
                    td.update(TABLE_SCORES, cv, COLUMN_ID + "=" + id, null);
                    td.close();
                    return optimalMoves;
                }
            });
        }
        res.moveToFirst();
        optimalMoves = res.getInt(res.getColumnIndex(COLUMN_OPTIMAL_MOVES));
        if (optimalMoves == -1){
            res.close();
            db.close();
            return Executors.newSingleThreadExecutor().submit(new Callable<Integer>() {
                @Override
                public Integer call(){
                    int packpos = id / 1000;
                    int levelpos = id % 1000;
                    int resourceID = Levels.packs[packpos].getLevelIDs()[levelpos];
                    int optimalMoves = Solver.solveFromResourceID(context, resourceID);
                    Log.d("DB", "Solved optimal moves");
                    ContentValues cv = new ContentValues();
                    cv.put(COLUMN_OPTIMAL_MOVES, optimalMoves);
                    SQLiteDatabase td = getWritableDatabase();
                    td.update(TABLE_SCORES, cv, COLUMN_ID + "=" + id, null);
                    td.close();
                    return optimalMoves;
                }
            });
        }
        final int optimalMovesDB = optimalMoves;
        Log.d("DB", "Found a previous calculation of optimal moves");
        res.close();
        db.close();
        return new Future<Integer>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }

            @Override
            public Integer get() throws ExecutionException, InterruptedException {
                return optimalMovesDB;
            }

            @Override
            public Integer get(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
                return optimalMovesDB;
            }
        };
    }

    public int howManyCompletedInPack(int packpos){
        SQLiteDatabase db = getReadableDatabase();
        int ans;
        Cursor res = db.rawQuery("SELECT * FROM " +  TABLE_SCORES + " WHERE " +
                COLUMN_COMPLETED + "=1 " + " AND " +
                COLUMN_ID + " BETWEEN " + (1000 * packpos) + " AND " + (1000 * (packpos + 1) - 1), null );
        if (res == null){
            ans = 0;
        } else {
            ans = res.getCount();
        }
        res.close();
        db.close();
        return ans;
    }

    public int howManyInUnder10Seconds(){
        SQLiteDatabase db = getReadableDatabase();
        int ans;
        Cursor res = db.rawQuery("SELECT * FROM " +  TABLE_SCORES + " WHERE " +
                COLUMN_BEST_TIME + " BETWEEN 0 AND 10", null );
        if (res == null){
            ans = 0;
        }
        ans = res.getCount();
        res.close();
        db.close();
        return ans;
    }

    public int howManyPerfect(){
        SQLiteDatabase db = getReadableDatabase();
        int ans;
        Cursor res = db.rawQuery("SELECT * FROM " +  TABLE_SCORES + " WHERE " +
                COLUMN_BEST_MOVES + "=" + COLUMN_OPTIMAL_MOVES +
                " AND " + COLUMN_BEST_MOVES + ">0", null );
        if (res == null){
            ans = 0;
        }
        ans = res.getCount();
        res.close();
        db.close();
        return ans;
    }

    public int howManyCompleted(){
        SQLiteDatabase db = getReadableDatabase();
        int ans;
        Cursor res = db.rawQuery("SELECT * FROM " +  TABLE_SCORES + " WHERE " +
                COLUMN_COMPLETED + "=1 ", null );
        if (res == null){
            ans = 0;
        }
        ans = res.getCount();
        res.close();
        db.close();
        return ans;
    }

    public boolean knowsOptimalMoves(int id){
        SQLiteDatabase db = getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_SCORES + " WHERE " + COLUMN_ID + "=" + id, null);
        boolean ans;
        if (res == null || res.getCount() == 0){
            ans = false;
        } else {
            res.moveToFirst();
            ans = res.getInt(res.getColumnIndex(COLUMN_BEST_MOVES)) != -1;
        }
        res.close();
        db.close();
        return ans;
    }

    public byte[] getBytes(Context context){
        File file = context.getDatabasePath(DATABASE_NAME);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            Log.d("DB", "Could not find database file");
            e.printStackTrace();
            return new byte[0];
        } catch (IOException e) {
            Log.d("DB", "Could not read database file");
            e.printStackTrace();
            return new byte[0];
        }
        return bytes;
    }

    public void restoreFromBytes(Context context, byte[] bytes){
        Log.d("DB", "Restoring from backup");
        try (FileOutputStream fos = new FileOutputStream(context.getDatabasePath(DATABASE_NAME))) {
            fos.write(bytes);
            Toast.makeText(context, "Transfer complete", Toast.LENGTH_LONG).show();
        } catch (IOException e){
            e.printStackTrace();
            Toast.makeText(context, "Error restoring backup", Toast.LENGTH_SHORT).show();
        }
    }

    public void mergeWithBytes(Context context, byte[] bytes){
        Log.d("DB", "Merging with backup");
        try (FileOutputStream fos = new FileOutputStream(context.getDatabasePath(BackUpHandler.BACKUP_NAME))) {
            fos.write(bytes);
        } catch (IOException e){
            e.printStackTrace();
            Toast.makeText(context, "Error restoring backup", Toast.LENGTH_SHORT).show();
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        BackUpHandler backUpHandler = new BackUpHandler(context);
        SQLiteDatabase backupDB = backUpHandler.getReadableDatabase();
        Cursor res = backupDB.rawQuery("SELECT * FROM " + TABLE_SCORES, null);
        if (res != null){
            res.moveToFirst();
            while (!res.isAfterLast()){
                int id = res.getInt(res.getColumnIndex(COLUMN_ID));
                ContentValues cv = new ContentValues();
                Log.d("DB", "Merging " + id);
                Cursor old = db.rawQuery("SELECT * FROM " + TABLE_SCORES + " WHERE " + COLUMN_ID + "=" + id, null);
                if (old.getCount() == 1){
                    old.moveToFirst();
                    int completed =  res.getInt(res.getColumnIndex(COLUMN_COMPLETED));
                    if (completed > 0){
                        cv.put(COLUMN_COMPLETED, res.getInt(res.getColumnIndex(COLUMN_COMPLETED)));
                    }
                    int bestTime = res.getInt(res.getColumnIndex(COLUMN_BEST_TIME));
                    if (bestTime != -1){
                        int oldTime = old.getInt(old.getColumnIndex(COLUMN_BEST_TIME));
                        if (oldTime > bestTime || oldTime == -1){
                            cv.put(COLUMN_BEST_TIME, bestTime);
                        }
                    }
                    int bestMoves = res.getInt(res.getColumnIndex(COLUMN_BEST_MOVES));
                    if (bestMoves != -1){
                        int oldMoves = old.getInt(old.getColumnIndex(COLUMN_BEST_MOVES));
                        if (oldMoves > bestMoves || oldMoves == -1){
                            cv.put(COLUMN_BEST_MOVES, bestMoves);
                        }
                    }
                    int oldOptimalMoves = old.getInt(old.getColumnIndex(COLUMN_OPTIMAL_MOVES));
                    int newOptimalMoves = res.getInt(res.getColumnIndex(COLUMN_OPTIMAL_MOVES));
                    if (oldOptimalMoves == -1 && newOptimalMoves != -1){
                        cv.put(COLUMN_OPTIMAL_MOVES, newOptimalMoves);
                    }
                    if (cv.size() > 0){
                        db.update(TABLE_SCORES, cv, COLUMN_ID + "=" + id, null);
                    }
                } else {
                    cv.put(COLUMN_ID, id);
                    cv.put(COLUMN_COMPLETED, res.getInt(res.getColumnIndex(COLUMN_COMPLETED)));
                    cv.put(COLUMN_BEST_TIME, res.getInt(res.getColumnIndex(COLUMN_BEST_TIME)));
                    cv.put(COLUMN_BEST_MOVES, res.getInt(res.getColumnIndex(COLUMN_BEST_MOVES)));
                    cv.put(COLUMN_OPTIMAL_MOVES, res.getInt(res.getColumnIndex(COLUMN_OPTIMAL_MOVES)));
                    db.insert(TABLE_SCORES, null, cv);
                }
                res.moveToNext();
            }
        }
        res.close();
        //TODO other tables
        backupDB.close();
        db.close();
        context.getDatabasePath(BackUpHandler.BACKUP_NAME).delete();
        Toast.makeText(context, "Transfer complete", Toast.LENGTH_LONG).show();
    }

    private class BackUpHandler extends SQLiteOpenHelper {

        public static final String BACKUP_NAME = "backup.db";
        public BackUpHandler(Context context) {
            super(context, BACKUP_NAME, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {}

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i1) {
            onCreate(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}
