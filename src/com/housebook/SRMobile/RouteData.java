package com.housebook.SRMobile;

//import java.util.ArrayList;

public class RouteData {	
	public  String mSRNo;
	public  String mStatus;
	public  String mCompleteDate;
	public  String mCompleteBy;
	public  String mCloseDate;
	public  String mNote;
	//public static ArrayList mPictureProp;
	//private static SinglePicArrayData instance = null;
	
	public RouteData() {
	      // Exists only to defeat instantiation.
		 //mPictureProp = new ArrayList();
	}
	
	// constructor
    public RouteData(String iSRNo, String iStatus, String iCompleteDate){
        this.mSRNo = iSRNo;
        this.mStatus = iStatus;
        this.mCompleteDate = iCompleteDate;
    }
    
 // constructor
    public RouteData(String iSRNo, String iStatus, String iCompleteDate, String iCompleteBy, String iCloseDate, String iNote){
        this.mSRNo = iSRNo;
        this.mStatus = iStatus;
        this.mCompleteDate = iCompleteDate;
        this.mCompleteBy = iCompleteBy;
        this.mCloseDate = iCloseDate;
        this.mNote = iNote;
    }
	
	// getting ID
    public String getSRNo(){
        return this.mSRNo;
    }
     
    // setting id
    public void setSRNo(String iSRNo){
        this.mSRNo = iSRNo;
    }
     
    // getting status
    public String getStatus(){
        return this.mStatus;
    }
     
    // setting status
    public void setStatus(String iStatus){
        this.mStatus = iStatus;
    }
     
    // getting complete date
    public String getCompleteDate(){
        return this.mCompleteDate;
    }
     
    // setting complete date
    public void setCompleteDate(String iCompleteDate){
        this.mCompleteDate = iCompleteDate;
    }
  
    // getting complete by
    public String getCompleteBy(){
        return this.mCompleteBy;
    }
     
    // setting complete by
    public void setCompleteBy(String iCompleteBy){
        this.mCompleteBy = iCompleteBy;
    }
    
    // getting close date
    public String getCloseDate(){
        return this.mCloseDate;
    }
     
    // setting close date
    public void setCloseDate(String iCloseDate){
        this.mCloseDate = iCloseDate;
    }
    
 // getting close date
    public String getNote(){
        return this.mNote;
    }
     
    // setting close date
    public void setNote(String iNote){
        this.mNote = iNote;
    }
   
}
