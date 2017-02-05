package test.example.atest;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.example.atest.BulkMobileDB;
import com.example.atest.R;
import com.example.atest.RouteData;

class testImageEncode {
	public static void main(String[] args)
	{
		System.out.println("hello world");
		
		//BulkMobileDB db = new BulkMobileDB(getApplicationContext());
		
	}
}
/*
public class testImageEncode {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
         
        BulkMobileDB db = new BulkMobileDB(this);
         
        
        // Inserting Contacts
        Log.d("Insert: ", "Inserting .."); 
        db.addRoute(new RouteData("111", "9100000000","2014/1/1"));        
        //db.addContact(new Contact("Srinivas", "9199999999"));
        //db.addContact(new Contact("Tommy", "9522222222"));
        //db.addContact(new Contact("Karthik", "9533333333"));
         
        // Reading all contacts
        Log.d("Reading: ", "Reading all routes.."); 
        List<RouteData> contacts = db.getAllRoutes();       
         
        for (RouteData cn : contacts) {
            String log = "Id: "+cn.getSRNo()+" ,Name: " + cn.getStatus() + " ,Phone: " + cn.getCompleteDate();
                // Writing Contacts to log
        Log.d("Name: ", log);
    }
    }
}*/