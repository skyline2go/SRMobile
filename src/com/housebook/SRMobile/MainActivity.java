package com.housebook.SRMobile;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.housebook.SRMobile.PictureProperty;
import com.housebook.SRMobile.SinglePicArrayData;

import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	//private static final String JPEG_FILE_PREFIX = "IMG_";
	//private static final String PNG_FILE_SUFFIX = ".png";
	
	//private int mCurrentPhotoIndex;
	private ProgressDialog mDialog;
	//dl 09-06-2014 comment the following line ??
	private ProgressDialog mLoadAllRouteDialog;
	//dl 12-12-2014 loaded message
	private ProgressDialog mLoadedMessage;
	private ProgressDialog mCloseAllRouteDialog;
	private ProgressDialog mCloseLaterDialog;
	private String mSaveLoadRoute;
	//private Intent mServiceIntent;
	
	/*private AlbumStorageDirFactory mAlbumStorageDirFactory = null;

	//private ArrayList mPictureProp = new ArrayList();
	
	// Photo album for this application 
	private String getAlbumName() {
		return getString(R.string.album_name);
	}*/
	
    private String restRouteURL = "http://maps2.dcgis.dc.gov/dcgis/rest/services/DCGIS_APPS/SR_30days_Open/MapServer/0/query?"; 
    //private String restRouteWherePart1 = "where=Route%3D'";  //+ selRt +  
    //private String restRouteWherePart2 = "'&text=&objectIds=&time=&geometry=&geometryType=esriGeometryEnvelope&inSR=&spatialRel=esriSpatialRelIntersects&relationParam=&outFields=Route%2C+Address%2C+Comments%2C+Sequence%2CSERVNO&returnGeometry=true&maxAllowableOffset=&geometryPrecision=&outSR=&returnIdsOnly=false&returnCountOnly=false&orderByFields=&groupByFieldsForStatistics=&outStatistics=&returnZ=false&returnM=false&gdbVersion=&f=pjson";
    private String restRouteWherePart1 = "where=ORGANIZATIONACRONYM+%3D+%27DPW%27+AND+SERVICEORDERSTATUS%3D+%27OPEN%27+AND+rownum%3C10+AND+SERVICECODEDESCRIPTION%3D+%27";//Street Cleaning%27";  //+ selRt +  
    private String restRouteWherePart2 = "%27&text=&objectIds=&time=&geometry=&geometryType=esriGeometryEnvelope&inSR=&spatialRel=esriSpatialRelIntersects&relationParam=&outFields=*&returnGeometry=true&returnTrueCurves=false&maxAllowableOffset=&geometryPrecision=&outSR=&returnIdsOnly=false&returnCountOnly=false&orderByFields=&groupByFieldsForStatistics=&outStatistics=&returnZ=false&returnM=false&gdbVersion=&returnDistinctValues=false&resultOffset=&resultRecordCount=&f=pjson";
    Spinner spinner;
    int m_SelectPosition;
    //String crNumStr;
    String srNumStr;
    Map<String, String> DetailsMap;
    String detailStr;
    
    ///private static String URL = "http://leaf.dpw.dc.gov/CloseSR.asmx";
    private static String URL = "http://trackster.dpw.dc.gov/CloseSR.asmx";
    private static String NAMESPACE = "http://Trakster.com/";
    private static String METHOD_NAME1 = "CloseTicket";
    private static String SOAP_ACTION1 = "http://Trakster.com/CloseTicket";
    
    private String mResult = null;
  //dl 09/19/2014 comment the following function as we don't network signal
    //private int mSignalDbm;
    private static HashMap<String,String> mResultMap;
    //dl 12-14-2014
    private static HashMap<String,Boolean> mLoadStatusMap;
    //This flag is used to know whether the routing information is in Memory or not.
    private static boolean mRoutesInMemory = false;
    private static HashMap<String,Boolean> mRouteStatusMap;
    
    private static String[] mTypeArray;

    private BulkMobileDB mdb;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	    //StrictMode.setThreadPolicy(policy);
		
		setContentView(R.layout.selectroute);	
		//setContentView(R.layout.activity_main);	
		//setContentView(R.layout.takephoto);	
		mTypeArray = getResources().getStringArray(R.array.SRTypes);
		
		spinner = (Spinner) findViewById(R.id.routes);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.SRTypes, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
		
		//mServiceIntent = new Intent(this,UploadOfflineService.class);
		//startService(mServiceIntent);
		//create_table();
		//test_ImageEncode();
		
		//dl 09/19/2014 comment the following function as we don't network signal
		//MyPhoneStateListener myListener   = new MyPhoneStateListener();
	    //TelephonyManager telManager  = ( TelephonyManager )getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
	    //telManager.listen(myListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
	    
	    //Read the result into memory
	    //HashMap<Integer,String> mResultMap = new HashMap<Integer,String>();
	    /*mResultMap = new HashMap<Integer,String>();
	    for (int i = 1; i < 17; i++) 
	    {
	    	String result = queryRESTurl(restRouteURL + restRouteWherePart1 + i + restRouteWherePart2 );
	    	mResultMap.put(new Integer(i),result);
	    }
	    */
	    //Initialization of RouteStatus;
   	    mRouteStatusMap = new HashMap<String,Boolean>();
   	    
	    //Based on Yanli's idea, show a dialog 06/20/2014
	    /*mDialog = ProgressDialog.show(MainActivity.this, "", "Starting. Please wait...", true);
	    final Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
            	mDialog.dismiss(); // when the task active then close the dialog
                t.cancel(); // also just top the timer thread, otherwise, you may receive a crash report
            }
        }, 2000);*/
		
   	    // dl 10-18-2014 if this is the first time loading, load the routes; Otherwise, skip the following step 
   	    //if (( savedInstanceState == null ) || (mRoutesInMemory == false)) {
   	 //dl 02-01-2015 when rotate, don't load the data one more time.. 
   	 //1)http://stackoverflow.com/questions/456211/activity-restart-on-rotation-android
   	 //2)I also changed the manifest file:android:configChanges="keyboardHidden|orientation|screenSize" based on the following link:
   	 //http://www.coboltforge.com/2012/08/tech-stuff-prevent-android-app-from-restarting-after-sliding-out-the-hardware-keyboard/
   	 if ( savedInstanceState == null ){  
	   	 if (mRoutesInMemory == false) {
	   		    /*String show= "";
	   		    if (savedInstanceState == null) {
	   		    	show = "null";
	   		    } else {
	   		    	show = String.valueOf(savedInstanceState.getBoolean(mSaveLoadRoute));
	   		    }*/
		   	    //mLoadAllRouteDialog = ProgressDialog.show(MainActivity.this, "", "Loading routes. Please wait... " + String.valueOf(mRoutesInMemory) + " " + show, true);
		   	    mLoadAllRouteDialog = ProgressDialog.show(MainActivity.this, "", "Loading routes. Please wait...", true); 
		   	    
				LoadAllRouteTask task = new LoadAllRouteTask();
				task.execute();
		   	    
				//mDialog = ProgressDialog.show(MainActivity.this, "", "Starting. Please wait...", true);
			    final Timer t = new Timer();
		        t.schedule(new TimerTask() {
		            public void run() {
		            	if (mLoadAllRouteDialog != null) {
		            		mLoadAllRouteDialog.dismiss();
		            	}
		            	//mDialog.dismiss(); // when the task active then close the dialog
		                //t.cancel(); // also just stop the timer thread, otherwise, you may receive a crash report
		            }
		        }, 60000);
		        
		        //If the ticket have too much data, clean the tickets.
		        mdb = new BulkMobileDB(this); 
		        //int lTicketCount1 = db.getRoutesCount();
		        if (mdb != null) { 
			        int lTicketCount = mdb.getTicketsCount();
			        if (lTicketCount > 1000) {
			        	//Remove
			        	mdb.deletePartialFromTicket();
			        }
		        }
	   	    }
   	    }
	}
	
	// dl 10-18-2014 Save the load status
	@Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putBoolean(mSaveLoadRoute, mRoutesInMemory);
    }
	
	public void test_ImageEncode() {
		
		/*FileInputStream fileInputStream = new FileInputStream(lPicProp.getFilePath());
		int bytesAvailable = fileInputStream.available();
		
		Bitmap lbm = BitmapFactory.decodeFile(lPicProp.getFilePath());
		 Bitmap lNewbm = lbm.createScaledBitmap(lbm, 640, 480, true);
		 ByteArrayOutputStream bos = new ByteArrayOutputStream();
		 lNewbm.compress(Bitmap.CompressFormat.PNG, 0, bos);
		 
		 //byte[] bitmapdata = bos.toByteArray();
		 byte[] buffer = bos.toByteArray();
		 int bytesRead = buffer.length;
		 int bufferSize = Math.min(bytesRead, maxBufferSize);
		//write the bytes in file
		//FileOutputStream fos = new FileOutputStream(f);
		//fos.write(bitmapdata);*/
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		//stopService(mServiceIntent);
	}

	public String queryRESTurl(String url) throws Exception {
		
		String responseString = null;		
	    
		try{
			HttpClient httpclient = new DefaultHttpClient();
			HttpUriRequest hRequest = new HttpGet(url);
		    HttpResponse response = httpclient.execute(hRequest);
		    StatusLine statusLine = response.getStatusLine();
		    if(statusLine.getStatusCode() == HttpStatus.SC_OK){
		        ByteArrayOutputStream out = new ByteArrayOutputStream();
		        response.getEntity().writeTo(out);
		        responseString = out.toString();
		        out.close();
		        
		        //..more logic
		    } else{
		        //Closes the connection.
		        response.getEntity().getContent().close();
		        throw new IOException(statusLine.getReasonPhrase());
		    }
		}catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            throw e;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            throw e;
        }
		return responseString;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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
	
	
	public void Write_file_to_Output_Encode(DataOutputStream iOutputStream, int iIndex) {
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary =  "BbC04y";
		
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 2*1024*1024;
		String lTemp = null;
		InputStream fileInputStream = null;
	
		try {
		SinglePicArrayData lSinglePicArray = SinglePicArrayData.getInstance();
		PictureProperty lPicProp = (PictureProperty)lSinglePicArray.mPictureProp.get(iIndex);
		
		if (lPicProp == null )
			return;
		fileInputStream = new FileInputStream(lPicProp.getFilePath());
		
		lTemp = twoHyphens + boundary + lineEnd;
		iOutputStream.writeBytes(lTemp);
		lTemp = "Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + lSinglePicArray.mSRNo + "_" + (iIndex + 1) +"\"" + lineEnd;
		iOutputStream.writeBytes(lTemp);
		//The following two lines are important
		lTemp = lineEnd;
		iOutputStream.write(lTemp.getBytes());
		//lTemp = "Content-Type: image/png" + lineEnd;
		//lTemp = "Content-Type: text/plain" + lineEnd;
		//outputStream.writeBytes(lTemp);
		//lTemp = "Content-Transfer-Encoding: binary";
		//outputStream.writeBytes(lTemp);

		bytesAvailable = fileInputStream.available();
		//if (bytesAvailable > maxBufferSize) {
		if (bytesAvailable > 200*1024) {
			/*File f = File(context.getCacheDir(), filename);
			f.createNewFile();*/
			 Bitmap lbm = BitmapFactory.decodeFile(lPicProp.getFilePath());
			 //Based on Yanli suggestion, change the size from 480*320 to 240*160. July 16,2014
			 //2.2 HQVGA (240x160) ref:http://en.wikipedia.org/wiki/Graphics_display_resolution
			 //Bitmap lNewbm = lbm.createScaledBitmap(lbm, 480, 320, true);
			 //dl 12-14-2014 Based on customer requirement, increase the image size to 640*480.
			 //Bitmap lNewbm = lbm.createScaledBitmap(lbm, 240, 160, true);
			 //dl 01-31-2015 Based on cutomer requirement, change the resolution to mid size 480*320.
			 Bitmap lNewbm = lbm.createScaledBitmap(lbm, 480, 320, true);
			 
			 ByteArrayOutputStream bos = new ByteArrayOutputStream();
			 lNewbm.compress(Bitmap.CompressFormat.PNG, 0, bos);
			 
			 //byte[] bitmapdata = bos.toByteArray();
			 buffer = bos.toByteArray();
			 bytesRead = buffer.length;
			 bufferSize = Math.min(bytesRead, maxBufferSize);
			 
			 iOutputStream.write(buffer, 0, bufferSize);
			//write the bytes in file
			//FileOutputStream fos = new FileOutputStream(f);
			//fos.write(bitmapdata);
		}
		else {
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];
	
			// Read file
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			
			while (bytesRead > 0)
			{
				iOutputStream.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}
		
		}	

		lTemp = lineEnd;
		iOutputStream.writeBytes(lTemp);
		
		fileInputStream.close();
		}
		catch (Exception ex)
		{
		//Exception handling
		}		
	}
	
	public void Upload_file_final() {
		HttpURLConnection connection = null;
		DataOutputStream outputStream = null;
				
		//String pathToOurFile = "";
		String urlServer = "http://leaf.dpw.dc.gov/deployment/ServiceHandler/ImageUploader.ashx";
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		//String boundary =  "*****";
		String boundary = "BbC04y";
		String lTemp = null;
		
		//int bytesRead, bytesAvailable, bufferSize;
		//byte[] buffer;
		//int maxBufferSize = 2*1024*1024;
		//pathToOurFile = this.getFilesDir() + "/test.png";
		try
		{
		
			URL url = new URL(urlServer);
			connection = (HttpURLConnection) url.openConnection();
	
			// Allow Inputs & Outputs
			connection.setDoInput(true);
			connection.setDoOutput(true);
			//improve the performance
			connection.setChunkedStreamingMode(0);
			connection.setUseCaches(false);
	
			// Enable POST method
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
			
			try {
			//BufferedOutputStream can't be used. Otherwise the images can't be uploaded.
			//outputStream = new DataOutputStream( new BufferedOutputStream(connection.getOutputStream() ));
			outputStream = new DataOutputStream( connection.getOutputStream() );
			
			SinglePicArrayData lSinglePicArray = SinglePicArrayData.getInstance();
			int lActualSize = lSinglePicArray.mPictureProp.size();
			
			for (int i =0; i< lActualSize;i++)
			{
				//Remember to check whether it is null or not.
				if (lSinglePicArray.mPictureProp.get(i) != null) {
					Write_file_to_Output_Encode(outputStream, i);
				}
				//Write_file_to_Output(outputStream, 0);
			}
							 
			lTemp = twoHyphens + boundary + twoHyphens + lineEnd;
			outputStream.writeBytes(lTemp);
	
			// Responses from the server (code and message)
			int serverResponseCode = connection.getResponseCode();
			String serverResponseMessage = connection.getResponseMessage();
	
			//fileInputStream.close();
		}finally {
		        try {
		        	//no need to flush.
		        	//outputStream.flush();
		        	if (outputStream != null)
		        		outputStream.close();
		        }
		        catch (IOException e) { 
		        	/* what can be done here anyway? */ 
		        	e.printStackTrace();
		        }
		    }
			
		}
		catch (Exception ex)
		{
		//Exception handling
		}
	}
/*
	public void gotoCloseout(View view){
		setContentView(R.layout.srdetail);	
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
		
		EditText etComment = (EditText)findViewById(R.id.txtComment);
		etComment.setText(commentStr);
		
		//Clean the picture information
		SinglePicArrayData lSinglePicArray = SinglePicArrayData.getInstance();
		lSinglePicArray.mPictureProp.clear();
		//Initialization
		//dl 09-13-2014 Do I really need to do this?
		for (int i =0; i <3; i++) {
			lSinglePicArray.mPictureProp.add(i, null);
		}
		//dl 09/19/2014 comment the following function as we don't network signal
		//EditText dbmEt = (EditText)findViewById(R.id.txtDbm);
		//dbmEt.setText(Integer.toString(mSignalDbm));
	}
	*/
	public void gotoDetail(View view){
		setContentView(R.layout.srdetail);	
		Spinner spStatus = (Spinner) findViewById(R.id.spStatus);
		// Create an ArrayAdapter using the string array and a default spinner layout
		/*ArrayAdapter<CharSequence> adapterSP = ArrayAdapter.createFromResource(this,
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
		spNotes.setAdapter(notesSP);*/
		
		//set SRNum based on user clicks on prev screen
		EditText et = (EditText)findViewById(R.id.txtSRNo);
		et.setText(srNumStr);
		
		String[] datas = detailStr.split("_");
		
		EditText etSRType = (EditText)findViewById(R.id.txtSRType);
		etSRType.setText(datas[1]);

		EditText etAddress = (EditText)findViewById(R.id.txtAddress);
		etAddress.setText(datas[2]+ " ,Washington DC," + datas[3]);
		
	}

	public void gotoPhotoPage(View view){
		//setContentView(R.layout.takephoto);	
		//setContentView(R.layout.activity_main);
		//Clean the picture information
		//SinglePicArrayData lSinglePicArray = SinglePicArrayData.getInstance();
		//lSinglePicArray.mPictureProp.clear();
		
		Intent nextScreen = new Intent(getApplicationContext(), TakePicsActivity.class);
		startActivity(nextScreen);		 
	}
	
	public String queryRESTurlInMemory(String iIndex) {
		String lResult;
		lResult = mResultMap.get(iIndex);
		
		//Return null is ok as that case is handled..
		/*if (lResult ==null) {
			return "";
		}*/
		return lResult;
	}
	/*
	 * comment the load all routes button.*/
	//2014-12-13 un-comment the following lines
	public void loadAllRoute(View view){
        // If routes are not in memory, load them
		if  (mRoutesInMemory == false || ! allLoadSuccess() ) {
			mLoadAllRouteDialog = ProgressDialog.show(MainActivity.this, "", "Loading Requests. Please wait...", true);
			
			LoadAllRouteTask task = new LoadAllRouteTask();
			task.execute();
		} else {
			mLoadedMessage = ProgressDialog.show(MainActivity.this, "", "Routes already loaded.", true);
			final Timer t = new Timer();
	        t.schedule(new TimerTask() {
	            public void run() {
	            	mLoadedMessage.dismiss(); // when the task active then close the dialog
	                t.cancel(); // also just top the timer thread, otherwise, you may receive a crash report
	            }
	        }, 2000);
		}
	}
	
	public boolean allLoadSuccess() {
		boolean result = true;
		for (int i =1; i< 17; i++) {
			if (mLoadStatusMap.get(Integer.valueOf(i)) == false) {
				result = false;
				break;
			}
		}
		return result;
	}
	
	public void CloseAllTickets(View view){

		//mCloseAllRouteDialog = ProgressDialog.show(MainActivity.this, "", "Closing. Please wait...", true);
		
		//CloseAllRouteTask task = new CloseAllRouteTask();
		//task.execute();
		
		Log.d("Reading: ", "Reading all routes.."); 
 		List<RouteData> routes = null;
 		if (mdb != null) {
 			routes = mdb.getAllRoutes();
 		}
 	    for (RouteData route : routes) {
 	    	OfflineUploadTaskNew task = new OfflineUploadTaskNew();
 			task.execute(route);
 	    }
		
	}
	
	public void gotoRouteResult(View view){
		//setContentView(R.layout.routeresult);

		Object val = spinner.getSelectedItem();
		HashMap<Integer,String> dataMap = new HashMap<Integer,String>();
				
		if (val != null){
			//String result = queryRESTurl(restRouteURL + restRouteWherePart1 + val.toString() + restRouteWherePart2 );
			String result;
			//If the routes data are in memory, query them from memory;
			//Otherwise, call the REST service to get the data;
			//dl 12-14-2014 Add more accurate control over the 
			if (mRoutesInMemory == true && mLoadStatusMap.get(val.toString()) == true ) {
				result = queryRESTurlInMemory(val.toString());
			}
			else {
				try {
					result = queryRESTurl(restRouteURL + restRouteWherePart1 + val.toString().replaceAll(" ", "%20") + restRouteWherePart2 );
				} catch (Exception e) {
					//If error happened, return directly
					result = null;
					e.printStackTrace();
					return;
				}
			}
			
			if (result ==null) 
				return;
			
			try{
				JSONObject rstJSON = new JSONObject(result);

				JSONArray features = rstJSON.getJSONArray("features");

				for(int i = 0; i < features.length(); i++){
			        JSONObject c = features.getJSONObject(i);
			        JSONObject b = c.getJSONObject("attributes");
			        dataMap.put(Integer.parseInt(b.getString("OBJECTID")), 
			        		b.getString("SERVICEREQUESTID") + '_' 
			        		+ b.getString("SERVICECODEDESCRIPTION") + '_' 
			        		+ b.getString("STREETADDRESS")+ "_" 
			        		+ b.getString("ZIPCODE") + "_"
			        		+ b.getString("STATUS_CODE") + "_"
			        		+ b.getString("WARD") + "_"
			        		+ b.getString("SERVICEDUEDATE")
			        		);
				}
				
			}catch (JSONException e) {
		        System.out.print("Error parsing JSON data " + e.toString());
		    }
		}
		
		TableLayout table = (TableLayout) findViewById(R.id.tableLayout2);
		//Remove all content in the table.
		table.removeAllViews();
	    table.setStretchAllColumns(true);  
	    table.setShrinkAllColumns(true); 
	    TableRow newRow;
	    TextView cellText1;
	    TextView cellText2;
	    TextView cellText3;
	    TextView cellText4;
	    TextView cellText5;
	    TextView cellNum;
	    Boolean searchResult;
	    
	    TableRow rowHeader = new TableRow(this);  
	    rowHeader.setGravity(Gravity.CENTER);  
	    TextView numHeaderTxt = new TextView(this);  
	    numHeaderTxt.setText("SR_Number");  
	    rowHeader.addView(numHeaderTxt);
	    
	    TextView srHeaderTxt = new TextView(this);
	    srHeaderTxt.setText("SR_Type");
	    rowHeader.addView(srHeaderTxt);
	    
	    TextView addHeaderTxt = new TextView(this);
	    addHeaderTxt.setText("Address");
	    rowHeader.addView(addHeaderTxt);
	    
	    TextView statusHeaderTxt = new TextView(this);
	    statusHeaderTxt.setText("Status");
	    rowHeader.addView(statusHeaderTxt);
	    
	    TextView wardHeaderTxt = new TextView(this);
	    wardHeaderTxt.setText("Ward");
	    rowHeader.addView(wardHeaderTxt);
	    
	    TextView dueHeaderTxt = new TextView(this);
	    dueHeaderTxt.setText("Due");
	    rowHeader.addView(dueHeaderTxt);
	    
	    table.addView(rowHeader);

	    //Sort these two maps
	    Map<Integer, String> DataTreeMap = new TreeMap<Integer, String>(dataMap);
	    
	    Set s = DataTreeMap.entrySet();
	    Iterator it = s.iterator();
	    //dl added 05/01/2015
	    DetailsMap = new HashMap<String, String>();
	    while ( it.hasNext() ) {
	    	Map.Entry entry = (Map.Entry) it.next();
	        Integer sequence = (Integer) entry.getKey();
	        String data = (String) entry.getValue();
	    	
	        String[] datas = data.split("_");
	    	newRow = new TableRow(this); 
	    	cellNum = new TextView(this);
	    	cellNum.setGravity(Gravity.CENTER_HORIZONTAL); 
	    	//cellNum.setText(String.valueOf(i+1));
	    	cellNum.setText(datas[0]);
	    	cellNum.setWidth(50);
	    	
	    	cellNum.setTextColor(Color.BLUE);
	    	
	    	//Search whether the route has been closed or not.
	    	if (mRouteStatusMap !=null) {
		    	searchResult = mRouteStatusMap.get(datas[0]);
		    	
		    	if (searchResult != null) {
		    		if (searchResult.equals(Boolean.TRUE)){
		    			cellNum.setTextColor(Color.RED);
		    		}
		    	} else {
		    		//Check the db whether that ticket is closed or not.
		    		BulkMobileDB db = new BulkMobileDB(this);
		    		int lcount = db.checkTicketExist(datas[0]);
		    		if (lcount == 1) {
		    			cellNum.setTextColor(Color.RED);
		    		}
		    	}
	    	}
	    		
	    	cellNum.setOnClickListener(new View.OnClickListener() {
	    	    public void onClick(View v) {
	    	    	TextView tv = (TextView)v;
	    	    	srNumStr = tv.getText().toString();
	    	    	//Save the selected value
	    	    	m_SelectPosition = spinner.getSelectedItemPosition();
	    	    	detailStr = DetailsMap.get(srNumStr);
	    	    	gotoDetail(v);
	    	    }
	    	});
	    	
	    	cellNum.setMovementMethod(LinkMovementMethod.getInstance());
	    	
	    	newRow.addView(cellNum);
	    	
	    	cellText1 = new TextView(this);
	    	cellText1.setGravity(Gravity.CENTER_HORIZONTAL); 
	    	cellText1.setText(Html.fromHtml( datas[1] ));
	    	cellText1.setWidth(60);

	    	newRow.addView(cellText1);
	    	
	    	cellText2 = new TextView(this);
	    	cellText2.setWidth(120);
	    	cellText2.setText(datas[2]+ " ,Washington DC," + datas[3] );
	    	newRow.addView(cellText2);  
	    	
	    	cellText3 = new TextView(this);
	    	cellText3.setWidth(40);
	    	cellText3.setText(datas[4] );
	    	newRow.addView(cellText3);  
	    	
	    	cellText4 = new TextView(this);
	    	cellText4.setWidth(30);
	    	cellText4.setText(datas[5]);
	    	newRow.addView(cellText4);  
	    	
	    	cellText5 = new TextView(this);
	    	
	    	Calendar duedate = Calendar.getInstance();
	    	duedate.setTimeInMillis(Long.parseLong(datas[6]));
	    	SimpleDateFormat format = 
	                new SimpleDateFormat("MM/dd/yyyy");
	    	String finalDue = format.format(duedate.getTime());
	    	
	    	cellText5.setWidth(60);
	    	cellText5.setText(finalDue);
	    	newRow.addView(cellText5);  
	    	DetailsMap.put(datas[0], data);
	    	table.addView(newRow); 
	    }
	}
	
	private String prepareXMLString() {
		String lStr = "<Document><Prc_instance>";
		PictureProperty lPictureProp;
		
		SinglePicArrayData lSinglePicArray = SinglePicArrayData.getInstance();
		
		lStr = lStr + "<SRNo>";
		//Todo
		/*lStr = lStr + "13-00129634";
		lStr = lStr + "</SRNo><Status>CLSD</Status>";
		lStr = lStr + "<DateComplete>";
		lStr = lStr + "02/22/2013";
		lStr = lStr + "</DateComplete>";
		lStr = lStr + "<CompletedBy>";
		lStr = lStr + "John Dove";
		lStr = lStr + "</CompletedBy>";
		lStr = lStr + "<DateClose>";
		lStr = lStr + "02/22/2013";
		lStr = lStr + "</DateClose>";
		
		lStr = lStr + "<Note>";
		lStr = lStr + "Ticket Close Information";
		lStr = lStr + "</Note>";*/
		
		lStr = lStr + lSinglePicArray.mSRNo;
		lStr = lStr + "</SRNo>";
		lStr = lStr + "<Status>";
		lStr = lStr + lSinglePicArray.mStatus;	//	"CLSD</Status>";
		lStr = lStr + "</Status>";
		lStr = lStr + "<DateComplete>";
		lStr = lStr + lSinglePicArray.mCompleteDate;
		lStr = lStr + "</DateComplete>";
		lStr = lStr + "<CompletedBy>";
		if (lSinglePicArray.mCompleteBy.length() == 0) {
			lStr = lStr + "John Dove";
		} else {
			lStr = lStr + lSinglePicArray.mCompleteBy;
		}
		lStr = lStr + "</CompletedBy>";
		lStr = lStr + "<DateClose>";
		lStr = lStr + lSinglePicArray.mCloseDate;
		lStr = lStr + "</DateClose>";
		
		lStr = lStr + "<Note>";
		lStr = lStr + lSinglePicArray.mNote;
		lStr = lStr + "</Note>";
		
		int lActualSize = lSinglePicArray.mPictureProp.size();
		
		//int lActualSize = 6;
		/*for (int i= 0; i < lActualSize; i++)
		{
			//lPictureProp = (PictureProperty)lSinglePicArray.mPictureProp.get(i);
			lStr = lStr + "<FileName" + (i+1) + ">";
			//lStr = lStr + lPictureProp.getFileName();
			lStr = lStr + "csr" + lSinglePicArray.mSRNo+ "_"+ (i+1) + ".png";
			lStr = lStr + "</FileName" + (i+1) + ">";
			
			lStr = lStr + "<ImageDesc" + (i+1) + ">";
			lStr = lStr + "file" + (i+1);
			lStr = lStr + "</ImageDesc" + (i+1) + ">";
			
			lStr = lStr + "<Image_Xcoord" + (i+1) + ">";
			lStr = lStr + "0.0";
			lStr = lStr + "</Image_Xcoord" + (i+1) + ">";
			
			lStr = lStr + "<Image_Ycoord" + (i+1) + ">";
			lStr = lStr + "0.0";
			lStr = lStr + "</Image_Ycoord" + (i+1) + ">";
		}*/
		for (int i= 0; i < lActualSize; i++)
		{
			lPictureProp = (PictureProperty)lSinglePicArray.mPictureProp.get(i);
			if (lPictureProp != null) {
				lStr = lStr + "<FileName" + (i+1) + ">";
				//lStr = lStr + lPictureProp.getFileName();
				lStr = lStr + lSinglePicArray.mSRNo+ "_"+ (i+1) + ".png";
				lStr = lStr + "</FileName" + (i+1) + ">";
				
				lStr = lStr + "<ImageDesc" + (i+1) + ">";
				lStr = lStr + lPictureProp.getFileDesc();
				lStr = lStr + "</ImageDesc" + (i+1) + ">";
				
				lStr = lStr + "<Image_Xcoord" + (i+1) + ">";
				lStr = lStr + lPictureProp.getLong();
				lStr = lStr + "</Image_Xcoord" + (i+1) + ">";
				
				lStr = lStr + "<Image_Ycoord" + (i+1) + ">";
				lStr = lStr + lPictureProp.getLat();
				lStr = lStr + "</Image_Ycoord" + (i+1) + ">";
			} else
			{
				lStr = lStr + "<FileName" + (i+1) + ">";
				lStr = lStr + "Null";
				lStr = lStr + "</FileName" + (i+1) + ">";
				
				lStr = lStr + "<ImageDesc" + (i+1) + ">";
				lStr = lStr + "none";
				lStr = lStr + "</ImageDesc" + (i+1) + ">";
				
				lStr = lStr + "<Image_Xcoord" + (i+1) + ">";
				lStr = lStr + "0";
				lStr = lStr + "</Image_Xcoord" + (i+1) + ">";
				
				lStr = lStr + "<Image_Ycoord" + (i+1) + ">";
				lStr = lStr + "0";
				lStr = lStr + "</Image_Ycoord" + (i+1) + ">";
			}
		}
		//Fill the missing information with default value;
		if (lActualSize < 6) {
			for (int i= lActualSize; i < 6; i++)
			{
				//lPictureProp = (PictureProperty)lSinglePicArray.mPictureProp.get(i);
				lStr = lStr + "<FileName" + (i+1) + ">";
				lStr = lStr + "Null";
				lStr = lStr + "</FileName" + (i+1) + ">";
				
				lStr = lStr + "<ImageDesc" + (i+1) + ">";
				lStr = lStr + "none";
				lStr = lStr + "</ImageDesc" + (i+1) + ">";
				
				lStr = lStr + "<Image_Xcoord" + (i+1) + ">";
				lStr = lStr + "0";
				lStr = lStr + "</Image_Xcoord" + (i+1) + ">";
				
				lStr = lStr + "<Image_Ycoord" + (i+1) + ">";
				lStr = lStr + "0";
				lStr = lStr + "</Image_Ycoord" + (i+1) + ">";
			}
		}
		
		lStr = lStr + "</Prc_instance></Document>";
		
		return lStr;
	}
	
	private String prepareXMLString_DB() {
		String lStr = "<Document><Prc_instance>";
		PictureProperty lPictureProp;
		
		//SinglePicArrayData lSinglePicArray = SinglePicArrayData.getInstance();
		
		BulkMobileDB db = new BulkMobileDB(this);
		Log.d("Reading: ", "Reading all routes.."); 
	    List<RouteData> routes = db.getAllRoutes();
	    RouteData lRoute;// = routes[0];
	    for (RouteData route : routes) {
            //String log = "Id: "+cn.getSRNo()+" ,Name: " + cn.getStatus() + " ,Phone: " + cn.getCompleteDate();
            String lSRNo = route.getSRNo();
            //lRoute = route;
	    	
	    	lStr = lStr + "<SRNo>";
			
			lStr = lStr + route.getSRNo();
			lStr = lStr + "</SRNo>";
			lStr = lStr + "<Status>";
			lStr = lStr + route.getStatus();	//	"CLSD</Status>";
			lStr = lStr + "</Status>";
			lStr = lStr + "<DateComplete>";
			lStr = lStr + route.getCompleteDate();
			lStr = lStr + "</DateComplete>";
			lStr = lStr + "<CompletedBy>";
			if (route.getCompleteBy().length() == 0) {
				lStr = lStr + "John Dove";
			} else {
				lStr = lStr + route.getCompleteBy();
			}
			lStr = lStr + "</CompletedBy>";
			lStr = lStr + "<DateClose>";
			lStr = lStr + route.getCloseDate();
			lStr = lStr + "</DateClose>";
			
			lStr = lStr + "<Note>";
			lStr = lStr + route.getNote();
			lStr = lStr + "</Note>";
	    	
            List<PictureData> pics = db.getPicturesWithID(lSRNo);
            int lCount = 0;
            for (PictureData pic : pics) {
            	//Write_file_to_Output_with_Picture(outputStream, i, pic, lSRNo);
            	//lPictureProp = (PictureProperty)lSinglePicArray.mPictureProp.get(i);
    			lStr = lStr + "<FileName" + (lCount+1) + ">";
    			//lStr = lStr + lPictureProp.getFileName();
    			lStr = lStr + lSRNo+ "_"+ (lCount+1) + ".png";
    			lStr = lStr + "</FileName" + (lCount+1) + ">";
    			
    			lStr = lStr + "<ImageDesc" + (lCount+1) + ">";
    			lStr = lStr + pic.getFileDesc();
    			lStr = lStr + "</ImageDesc" + (lCount+1) + ">";
    			
    			lStr = lStr + "<Image_Xcoord" + (lCount+1) + ">";
    			lStr = lStr + pic.getLong();
    			lStr = lStr + "</Image_Xcoord" + (lCount+1) + ">";
    			
    			lStr = lStr + "<Image_Ycoord" + (lCount+1) + ">";
    			lStr = lStr + pic.getLat();
    			lStr = lStr + "</Image_Ycoord" + (lCount+1) + ">";
            	
            	lCount ++;
            }
            
            if (lCount < 6) {
    			for (int i= lCount; i < 6; i++)
    			{
    				//lPictureProp = (PictureProperty)lSinglePicArray.mPictureProp.get(i);
    				lStr = lStr + "<FileName" + (i+1) + ">";
    				lStr = lStr + "Null";
    				lStr = lStr + "</FileName" + (i+1) + ">";
    				
    				lStr = lStr + "<ImageDesc" + (i+1) + ">";
    				lStr = lStr + "none";
    				lStr = lStr + "</ImageDesc" + (i+1) + ">";
    				
    				lStr = lStr + "<Image_Xcoord" + (i+1) + ">";
    				lStr = lStr + "0";
    				lStr = lStr + "</Image_Xcoord" + (i+1) + ">";
    				
    				lStr = lStr + "<Image_Ycoord" + (i+1) + ">";
    				lStr = lStr + "0";
    				lStr = lStr + "</Image_Ycoord" + (i+1) + ">";
    			}
    		}
	    	// Writing Contacts to log
            //db.deleteRoute(cn);
            break;
        //Log.d("Name: ", log);
        }
		
	    lStr = lStr + "</Prc_instance></Document>";
		
		return lStr;
		
	}
	
	public void Collect_Data(View v)
    {
		String lTemp = null;
		SinglePicArrayData lSinglePicArray = SinglePicArrayData.getInstance();
		lSinglePicArray.mSRNo = "";
		EditText lSRNoEditText = (EditText) findViewById(R.id.txtCRNo);
		lSinglePicArray.mSRNo = lSRNoEditText.getText().toString();
		
		Spinner lStatusSpinner = (Spinner) findViewById(R.id.spStatus);
		lSinglePicArray.mStatus = lStatusSpinner.getSelectedItem().toString();
		
		DatePicker lCompleteDate = (DatePicker) findViewById(R.id.datePicker1);
		
		lTemp = Integer.toString(lCompleteDate.getYear()) + "-";
		
		if ((lCompleteDate.getMonth() + 1) < 10) {
			lTemp = lTemp + "0"; 
		} else {
			lTemp = lTemp + "";
		}
		
		lTemp = lTemp + Integer.toString(lCompleteDate.getMonth() + 1);
		
		if (lCompleteDate.getDayOfMonth() < 10) {
			lTemp = lTemp + "-0"; 
		} else {
			lTemp = lTemp + "-"; 
		}
		
		lTemp = lTemp + Integer.toString(lCompleteDate.getDayOfMonth());
		//lTemp = lTemp +
		//		Integer.toString(lCompleteDate.getDayOfMonth()) + "/" +
		//		Integer.toString(lCompleteDate.getYear());
		
		SimpleDateFormat lDateFormat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss SSS"); 
		lDateFormat.setTimeZone(TimeZone.getTimeZone("EST5EDT"));
		String lTime = lDateFormat.format(new Date());
		String lDateTime[];
		lDateTime = lTime.split(" ");
		
		lSinglePicArray.mCompleteDate = lTemp + " " + lDateTime[1];
		
		EditText lCompleteByEditText = (EditText) findViewById(R.id.editText1);
		lSinglePicArray.mCompleteBy = lCompleteByEditText.getText().toString();
				
		DatePicker lCloseDate = (DatePicker) findViewById(R.id.datePicker2);
		
		lTemp = Integer.toString(lCloseDate.getYear()) + "-";
		
		if ((lCloseDate.getMonth() + 1) < 10) {
			lTemp = lTemp + "0"; 
		} else {
			lTemp = lTemp + "";
		};
		
		lTemp = lTemp + Integer.toString(lCloseDate.getMonth() + 1);
		
		if (lCloseDate.getDayOfMonth() < 10) {
			lTemp = lTemp + "-0"; 
		} else {
			lTemp = lTemp + "-"; 
		}
		
		lTemp = lTemp + Integer.toString(lCloseDate.getDayOfMonth());
		//lTemp = lTemp +
		//		Integer.toString(lCloseDate.getDayOfMonth()) + "/" +
		//		Integer.toString(lCloseDate.getYear());
		
		
		lSinglePicArray.mCloseDate = lTemp + " " + lDateTime[1];
		
		//lSinglePicArray.mCloseDate = "0" + Integer.toString(lCloseDate.getMonth() + 1) + "/0" + 
		//		Integer.toString(lCloseDate.getDayOfMonth()) + "/" +
		//		Integer.toString(lCloseDate.getYear());
		
		Spinner lNoteSpinner = (Spinner) findViewById(R.id.spNotes);
		lSinglePicArray.mNote = lNoteSpinner.getSelectedItem().toString();
		
		//dl Added on Aug 31, 2014
		// if the notes don't have "Close SR", that means the status should be open.
		if (lSinglePicArray.mNote.indexOf("Close SR") < 0 )
		{
			//dl changed on apr 28, 2015
			lSinglePicArray.mStatus = "OPN";
		}
    }
	
	
	
	public void showResultMessage() {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
	    alertDialog.setTitle("CloseTicket Result");
	    alertDialog.setMessage(mResult);
	    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
	       public void onClick(DialogInterface dialog, int which) {
	          // TODO Add your code for the button here.
	       }
	    });
	    // Set the Icon for the Dialog
	    //alertDialog.setIcon(R.drawable.icon);
	    alertDialog.show();
	}
	
	public void showCloseLaterMessage() {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
	    alertDialog.setTitle("INFO");
	    alertDialog.setMessage("Cannot close the ticket now. Please close it later.");
	    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
		       public void onClick(DialogInterface dialog, int which) {
		          // TODO Add your code for the button here.
		       }
		    });
	    
	    alertDialog.show();
	}
	
	private class UploadTask extends AsyncTask<Void, Void, Void> {
	     protected Void doInBackground(Void... args) {
	         //refresh the view
	         //taskLiteApplication.setData();
	    	 //Upload_file(0);
	    	 Upload_file_final();
	         return null;
	     }

	     protected void onPostExecute(Void results) {
	         //change view
	         //Intent myIntent = new Intent(MainActivity.this, MainActivity.class);
	         //MainActivity.this.startActivity(myIntent);

	         //kill the dialog waiting
	         mDialog.dismiss();
	         mDialog = null;
	         
	     }
	 }
	//dl 09-06-2014 comment the following task.
	private class LoadAllRouteTask extends AsyncTask<Void, Void, Void> {
	     protected Void doInBackground(Void... args) {
	         //refresh the view
	         //taskLiteApplication.setData();
	    	 //Upload_file(0);
	    	 //Upload_file_final();
	    	 //set RoutesInMemory to false;
	    	 mRoutesInMemory = false;
	    	 mResultMap = new HashMap<String,String>();
	    	 mLoadStatusMap = new HashMap<String,Boolean>();
	    	 //Initialization of RouteStatus;
	    	 //mRouteStatusMap = new HashMap<String,Boolean>();
	    	 
	    	 int len = mTypeArray.length;
		 	 //for (int i = 3; i < len; i++) 
	    	 //For now, for performance reason, just select one type
	    	 for (int i = 3; i < 4; i++) 
		 	 {
		 	    	String result;
		 	    	try {
		 	    		result = queryRESTurl(restRouteURL + restRouteWherePart1 + mTypeArray[i].replaceAll(" ", "%20") + restRouteWherePart2 );
		 	    	} catch (Exception e) {
		 	    		result = null;
		 	    		e.printStackTrace();
		 	    		mResultMap.put(mTypeArray[i], result);
		 	    		mLoadStatusMap.put(mTypeArray[i], false);
		 	    	}
		 	    	mResultMap.put(mTypeArray[i], result);
		 	    	mLoadStatusMap.put(mTypeArray[i], true);
		 	 }
	         return null;
	     }

	     protected void onPostExecute(Void results) {

	         //kill the dialog waiting
	         mLoadAllRouteDialog.dismiss();
	         mLoadAllRouteDialog = null;
	         //Set RouteInMemory to true;
	         mRoutesInMemory = true;
	     }
	 }
	
	//dl 09-06-2014 add the following task
	private class CloseAllRouteTask extends AsyncTask<Void, Void, Void> {
	     protected Void doInBackground(Void... args) {
	    	
	    	
	    	//BulkMobileDB db = new BulkMobileDB(this);
	 		Log.d("Reading: ", "Reading all routes.."); 
	 		List<RouteData> routes = null;
	 		if (mdb != null) {
	 			routes = mdb.getAllRoutes();
	 		}
	 	    for (RouteData route : routes) {
	 	    	
	 	    	
	 	    	//Close Tickets
	 	    	Close_Ticket1(route); 
	 	    }
	    	 
	        return null;
	     }

	     protected void onPostExecute(Void results) {

	         //kill the dialog waiting
	         mCloseAllRouteDialog.dismiss();
	         mCloseAllRouteDialog = null;
	        
	     }
	 }
	
	private boolean check_ActualSize() {
		boolean lResult = false;
		
		SinglePicArrayData lSinglePicArray = SinglePicArrayData.getInstance();
		int lActualSize = lSinglePicArray.mPictureProp.size();
		
		for (int i =0; i< lActualSize;i++)
		{
			//Remember to check whether it is null or not.
			if (lSinglePicArray.mPictureProp.get(i) != null) {
				lResult = true;
				break;
			}
			//Write_file_to_Output(outputStream, 0);
		}
		return lResult;
	}
	
	public void Close_Ticket(View v)
    {	

	//Collect Data first 
	Collect_Data(v);
	//Upload image first
	//09/17/2014 yanli don't want to use signal to decide when to save the ticket comment the following line.
	//if (isDataSignalStrong(getApplicationContext())) {
	//if (isDataConnected(getApplicationContext())) {
		//Save_Data();
		
		//09/17 comment the following, use check_ActualSize() to decide whether to upload or not
		//SinglePicArrayData lSinglePicArray = SinglePicArrayData.getInstance();
		//int lActualSize = lSinglePicArray.mPictureProp.size();
		
		//If there is any picture, upload first. Otherwise, close ticket directly. 07/6/2014
		//if (lActualSize > 0) {
		if (check_ActualSize()) {
			mDialog = ProgressDialog.show(MainActivity.this, "", "Uploading. Please wait...", true);
			/*final Timer t = new Timer();
	        t.schedule(new TimerTask() {
	            public void run() {
	            	if (mDialog){
	            		mDialog.dismiss(); // when the task active then close the dialog
	            	}
	                t.cancel(); // also just top the timer thread, otherwise, you may receive a crash report
	            }
	        }, 10000);*/
			
			UploadTask task = new UploadTask();
			task.execute();
		}
		
		//Close the ticket
		Close_Ticket1(false);
	/*}
	//dl 09-13-2014 Save the data if the network signal is very weak.
	else {
		//Save the data
		RouteData lRoute = Save_Data();
	}*/
	//No network connection store the data
	//dl 09-06-2014 comment the following part
	/*else {
		RouteData lRoute = Save_Data();
		mDialog = ProgressDialog.show(MainActivity.this, "", "Uploading Offline. Will try every 1 minute", true);*/
		/*try {
			Thread.sleep(2 * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mDialog.dismiss();
        mDialog = null;*/
	/*	
		final Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
            	mDialog.dismiss(); // when the task active then close the dialog
                t.cancel(); // also just top the timer thread, otherwise, you may receive a crash report
            }
        }, 2000);
		
		OfflineUploadTask task = new OfflineUploadTask();
		task.execute(lRoute);
		//Close the ticket
		//Close_Ticket1(lRoute);
	 }*/
    }
	
	void Clean_Data(RouteData iRoute) {
		BulkMobileDB db = new BulkMobileDB(this);
		 
		String lSRNo = iRoute.getSRNo();
        db.deleteRoute(lSRNo);
        db.deletePic(lSRNo);
	}
	
	//dl 09-13-2014 add to test save data
	public void Save_Ticket(View v)
    {	
		//Collect the data
		Collect_Data(v);
		//Save the data
		RouteData lRoute = Save_Data();
    }
	
	private int getRecordSizeByRoute(RouteData iRoute) {
		String lSRNo = iRoute.getSRNo();
		BulkMobileDB db = new BulkMobileDB(this);
        List<PictureData> pics = db.getPicturesWithID(lSRNo);
        
        if (pics == null)
        	return 0;
        else 
        	return pics.size();
	}
	
	/*private class OfflineUploadTask extends AsyncTask<RouteData, Void, Void> {
	     protected Void doInBackground(RouteData... iRoute) {
	         //refresh the view
	         //taskLiteApplication.setData();
	    	 //Upload_file(0);
	    	 while(!isDataSignalStrong(getApplicationContext())) {
	    	 //while(!isDataConnected(getApplicationContext())) {
	    		 try {
					Thread.sleep(5 * 1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	 };
	    	 boolean lUploadStatus;
	    	 //Check the record size 
	    	 //If the record size is 0, no need to upload. 07/20/2014
	    	 int lRecordSize = getRecordSizeByRoute(iRoute[0]);
	    	 
	    	 if (lRecordSize >0) {
	    		 lUploadStatus = Upload_file_offline(iRoute[0]);
	    	 } else {
	    		 lUploadStatus = false;
	    	 }	 
	    	 
	    	 Close_Ticket1(iRoute[0]);
	    	 
	    	 if (lUploadStatus) {
	    		Clean_Data(iRoute[0]);
	    	 }
	         return null;
	     }

	     protected void onPostExecute(Void results) {
	         //change view
	         //Intent myIntent = new Intent(MainActivity.this, MainActivity.class);
	         //MainActivity.this.startActivity(myIntent);

	         //kill the dialog waiting
	         //mDialog.dismiss();
	         //mDialog = null;
	         //show the result message here if there is any
	         if (mResult.length() > 0)
	        	 showResultMessage();	
	         mResult ="";
	     }
	 }*/
	
	private class OfflineUploadTaskNew extends AsyncTask<RouteData, Void, Void> {
	     protected Void doInBackground(RouteData... iRoute) {
	         
	    	 boolean lUploadStatus;
	    	 //Check the record size 
	    	 //If the record size is 0, no need to upload. 07/20/2014
	    	 int lRecordSize = getRecordSizeByRoute(iRoute[0]);
	    	 
	    	 if (lRecordSize >0) {
	    		 lUploadStatus = Upload_file_offline(iRoute[0]);
	    	 } else {
	    		 lUploadStatus = false;
	    	 }	 
	    	 
	    	 Close_Ticket1(iRoute[0]);
	    	 
	    	 /*if (lUploadStatus) {
	    		Clean_Data(iRoute[0]);
	    	 }*/
	         return null;
	     }

	     protected void onPostExecute(Void results) {
	         if (mResult.length() > 0)
	        	 showResultMessage();	
	         mResult ="";
	     }
	 }
	
	public boolean Upload_file_offline(RouteData iRoute) {
		HttpURLConnection connection = null;
		DataOutputStream outputStream = null;
				
		//String pathToOurFile = "";
		String urlServer = "http://leaf.dpw.dc.gov/deployment/ServiceHandler/ImageUploader.ashx";
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		//String boundary =  "*****";
		String boundary = "BbC04y";
		String lTemp = null;
		boolean lUploadSucceed = true;
		
		//int bytesRead, bytesAvailable, bufferSize;
		//byte[] buffer;
		//int maxBufferSize = 2*1024*1024;
		//pathToOurFile = this.getFilesDir() + "/test.png";
		try
		{
		
			URL url = new URL(urlServer);
			connection = (HttpURLConnection) url.openConnection();
	
			// Allow Inputs & Outputs
			connection.setDoInput(true);
			connection.setDoOutput(true);
			//improve the performance
			connection.setChunkedStreamingMode(0);
			connection.setUseCaches(false);
	
			// Enable POST method
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
			
			try {
			//BufferedOutputStream can't be used. Otherwise the images can't be uploaded.
			//outputStream = new DataOutputStream( new BufferedOutputStream(connection.getOutputStream() ));
			outputStream = new DataOutputStream( connection.getOutputStream() );
			
			BulkMobileDB db = new BulkMobileDB(this);
			Log.d("Reading: ", "Reading all routes.."); 
		    //List<RouteData> routes = db.getAllRoutes();
		    //for (RouteData route : routes) {
	            //String log = "Id: "+cn.getSRNo()+" ,Name: " + cn.getStatus() + " ,Phone: " + cn.getCompleteDate();
	            String lSRNo = iRoute.getSRNo();
	            
	            List<PictureData> pics = db.getPicturesWithID(lSRNo);
	            int i = 0;
	            
	            for (PictureData pic : pics) {
	            	if (!Write_file_to_Output_with_Picture_Encode(outputStream, i, pic, lSRNo))
	            	{
	            		lUploadSucceed = false;
	            	}
	            	i ++;
	            }
		    	// Delete the route after successfully upload
	            /*if (lUploadSucceed) {
	            	db.deleteRoute(lSRNo);
	            	db.deletePic(lSRNo);
	            	
	            	//List<RouteData> routes = db.getAllRoutes();
	        		//List<PictureData> pic1s = db.getAllPics();
	            }*/
	            //break;
	        //Log.d("Name: ", log);
	        //}
			//SinglePicArrayData lSinglePicArray = SinglePicArrayData.getInstance();
			//int lActualSize = lSinglePicArray.mPictureProp.size();
			
			//for (int i =0; i< lActualSize;i++)
			//{
				//Write_file_to_Output_New(outputStream,i);
			//}
							
			lTemp = twoHyphens + boundary + twoHyphens + lineEnd;
			outputStream.writeBytes(lTemp);
	
			// Responses from the server (code and message)
			int serverResponseCode = connection.getResponseCode();
			String serverResponseMessage = connection.getResponseMessage();
	
			//fileInputStream.close();
		}finally {
		        try {
		        	//no need to flush.
		        	//outputStream.flush();
		        	if (outputStream != null)
		        		outputStream.close();
		        }
		        catch (IOException e) { 
		        	/* what can be done here anyway? */ 
		        	e.printStackTrace();
		        }
		    }
			
		}
		catch (Exception ex)
		{
		//Exception handling
		}
		return lUploadSucceed;
	}
	
	/*public boolean Write_file_to_Output_with_Picture(DataOutputStream iOutputStream, int iIndex, PictureData iPic, String iSRNo) {
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary =  "BbC04y";
		
		boolean lResult = true;
		
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 2*1024*1024;
		String lTemp = null;
		InputStream fileInputStream = null;
	
		try {
		//SinglePicArrayData lSinglePicArray = SinglePicArrayData.getInstance();
		//PictureProperty lPicProp = (PictureProperty)lSinglePicArray.mPictureProp.get(iIndex);
		fileInputStream = new FileInputStream(iPic.getFilePath());
		
		lTemp = twoHyphens + boundary + lineEnd;
		iOutputStream.writeBytes(lTemp);
		lTemp = "Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + iSRNo + "_" + (iIndex + 1) +"\"" + lineEnd;
		iOutputStream.writeBytes(lTemp);
		//The following two lines are important
		lTemp = lineEnd;
		iOutputStream.write(lTemp.getBytes());
		//lTemp = "Content-Type: image/png" + lineEnd;
		//lTemp = "Content-Type: text/plain" + lineEnd;
		//outputStream.writeBytes(lTemp);
		//lTemp = "Content-Transfer-Encoding: binary";
		//outputStream.writeBytes(lTemp);

		bytesAvailable = fileInputStream.available();
		bufferSize = Math.min(bytesAvailable, maxBufferSize);
		buffer = new byte[bufferSize];

		// Read file
		bytesRead = fileInputStream.read(buffer, 0, bufferSize);
		while (bytesRead > 0)
		{
			iOutputStream.write(buffer, 0, bufferSize);
			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);
		}

		lTemp = lineEnd;
		iOutputStream.writeBytes(lTemp);
		
		fileInputStream.close();
		}
		catch (Exception ex)
		{
		//Exception handling
			lResult = false;
		}
		return lResult;
		
	}*/
	
	public boolean Write_file_to_Output_with_Picture_Encode(DataOutputStream iOutputStream, int iIndex, PictureData iPic, String iSRNo) {
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary =  "BbC04y";
		
		boolean lResult = true;
		
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 2*1024*1024;
		String lTemp = null;
		InputStream fileInputStream = null;
	
		try {
		//SinglePicArrayData lSinglePicArray = SinglePicArrayData.getInstance();
		//PictureProperty lPicProp = (PictureProperty)lSinglePicArray.mPictureProp.get(iIndex);
		fileInputStream = new FileInputStream(iPic.getFilePath());
		
		lTemp = twoHyphens + boundary + lineEnd;
		iOutputStream.writeBytes(lTemp);
		lTemp = "Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + iSRNo + "_" + (iIndex + 1) +"\"" + lineEnd;
		iOutputStream.writeBytes(lTemp);
		//The following two lines are important
		lTemp = lineEnd;
		iOutputStream.write(lTemp.getBytes());
		//lTemp = "Content-Type: image/png" + lineEnd;
		//lTemp = "Content-Type: text/plain" + lineEnd;
		//outputStream.writeBytes(lTemp);
		//lTemp = "Content-Transfer-Encoding: binary";
		//outputStream.writeBytes(lTemp);

		bytesAvailable = fileInputStream.available();
		
		if (bytesAvailable > 200*1024) {
			 Bitmap lbm = BitmapFactory.decodeFile(iPic.getFilePath());
			 Bitmap lNewbm = lbm.createScaledBitmap(lbm, 480, 320, true);
			 ByteArrayOutputStream bos = new ByteArrayOutputStream();
			 lNewbm.compress(Bitmap.CompressFormat.PNG, 0, bos);
			 
			 //byte[] bitmapdata = bos.toByteArray();
			 buffer = bos.toByteArray();
			 bytesRead = buffer.length;
			 bufferSize = Math.min(bytesRead, maxBufferSize);
			 
			 iOutputStream.write(buffer, 0, bufferSize);
			 
		} else {
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];
	
			// Read file
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			while (bytesRead > 0)
			{
				iOutputStream.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}
		}

		lTemp = lineEnd;
		iOutputStream.writeBytes(lTemp);
		
		fileInputStream.close();
		}
		catch (Exception ex)
		{
		//Exception handling
			lResult = false;
		}
		return lResult;
		
	}
	
	private RouteData Save_Data() {
		BulkMobileDB db = new BulkMobileDB(this);
        /**
         * CRUD Operations
         * */
        // Inserting Routes
        Log.d("Insert: ", "Inserting .."); 
        SinglePicArrayData lSinglePicArray = SinglePicArrayData.getInstance();
		int lActualSize = lSinglePicArray.mPictureProp.size();
		
		//EMPTY the table;
		//db.deleteAllRoute();
		//db.deleteAllPic();
		
		//check mSRNo 
		if (lSinglePicArray.mSRNo == null )
			return null;
		
		//dl show the dialog for 3 seconds
		showCloseLaterMessage();
		
		RouteData lRoute = new RouteData(lSinglePicArray.mSRNo, lSinglePicArray.mStatus,
			      lSinglePicArray.mCompleteDate, lSinglePicArray.mCompleteBy,
			      lSinglePicArray.mCloseDate, lSinglePicArray.mNote);
		db.addRoute(lRoute);
		
		for (int i =0; i< lActualSize;i++)
		{
			if (lSinglePicArray.mPictureProp.get(i) != null) {
				PictureProperty lPicProp = (PictureProperty)lSinglePicArray.mPictureProp.get(i);
				//lPicDrop maybe null
				//if (lPicProp != null) {
				db.addPic(
						new PictureData(Integer.toString(i), lSinglePicArray.mSRNo, 
								lPicProp.getFileName(),lPicProp.getFilePath(),
								lPicProp.getFileDesc(),lPicProp.getLong(),
								lPicProp.getLat()
								)
						);
				//}
			}
		}
		
		//List<RouteData> routes = db.getAllRoutes();
		//List<PictureData> pics = db.getAllPics();
        return lRoute;
	}
	
	private void Test_Save_Date() {
		BulkMobileDB db = new BulkMobileDB(this);
        
        /**
         * CRUD Operations
         * */
        // Inserting Routes
        Log.d("Insert: ", "Inserting .."); 
        SinglePicArrayData lSinglePicArray = SinglePicArrayData.getInstance();
		int lActualSize = lSinglePicArray.mPictureProp.size();
		
		db.addRoute(
				new RouteData(lSinglePicArray.mSRNo, lSinglePicArray.mStatus,
						      lSinglePicArray.mCompleteDate, lSinglePicArray.mCompleteBy,
						      lSinglePicArray.mCloseDate, lSinglePicArray.mNote)
		);
		
		for (int i =0; i< lActualSize;i++)
		{
			PictureProperty lPicProp = (PictureProperty)lSinglePicArray.mPictureProp.get(i);
			db.addPic(
					new PictureData(Integer.toString(i), lSinglePicArray.mSRNo, 
							lPicProp.getFileName(),lPicProp.getFilePath(),
							lPicProp.getFileDesc(),lPicProp.getLong(),
							lPicProp.getLat()
							)
					);
			
		}
		
		
        //db.addRoute(new RouteData("111", "9100000000","2014/1/1"));        
        //db.addContact(new Contact("Srinivas", "9199999999"));
        //db.addContact(new Contact("Tommy", "9522222222"));
        //db.addContact(new Contact("Karthik", "9533333333"));
         
        // Reading all contacts
        Log.d("Reading: ", "Reading all routes.."); 
        List<RouteData> routes = db.getAllRoutes();       
         
        for (RouteData cn : routes) {
            String log = "Id: "+cn.getSRNo()+" ,Name: " + cn.getStatus() + " ,Phone: " + cn.getCompleteDate();
                // Writing Contacts to log
            db.deleteRoute(cn);
        Log.d("Name: ", log);
        }
        
        routes = db.getAllRoutes(); 
        //db.addPic(new PictureData("111", "1"));   
        //db.addPic(new PictureData("111", "2"));
        //db.addPic(new PictureData("111", "3"));
        
        db.addPic(
				new PictureData("1", "13-00300483",
						"FileName","FilePath",
						"FileDesc",(float)0.0,
						(float)0.0
						)
				);
        
        List<PictureData> pics = db.getAllPics();       
        
        for (PictureData cn : pics) {
            String log = "Id: "+cn.getID()+ " ,SRNo: " + cn.getSRNo();
                // Writing Contacts to log
            db.deletePic(cn);
        Log.d("Name: ", log);
        }
        
        pics = db.getAllPics();
	}
	
	//public void Close_Ticket1(View v, boolean iOffline) {
	public void Close_Ticket1(boolean iOffline) {
    
	//Initialize soap request + add parameters
    
    SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME1);       

    //Use this to add parameters
    String lString = "<Document><Prc_instance><SRNo>13-00169462</SRNo><Status>CLSD</Status>" +
    		"<DateComplete>02/22/2013 1:45:30 PM</DateComplete><CompletedBy>John Dove</CompletedBy>" +
    		"<DateClose>02/22/2013 1:45:30 PM</DateClose><Note>Furniture, Front Collection</Note>" +
    		"<FileName1>12-00000848_xxxx1.png</FileName1><ImageDesc1> Bulk Collection Pic1</ImageDesc1>" +
    		"<Image_Xcoord1>401988.65</Image_Xcoord1><Image_Ycoord1>133315.80</Image_Ycoord1><FileName2>" +
    		"12-00000848_xxxx2.png</FileName2><ImageDesc2> Bulk Collection Pic2</ImageDesc2><Image_Xcoord2>" +
    		"401998.65</Image_Xcoord2><Image_Ycoord2>133325.80</Image_Ycoord2><FileName3>" +
    		"12-00000848_xxxx3.png</FileName3><ImageDesc3> Bulk Collection Pic3</ImageDesc3>" +
    		"<Image_Xcoord3>401978.65</Image_Xcoord3><Image_Ycoord3>133345.80</Image_Ycoord3>" +
    		"<FileName4>12-00000848_xxxx4.png</FileName4><ImageDesc4> Bulk Collection Pic4</ImageDesc4>" +
    		"<Image_Xcoord4>401968.65</Image_Xcoord4><Image_Ycoord4>133385.80</Image_Ycoord4>" +
    		"<FileName5>12-00000848_xxxx5.png</FileName5><ImageDesc5> Bulk Collection Pic5</ImageDesc5>" +
    		"<Image_Xcoord5>401938.65</Image_Xcoord5><Image_Ycoord5>133335.80</Image_Ycoord5>" +
    		"<FileName6>12-00000848_xxxx6.png</FileName6><ImageDesc6> Bulk Collection Pic6</ImageDesc6>" +
    		"<Image_Xcoord6>401908.65</Image_Xcoord6><Image_Ycoord6>133305.80</Image_Ycoord6>" +
    		"</Prc_instance></Document>";
    /*
    String lString1 = "<Document><Prc_instance><SRNo>13-00168178</SRNo><Status>OPN</Status>" +
    		"<DateComplete>02/22/2013</DateComplete><CompletedBy>John Dove</CompletedBy>" +
    		"<DateClose>02/22/2013</DateClose><Note>Furniture, Front Collection</Note>" +
    		"<FileName1>12-00000848_xxxx1.png</FileName1><ImageDesc1> Bulk Collection Pic1</ImageDesc1>" +
    		"<Image_Xcoord1>401988.65</Image_Xcoord1><Image_Ycoord1>133315.80</Image_Ycoord1>" + 
    		"<FileName2>Null</FileName2><ImageDesc2>none</ImageDesc2>" +
    		"<Image_Xcoord2>0</Image_Xcoord2><Image_Ycoord2>0</Image_Ycoord2>" +
    		"<FileName3>Null</FileName3><ImageDesc3>none</ImageDesc3>" +
    		"<Image_Xcoord3>0</Image_Xcoord3><Image_Ycoord3>0</Image_Ycoord3>" +
    		"<FileName4>Null</FileName4><ImageDesc4>none</ImageDesc4>" +
    		"<Image_Xcoord4>0</Image_Xcoord4><Image_Ycoord4>0</Image_Ycoord4>" +
    		"<FileName5>Null</FileName5><ImageDesc5>none</ImageDesc5>" +
    		"<Image_Xcoord5>0</Image_Xcoord5><Image_Ycoord5>0</Image_Ycoord5>" +
    		"<FileName6>Null</FileName6><ImageDesc6>none</ImageDesc6>" +
    		"<Image_Xcoord6>0</Image_Xcoord6><Image_Ycoord6>0</Image_Ycoord6>" +
    		"</Prc_instance></Document>";*/
    
    /*String lString = "<Document><Prc_instance><SRNo>13-00129634</SRNo><Status>CLSD</Status>" +
    		"<DateComplete>02/22/2013</DateComplete><CompletedBy>John Dove</CompletedBy>" +
    		"<DateClose>02/22/2013</DateClose><Note>Furniture, Front Collection</Note>" +
    		"<FileName1>12-00000848_xxxx1.png</FileName1><ImageDesc1> Bulk Collection Pic1</ImageDesc1>" +
    		"<Image_Xcoord1>401988.65</Image_Xcoord1><Image_Ycoord1>133315.80</Image_Ycoord1>" +
    		"</Prc_instance></Document>";*/

    //request.addProperty("xmlstring","<Document><Prc_instance><SRNo>13-00169460</SRNo><Status>CLSD</Status><DateComplete>02/22/2013</DateComplete><CompletedBy>John Dove</CompletedBy><DateClose>02/22/2013</DateClose><Note>Furniture, Front Collection</Note><FileName1>12-00000848_xxxx1.png</FileName1><ImageDesc1> Bulk Collection Pic1</ImageDesc1><Image_Xcoord1>401988.65</Image_Xcoord1><Image_Ycoord1>133315.80</Image_Ycoord1><FileName2>12-00000848_xxxx2.png</FileName2><ImageDesc2> Bulk Collection Pic2</ImageDesc2><Image_Xcoord2>401998.65</Image_Xcoord2><Image_Ycoord2>133325.80</Image_Ycoord2><FileName3>12-00000848_xxxx3.png</FileName3><ImageDesc3> Bulk Collection Pic3</ImageDesc3><Image_Xcoord3>401978.65</Image_Xcoord3><Image_Ycoord3>133345.80</Image_Ycoord3><FileName4>12-00000848_xxxx4.png</FileName4><ImageDesc4> Bulk Collection Pic4</ImageDesc4><Image_Xcoord4>401968.65</Image_Xcoord4><Image_Ycoord4>133385.80</Image_Ycoord4><FileName5>12-00000848_xxxx5.png</FileName5><ImageDesc5> Bulk Collection Pic5</ImageDesc5><Image_Xcoord5>401938.65</Image_Xcoord5><Image_Ycoord5>133335.80</Image_Ycoord5><FileName6>12-00000848_xxxx6.png</FileName6><ImageDesc6> Bulk Collection Pic6</ImageDesc6><Image_Xcoord6>401908.65</Image_Xcoord6><Image_Ycoord6>133305.80</Image_Ycoord6></Prc_instance></Document>");
    //request.addProperty("xmlstring","<Document><Prc_instance><SRNo>13-00169727</SRNo><Status>CLSD</Status><DateComplete>02/22/2013</DateComplete><CompletedBy>John Dove</CompletedBy><DateClose>02/22/2013</DateClose><Note>Furniture, Front Collection</Note><FileName1>12-00000848_xxxx1.png</FileName1><ImageDesc1> Bulk Collection Pic1</ImageDesc1><Image_Xcoord1>401988.65</Image_Xcoord1><Image_Ycoord1>133315.80</Image_Ycoord1></Prc_instance></Document>");
    //request.addProperty("xmlstring",lString1);
    
    String lStr;
    if (iOffline) {
    	lStr = prepareXMLString_DB();
    } else {
     lStr = prepareXMLString();
    }
    
    //dl 1/31/2015 compare of xml and json https://blog.udemy.com/json-vs-xml/
    request.addProperty("xmlstring", lStr);
    //request.addProperty("xmlstring", lString);
    //Declare the version of the SOAP request

    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

    envelope.setOutputSoapObject(request);

    envelope.dotNet = true;

    try {
    	   //Default timeout is 20 seconds. http://stackoverflow.com/questions/14665729/what-is-the-default-time-out-of-httptransportse
    	  //09/01/2014 change the timeout from 60 seconds to 5 seconds.
    	  //int lTimeOut=60000;
    	  //09/17/2014 change to 10s again.
    	  int lTimeOut= 10000;
    	  HttpTransportSE androidHttpTransport = new HttpTransportSE(URL, lTimeOut);

          //this is the actual part that will call the webservice

          androidHttpTransport.call(SOAP_ACTION1, envelope);

          // Get the SoapResult from the envelope body.

          SoapObject result = (SoapObject)envelope.bodyIn;

          if(result != null)
          {
                //Get the first property and change the label text

                //txtCel.setText(result.getProperty(0).toString());
        	    mResult = result.getProperty(0).toString();
        	    //Update the route status  07/13/2014
        	    SinglePicArrayData lSinglePicArray = SinglePicArrayData.getInstance();
        	    if (mRouteStatusMap != null) {
        	    	mRouteStatusMap.put(lSinglePicArray.mSRNo, new Boolean(true));
        	    }
        	    // Add the ticket to the database
        	    
        	    BulkMobileDB db = new BulkMobileDB(this);
        		Log.d("Adding: ", "Adding the ticket.."); 
        		
        		if (db.checkTicketExist(lSinglePicArray.mSRNo) == 0) {
        			//Add the ticket to the database
        			db.addTicket(lSinglePicArray.mSRNo);
        		}
        		//show the result message here if there is any
	   	        if (mResult.length() > 0)
	   	        	 showResultMessage();	
	   	         mResult ="";
          }
          else
          {
               Toast.makeText(getApplicationContext(), "No Response",Toast.LENGTH_LONG).show();
          }

    } 
    catch (SocketTimeoutException e) { 
    	
    	e.printStackTrace();
        mResult = e.getMessage();
        // dl 09-06-2014 Process the timeout case
        HandleTimeout();
    }
    catch (Exception e) {

          e.printStackTrace();
          mResult = e.getMessage();
    }
    
    }
	
	public void HandleTimeout() {
		RouteData lRoute = Save_Data();
		
	}
	
	public void ReturnToRouteResult(View v)
    {	
		//change view
        //Intent myIntent = new Intent(MainActivity.this, MainActivity.class);
        //MainActivity.this.startActivity(myIntent);
		//setContentView(R.layout.selectroute);
		//gotoRouteResult(v);
		//setContentView(R.layout.selectroute);
		setContentView(R.layout.selectroute);		
		
		spinner = (Spinner) findViewById(R.id.routes);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.collRt_array, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
		
		spinner.setSelection(m_SelectPosition);
		//TableLayout table = (TableLayout) findViewById(R.id.tableLayout2);
		//table.setVisibility(View.VISIBLE);
		//Add two test cases: 07/13/2014
		//mRouteStatusMap.put("14-00188562", new Boolean(true));
		//mRouteStatusMap.put("14-00187693", new Boolean(true));
		
		gotoRouteResult(v);
		//LinearLayout lLayout= (LinearLayout)findViewById(R.id.closeout);
		//lLayout.setVisibility(View.GONE);
    }
	
	public void returnToRouteSelection(View v)
    {	
		//change view
        Intent myIntent = new Intent(MainActivity.this, MainActivity.class);
        MainActivity.this.startActivity(myIntent);
    }
	
	/**
	 * Check if there is fast connectivity
	 * @param context
	 * @return
	 */
//	private static boolean isDataConnected(Context context){
//	    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//	    NetworkInfo info = cm.getActiveNetworkInfo();
//	    TelephonyManager lTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//	    
//	 // Skip if no connection, or background data disabled
//	    if (info == null) {
//	        return false;
//	    }
//	    
//	 // Only update if WiFi or 3G is connected and not roaming
//	    int netType = info.getType();
//	    //int netSubtype = info.getSubtype();
//	    if (netType == ConnectivityManager.TYPE_WIFI) {
//	        return false;
//	    } else if (netType == ConnectivityManager.TYPE_MOBILE
//	        //&& netSubtype == TelephonyManager.NETWORK_TYPE_UMTS
//	        && !lTelephonyManager.isNetworkRoaming()) {
//	            return info.isConnected();
//	    } else {
//	        return false;
//	    }
//	    //return (info != null && info.isConnected() && Connectivity.isConnectionFast(info.getType(), tm.getNetworkType()));
//	}
	//dl 09/19/2014 comment the following function as we don't network signal
//	private boolean isDataSignalStrong(Context context){
//		if (isDataConnected(context)) {
//			
//			//TelephonyManager lTelephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
//			// for example value of first element
//			/*CellInfoCdma cellinfocdma = (CellInfoCdma)lTelephonyManager.getAllCellInfo().get(0);
//			CellSignalStrengthCdma cellSignalStrengthCdma = cellinfocdma.getCellSignalStrength();
//			int lLevel = cellSignalStrengthCdma.getLevel();*/
//			if (mSignalDbm < -98)
//				return false;
//			else 
//				return true;
//		}
//		else 
//			return false;
//	}
	
	
	private String prepareXMLString_DB(RouteData iRoute) {
		String lStr = "<Document><Prc_instance>";
		PictureProperty lPictureProp;
		
		//SinglePicArrayData lSinglePicArray = SinglePicArrayData.getInstance();
		
		BulkMobileDB db = new BulkMobileDB(this);
		Log.d("Reading: ", "Reading all routes.."); 
	    //List<RouteData> routes = db.getAllRoutes();
	    //RouteData lRoute;// = routes[0];
	    //for (RouteData route : routes) {
            //String log = "Id: "+cn.getSRNo()+" ,Name: " + cn.getStatus() + " ,Phone: " + cn.getCompleteDate();
            String lSRNo = iRoute.getSRNo();
            //lRoute = route;
	    	
	    	lStr = lStr + "<SRNo>";
			
			lStr = lStr + iRoute.getSRNo();
			lStr = lStr + "</SRNo>";
			lStr = lStr + "<Status>";
			lStr = lStr + iRoute.getStatus();	//	"CLSD</Status>";
			lStr = lStr + "</Status>";
			lStr = lStr + "<DateComplete>";
			lStr = lStr + iRoute.getCompleteDate();
			lStr = lStr + "</DateComplete>";
			lStr = lStr + "<CompletedBy>";
			if (iRoute.getCompleteBy().length() == 0) {
				lStr = lStr + "John Dove";
			} else {
				lStr = lStr + iRoute.getCompleteBy();
			}
			lStr = lStr + "</CompletedBy>";
			lStr = lStr + "<DateClose>";
			lStr = lStr + iRoute.getCloseDate();
			lStr = lStr + "</DateClose>";
			
			lStr = lStr + "<Note>";
			lStr = lStr + iRoute.getNote();
			lStr = lStr + "</Note>";
	    	
            List<PictureData> pics = db.getPicturesWithID(lSRNo);
            int lCount = 0;
            for (PictureData pic : pics) {
            	//Write_file_to_Output_with_Picture(outputStream, i, pic, lSRNo);
            	//lPictureProp = (PictureProperty)lSinglePicArray.mPictureProp.get(i);
    			lStr = lStr + "<FileName" + (lCount+1) + ">";
    			//lStr = lStr + lPictureProp.getFileName();
    			lStr = lStr + lSRNo+ "_"+ (lCount+1) + ".png";
    			lStr = lStr + "</FileName" + (lCount+1) + ">";
    			
    			lStr = lStr + "<ImageDesc" + (lCount+1) + ">";
    			lStr = lStr + pic.getFileDesc();
    			lStr = lStr + "</ImageDesc" + (lCount+1) + ">";
    			
    			lStr = lStr + "<Image_Xcoord" + (lCount+1) + ">";
    			lStr = lStr + pic.getLong();
    			lStr = lStr + "</Image_Xcoord" + (lCount+1) + ">";
    			
    			lStr = lStr + "<Image_Ycoord" + (lCount+1) + ">";
    			lStr = lStr + pic.getLat();
    			lStr = lStr + "</Image_Ycoord" + (lCount+1) + ">";
            	
            	lCount ++;
            }
            
            if (lCount < 6) {
    			for (int i= lCount; i < 6; i++)
    			{
    				//lPictureProp = (PictureProperty)lSinglePicArray.mPictureProp.get(i);
    				lStr = lStr + "<FileName" + (i+1) + ">";
    				lStr = lStr + "Null";
    				lStr = lStr + "</FileName" + (i+1) + ">";
    				
    				lStr = lStr + "<ImageDesc" + (i+1) + ">";
    				lStr = lStr + "none";
    				lStr = lStr + "</ImageDesc" + (i+1) + ">";
    				
    				lStr = lStr + "<Image_Xcoord" + (i+1) + ">";
    				lStr = lStr + "0";
    				lStr = lStr + "</Image_Xcoord" + (i+1) + ">";
    				
    				lStr = lStr + "<Image_Ycoord" + (i+1) + ">";
    				lStr = lStr + "0";
    				lStr = lStr + "</Image_Ycoord" + (i+1) + ">";
    			}
    		}
	    	
		
	    lStr = lStr + "</Prc_instance></Document>";
		
		return lStr;
		
	}
	
	//public void Close_Ticket1(View v, boolean iOffline) {
	public void Close_Ticket1(RouteData iRoute) {
    //Initialize soap request + add parameters

    SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME1);       

    //Use this to add parameters
    String lString = "<Document><Prc_instance><SRNo>13-00169462</SRNo><Status>CLSD</Status>" +
    		"<DateComplete>02/22/2013 1:45:30 PM</DateComplete><CompletedBy>John Dove</CompletedBy>" +
    		"<DateClose>02/22/2013 1:45:30 PM</DateClose><Note>Furniture, Front Collection</Note>" +
    		"<FileName1>12-00000848_xxxx1.png</FileName1><ImageDesc1> Bulk Collection Pic1</ImageDesc1>" +
    		"<Image_Xcoord1>401988.65</Image_Xcoord1><Image_Ycoord1>133315.80</Image_Ycoord1><FileName2>" +
    		"12-00000848_xxxx2.png</FileName2><ImageDesc2> Bulk Collection Pic2</ImageDesc2><Image_Xcoord2>" +
    		"401998.65</Image_Xcoord2><Image_Ycoord2>133325.80</Image_Ycoord2><FileName3>" +
    		"12-00000848_xxxx3.png</FileName3><ImageDesc3> Bulk Collection Pic3</ImageDesc3>" +
    		"<Image_Xcoord3>401978.65</Image_Xcoord3><Image_Ycoord3>133345.80</Image_Ycoord3>" +
    		"<FileName4>12-00000848_xxxx4.png</FileName4><ImageDesc4> Bulk Collection Pic4</ImageDesc4>" +
    		"<Image_Xcoord4>401968.65</Image_Xcoord4><Image_Ycoord4>133385.80</Image_Ycoord4>" +
    		"<FileName5>12-00000848_xxxx5.png</FileName5><ImageDesc5> Bulk Collection Pic5</ImageDesc5>" +
    		"<Image_Xcoord5>401938.65</Image_Xcoord5><Image_Ycoord5>133335.80</Image_Ycoord5>" +
    		"<FileName6>12-00000848_xxxx6.png</FileName6><ImageDesc6> Bulk Collection Pic6</ImageDesc6>" +
    		"<Image_Xcoord6>401908.65</Image_Xcoord6><Image_Ycoord6>133305.80</Image_Ycoord6>" +
    		"</Prc_instance></Document>";
  
    
    String lStr;
    //if (iOffline) {
    	lStr = prepareXMLString_DB(iRoute);
    //} else {
     //lStr = prepareXMLString();
    //}
    request.addProperty("xmlstring", lStr);
    //request.addProperty("xmlstring", lString);
    //Declare the version of the SOAP request

    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

    envelope.setOutputSoapObject(request);

    envelope.dotNet = true;

    try {

    	  int lTimeout =10000;
          HttpTransportSE androidHttpTransport = new HttpTransportSE(URL, lTimeout);

          //this is the actual part that will call the webservice

          androidHttpTransport.call(SOAP_ACTION1, envelope);

          // Get the SoapResult from the envelope body.

          SoapObject result = (SoapObject)envelope.bodyIn;

          if(result != null)
          {
                //Get the first property and change the label text

                //txtCel.setText(result.getProperty(0).toString());
        	    mResult = result.getProperty(0).toString();
        	    
        	    //Update the route status  09/13/2014
        	    //SinglePicArrayData lSinglePicArray = SinglePicArrayData.getInstance();
        	    String lSRNo = iRoute.getSRNo();
        	    if (mRouteStatusMap != null) {
        	    	mRouteStatusMap.put(lSRNo, new Boolean(true));
        	    }
        	    
        	    // Add the ticket to the database
        	    BulkMobileDB db = new BulkMobileDB(this);
        		Log.d("Adding: ", "Adding the ticket.."); 
        		
        		if (db.checkTicketExist(lSRNo) == 0) {
        			//Add the ticket to the database
        			db.addTicket(lSRNo);
        		}
        		//Clean the data
        		Clean_Data(iRoute);
          }
          else
          {
               Toast.makeText(getApplicationContext(), "No Response",Toast.LENGTH_LONG).show();
          }

    } catch (SocketTimeoutException e) { 
    	
    	e.printStackTrace();
        mResult = e.getMessage(); 
        
    } catch (Exception e) {
	
	    e.printStackTrace();
	    mResult = e.getMessage();
	}
    
    }
	
	//dl 09/19/2014 comment the following function as we don't network signal
//	private class MyPhoneStateListener extends PhoneStateListener{
//	     public int singalStenths =0; 
//	      @Override
//	      public void onSignalStrengthsChanged(SignalStrength signalStrength){
//	         super.onSignalStrengthsChanged(signalStrength);
//	         //int singalStrength  = signalStrength.getGsmSignalStrength();
//	         //singalStenths = signalStrength.getGsmSignalStrength();
//	         int cdmadbm = signalStrength.getCdmaDbm();
//	         int evdodbm = signalStrength.getEvdoDbm();
//	         mSignalDbm = evdodbm;
//	     
//	         EditText dbmEt = (EditText)findViewById(R.id.txtDbm);
//	         if (dbmEt !=null) {
//	        	 dbmEt.setText(Integer.toString(mSignalDbm));
//	         }
//	             //System.out.println("----- gsm strength" + singalStrength );
//	             //System.out.println("----- gsm strength" + singalStenths );
//	             /*
//	             if(singalStenths > 30)
//	             {
//	                 signalstrength.setText("Signal Str : Good");
//	                 signalstrength.setTextColor(getResources().getColor(R.color.good));
//	             }
//	             else if(singalStenths > 20 && singalStenths < 30)
//	             {
//	                 signalstrength.setText("Signal Str : Average");
//	                 signalstrength.setTextColor(getResources().getColor(R.color.average));
//	             }
//	             else if(singalStenths < 20)
//	             {
//	                 signalstrength.setText("Signal Str : Weak");
//	                 signalstrength.setTextColor(getResources().getColor(R.color.weak));
//	             }*/
//	      }
//	}
}
