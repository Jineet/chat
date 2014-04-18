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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alljoyn.bus.BusException;




@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class DialogBuilder {
    private static final String TAG = "chat.Dialogs";
    public DialogBuilder(Handler handler){
    	mHandler=handler;
    }
    public Dialog createUseJoinDialog(final Activity activity, final ChatApplication application)  {
    	
    	Log.i(TAG, "createUseJoinDialog()");
    	
    	
    	
    	final Dialog dialog = new Dialog(activity);
    	dialog.requestWindowFeature(dialog.getWindow().FEATURE_NO_TITLE);
    	dialog.setContentView(R.layout.usejoindialog);
    	
        final ArrayAdapter<String> channelListAdapter = new ArrayAdapter<String>(activity, android.R.layout.test_list_item);
    	final ListView channelList = (ListView)dialog.findViewById(R.id.useJoinChannelList);
        channelList.setAdapter(channelListAdapter);
        
	    List<String> channels = application.getFoundChannels();
        for (String channel : channels) {
        	int lastDot = channel.lastIndexOf('.');
        	if (lastDot < 0) {
        		continue;
        	}
            channelListAdapter.add(channel.substring(lastDot + 1));
        }
	    channelListAdapter.notifyDataSetChanged();
    	
    	channelList.setOnItemClickListener(new ListView.OnItemClickListener() {
    		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    			String name = channelList.getItemAtPosition(position).toString();
				application.useSetChannelName(name);
				application.useJoinChannel();
				/*
				 * Android likes to reuse dialogs for performance reasons.  If
				 * we reuse this one, the list of channels will eventually be
				 * wrong since it can change.  We have to tell the Android
				 * application framework to forget about this dialog completely.
				 */
    			
    			activity.showDialog(UseActivity.DIALOG_NICK_ID);
    			activity.removeDialog(UseActivity.DIALOG_JOIN_ID);
    			
    		}
    	});
    	        	           
    	Button cancel = (Button)dialog.findViewById(R.id.useJoinCancel);
    	cancel.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View view) {
    			if(application.getFlag()==false){
    			application.useLeaveChannel();
    			}
				/*
				 * Android likes to reuse dialogs for performance reasons.  If
				 * we reuse this one, the list of channels will eventually be
				 * wrong since it can change.  We have to tell the Android
				 * application framework to forget about this dialog completely.
				 */
    			activity.removeDialog(UseActivity.DIALOG_JOIN_ID);
    		}
    	});
    	
    	Button refresh = (Button)dialog.findViewById(R.id.useJoinRefersh);
    	refresh.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View view) {
    			List<String> channels = application.getFoundChannels();
    	        for (String channel : channels) {
    	        	int lastDot = channel.lastIndexOf('.');
    	        	if (lastDot < 0) {
    	        		continue;
    	        	}
    	            channelListAdapter.add(channel.substring(lastDot + 1));
    	            Log.i(TAG,"refresh called"+channel.substring(lastDot + 1));
    	        }
    	        
    		    channelListAdapter.notifyDataSetChanged();
    		}
    	});
    	
    	return dialog;
    }
    
    public Dialog createUseLeaveDialog(Activity activity, final ChatApplication application) {
       	Log.i(TAG, "createUseLeaveDialog()");
    	final Dialog dialog = new Dialog(activity);
    	dialog.requestWindowFeature(dialog.getWindow().FEATURE_NO_TITLE);
    	dialog.setContentView(R.layout.useleavedialog);
	        	       	
    	Button yes = (Button)dialog.findViewById(R.id.useLeaveOk);
    	yes.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View view) {
    			application.useLeaveChannel();
    			application.useSetChannelName("Not set");
    			dialog.cancel();
    		}
    	});
	            
    	Button no = (Button)dialog.findViewById(R.id.useLeaveCancel);
    	no.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View view) {
    			dialog.cancel();
    		}
    	});
    	
    	return dialog;
    }
    
    public Dialog createHostNameDialog(final Activity activity, final ChatApplication application) {
       	Log.i(TAG, "createHostNameDialog()");
    	final Dialog dialog = new Dialog(activity);
    	dialog.requestWindowFeature(dialog.getWindow().FEATURE_NO_TITLE);
    	dialog.setContentView(R.layout.hostnamedialog);
    	
        final EditText channel = (EditText)dialog.findViewById(R.id.hostNameChannel);
        channel.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                	String name = view.getText().toString();
                	if(name.equals("")){
                		Dialog d= createNickErrorDialog(activity);
                		d.show();
                	}
                	else{
    				application.hostSetChannelName(name);
    				application.hostInitChannel();
        			dialog.cancel();
                    }
              }
                return true;
            }
        });
    	
        Button okay = (Button)dialog.findViewById(R.id.hostNameOk);
        okay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	String name = channel.getText().toString();
            	if(name.equals("")){
            		Dialog d= createNickErrorDialog(activity);
            		d.show();
            	}
            	else{
				application.hostSetChannelName(name);
				application.hostInitChannel();
    			dialog.cancel();
            }
           } 	
        });
        
        Button cancel = (Button)dialog.findViewById(R.id.hostNameCancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        
        return dialog;
    }
    
    
    
    public Dialog createHostNickDialog(final Activity activity, final ChatApplication application) {
       	Log.i(TAG, "createHostNickDialog()");
    	final Dialog dialog = new Dialog(activity);
    	dialog.requestWindowFeature(dialog.getWindow().FEATURE_NO_TITLE);
    	dialog.setContentView(R.layout.hostnickdialog);
    	
        final EditText channel = (EditText)dialog.findViewById(R.id.editTextNick);
        
        channel.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                	String name = view.getText().toString();
                	ArrayList<String> selectedDevices;
                	String[] selD=null;
                	if(application.getFlag()==false){
                	try {
						 selD= AllJoynService.mGroupInterface.getMem();
						 Log.i(TAG,"selD set");
					     } catch (BusException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					    }
                	     selectedDevices= new ArrayList<String>(Arrays.asList(selD));
                	}
                	else{
                		selectedDevices= new ArrayList<String>();
                	}
               	    if(selectedDevices.contains(name)){
                		Dialog d= createNickErrorDialog(activity);
                		d.show();
                		application.useLeaveChannel();
            			application.useSetChannelName("Not set");
            			dialog.cancel();
                	} 
               	    else{
                	application.setNickName(name);
                	Message message = mHandler.obtainMessage(UseActivity.HANDLE_NICK_CHANGE_EVENT);
                    mHandler.sendMessage(message);
                    Log.i(TAG,"Handler message"+message);
                    
                	if(application.getFlag()==false){
                		try {
							AllJoynService.sendNick(name);
						} catch (BusException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                	}
                	else
                	{
                		if(!AllJoynMasterService.nicks.contains(name))
                		AllJoynMasterService.nicks.add(name);
                	}
    				//validate method to be called here
                	dialog.cancel();
               	    }
                }
                return true;
            }
        });
    	
        Button okay = (Button)dialog.findViewById(R.id.buttonOkNick);
        okay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	String name = channel.getText().toString();
            	ArrayList<String> selectedDevices;
            	String[] selD=null;
            	if(application.getFlag()==false){
            	try {
            		
					 selD= AllJoynService.mGroupInterface.getMem();
					 Log.i(TAG,"selD set" + selD[0]);
				     } catch (BusException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				    }
            	     selectedDevices= new ArrayList<String>(Arrays.asList(selD));
            	}
            	else{
            		selectedDevices= new ArrayList<String>();
            	}
            	if(selectedDevices.contains(name)||name.equals("")){
            		Dialog d= createNickErrorDialog(activity);
            		d.show();
            		application.useLeaveChannel();
        			application.useSetChannelName("Not set");
        			dialog.cancel();
            	} 
            	else{
            	application.setNickName(name);
            	Message message = mHandler.obtainMessage(UseActivity.HANDLE_NICK_CHANGE_EVENT);
                mHandler.sendMessage(message);
                Log.i(TAG,"Handler message"+message);
                
            	if(application.getFlag()==false){
            		try {
						AllJoynService.sendNick(name);
					} catch (BusException e) {
					
						e.printStackTrace();
					}
            	}
            	else
            	{
            		AllJoynMasterService.nicks.add(name);
            	}
				//validate method to be called here
    			dialog.cancel();
            	}
            }
        });
        
        Button cancel = (Button)dialog.findViewById(R.id.buttonOkNick2);
        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        
        return dialog;
    }
    public Dialog createHostStartDialog(final Activity activity, final ChatApplication application) {
       	Log.i(TAG, "createHostStartDialog()");
    	final Dialog dialog = new Dialog(activity);
    	dialog.requestWindowFeature(dialog.getWindow().FEATURE_NO_TITLE);
    	dialog.setContentView(R.layout.hoststartdialog);
	        	       	
    	Button yes = (Button)dialog.findViewById(R.id.hostStartOk);
    	yes.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View view) {
    			application.hostStartChannel();
    			activity.showDialog(HostActivity.DIALOG_NICK1_ID);
    			dialog.cancel();
    		}
    	});
	            
    	Button no = (Button)dialog.findViewById(R.id.hostStartCancel);
    	no.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View view) {
    			dialog.cancel();
    		}
    	});
    	
    	return dialog;
    }

    public Dialog createHostStopDialog(Activity activity, final ChatApplication application) {
       	Log.i(TAG, "createHostStopDialog()");
    	final Dialog dialog = new Dialog(activity);
    	dialog.requestWindowFeature(dialog.getWindow().FEATURE_NO_TITLE);
    	dialog.setContentView(R.layout.hoststopdialog);
	        	       	
    	Button yes = (Button)dialog.findViewById(R.id.hostStopOk);
    	yes.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View view) {
    			application.hostStopChannel();
    			dialog.cancel();
    		}
    	});
	            
    	Button no = (Button)dialog.findViewById(R.id.hostStopCancel);
    	no.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View view) {
    			dialog.cancel();
    		}
    	});
    	
    	return dialog;
    }
    
    public Dialog createAllJoynErrorDialog(Activity activity, final ChatApplication application) {
       	Log.i(TAG, "createAllJoynErrorDialog()");
    	final Dialog dialog = new Dialog(activity);
    	dialog.requestWindowFeature(dialog.getWindow().FEATURE_NO_TITLE);
    	dialog.setContentView(R.layout.alljoynerrordialog);
    	
    	TextView errorText = (TextView)dialog.findViewById(R.id.errorDescription);
        errorText.setText(application.getErrorString());
	        	       	
    	Button yes = (Button)dialog.findViewById(R.id.errorOk);
    	yes.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View view) {
    			dialog.cancel();
    		}
    	});
    	
    	return dialog;
    }
   
    public Dialog createNickErrorDialog(Activity activity) {
       	Log.i(TAG, "createAllJoynErrorDialog()");
    	final Dialog dialog = new Dialog(activity);
    	dialog.requestWindowFeature(dialog.getWindow().FEATURE_NO_TITLE);
    	dialog.setContentView(R.layout.hostnickokdialog);
    	
    	TextView errorText = (TextView)dialog.findViewById(R.id.textView1);
        
	        	       	
    	Button yes = (Button)dialog.findViewById(R.id.button1);
    	yes.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View view) {
    			dialog.cancel();
    		}
    	});
    	
    	return dialog;
    } 
    
    public Dialog createHostNickDialog1(final Activity activity, final ChatApplication application) {
       	Log.i(TAG, "createHostNickDialog()");
    	final Dialog dialog = new Dialog(activity);
    	dialog.requestWindowFeature(dialog.getWindow().FEATURE_NO_TITLE);
    	dialog.setContentView(R.layout.hostnickdialog);
    	
        final EditText channel = (EditText)dialog.findViewById(R.id.editTextNick);
        
        channel.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                	String name = view.getText().toString();
                	if(name.equals("")){
                		Dialog d= createNickErrorDialog(activity);
                		d.show();
                		
                	}
                	else{
                	Intent intent=new Intent("joindialog");
                	activity.sendBroadcast(intent);
                	Log.i(TAG,"intent sent");
                	application.setNickName(name);
                	Message message = mHandler.obtainMessage(UseActivity.HANDLE_NICK_CHANGE_EVENT);
                    mHandler.sendMessage(message);
                    Log.i(TAG,"Handler message"+message);
                    
                	if(application.getFlag()==false){
                		try {
							AllJoynService.sendNick(name);
						} catch (BusException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                	}
                	else
                	{
                		if(!AllJoynMasterService.nicks.contains(name))
                		AllJoynMasterService.nicks.add(name);
                	}
    				//validate method to be called here
                	dialog.cancel();
               	    }
                }
                return true;
            }
        });
    	
        Button okay = (Button)dialog.findViewById(R.id.buttonOkNick);
        okay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	String name = channel.getText().toString();
            	if(name.equals("")){
            		Dialog d= createNickErrorDialog(activity);
            		d.show();
            	}
            	else{
            	application.setNickName(name);
            	
            	Intent intent=new Intent("joindialog");
            	activity.sendBroadcast(intent);
            	Log.i(TAG,"intent sent");
            	Message message = mHandler.obtainMessage(UseActivity.HANDLE_NICK_CHANGE_EVENT);
                mHandler.sendMessage(message);
                Log.i(TAG,"Handler message"+message);
                
            	if(application.getFlag()==false){
            		try {
						AllJoynService.sendNick(name);
					} catch (BusException e) {
					
						e.printStackTrace();
					}
            	}
            	else
            	{
            		AllJoynMasterService.nicks.add(name);
            	}
				//validate method to be called here
    			dialog.cancel();
            	}
            }
        });
        
        Button cancel = (Button)dialog.findViewById(R.id.buttonOkNick2);
        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        
        return dialog;
    }
    
   Handler mHandler;
}