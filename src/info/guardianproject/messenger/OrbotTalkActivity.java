package info.guardianproject.messenger;

import info.guardianproject.messenger.R;
import info.guardianproject.messenger.server.OrbotTalkService;
import info.guardianproject.net.SocksHttpClient;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class OrbotTalkActivity extends Activity {
	
	SocksHttpClient hClient = null;
	
	private Context mContext = null;
	
	private final static String TAG = "ORTALK";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        restoreFromBundle(savedInstanceState);
		
        stopTalkService();
        startTalkService();
        
        mContext = this;
        
        hClient = new SocksHttpClient ();
    }
    
    

	@Override
	protected void onStart() {
		super.onStart();
		
		Button btnSend = ((Button)findViewById(R.id.btn_send_msg));
		btnSend.setOnClickListener(new OnClickListener ()
		{
			
				public void onClick(View view)
				{
					sendMessage();
				}
		});
		
		Button btnPing = ((Button)findViewById(R.id.btn_ping));
		btnPing.setOnClickListener(new OnClickListener ()
		{
			
				public void onClick(View view)
				{
					sendPing();
				}
		});
	
		((EditText)findViewById(R.id.txt_from)).setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				
				if (!hasFocus)
				{
					saveSettings();
				}
				
			}
			
		});
		
		((EditText)findViewById(R.id.txt_to)).setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				
				if (!hasFocus)
				{
					saveSettings();
				}
				
			}
			
		});
	}
	
	
	
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		restoreFromBundle(savedInstanceState);
		
	}
	
	private void restoreFromBundle (Bundle savedInstanceState)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		
		EditText etFrom = (EditText)findViewById(R.id.txt_from);
		EditText etTo = (EditText)findViewById(R.id.txt_to);
		EditText etMsg = (EditText)findViewById(R.id.txt_msg);

		if (etFrom.getText().length() == 0)
		{
			String from = null;
			
			if (savedInstanceState != null)
					from = savedInstanceState.getString("from");
			else
					from = prefs.getString("from", "");
			
			if (from != null && from.length() > 0)
				etFrom.setText(from);
		}
		
		if (etTo.getText().length() == 0)
		{
			String to = null;
			
			if (savedInstanceState != null)
					to = savedInstanceState.getString("to");
			else
					to = prefs.getString("to", "");
			
			if (to != null && to.length() > 0)
				etTo.setText(to);
		}
		
		if (etMsg.getText().length() == 0)
		{
			if (savedInstanceState != null)
			{
				String msg = savedInstanceState.getString("msg");
				if (msg != null && msg.length() > 0)
					etMsg.setText(msg);
			}
		}
    	
	}



	@Override
	protected void onResume() {
		super.onResume();
		
		
		String action = getIntent().getAction();
		
		if (action != null)
		{
			if (action.equals("info.guardianproject.messenger.DISPLAY_MESSAGE"))
			{
				restoreFromBundle(null);
				
				String sender = getIntent().getExtras().getString("sender");
				String msg = getIntent().getExtras().getString("msg");
	
				
				((EditText)findViewById(R.id.txt_to)).setText(sender);
		    	((EditText)findViewById(R.id.txt_msg)).setText(msg);
			}
			else if (action.equals("android.intent.action.SEND"))
			{
				Intent intent = getIntent();
				
				
				String text = intent.getStringExtra(Intent.EXTRA_TEXT);
	            
	            String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);

	            String type = intent.getType();
                Uri stream = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);

                String msg = type + "\n" + stream.toString();
		    	((EditText)findViewById(R.id.txt_msg)).setText(msg);

			}
		}
	}



	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		String from = ((EditText)findViewById(R.id.txt_from)).getText().toString();
		String to = ((EditText)findViewById(R.id.txt_to)).getText().toString();
    	String msg = ((EditText)findViewById(R.id.txt_msg)).getText().toString();
    	
    	outState.putString("from", from);
    	outState.putString("to", to);
    	outState.putString("msg", msg);
    	
	}

	
	private void requestHiddenService ()
	{
		Intent nIntent = new Intent();
		nIntent.setAction("org.torproject.android.REQUEST_HS_PORT");
		nIntent.putExtra("hs_port", AppConstants.ORTALK_PORT);
		startActivityForResult(nIntent, AppConstants.ORTALK_PORT);
		
	}
	
	private void requestStartOrbot ()
	{
		Intent nIntent = new Intent();
		nIntent.setAction("org.torproject.android.START_TOR");
		startActivity(nIntent);
		
	}

	private void startTalkService ()
	{
		 Intent svc = new Intent(this, OrbotTalkService.class);
	     startService(svc);
	}
	
	private void stopTalkService ()
	{
		 Intent svc = new Intent(this, OrbotTalkService.class);
	     stopService(svc);
	}
	
	private void saveSettings ()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Editor pEdit = prefs.edit();
		
		pEdit.putString("from",((EditText)findViewById(R.id.txt_from)).getText().toString());
		pEdit.putString("to",((EditText)findViewById(R.id.txt_to)).getText().toString());

		pEdit.commit();

	}
	
	
    public void sendMessage ()
    {
    	
    	saveSettings();
    	
				
    	Thread thread = new Thread () {
    		
    		public void run ()
    		{
    	
    			String from = ((EditText)findViewById(R.id.txt_from)).getText().toString();

    			String to = ((EditText)findViewById(R.id.txt_to)).getText().toString();
    			String msg = ((EditText)findViewById(R.id.txt_msg)).getText().toString();
    	
    			if (to.indexOf(".")==-1)
    			{
    				to += ".onion:" + AppConstants.ORTALK_PORT;
    			}
    			else if (to.indexOf(":")==-1)
    			{
    				to += ":" + AppConstants.ORTALK_PORT;
    			}
    			
		    	try {
		    	

		    		from = java.net.URLEncoder.encode(from, "UTF-8");
		    		msg = java.net.URLEncoder.encode(msg, "UTF-8");
		    	
		    		String url = "http://" + to + "/talk?sender=" + from + "&msg=" + msg;
		    	
		    		Log.i(TAG,"exec url: " + url);

		    		updateStatus("Sending message...");
		    		
		    		HttpGet doGet = new HttpGet(url);
		   
					hClient.execute(doGet);
					
					updateStatus("Sending message... success!");
					
		    		
				} catch (ClientProtocolException e) {
					updateStatus("err: " + e.getMessage());
					
					e.printStackTrace();
				} catch (IOException e) {
					updateStatus("error: " + e.getMessage());
					e.printStackTrace();
				}
    		}
    	};
    	
    	thread.start();
    	
    }
    
    public void sendPing ()
    {
    	
    	Thread thread = new Thread () {
    		
    		public void run ()
    		{
    	
    			String from = ((EditText)findViewById(R.id.txt_from)).getText().toString();

    			String to = ((EditText)findViewById(R.id.txt_to)).getText().toString();
    			String msg = ((EditText)findViewById(R.id.txt_msg)).getText().toString();
    	
    			if (to.indexOf(".")==-1)
    			{
    				to += ".onion:" + AppConstants.ORTALK_PORT;
    			}
    			else if (to.indexOf(":")==-1)
    			{
    				to += ":" + AppConstants.ORTALK_PORT;
    			}
    			
		    	try {
		    	

		    		from = java.net.URLEncoder.encode(from, "UTF-8");
		    		msg = java.net.URLEncoder.encode(msg, "UTF-8");
		    	
		    		String url = "http://" + to + "/talk?action-ping";
		    		
		    		Log.i(TAG,"exec url: " + url);
		    		
		    		updateStatus("Sending ping...");
		    		
		    		HttpGet doGet = new HttpGet(url);
		   
					hClient.execute(doGet);
					
					updateStatus("Sending ping... remote user is online.");
					
		    		
				} catch (ClientProtocolException e) {
					updateStatus("Sending ping... protocol err: " + e.getMessage());
					
					e.printStackTrace();
				} catch (IOException e) {
					updateStatus("Sending ping... ioerror: " + e.getMessage());
					e.printStackTrace();
				}
    		}
    	};
    	
    	thread.start();
    	
    }
    
    private void updateStatus (String status)
    {
    	Message msg = mHandler.obtainMessage(STATUS_MSG);
    	msg.getData().putString(HANDLER_STATUS_MSG, status);
    	mHandler.sendMessage(msg);
    }
    
    private static final int STATUS_MSG = 1;
    public final static String HANDLER_STATUS_MSG = "status";

    
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STATUS_MSG:

                	String statusMsg = (String)msg.getData().getString(HANDLER_STATUS_MSG);
                	
        			TextView lblStatus = (TextView)findViewById(R.id.lbl_status);
        			lblStatus.setText(statusMsg);
                	
                    break;
               
                		
                default:
                    super.handleMessage(msg);
            }
        }
        
    };

    public void startScan ()
    {
    	IntentIntegrator.initiateScan(this);
    }
    
    public void displayOnionQRCode (String host)
    {
    	IntentIntegrator.shareText(this, host);
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
	     IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
	     if (scanResult != null) {
	        
	    	 String remoteHostname = scanResult.getContents();
	    	 
	    	 ((EditText)findViewById(R.id.txt_to)).setText(remoteHostname);
	    	
	      }
	     else if (requestCode == AppConstants.ORTALK_PORT)
	     {
	    	 if (intent != null)
	    	 {
		    	 String localHostname = intent.getStringExtra("hs_host");
		    	 
		    	 if (localHostname != null)
		    	 {
		    		 ((EditText)findViewById(R.id.txt_from)).setText(localHostname);
		    	 }
	    	 } 
	    	 
	     }
    // else continue with any other code you need in the method
     }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        MenuItem mItem = null;
        
        
        mItem = menu.add(0, 1, Menu.NONE, "Scan Code");
        
        mItem = menu.add(0, 2, Menu.NONE, "Display Code");
        
        mItem = menu.add(0, 3, Menu.NONE, "Enable Hidden Service");
        
        mItem = menu.add(0, 4, Menu.NONE, "Start Orbot");
       
        return true;
    }
    
    /* When a menu item is selected launch the appropriate view or activity
     * (non-Javadoc)
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
	 */
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		
		super.onMenuItemSelected(featureId, item);
		
		if (item.getItemId() == 1)
		{
			startScan();
		}
		else if (item.getItemId() == 2)
		{
			displayOnionQRCode( ((EditText)findViewById(R.id.txt_from)).getText().toString());
		}
		else if (item.getItemId() == 3)
		{
			requestHiddenService();
		}
		else if (item.getItemId() == 4)
		{
			requestStartOrbot();
		}
		
		
        return true;
	}

    
}