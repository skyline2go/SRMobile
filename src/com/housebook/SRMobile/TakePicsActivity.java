package com.housebook.SRMobile;

import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.Menu;
import android.view.View;
import java.io.File;
import java.io.IOException;
import java.util.List;

import com.example.atest.AlbumStorageDirFactory;
import android.util.Log;

import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class TakePicsActivity extends Activity {
	
	private static final int ACTION_TAKE_PHOTO_B = 1;
	private static final int ACTION_TAKE_PHOTO_S = 2;
	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String PNG_FILE_SUFFIX = ".png";
	
	private String mCurrentPhotoPath;
	private String mCurrentPhotoName;
	private int mCurrentPhotoIndex;
	
	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;

	//private ArrayList mPictureProp = new ArrayList();
	
	/* Photo album for this application */
	private String getAlbumName() {
		return getString(R.string.album_name);
	}
	
	Spinner spinner;
    String crNumStr;
    
  	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	    StrictMode.setThreadPolicy(policy);
		
		//setContentView(R.layout.selectroute);	
		setContentView(R.layout.activity_main);	
		//setContentView(R.layout.takephoto);		
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
		} else {
			mAlbumStorageDirFactory = new BaseAlbumDirFactory();
		}

	}	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			
			storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());

			if (storageDir != null) {
				if (! storageDir.mkdirs()) {
					if (! storageDir.exists()){
						Log.d("CameraSample", "failed to create directory");
						return null;
					}
				}
			}
			
		} else {
			Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
		}
		
		return storageDir;
	}
	
	private File createImageFile() throws IOException {
		// Create an image file name
		//String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		//String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_"+ mCurrentPhotoIndex + "_";
		String imageFileName = JPEG_FILE_PREFIX + mCurrentPhotoIndex + "_";
		
		File albumF = getAlbumDir();
		//File albumF = this.getFilesDir();
		File imageF = File.createTempFile(imageFileName, PNG_FILE_SUFFIX, albumF);
		return imageF;
	}

	private File setUpPhotoFile() throws IOException {
		
		File f = createImageFile();
		mCurrentPhotoPath = f.getAbsolutePath();
		mCurrentPhotoName = f.getName();
		return f;
	}
	
	private void dispatchTakePictureIntent(int actionCode) {

		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		switch(actionCode) {
		case ACTION_TAKE_PHOTO_B:
			File f = null;
			
			try {
				f = setUpPhotoFile();
				mCurrentPhotoPath = f.getAbsolutePath();
				//http://stackoverflow.com/questions/18228365/how-to-capture-low-resolution-picture-using-android-camera
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
			} catch (IOException e) {
				e.printStackTrace();
				f = null;
				mCurrentPhotoPath = null;
			}
			break;
		case ACTION_TAKE_PHOTO_S:
			File f1 = null;
			
			try {
				f1 = setUpPhotoFile();
				mCurrentPhotoPath = f1.getAbsolutePath();
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f1));
			} catch (IOException e) {
				e.printStackTrace();
				f1 = null;
				mCurrentPhotoPath = null;
			}
			break;
		default:
			break;			
		} // switch

		startActivityForResult(takePictureIntent, actionCode);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ACTION_TAKE_PHOTO_B: {
			if (resultCode == RESULT_OK) {
				handleBigCameraPhoto();
				//Upload_file();
			}
			break;
		} // ACTION_TAKE_PHOTO_B
	
		} // switch
	}
	
	private void handleBigCameraPhoto() {

		if (mCurrentPhotoPath != null) {
			
			getPicProp(mCurrentPhotoPath);
			//getGeoLocation(mCurrentPhotoPath);
			//setPic();
			//galleryAddPic();
			//Upload_file();
			//mCurrentPhotoPath = null;
		}

	}
	
	/*private void getGeoLocation(String iPath){
		float[] lOutput = {0.0f,0.0f};
		
		try {
			ExifInterface lExif = new ExifInterface(iPath);
			lExif.getLatLong(lOutput);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
	
	private void getPicProp(String iPath){
		
		PictureProperty lPicProp = new PictureProperty();
		
		lPicProp.setFileName(mCurrentPhotoName);
		lPicProp.setFilePath(iPath);
		lPicProp.setFileDesc("Picture" + mCurrentPhotoIndex);
		float[] lOutput = {0.0f,0.0f};
		
		try {
			ExifInterface lExif = new ExifInterface(iPath);
			lExif.getLatLong(lOutput);
			lPicProp.setLong(lOutput[0]);
			lPicProp.setLat(lOutput[1]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SinglePicArrayData lSinglePicArray = SinglePicArrayData.getInstance();
		//To prevent the outofboundary error.
		if (lSinglePicArray.mPictureProp.size() > mCurrentPhotoIndex - 1) {
			lSinglePicArray.mPictureProp.set(mCurrentPhotoIndex - 1, lPicProp);
		}
	}
	/**
	 * Indicates whether the specified action can be used as an intent. This
	 * method queries the package manager for installed packages that can
	 * respond to an intent with the specified action. If no suitable package is
	 * found, this method returns false.
	 * http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html
	 *
	 * @param context The application's environment.
	 * @param action The Intent action to check for availability.
	 *
	 * @return True if an Intent with the specified action can be sent and
	 *         responded to, false otherwise.
	 */
	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list =
			packageManager.queryIntentActivities(intent,
					PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}
	
	public void Take_Pic1(View view) {
		mCurrentPhotoIndex = 1;
		dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
		//Upload_file();
	}
	
	public void Take_Pic2(View view) {
		mCurrentPhotoIndex = 2;
		dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
		//Upload_file();
	}
	
	public void Take_Pic3(View view) {
		mCurrentPhotoIndex = 3;
		dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
		//Upload_file();
	}
	
	public void Take_Pic4(View view) {
		mCurrentPhotoIndex = 4;
		dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
		//Upload_file();
	}
	
	public void Take_Pic5(View view) {
		mCurrentPhotoIndex = 5;
		dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
		//Upload_file();
	}
	
	public void Take_Pic6(View view) {
		mCurrentPhotoIndex = 6;
		dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
		//Upload_file();
	}

	public void gotoCloseout(View view){
		setContentView(R.layout.closeout);	
		Spinner spStatus = (Spinner) findViewById(R.id.spStatus);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapterSP = ArrayAdapter.createFromResource(this,
		        R.array.collStatus, android.R.layout.simple_spinner_dropdown_item);
		// Specify the layout to use when the list of choices appears
		adapterSP.setDropDownViewResource(android.R.layout.simple_spinner_item);
		// Apply the adapter to the spinner
		spStatus.setAdapter(adapterSP);
		
		Spinner spNotes = (Spinner) findViewById(R.id.spNotes);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> notesSP = ArrayAdapter.createFromResource(this,
		        R.array.collNotes, android.R.layout.simple_spinner_dropdown_item);   //spinnersmallfont);
		
		// Specify the layout to use when the list of choices appears
		notesSP.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spNotes.setAdapter(notesSP);
		
		//set SRNum based on user clicks on prev screen
		EditText et = (EditText)findViewById(R.id.txtCRNo);
		et.setText(crNumStr);
	}
	

	public void gotoPhotoPage(View view){
		//setContentView(R.layout.takephoto);	
		setContentView(R.layout.activity_main);
	}
	
	public void returnToResultDetail(View view){
		//gotoRouteResult(view);
		//setContentView(R.layout.routeresult);
		//finish();
		super.onBackPressed();
	}
	
}
