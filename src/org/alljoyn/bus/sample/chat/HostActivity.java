/*
 * Copyright (c) 2011, AllSeen Alliance. All rights reserved.
 *
 *    Permission to use, copy, modify, and/or distribute this software for any
 *    purpose with or without fee is hereby granted, provided that the above
 *    copyright notice and this permission notice appear in all copies.
 *
 *    THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 *    WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 *    MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 *    ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 *    WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 *    ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 *    OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.alljoyn.bus.sample.chat;

import java.util.ArrayList;
import java.util.Arrays;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.sample.chat.ChatApplication;
import org.alljoyn.bus.sample.chat.Observable;
import org.alljoyn.bus.sample.chat.Observer;
import org.alljoyn.bus.sample.chat.DialogBuilder;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import android.util.Log;

public class HostActivity extends Activity implements Observer {
    private static final String TAG = "chat.HostActivity";
     
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.host);
              
        mChannelName = (TextView)findViewById(R.id.hostChannelName);
        mChannelName.setText("");
        
       
       
        
        mChannelStatus = (TextView)findViewById(R.id.hostChannelStatus);
        mChannelStatus.setText("Idle");
        
        mSetNameButton = (Button)findViewById(R.id.hostSetName);
        mSetNameButton.setEnabled(true);
        mSetNameButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_SET_NAME_ID);
        	}
        });

        mStartButton = (Button)findViewById(R.id.hostStart);
        mStartButton.setEnabled(false);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(mChatApplication.getCounter()==0){
            	mChatApplication.setFlag(true);
            	mChatApplication.incCounter(); 
                 Intent intent = new Intent(HostActivity.this, AllJoynMasterService.class);
                mRunningService1= startService(intent);
                 if (mRunningService1 == null) {
                     Log.i(TAG, "onCreate(): failed to startService()");
                 }
            	}
            	showDialog(DIALOG_START_ID);
                
                
            }
        });
        
        mStopButton = (Button)findViewById(R.id.hostStop);
        mStopButton.setEnabled(false);
        mStopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_STOP_ID);
            }
        });
        
        
        /*
         * Keep a pointer to the Android Appliation class around.  We use this
         * as the Model for our MVC-based application.  Whenever we are started
         * we need to "check in" with the application so it can ensure that our
         * required services are running.
         */
        mChatApplication = (ChatApplication)getApplication();
        
        mSelectButton = (Button)findViewById(R.id.hostSelect);
        mSelectButton.setEnabled(true);
        mSelectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_SELECT_ID);
            }
        });
        
        /*
         * Call down into the model to get its current state.  Since the model
         * outlives its Activities, this may actually be a lot of state and not
         * just empty.
         */
        updateChannelState();
        
        /*
         * Now that we're all ready to go, we are ready to accept notifications
         * from other components.
         */
        mChatApplication.addObserver(this);
        
        
        
        
        mQuitButton = (Button)findViewById(R.id.hostQuit);
        mQuitButton.setEnabled(true);
        mQuitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mChatApplication.quit();
            }
        });
   
    
    }
    
    
	public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        mChatApplication = (ChatApplication)getApplication();
        mChatApplication.deleteObserver(this);
        super.onDestroy();
 	}
	
    private ChatApplication mChatApplication = null;
    
    static final int DIALOG_SET_NAME_ID = 0;
    static final int DIALOG_START_ID = 1;
    static final int DIALOG_STOP_ID = 2;
    public static final int DIALOG_ALLJOYN_ERROR_ID = 3;
    static final int DIALOG_SELECT_ID=4;

    protected Dialog onCreateDialog(int id) {
        Log.i(TAG, "onCreateDialog()");
        Dialog result = null;
        switch(id) {
        case DIALOG_SET_NAME_ID:
	        { 
	        	DialogBuilder builder = new DialogBuilder(mHandler);
	        	result = builder.createHostNameDialog(this, mChatApplication);
	        }  
        	break;
        case DIALOG_START_ID:
	        { 
	        	DialogBuilder builder = new DialogBuilder(mHandler);
	        	result = builder.createHostStartDialog(this, mChatApplication);
	        } 
            break;
        case DIALOG_STOP_ID:
	        { 
	        	DialogBuilder builder = new DialogBuilder(mHandler);
	        	result = builder.createHostStopDialog(this, mChatApplication);
	        }
	        break;
	    case DIALOG_ALLJOYN_ERROR_ID:
	        { 
	        	DialogBuilder builder = new DialogBuilder(mHandler);
	        	result = builder.createAllJoynErrorDialog(this, mChatApplication);
	        }
	        break;	      
	    case DIALOG_SELECT_ID:
        { 
        	
        	try {
				result = createHostSelectDeviceDialog(this, mChatApplication);
			} catch (BusException e) {
				e.printStackTrace();
			}
        }
        break;	
        }
        
        return result;
    }
    
    public synchronized void update(Observable o, Object arg) {
        Log.i(TAG, "update(" + arg + ")");
        String qualifier = (String)arg;
        
        if (qualifier.equals(ChatApplication.APPLICATION_QUIT_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_APPLICATION_QUIT_EVENT);
            mHandler.sendMessage(message);
        }
        
        if (qualifier.equals(ChatApplication.HOST_CHANNEL_STATE_CHANGED_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_CHANNEL_STATE_CHANGED_EVENT);
            mHandler.sendMessage(message);
        }
        
        if (qualifier.equals(ChatApplication.ALLJOYN_ERROR_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_ALLJOYN_ERROR_EVENT);
            mHandler.sendMessage(message);
        }
        if (qualifier.equals(ChatApplication.USE_CHANNEL_STATE_CHANGED_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_USE_CHANNEL_STATE_CHANGED_EVENT);
            mHandler.sendMessage(message);
        }
        
    }
    
    
    
    public void updateChannelState() {
    	if(mChatApplication.getFlag()==false){
    	AllJoynService.HostChannelState channelState = mChatApplication.hostGetChannelState();
    	String name = mChatApplication.hostGetChannelName();
    	
    	boolean haveName = true;
    	if (name == null) {
    		haveName = false;
    		name = "Not set";
    	}
        mChannelName.setText(name);
      
        switch (channelState) {
        case IDLE:
            mChannelStatus.setText("Idle");
            break;
        case NAMED:
            mChannelStatus.setText("Named");
            break;
        case BOUND:
            mChannelStatus.setText("Bound");
            break;
        case ADVERTISED:
            mChannelStatus.setText("Advertised");
            break;
        case CONNECTED:
            mChannelStatus.setText("Connected");
           
            break;
        default:
            mChannelStatus.setText("Unknown");
            break;
        }
        
        if (channelState == AllJoynService.HostChannelState.IDLE) {
            mSetNameButton.setEnabled(true);
            if (haveName) {
            	mStartButton.setEnabled(true);
            } else {
                mStartButton.setEnabled(false);
            }
            mStopButton.setEnabled(false);
            
        } else {
            mSetNameButton.setEnabled(false);
            mStartButton.setEnabled(false);
            mStopButton.setEnabled(true);
            
        }
      }
    	else{
    		AllJoynMasterService.HostChannelState channelState = mChatApplication.hostGetChannelState1();
        	String name = mChatApplication.hostGetChannelName();
        	
        	boolean haveName = true;
        	if (name == null) {
        		haveName = false;
        		name = "Not set";
        	}
            mChannelName.setText(name);
          
            switch (channelState) {
            case IDLE:
                mChannelStatus.setText("Idle");
                break;
            case NAMED:
                mChannelStatus.setText("Named");
                break;
            case BOUND:
                mChannelStatus.setText("Bound");
                break;
            case ADVERTISED:
                mChannelStatus.setText("Advertised");
                break;
            case CONNECTED:
                mChannelStatus.setText("Connected");
               
                break;
            default:
                mChannelStatus.setText("Unknown");
                break;
            }
            
            if (channelState == AllJoynMasterService.HostChannelState.IDLE) {
                mSetNameButton.setEnabled(true);
                if (haveName) {
                	mStartButton.setEnabled(true);
                } else {
                    mStartButton.setEnabled(false);
                }
                mStopButton.setEnabled(false);
                
            } else {
                mSetNameButton.setEnabled(false);
                mStartButton.setEnabled(false);
                mStopButton.setEnabled(true);
                
            }
    		
    	}
    }
    
    
    
    
   
    
    
    private TextView mChannelName;
    private TextView mChannelStatus;
   
    private Button mSetNameButton;
    private Button mStartButton;
    private Button mStopButton;
    private Button mQuitButton;
    private Button mSelectButton;
    
    private void alljoynError() {
    	if (mChatApplication.getErrorModule() == ChatApplication.Module.GENERAL ||
    		mChatApplication.getErrorModule() == ChatApplication.Module.USE) {
    		showDialog(DIALOG_ALLJOYN_ERROR_ID);
    	}
    }
    
    private static final int HANDLE_APPLICATION_QUIT_EVENT = 0;
    private static final int HANDLE_CHANNEL_STATE_CHANGED_EVENT = 1;
    private static final int HANDLE_ALLJOYN_ERROR_EVENT = 2;
    private static final int HANDLE_USE_CHANNEL_STATE_CHANGED_EVENT=3;
    public static ComponentName mRunningService1;
    
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
	            case HANDLE_APPLICATION_QUIT_EVENT:
	            {
	                Log.i(TAG, "mHandler.handleMessage(): HANDLE_APPLICATION_QUIT_EVENT");
	                finish();
	            }
	            break; 
            case HANDLE_CHANNEL_STATE_CHANGED_EVENT:
	            {
	                Log.i(TAG, "mHandler.handleMessage(): HANDLE_CHANNEL_STATE_CHANGED_EVENT");
	                updateChannelState();
	            }
                break;
            case HANDLE_ALLJOYN_ERROR_EVENT:
            {
                Log.i(TAG, "mHandler.handleMessage(): HANDLE_ALLJOYN_ERROR_EVENT");
                alljoynError();
            }
            break;
            case HANDLE_USE_CHANNEL_STATE_CHANGED_EVENT :
            {
                Log.i(TAG, "mHandler.handleMessage(): HANDLE_USE_CHANNEL_STATE_CHANGE");
                mSelectButton.setEnabled(true);
            }
            break;
            default:
                break;
            }
        }
    };
    
    private Dialog createHostSelectDeviceDialog(Activity activity, final ChatApplication application) throws BusException {
       	Log.i(TAG, "createAllJoynErrorDialog()");
    	final Dialog dialog = new Dialog(activity);
    	dialog.requestWindowFeature(dialog.getWindow().FEATURE_NO_TITLE);
    	dialog.setContentView(R.layout.hostselectdevicedialog);
    	
    	if(application.getFlag()==false){
    	if(application.useGetChannelState()!=AllJoynService.UseChannelState.JOINED){
    	Toast toast=Toast.makeText(this,"Please join a channel first", Toast.LENGTH_SHORT);
		 toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 10);
		 toast.show();
    		return null;
         }
    	}
    	else{
    		if(application.useGetChannelState1()!=AllJoynMasterService.UseChannelState.JOINED){
    	    	Toast toast=Toast.makeText(this,"Please join a channel first", Toast.LENGTH_SHORT);
    			 toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 10);
    			 toast.show();
    	    		return null;
    	         }
    	}
    	Button ok = (Button)dialog.findViewById(R.id.findSelected);
    	ok.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View view) {
    			if(mChatApplication.getFlag()==false){
    				try {
						AllJoynService.sendKeys(checkedDevices);
					} catch (BusException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    			}
    			else
    			{
    				try {
						AllJoynMasterService.sendKeys(checkedDevices);
					} catch (BusException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    			}
    			dialog.cancel();
    		}
    	});
    	displayListView(application, dialog);
    	return dialog;
    }
    
    private void displayListView(ChatApplication application, Dialog d) throws BusException{
    	ArrayList<String> selectedDevices;
    	if(application.getFlag()==false){
    	 
    		String[] selD= AllJoynService.mGroupInterface.getMem();
    		AllJoynService.uniArray= AllJoynService.mGroupInterface.getUni();
    		AllJoynService.memArray=selD;
    		
    		selectedDevices= new ArrayList<String>(Arrays.asList(selD));
    	 
    	 
    	}
    	else{
    		
    		selectedDevices=AllJoynMasterService.nicks;
    		
    	}
    	       MyCustomAdapter dataAdapter=null;
    	        dataAdapter = new MyCustomAdapter(this,
    		    R.layout.member_info, selectedDevices);
    		  ListView listView = (ListView) d.findViewById(R.id.listView1);
    		  // Assign adapter to ListView
    		  if(listView!=null)
    		  listView.setAdapter(dataAdapter);
    		  
    }
    private class MyCustomAdapter extends ArrayAdapter<String> {
    	 
    	  private ArrayList<String> selectedD= new ArrayList<String>();
    	 
    	  public MyCustomAdapter(Context context, int textViewResourceId, 
    	    ArrayList<String> DList) {
    	   super(context, textViewResourceId, DList);
    	   this.selectedD = new ArrayList<String>();
    	   this.selectedD.addAll(DList);
    	  }
    	  private class ViewHolder {
    		   TextView code;
    		   CheckBox name;
    		  }
    	  @Override
    	  public View getView(int position, View convertView, ViewGroup parent) {
    	 
    	   ViewHolder holder = null;
    	   Log.v("ConvertView", String.valueOf(position));
    	 
    	   if (convertView == null) {
    	   LayoutInflater vi = (LayoutInflater) getSystemService(
    	     Context.LAYOUT_INFLATER_SERVICE);
    	   convertView = vi.inflate(R.layout.member_info, null);
    	 
    	   holder = new ViewHolder();
    	   holder.code = (TextView) convertView.findViewById(R.id.code);
    	   holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
    	   convertView.setTag(holder);
    	 
    	    holder.name.setOnClickListener( new View.OnClickListener() {  
    	     public void onClick(View v) {  
    	      CheckBox cb = (CheckBox) v ;  
    	       
    	      Toast.makeText(getApplicationContext(),
    	       "Clicked on Checkbox: " + cb.getText() +
    	       " is " + cb.isChecked(), 
    	       Toast.LENGTH_LONG).show();
    	       if(cb.isChecked())
    	    	   checkedDevices.add(cb.getText().toString());
    	       else{
    	    	   if(checkedDevices.contains(cb.getText().toString()))
    	    		   checkedDevices.remove(cb.getText().toString());
    	       }
    	     }  
    	    });  
    	   } 
    	   else {
    	    holder = (ViewHolder) convertView.getTag();
    	   }
    	 
    	   String device = selectedD.get(position);
    	   holder.code.setText(device);
    	   holder.name.setText(device);
    	   if(checkedDevices.contains(device))
    	   holder.name.setChecked(true);
    	   else
           holder.name.setChecked(false);
    	   
    	 
    	   return convertView;
    	 
    	  }
    
    }
    private ArrayList<String> checkedDevices=new ArrayList<String>();
}