package com.example.atest;

public class PictureData extends PictureProperty {
	
	private String mID;
	private String mSRNo;
	
	public PictureData() {
	      // Exists only to defeat instantiation.
		 //mPictureProp = new ArrayList();
	}
	
	// constructor
    public PictureData(String iSRNo, String iID){
        this.mSRNo = iSRNo;
        this.mID = iID;
    }
	
    public PictureData(String iID, String iSRNo, String iFileName, String iFilePath, String iFileDesc, float iLong, float iLat){
    	this.mID = iID;
    	this.mSRNo = iSRNo;
        this.mfileName = iFileName;
        this.mfilePath = iFilePath;
        this.mfileDesc = iFileDesc;
        this.mLong = iLong;
        this.mLat = iLat;
        //this.mCompleteDate = iCompleteDate;
    }
    
	// getting PicID
    public String getID(){
        return this.mID;
    }
     
    // setting PicID
    public void setID(String iID){
        this.mSRNo = iID;
    }
    
 // getting ID
    public String getSRNo(){
        return this.mSRNo;
    }
     
    // setting ID
    public void setSRNo(String iSRNo){
        this.mSRNo = iSRNo;
    }
	
}
