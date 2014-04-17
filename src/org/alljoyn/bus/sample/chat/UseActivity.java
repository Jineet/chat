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

import java.lang.reflect.Method;
import java.util.List;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class UseActivity extends Activity implements Observer {
    private static final String TAG = "chat.UseActivity";
    
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.use);
                
        mHistoryList = new ArrayAdapter<String>(this, android.R.layout.test_list_item);
        ListView hlv = (ListView) findViewById(R.id.useHistoryList);
        hlv.setAdapter(mHistoryList);
        
        EditText messageBox = (EditText)findViewById(R.id.useMessage);
        messageBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                	String message = view.getText().toString();
                    Log.i(TAG, "useMessage.onEditorAction(): got message " + message + ")");
    	            mChatApplication.newLocalUserMessage(message);
    	            view.setText("");
                }
                return true;
            }
        });
        
        
        mJoinButton = (Button)findViewById(R.id.useJoin);
        mJoinButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
            	
            	if(mChatApplication.getFlag()==false){
            		Log.i(TAG,"chatapplication flag is " + mChatApplication.getFlag());
            		if(mChatApplication.getCounter()==0){
            	Log.i(TAG,"chatapplication counter is " + mChatApplication.getCounter());
            	Intent intent = new Intent(UseActivity.this, AllJoynService.class);
            	Log.i(TAG,"intent called");
                mRunningService= startService(intent);
                Log.i(TAG,"startService called");
                mChatApplication.incCounter();
                 
                if (mRunningService == null) {
                    Log.i(TAG, "onCreate(): failed to startService()");
                }
                }
            }
            	
                showDialog(DIALOG_JOIN_ID);
        	}
        });

        mLeaveButton = (Button)findViewById(R.id.useLeave);
        mLeaveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_LEAVE_ID);
            }
        });
        
        mChannelName = (TextView)findViewById(R.id.useChannelName);
        mChannelStatus = (TextView)findViewById(R.id.useChannelStatus);
        mChannelNick= (TextView)findViewById(R.id.hostNickname);
        
        /*
         * Keep a pointer to the Android Appliation class around.  We use this
         * as the Model for our MVC-based application.    Whenever we are started
         * we need to "check in" with the application so it can ensure that our
         * required services are running.
         */
        mChatApplication = (ChatApplication)getApplication();
       
        
        
        /*
         * Call down into the model to get its current state.  Since the model
         * outlives its Activities, this may actually be a lot of state and not
         * just empty.
         */
        updateChannelState();
        updateHistory();
        
        /*
         * Now that we're all ready to go, we are ready to accept notifications
         * from other components.
         */
        mChatApplication.addObserver(this);
        
        telManager = (TelephonyManager)
        		getSystemService(TELEPHONY_SERVICE);
        		telManager.listen(new TelListener(),
        		PhoneStateListener.LISTEN_CALL_STATE);
             
    }
    
    @TargetApi(Build.VERSION_CODES.ECLAIR)
	private class TelListener extends PhoneStateListener {
    	
		public void onCallStateChanged(int state, String incomingNumber) {
    	super.onCallStateChanged(state, incomingNumber);
    	Log.v("Phone State", "state:"+state);
    	switch (state) {
    	case TelephonyManager.CALL_STATE_IDLE:
    	//Log.v("Phone State",
    	//"incomingNumber:"+incomingNumber+" ended");
    	break;
    	case TelephonyManager.CALL_STATE_OFFHOOK:
    	//Log.v("Phone State",
    	//"incomingNumber:"+incomingNumber+" picked up");
    	break;
    	case TelephonyManager.CALL_STATE_RINGING:
    	//Log.v("Phone State",
    	//"incomingNumber:"+incomingNumber+" received");
    		mChatApplication.newLocalUserMessage(incomingNumber);
    		try{
    		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(incomingNumber));
    		
    	    Cursor cur=getContentResolver().query(uri,new String[] {PhoneLookup.DISPLAY_NAME},null,null,null);
    		if(cur!=null&&cur.moveToFirst()){
    			String name=cur.getString(cur.getColumnIndex(PhoneLookup.DISPLAY_NAME));
    			mChatApplication.newLocalUserMessage(name);
    		}
    	}
    	catch (Exception e){
    		e.printStackTrace();
    	}	
    		 
    		 
    		 
    		 
    	break;
    	default:
    	break;
    	}
    }
  }
    

    	
	public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        mChatApplication = (ChatApplication)getApplication();
        mChatApplication.deleteObserver(this);
    	super.onDestroy();
 	}
    
    public static final int DIALOG_JOIN_ID = 0;
    public static final int DIALOG_LEAVE_ID = 1;
    public static final int DIALOG_ALLJOYN_ERROR_ID = 2;
    public static final int DIALOG_NICK_ID = 3;
    
    
    protected Dialog onCreateDialog(int id) {
    	Log.i(TAG, "onCreateDialog()");
    	Dialog result = null;
        switch(id) {
        case DIALOG_JOIN_ID:
	        { 
	        	 DialogBuilder builder = new DialogBuilder(mHandler);
	        	 result = builder.createUseJoinDialog(this, mChatApplication);
	        	
	         }        	
        	break;
        case DIALOG_LEAVE_ID:
	        { 
	        	DialogBuilder builder = new DialogBuilder(mHandler);
	        	result = builder.createUseLeaveDialog(this, mChatApplication);
	        }
	        break;
        case DIALOG_ALLJOYN_ERROR_ID:
	        { 
	        	DialogBuilder builder = new DialogBuilder(mHandler);
	        	result = builder.createAllJoynErrorDialog(this, mChatApplication);
	        }
	        break;
        case DIALOG_NICK_ID:
        { 
        	DialogBuilder builder = new DialogBuilder(mHandler);
        	result = builder.createHostNickDialog(this, mChatApplication);
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
        
        if (qualifier.equals(ChatApplication.HISTORY_CHANGED_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_HISTORY_CHANGED_EVENT);
            mHandler.sendMessage(message);
        }
        
        if (qualifier.equals(ChatApplication.USE_CHANNEL_STATE_CHANGED_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_CHANNEL_STATE_CHANGED_EVENT);
            mHandler.sendMessage(message);
        }
        
        if (qualifier.equals(ChatApplication.ALLJOYN_ERROR_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_ALLJOYN_ERROR_EVENT);
            mHandler.sendMessage(message);
        }
    }
    
    private void updateHistory() {
        Log.i(TAG, "updateHistory()");
	    mHistoryList.clear();
	    List<String> messages = mChatApplication.getHistory();
        for (String message : messages) {
            mHistoryList.add(message);
        }
	    mHistoryList.notifyDataSetChanged();
    }
   
    
    private void updateChannelState() {
        Log.i(TAG, "updateHistory()");
        String name;
        if(mChatApplication.getFlag()==false){
    	AllJoynService.UseChannelState channelState = mChatApplication.useGetChannelState();
    	 name = mChatApplication.useGetChannelName();
    	 switch (channelState) {
         case IDLE:
             mChannelStatus.setText("Idle");
             mJoinButton.setEnabled(true);
             mLeaveButton.setEnabled(false);
             break;
         case JOINED:
             mChannelStatus.setText("Joined");
        
             mJoinButton.setEnabled(false);
             mLeaveButton.setEnabled(true);
             break;	
         }
        }
        else
        {
        	AllJoynMasterService.UseChannelState channelState = mChatApplication.useGetChannelState1();
        	 name = mChatApplication.useGetChannelName();
        	 switch (channelState) {
             case IDLE:
                 mChannelStatus.setText("Idle");
                 mJoinButton.setEnabled(true);
                 mLeaveButton.setEnabled(false);
                 break;
             case JOINED:
                 mChannelStatus.setText("Joined");
               
                 mJoinButton.setEnabled(false);
                 mLeaveButton.setEnabled(true);
                 break;	
             }
        }
    	if (name == null) {
    		name = "Not set";
    	}
       
    	mChannelName.setText(name);
        
        
    }
    
    /**
     * An AllJoyn error has happened.  Since this activity pops up first we
     * handle the general errors.  We also handle our own errors.
     */
    private void alljoynError() {
    	if (mChatApplication.getErrorModule() == ChatApplication.Module.GENERAL ||
    		mChatApplication.getErrorModule() == ChatApplication.Module.USE) {
    		showDialog(DIALOG_ALLJOYN_ERROR_ID);
    	}
    }
    
    private static final int HANDLE_APPLICATION_QUIT_EVENT = 0;
    private static final int HANDLE_HISTORY_CHANGED_EVENT = 1;
    private static final int HANDLE_CHANNEL_STATE_CHANGED_EVENT = 2;
    private static final int HANDLE_ALLJOYN_ERROR_EVENT = 3;
    public static final int HANDLE_NICK_CHANGE_EVENT= 4 ;
    
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case HANDLE_APPLICATION_QUIT_EVENT:
	            {
	                Log.i(TAG, "mHandler.handleMessage(): HANDLE_APPLICATION_QUIT_EVENT");
	                finish();
	            }
	            break; 
            case HANDLE_HISTORY_CHANGED_EVENT:
                {
                    Log.i(TAG, "mHandler.handleMessage(): HANDLE_HISTORY_CHANGED_EVENT");
                    updateHistory();
                    break;
                }
            case HANDLE_CHANNEL_STATE_CHANGED_EVENT:
	            {
	                Log.i(TAG, "mHandler.handleMessage(): HANDLE_CHANNEL_STATE_CHANGED_EVENT");
	                updateChannelState();
	                break;
	            }
            case HANDLE_ALLJOYN_ERROR_EVENT:
	            {
	                Log.i(TAG, "mHandler.handleMessage(): HANDLE_ALLJOYN_ERROR_EVENT");
	                alljoynError();
	                break;
	            }
            case HANDLE_NICK_CHANGE_EVENT :
            {
                Log.i(TAG, "mHandler.handleMessage(): HANDLE_NICK_CHANGE_EVENT");
                updateNick();
                break;
            }
            default:
                break;
            }
        }
    };
    private void updateNick(){
    	Log.i(TAG,"updateNick() called");
    	String nick=mChatApplication.getNickName();
    	  mChannelNick.setText(nick);
    }
    
    
    private ChatApplication mChatApplication = null;
    private TextView mChannelNick;
    private ArrayAdapter<String> mHistoryList;
    
    private Button mJoinButton;
    private Button mLeaveButton;
    
    private TextView mChannelName;
      
    private TextView mChannelStatus;
    
    private TelephonyManager telManager; 
    
    public static ComponentName mRunningService;

}
