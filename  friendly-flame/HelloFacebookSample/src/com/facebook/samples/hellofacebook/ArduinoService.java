package com.facebook.samples.hellofacebook;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;


public class ArduinoService extends Service implements Runnable {
	private static final String TAG = "ArduinoService";
	private UsbManager mUsbManager;
	private UsbAccessory mAccessory;
	
	private MyServiceBinder myServiceBinder = new MyServiceBinder();
	private static HelloFacebookSampleActivity.RunnableForArduinoService arduinoRunnable;

	ParcelFileDescriptor mFileDescriptor;
	FileInputStream mInputStream;
	static FileOutputStream mOutputStream;
	
	private static final int MESSAGE_BUTTON_PRESSED = 1;

	protected static final int DISPLAY_BUFFER = 2;
	
	@Override
	public IBinder onBind(Intent arg0) { 
		Log.e(TAG,"ArduinoService is bound"); 
		return myServiceBinder;
	} 
	
	
	@Override
	public boolean onUnbind(Intent intent) {
		Log.i(TAG,"ArduinoService unbound!!!!!!!!!!!!");
		return true;
	}
	
	@Override 
	public void onCreate() {
		Log.d("ArduinoService","onCreate");
		super.onCreate(); 
        mUsbManager = UsbManager.getInstance(this);
		Log.d(TAG,"Usbmanager: "+mUsbManager.toString());
		UsbAccessory[] accessories = mUsbManager.getAccessoryList();
		if (accessories == null) { 
		} else {
			mAccessory = accessories[0];
			openAccessory(mAccessory); 
		}
	}
	
	
	@Override 
	public void onDestroy() {
		super.onDestroy(); 
		closeAccessory();
	}
	
	 private void openAccessory(UsbAccessory accessory) {
			mFileDescriptor = mUsbManager.openAccessory(accessory);
			if (mFileDescriptor != null) {
				FileDescriptor fd = mFileDescriptor.getFileDescriptor();
				mInputStream = new FileInputStream(fd);
				mOutputStream = new FileOutputStream(fd);
				Thread thread = new Thread(null, this, "BeyondTheDesktop");
				thread.start();
		     	Toast.makeText(this, "Accessory opened!", Toast.LENGTH_LONG).show();
				Log.d(TAG, "accessory opened");
			} else {
				Log.d(TAG, "accessory open fail");
			}
		}

	 public void run() {	
			int ret = 0;
			byte[] buffer = new byte[16384];
			int i;

			while (ret >= 0) {
				try {
					ret = mInputStream.read(buffer); 
				} catch (IOException e) {
					break;
				}
				i = 0;		
				Log.d("BUFFER","buffer: "+new String(buffer));
								
				while (i < ret) {
					Message m = Message.obtain(messageHandler, MESSAGE_BUTTON_PRESSED);
					m.obj = buffer[i];
					messageHandler.sendMessage(m); 
					i++;
				}

			}
		} // end of run

		private void closeAccessory() {
			try {
				if (mFileDescriptor != null) {
					mFileDescriptor.close();
				}
			} catch (IOException e) {
			} finally {
				mFileDescriptor = null;
				mAccessory = null;
			}
		}
		
		  public Handler messageHandler = new Handler() {
		      @Override
		      public void handleMessage(Message msg) { 
		    	  Log.d(TAG,"message to be handled.");
		          switch(msg.what) {

		          case MESSAGE_BUTTON_PRESSED:
		        	  String str = String.valueOf(msg.obj);
	 	  			  Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();		        	 
	 	  			  if (str.equals("66")) {
		 	  			  MyServiceBinder.buttonPressed();		 	  			  
	 	  			  } 
		        	  break;
		          }
		      }
		  };
		   
			
		public static class MyServiceBinder extends Binder {
			private static Handler arduinoCallbackHandler;
			public void setRunnable (final HelloFacebookSampleActivity.RunnableForArduinoService runnable){
				arduinoRunnable = runnable;
			}
			public void sendMessageToArduino(byte[] buffer) {
				sendCommand(buffer);
				Log.d("Buffer-sendMessage: ", Byte.valueOf(buffer[0]).toString());
			}
			public void setActivityCallbackHandler(final Handler callback) {
				arduinoCallbackHandler = callback;
			}
			
			public static void buttonPressed() {
				arduinoRunnable.msg = 1; 
				arduinoCallbackHandler.post(arduinoRunnable);
			}
		} // end binder
		
		public static void sendCommand(byte[] message) {
			

			if (mOutputStream != null && message.length != 0) {
				Log.d("Buffer-nach IF: ", Byte.valueOf(message[0]).toString());
				try {
					mOutputStream.write(message);
					Log.d("Buffer-nach write: ", Byte.valueOf(message[0]).toString());
				} catch (IOException e) {
					Log.e("MainActivity", "write failed", e);
				}
			}
		} 
		  
		
}

