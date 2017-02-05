package com.housebook.SRMobile;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class BulkMobileDB{  

private BulkMobileDatabaseHelper dbHelper;  

//private SQLiteDatabase database;  
//private SQLiteDatabase database2;  

//Routes table name
private static final String TABLE_ROUTES = "routes";
public final static String ROUTE_RID="RID"; // id value for route
public final static String ROUTE_SRNO="SRNo"; // id value for route
public final static String ROUTE_STATUS="Status"; // status value for route
public final static String ROUTE_COMPLETEDATE="CompleteDate"; // complete date value for route
public final static String ROUTE_COMPLETEBY="CompleteBy"; // complete by value for route
public final static String ROUTE_CLOSEDATE="CloseDate"; // close by value for route
public final static String ROUTE_NOTE="Note"; // note value for route

//Pictures table name
private static final String TABLE_PICS = "pics";
public final static String PIC_RID="RID"; // id value for route
public final static String PIC_ID="ID"; // status value for route
public final static String PIC_FILENAME="FileName"; // complete date value for route
public final static String PIC_FILEPATH="FilePath"; // complete by value for route
public final static String PIC_FILEDESC="FileDesc"; // close by value for route
public final static String PIC_LONG="Long"; // note value for route
public final static String PIC_LAT="Lat"; // note value for route

//Routes table name
private static final String TABLE_TICKETS = "tickets";
public final static String TICKET_ID="ID"; // id value for route
public final static String TICKET_RID="RID"; // id value for route
public final static String TICKET_STATUS="Status"; // Ticket status value for route

public BulkMobileDB(Context context){  
    dbHelper = new BulkMobileDatabaseHelper(context);  
    //database = dbHelper.getWritableDatabase();  
    //database2 = dbHelper.getReadableDatabase();  
}

//
// All CRUD(Create, Read, Update, Delete) Operations
//
//
// Adding new route
public void addRoute(RouteData iRoute) {
	SQLiteDatabase database = dbHelper.getWritableDatabase();
	
    ContentValues values = new ContentValues();
    //values.put(ROUTE_RID,Integer.parseInt(iRoute.getSRNo())); // Route SR No
    values.put(ROUTE_RID,convertToRID(iRoute.getSRNo()));
    values.put(ROUTE_SRNO, iRoute.getSRNo()); // Route SR No
    values.put(ROUTE_STATUS, iRoute.getStatus()); // Route Status;
    values.put(ROUTE_COMPLETEDATE, iRoute.getCompleteDate()); // Route Status;
    values.put(ROUTE_COMPLETEBY, iRoute.getCompleteBy());
    values.put(ROUTE_CLOSEDATE, iRoute.getCloseDate());
    values.put(ROUTE_NOTE, iRoute.getNote());

    // Inserting Row
    database.insert(TABLE_ROUTES, null, values);
    database.close(); // Closing database connection
}

private int convertToRID(String iSRNo) {
	if (iSRNo!= null) {
		String[] lResult = iSRNo.split("-");
		if (lResult.length ==2) {
			return Integer.parseInt(lResult[1]);
		}
		else return 0;
			
	}else 
		return 0;
}
//Getting single route
RouteData getRoute(String iRid) {
	SQLiteDatabase database = dbHelper.getReadableDatabase();
	
    Cursor cursor = database.query(TABLE_ROUTES, new String[] { ROUTE_RID,
    		ROUTE_STATUS, ROUTE_COMPLETEDATE,ROUTE_COMPLETEBY, ROUTE_CLOSEDATE,ROUTE_NOTE }, ROUTE_RID + "=?",
            new String[] { String.valueOf(iRid) }, null, null, null, null);
    if (cursor != null)
        cursor.moveToFirst();

    RouteData route = new RouteData(
    		cursor.getString(0), cursor.getString(1), 
    		cursor.getString(2), cursor.getString(3), 
    		cursor.getString(4), cursor.getString(5));
    // return contact
    return route;
}

//Getting All Routes
public List<RouteData> getAllRoutes() {
	
	SQLiteDatabase database = dbHelper.getWritableDatabase();
    List<RouteData> contactList = new ArrayList<RouteData>();
    // Select All Query
    String selectQuery = "SELECT * FROM " + TABLE_ROUTES;

    //SQLiteDatabase db = this.getWritableDatabase();
    Cursor cursor = database.rawQuery(selectQuery, null);

    // looping through all rows and adding to list
    if (cursor.moveToFirst()) {
        do {
        	RouteData contact = new RouteData();
            contact.setSRNo(cursor.getString(1));
            contact.setStatus(cursor.getString(2));
            contact.setCompleteDate(cursor.getString(3));
            contact.setCompleteBy(cursor.getString(4));
            contact.setCloseDate(cursor.getString(5));
            contact.setNote(cursor.getString(6));
            // Adding contact to list
            // Adding contact to list
            contactList.add(contact);
        } while (cursor.moveToNext());
    }

    // return contact list
    return contactList;
}

//Updating single route
public int updateRoute(RouteData iRoute) {
	SQLiteDatabase database = dbHelper.getWritableDatabase();
	
    ContentValues values = new ContentValues();
    values.put(ROUTE_STATUS, iRoute.getStatus());
    values.put(ROUTE_COMPLETEDATE, iRoute.getCompleteDate());

    // updating row
    return database.update(TABLE_ROUTES, values, ROUTE_RID + " = ?",
            new String[] { String.valueOf(iRoute.getSRNo()) });
}

//Deleting single route
public void deleteRoute(RouteData iRoute) {
	SQLiteDatabase database = dbHelper.getWritableDatabase();
    //SQLiteDatabase db = this.getWritableDatabase();
    database.delete(TABLE_ROUTES, ROUTE_RID + " = ?",
            new String[] { String.valueOf(convertToRID(iRoute.getSRNo())) });
    database.close();
}

//Deleting single route
public void deleteRoute(String iRouteID) {
	SQLiteDatabase database = dbHelper.getWritableDatabase();
  //SQLiteDatabase db = this.getWritableDatabase();
  database.delete(TABLE_ROUTES, ROUTE_RID + " = ?",
          new String[] { String.valueOf(convertToRID(iRouteID)) });
  database.close();
}

public void deleteAllRoute() {
	SQLiteDatabase database = dbHelper.getWritableDatabase();
	//String countQuery = "DELETE FROM " + TABLE_ROUTES;
    // database.execSQL(countQuery, null);
    database.delete(TABLE_ROUTES,null,null);
	
    database.close();
}

public void deleteAllPic() {
	SQLiteDatabase database = dbHelper.getWritableDatabase();
	//String countQuery = "DELETE FROM " + TABLE_PICS;
    //database.execSQL(countQuery, null);
    database.delete(TABLE_PICS,null,null);
    database.close();
}

//Getting routes Count
public int getRoutesCount() {
	SQLiteDatabase database = dbHelper.getReadableDatabase();
	
    String countQuery = "SELECT  * FROM " + TABLE_ROUTES;
    //SQLiteDatabase db = this.getReadableDatabase();
    Cursor cursor = database.rawQuery(countQuery, null);
    int lCount = cursor.getCount();
    cursor.close();

    // return count
    return lCount;
	}

//
//All CRUD(Create, Read, Update, Delete) Operations
//
//
//Adding new pic
void addPic(PictureData iPic) {
SQLiteDatabase database = dbHelper.getWritableDatabase();

 ContentValues values = new ContentValues();
 //values.put(PIC_ID, Integer.parseInt(iPic.getID())); // Route Status;
 //values.put(PIC_ID, null);
 values.put(PIC_RID, convertToRID(iPic.getSRNo())); // Route SR No
 values.put(PIC_FILENAME, iPic.getFileName()); // Route SR No
 values.put(PIC_FILEPATH, iPic.getFilePath()); // Route SR No
 values.put(PIC_FILEDESC, iPic.getFileDesc()); // Route SR No
 values.put(PIC_LONG, iPic.getLong()); // Route SR No
 values.put(PIC_LAT, iPic.getLat()); // Route SR No
 
 // Inserting Row
 database.insert(TABLE_PICS, null, values);
 
 database.close(); // Closing database connection
}

//Deleting single picture
public void deletePic(PictureData iPic) {
  SQLiteDatabase database = dbHelper.getWritableDatabase();
  
  database.delete(TABLE_PICS, PIC_RID + " = ?",
          new String[] { iPic.getSRNo() });
  
  database.close();
}

//Deleting single picture
public void deletePic(String iSRNo) {
SQLiteDatabase database = dbHelper.getWritableDatabase();

database.delete(TABLE_PICS, PIC_RID + " = ?",
        new String[] { ""+ convertToRID(iSRNo) });

database.close();
}

//Getting All Routes
public List<PictureData> getAllPics() {
	
  SQLiteDatabase database = dbHelper.getWritableDatabase();
  
  List<PictureData> contactList = new ArrayList<PictureData>();
  // Select All Query
  String selectQuery = "SELECT  * FROM " + TABLE_PICS;

  //SQLiteDatabase db = this.getWritableDatabase();
  Cursor cursor = database.rawQuery(selectQuery, null);

  // looping through all rows and adding to list
  if (cursor.moveToFirst()) {
      do {
      	PictureData picture = new PictureData();
      	 picture.setID(cursor.getString(0)); 
      	 picture.setSRNo(cursor.getString(1));
      	 picture.setFileName(cursor.getString(2));
      	 picture.setFilePath(cursor.getString(3));
      	 picture.setFileDesc(cursor.getString(4));
      	 picture.setLong(Float.parseFloat(cursor.getString(5)));
      	 picture.setLat(Float.parseFloat(cursor.getString(6)));
      	  
          //contact.setCompleteDate(cursor.getString(2));
          // Adding contact to list
          // Adding contact to list
          contactList.add(picture);
      } while (cursor.moveToNext());
  }

  // return contact list
  return contactList;
}

//Getting single route
public List<PictureData> getPicturesWithID(String iRid) {
	SQLiteDatabase database = dbHelper.getReadableDatabase();
	 
	List<PictureData> picList = new ArrayList<PictureData>();
	
  Cursor cursor = database.query(TABLE_PICS, new String[] { PIC_RID,
  		PIC_FILENAME, PIC_FILEPATH, PIC_FILEDESC, PIC_LONG, PIC_LAT }, PIC_RID + "=?",
          new String[] { String.valueOf(convertToRID(iRid)) }, null, null, null, null);
  //if (cursor != null)
  //    cursor.moveToFirst();
  int index = 0;
  if (cursor.moveToFirst()) {
	  do {
		  PictureData pic = new PictureData(String.valueOf(index),
		  cursor.getString(0), cursor.getString(1), 
		  cursor.getString(2), cursor.getString(3), 
          Float.parseFloat(cursor.getString(4)),Float.parseFloat(cursor.getString(5)));
  
   		  picList.add(pic);
   		  index ++;
	  } while (cursor.moveToNext());
  }
  // return contact
  return picList;
}

	//Adding new ticket
void addTicket(String iSRNo) {
	SQLiteDatabase database = dbHelper.getWritableDatabase();
	
	ContentValues values = new ContentValues();
	//values.put(PIC_ID, Integer.parseInt(iPic.getID())); // Route Status;
	//values.put(PIC_ID, null);
	values.put(TICKET_RID, convertToRID(iSRNo)); // Route SR No
	values.put(TICKET_STATUS, "1");
	
	// Inserting Row
	database.insert(TABLE_TICKETS, null, values);
	
	database.close(); // Closing database connection
}
	
		
	//Deleting single picture
	public void deleteTicket(String iSRNo) {
	SQLiteDatabase database = dbHelper.getWritableDatabase();

	database.delete(TABLE_TICKETS, TICKET_RID + " = ?",
	        new String[] { ""+ convertToRID(iSRNo) });

	database.close();
	}
	
	public void deletePartialFromTicket() {
		SQLiteDatabase database = dbHelper.getWritableDatabase();

		String selectQuery = "SELECT * FROM " + TABLE_TICKETS + " ORDER BY TICKET_RID LIMIT 900";

		Cursor cursor = database.rawQuery(selectQuery, null);
		
		// looping through these rows and delete them
		if (cursor.moveToFirst()) {
		      do {
		      	//delete that ticket
		      	database.delete(TABLE_TICKETS, TICKET_RID + " = ?",
				        new String[] { ""+ cursor.getString(1) });
		      } while (cursor.moveToNext());
		}
		
		database.close();
		}
	
	//Getting routes Count
	public int getTicketsCount() {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		
	    String countQuery = "SELECT  * FROM " + TABLE_TICKETS;
	    int lCount;
	    //SQLiteDatabase db = this.getReadableDatabase();
	    Cursor cursor = database.rawQuery(countQuery, null);
	    lCount = cursor.getCount();
	    cursor.close();
	    database.close();
	    // return count
	    return lCount;
	}
	
	public int checkTicketExist(String iSRNo) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		
		Cursor cursor = database.query(TABLE_TICKETS, new String[] { TICKET_RID,
		  		TICKET_STATUS }, TICKET_RID + "=?",
		          new String[] { String.valueOf(convertToRID(iSRNo)) }, null, null, null, null);
		
		int lCount = cursor.getCount();
		
	    cursor.close();
	    database.close();

	    // return count
	    return lCount;
		
	}
}
/*public long createRecords(String id, String name){  
	   ContentValues values = new ContentValues();  
	   values.put(EMP_ID, id);  
	   values.put(EMP_NAME, name);  
	   return database.insert(EMP_TABLE, null, values);  
	}    

	     public selectRecords() {
	       String[] cols = new String[] {EMP_ID, EMP_NAME};  
	       Cursor mCursor = database.query(true, EMP_TABLE,cols,null  
	            , null, null, null, null, null);  
	       if (mCursor != null) {  
	        mCursor.moveToFirst();  


	     }  
	     return mCursor; // iterate to get each value.
	}
	     
}*/