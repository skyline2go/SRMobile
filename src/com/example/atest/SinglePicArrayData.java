package com.example.atest;

import java.util.ArrayList;

public class SinglePicArrayData {
	public static ArrayList mPictureProp;
	public  String mSRNo;
	public  String mStatus;
	public  String mCompleteDate;
	public  String mCompleteBy;
	public  String mCloseDate;
	public  String mNote;
	//private static HashMap<Integer,String> mResultMap;
	private static SinglePicArrayData instance = null;
	
	protected SinglePicArrayData() {
	      // Exists only to defeat instantiation.
	}
	
   public static SinglePicArrayData getInstance() {
      if(instance == null) {
         instance = new SinglePicArrayData();
         mPictureProp = new ArrayList();
         //mResultMap = new ArrayList();
      }
      return instance;
   }
   
}
