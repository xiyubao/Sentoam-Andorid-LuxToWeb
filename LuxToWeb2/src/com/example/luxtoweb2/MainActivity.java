package com.example.luxtoweb2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;

import android.R.bool;
import android.R.string;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.os.SystemClock;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Menu;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private TextView txtReceiveInfo;
	private EditText edtRemoteIP,edtRemotePort,edtSendInfo;
	private Button btnConnect,btnSend;
	private boolean isConnected=false;
	private Socket socketClient=null;
	private String receiveInfoClient;
	static BufferedReader bufferedReaderClient	= null;
	static PrintWriter printWriterClient = null;
	
	public List<Sensor> allSensors;
	private SensorManager mSensorManager;
	private LocationManager locationManager;
    private float mLux;
    private double xLoc=36.665475;
    private double yLoc=117.132476;
    private TextView mTextView;
    private Sensor mSensor;
	
    private int experiment =1;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();   

		StrictMode.setThreadPolicy(policy);
		
		btnConnect=(Button)findViewById(R.id.btnConnect);
		btnSend=(Button)findViewById(R.id.btnSend);
		txtReceiveInfo=(TextView)findViewById(R.id.txtReceiveInfo);
		edtRemoteIP=(EditText)findViewById(R.id.edtRemoteIP);
		edtRemotePort=(EditText)findViewById(R.id.edtRemotePort);
		edtSendInfo=(EditText)findViewById(R.id.edtSendInfo);
		
		
		
		mSensorManager = (SensorManager) 
                getSystemService(Context.SENSOR_SERVICE);
		
		//***************Light********************************
        mSensorManager.registerListener(Lightlistener, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
                SensorManager.SENSOR_DELAY_NORMAL);
        //****************************************************
/*
      //***************Location*****************************
        String serviceString = Context.LOCATION_SERVICE;// ��ȡ����λ�÷���
        locationManager = (LocationManager) getSystemService(serviceString);// ����getSystemService()��������ȡLocationManager����
        String provider = LocationManager.PASSIVE_PROVIDER;// ָ��LocationManager�Ķ�λ����
        Location location = locationManager.getLastKnownLocation(provider);// ����getLastKnownLocation()������ȡ��ǰ��λ����Ϣ
        
        //TextView tv2 = (TextView)findViewById(R.id.textView2);
     
        //tv2.setText(xLoc+"."+yLoc);
        
        if(location != null)
        {
        	//tv2.setText("!!!");
            xLoc = location.getLatitude();
            yLoc = location.getLongitude();
        	//tv2.setText(location.getLatitude()+","+location.getLongitude());
        }
        
        locationManager.requestLocationUpdates(provider, 2000, 10,locationListener);// ����λ�øı��¼��������趨Ϊ����ı�10�ף�ʱ����Ϊ2�룬�趨����λ�ñ仯
        */
       // String provider = LocationManager.GPS_PROVIDER;
        
        
        //****************************************************
        
        /*
        
        WebView webview = (WebView)findViewById(R.id.webView1);
        
        
      //֧��JavaScript
        WebSettings setting = webview.getSettings();
        
        
        setting.setJavaScriptEnabled(true);
        //��web�ṩ���ýӿ�
        webview.addJavascriptInterface(new WebAppInterface(), "Android");
        webview.getSettings().setDomStorageEnabled(true);
        
        webview.loadUrl("http://111.231.57.188/");
        
        */
        
        try{
        if(getIntent()!=null)
        	if(getIntent().getStringExtra("flag").equals("0"))
        		ConnectButtonClick(null);
        }
        catch(Exception e)
        {
        	
        }
	}
	
	public class WebAppInterface {
        @JavascriptInterface
        public void backToApp() {
            finish();
        }
        
        @JavascriptInterface
        public String getLux() {
            return String.valueOf(lux);
        }
        @JavascriptInterface
        public String getxLoc(){
        	return String.valueOf(xLoc);
        }
        @JavascriptInterface
        public String getyLoc(){
        	return String.valueOf(yLoc);
        }
    }
	
	//���Ӱ�ť�����¼�
	public void ConnectButtonClick(View source)
	{
		if(isConnected)
		{
			isConnected=false;
			if(socketClient!=null)
			{
				try 
				{
					socketClient.close();
					socketClient=null;	
					printWriterClient.close();
					printWriterClient = null;
				} 
				catch (IOException e) 
				{
					// TODO �Զ����ɵ� catch ��
					e.printStackTrace();
				}
			}
			new tcpThread().interrupt();
			btnConnect.setText("��ʼ����");
			edtRemoteIP.setEnabled(true);
			edtRemotePort.setEnabled(true);
			txtReceiveInfo.setText("ReceiveInfo:\n");
		}
		else
		{
			isConnected=true;
			btnConnect.setText("ֹͣ����");
			edtRemoteIP.setEnabled(false);
			edtRemotePort.setEnabled(false);
			new tcpThread().start();
		}
	}
	//������Ϣ��ť�����¼�
	public void SendButtonClick(View source)
	{
		if ( isConnected && socketClient!=null) 
		{
			String sendInfo =edtSendInfo.getText().toString();//ȡ�ñ༭�����������������
			
			try 
			{				    	
				printWriterClient.print(sendInfo);//���͸�������
		    	printWriterClient.flush();
				receiveInfoClient = "Send "+"\""+sendInfo+"\""+" to server"+"\n";//��Ϣ����
				Message msg = new Message();
				handler.sendMessage(msg);
			}
			catch (Exception e) 
			{
				receiveInfoClient = e.getMessage() + "\n";//��Ϣ����
				Message msg = new Message();
				handler.sendMessage(msg);
			}
		}
	}
	//�߳�:������������������Ϣ
	private class tcpThread	extends Thread 
	{
		public void run()
		{
			try 
			{				
				//���ӷ�����
				socketClient = new Socket(edtRemoteIP.getText().toString(), Integer.parseInt(edtRemotePort.getText().toString()));	
				//ȡ�����롢�����
				bufferedReaderClient = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
				printWriterClient = new PrintWriter(socketClient.getOutputStream(), true);
				receiveInfoClient = "Connect to the server successfully!\n";//��Ϣ����
				Message msg = new Message();
				handler.sendMessage(msg);	
			}
			catch (Exception e) 
			{
				receiveInfoClient = "error in 1" + "\n";//��Ϣ����
				Message msg = new Message();
				handler.sendMessage(msg);
			}			
			char[] buffer = new char[256];
			int count = 0;
			while (isConnected)
			{
				try
				{
					if((count = bufferedReaderClient.read(buffer))>0)
					{			
						String message = getInfoBuff(buffer, count);
						receiveInfoClient = "Receive "+"\""+message+"\"" +" from server"+ "\n";//��Ϣ����
						Message msg = new Message();
						handler.sendMessage(msg);
						//String SensorInfo = GetInfo(message);
						
						//if(SensorInfo!=null)
						//	SendMsg(SensorInfo);
						//String[] ss = message.split("@");
						//if(ss[0]=="web"&&ss[1]=="1")
						if(message.equals("web@1"))
							SendMsg("android@1#"+lux);
						
					}
				}
				catch (Exception e)
				{
					receiveInfoClient = "error in 2" + "\n"+e.getMessage()+"\n";//��Ϣ����
					Message msg = new Message();
					handler.sendMessage(msg);
					
					//OnReStart();
				}
			}
		}
	};
	Handler handler = new Handler()
	{										
		  public void handleMessage(Message msg)										
		  {											
			  txtReceiveInfo.append("TCPClient: "+receiveInfoClient);	// ˢ��
		  }									
	 };
	 /*
	 private final LocationListener locationListener = 
	    		new LocationListener() {
	    	 
	        @Override
	        public void onLocationChanged(Location location) {
	            // TODO Auto-generated method stub
	        	//TextView tv2 = (TextView)findViewById(R.id.textView2);
	            //tv2.setText(location.getLatitude()+","+location.getLongitude());
	            
	            xLoc = location.getLatitude();
	            yLoc = location.getLongitude();
	        }
	 
	        @Override
	        public void onProviderDisabled(String arg0) {
	            // TODO Auto-generated method stub
	             
	        }
	 
	        @Override
	        public void onProviderEnabled(String arg0) {
	            // TODO Auto-generated method stub
	             
	        }
	 
	        @Override
	        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
	            // TODO Auto-generated method stub
	 
	        }
	 
	    };
	 */
	 
	 private void OnReStart(){
		 
		 ConnectButtonClick(null);
		 
		 try{
		 Thread.sleep(2000);
		 }
		 catch(Exception e)
		 {
			 
		 }
		 ConnectButtonClick(null);
		 
		 
	    	/*final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());  
	        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  
	        intent.putExtra("flag", "0");
	        startActivity(intent);  
	        
	        
	        android.os.Process.killProcess(android.os.Process.myPid());*/
	    }
	 
	public boolean conti =true;
	 
	private String getInfoBuff(char[] buff, int count)
	{
		char[] temp = new char[count];
		for(int i=0; i<count; i++)
		{
			temp[i] = buff[i];
		}
		return new String(temp);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void ShowMsg(String str)
	{
		receiveInfoClient = str+"\n";//��Ϣ����
		Message msg = new Message();
		handler.sendMessage(msg);
	}
	
	
	
	public boolean isstart=false;
	public String lux;
	
	public void SendMsg(String sendInfo)
	{
		if ( isConnected && socketClient!=null) 
		{
			//String sendInfo =edtSendInfo.getText().toString();//ȡ�ñ༭�����������������
			
			try 
			{				    	
				printWriterClient.print(sendInfo);//���͸�������
		    	printWriterClient.flush();
				receiveInfoClient = "Send "+"\""+sendInfo+"\""+" to server"+"\n";//��Ϣ����
				Message msg = new Message();
				handler.sendMessage(msg);
				
				//wait.postDelayed(r, 1000);
				//conti = true;
			}
			catch (Exception e) 
			{
				receiveInfoClient = e.getMessage() + "\n";//��Ϣ����
				Message msg = new Message();
				handler.sendMessage(msg);
			}
		}
	}

	@Override
    protected void onDestroy() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(Lightlistener);
        }
        super.onDestroy();
    }

    private SensorEventListener Lightlistener = 
            new SensorEventListener() {

        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                // ��ȡ����ǿ��
            	{
            		SendMsg(".");
            		
            			lux = String.valueOf(event.values[0]);
            		
            	}
                //if(isstart)
               // TextView tv1 = (TextView)findViewById(R.id.textView1);
              //  tv1.setText(event.values[0]+"lux");

            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
	
    
   
    
	
  
}
