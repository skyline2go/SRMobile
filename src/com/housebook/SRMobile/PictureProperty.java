package com.housebook.SRMobile;

public class PictureProperty {
	protected String mfileName;
	protected String mfilePath;
	protected String mfileDesc;
	protected float mLong;
	protected float mLat;
	
	public void setFileName(String ifileName) {
		mfileName = ifileName;
	}
	
	public String getFileName() {
		return mfileName;
	}
	
	public void setFilePath(String ifilePath) {
		mfilePath = ifilePath;
	}
	
	public String getFilePath() {
		return mfilePath;
	}
	
	public void setFileDesc(String ifileDesc) {
		mfileDesc = ifileDesc;
	}
	
	public String getFileDesc() {
		return mfileDesc;
	}
	
	public void setLong(float iLong) {
		mLong = iLong;
	}
	
	public float getLong() {
		return mLong;
	}
	
	public void setLat(float iLat) {
		mLat = iLat;
	}
	
	public float getLat() {
		return mLat;
	}
}
