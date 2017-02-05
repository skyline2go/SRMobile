package com.example.atest;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class UploadOfflineService extends IntentService {
	
	 private static String URL = "http://trackster.dpw.dc.gov/CloseSR.asmx";
	 private static String NAMESPACE = "http://Trakster.com/";
	 private static String METHOD_NAME1 = "CloseTicket";
	 private static String SOAP_ACTION1 = "http://Trakster.com/CloseTicket";
	
	public UploadOfflineService() {
		super("uploadOffline");
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/**
	 * Check if there is fast connectivity
	 * @param context
	 * @return
	 */
	private static boolean isDataConnected(Context context){
	    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo info = cm.getActiveNetworkInfo();
	    TelephonyManager lTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	    
	 // Skip if no connection, or background data disabled
	    if (info == null) {
	        return false;
	    }
	    
	 // Only update if WiFi or 3G is connected and not roaming
	    int netType = info.getType();
	    //int netSubtype = info.getSubtype();
	    if (netType == ConnectivityManager.TYPE_WIFI) {
	        return false;
	    } else if (netType == ConnectivityManager.TYPE_MOBILE
	        //&& netSubtype == TelephonyManager.NETWORK_TYPE_UMTS
	        && !lTelephonyManager.isNetworkRoaming()) {
	            return info.isConnected();
	    } else {
	        return false;
	    }
	    //return (info != null && info.isConnected() && Connectivity.isConnectionFast(info.getType(), tm.getNetworkType()));
	}
	
	public boolean Write_file_to_Output_with_Picture(DataOutputStream iOutputStream, int iIndex, PictureData iPic, String iSRNo) {
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
		
	}
	
	public void Upload_file_offline(RouteData iRoute) {
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
			
			BulkMobileDB db = new BulkMobileDB(this);
			//Log.d("Reading: ", "Reading all routes.."); 
		    //List<RouteData> routes = db.getAllRoutes();
		    //for (RouteData route : routes) {
	            //String log = "Id: "+cn.getSRNo()+" ,Name: " + cn.getStatus() + " ,Phone: " + cn.getCompleteDate();
	            String lSRNo = iRoute.getSRNo();
	            
	            List<PictureData> pics = db.getPicturesWithID(lSRNo);
	            int i = 0;
	            boolean lUploadSucceed = true;
	            for (PictureData pic : pics) {
	            	if (!Write_file_to_Output_with_Picture(outputStream, i, pic, lSRNo))
	            	{
	            		lUploadSucceed = false;
	            	}
	            	i ++;
	            }
		    	// Delete the route after successfully upload
	            if (lUploadSucceed) {
	            	db.deleteRoute(lSRNo);
	            }
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
	}
	
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
	    	// Writing Contacts to log
            //db.deleteRoute(cn);
            
        //Log.d("Name: ", log);
        
		
	    lStr = lStr + "</Prc_instance></Document>";
		
		return lStr;
		
	}
	
	//public void Close_Ticket1(View v, boolean iOffline) {
	public void Close_Ticket1(RouteData iRoute, boolean iOffline) {
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

          HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

          //this is the actual part that will call the webservice

          androidHttpTransport.call(SOAP_ACTION1, envelope);

          // Get the SoapResult from the envelope body.

          SoapObject result = (SoapObject)envelope.bodyIn;

          if(result != null)
          {
                //Get the first property and change the label text

                //txtCel.setText(result.getProperty(0).toString());
        	    String lResult = result.getProperty(0).toString();
          }
          else
          {
               Toast.makeText(getApplicationContext(), "No Response",Toast.LENGTH_LONG).show();
          }

    } catch (Exception e) {

          e.printStackTrace();
          String lResult = e.getMessage();
    }
    
    }
	
	@Override
	protected void onHandleIntent(Intent arg0) {
		
		//while (true) {
		    // TODO Auto-generated method stub
		while(!isDataConnected(getApplicationContext())) {
	   		 try {
					Thread.sleep(5 * 1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	   	};
	   	 	
	   	 BulkMobileDB db = new BulkMobileDB(this);
		 Log.d("Reading: ", "Reading all routes.."); 
		 List<RouteData> routes = db.getAllRoutes();
		 for (RouteData route : routes) {
	   	 	Upload_file_offline(route);
	   	 	Close_Ticket1(route,true);
	   	 	//db.deleteRoute(route);
		 }
		//}
	}
}
