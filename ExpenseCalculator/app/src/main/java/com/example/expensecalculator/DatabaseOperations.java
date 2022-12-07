package com.example.expensecalculator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseOperations extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "UserList.db";
    private static final String TABLE_NAME = "Users";
    private static final String COL_1 = "EMAIL";
    private static final String COL_2 = "EXPENSE";

    public DatabaseOperations(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + TABLE_NAME + " "
                + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, Email TEXT, EXPENSE TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME, null);

        while(res.moveToNext()){
            Log.i("Get Data", "getAllData: " + res.getString(1) + " " + res.getString(2));
        }
        res.close();
    }

    public String getExpenseData(String email){
        // Map Data to insert into Query
        SQLiteDatabase db = this.getWritableDatabase();
        String[] columns = { COL_1 };
        String selection = COL_1 + " =?";
        String[] selectionArgs = { email };
        String limit = "1";
//        Fire Query
//        Cursor cursor = db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, limit);

        String expenseData = null;

        Log.i("getExpenseData: ", email);


        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " where EMAIL = '" +email + "'" , null);
        cursor.moveToNext();

        expenseData = cursor.getString(2);

        cursor.close(); // closing cursor
        db.close(); // closing db

        return expenseData;
    }

    // Check if user Exist
    public Boolean isUserExist(String email){

        // Map Data to insert into Query
        SQLiteDatabase db = this.getWritableDatabase();
        String[] columns = { COL_1 };
        String selection = COL_1 + " =?";
        String[] selectionArgs = { email };
        String limit = "1";

        Cursor cursor = db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, limit);
        boolean exists = (cursor.getCount() > 0);
        cursor.close(); // closing cursor
        db.close(); // closing db
        return exists;
    }

    // Insert new users into the DB
    public Boolean insertUser(String email){

        // Map Data to insert into Query
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, email);
        contentValues.put(COL_2, "{}");

        long result = db.insert(TABLE_NAME, null, contentValues);
        db.close(); // closing db
        if (result == -1) {
            Log.i("InsertData", "No Record Added");
            return false;
        }
        else {
            Log.i("InsertData", "Record Added");
            return true;
        }
    }


    // Insert Expenses
    public void insertExpense(){

    }

    public void updateUserData(String email, String expenseData){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
//        contentValues.put(COL_1, email);
        contentValues.put(COL_2, expenseData);

        int isDataUpdated = db.update(TABLE_NAME, contentValues, "EMAIL = ?", new String[]{email});

        // Check if the data was updated
        if(isDataUpdated != 0) Log.i("Btn Delete listener", "Record Updated");
        else Log.i("Btn Delete listener", "No Record updated");

    };


//    public boolean updateData(String id, String name, String marks) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(COL_1, id);
//        contentValues.put(COL_2, name);
//        contentValues.put(COL_3, marks);
//
//        int isDataDeleted = db.update(TABLE_NAME, contentValues, "ID = ?", new String[]{id});
//
//        // Check if the data was updated
//        if(isDataDeleted != 0)
//            Log.i("Btn Delete listener", "Record Updated");
//        else
//            Log.i("Btn Delete listener", "No Record updated");
//
//        return true;
////        return isDataUpdated;
//    }
//
//    public Integer deleteData(String id) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        return db.delete(TABLE_NAME, "ID = ?", new String[]{id});
//    }

}
