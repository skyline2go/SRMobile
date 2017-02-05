package com.example.atest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BulkMobileDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "BulkDB";

 // Table Names
    private static final String TABLE_ROUTE = "routes";
    private static final String TABLE_PIC = "pics";
    private static final String TABLE_TICKET = "tickets";
    
    private static final int DATABASE_VERSION = 8;

    // Database creation sql statement
    private static final String DATABASE_CREATE_ROUTE = "create table routes(RID integer Primary Key, " 
    		     + "SRNo text, Status text, CompleteDate text, " 
    		     + "CompleteBy text, CloseDate text, Note text" + ");";

    private static final String DATABASE_CREATE_PIC = "CREATE TABLE pics(ID integer Primary Key AUTOINCREMENT NOT NULL, " 
            +	"RID text, FileName text, "
            + "FilePath text, FileDesc text,"  
            + "Long text, Lat text" + ");";
    
    private static final String DATABASE_CREATE_TICKET = "CREATE TABLE tickets(ID integer Primary Key AUTOINCREMENT NOT NULL, " 
    		+ "RID text, Status text" 
		    + ");";
    
    public BulkMobileDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase database) {
    	database.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTE);
        database.execSQL(DATABASE_CREATE_ROUTE);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_PIC);
        database.execSQL(DATABASE_CREATE_PIC);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_TICKET);
        database.execSQL(DATABASE_CREATE_TICKET);
    }

    // Method is called during an upgrade of the database,
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion,
            int newVersion) {
        Log.w(BulkMobileDatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTE);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_PIC);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_TICKET);
        onCreate(database);
    }
}