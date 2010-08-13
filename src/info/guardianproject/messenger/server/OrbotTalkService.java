package info.guardianproject.messenger.server;

import info.guardianproject.messenger.AppConstants;
import info.guardianproject.messenger.OrbotTalkActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class OrbotTalkService extends Service {

	private MyServ srv = null;
	
	private final static String TAG = "ORTALK";
	private static final int NOTIFY_ID = 1;

	
	private Thread wsThread = null;

    
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	@Override
	public void onCreate() {
		
	
		wsThread = new Thread ()
		{
			public void run ()
			{
				startWebServer();
			}
		};
		
		wsThread.start();
	}
	

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		Log.i(TAG,"service starting");

		
	}

	


	@Override
	public void onDestroy() {
		super.onDestroy();
		
		Log.i(TAG,"service destroy");

	}



	public void startWebServer ()
	{
		Log.i(TAG,"Starting web server");
			
		srv = new MyServ();
		
		String sHost = AppConstants.ORTALK_HOST;
		int sPort = AppConstants.ORTALK_PORT;
		
 		// setting aliases, for an optional file servlet
          Acme.Serve.Serve.PathTreeDictionary aliases = new Acme.Serve.Serve.PathTreeDictionary();
            		//  note cast name will depend on the class name, since it is anonymous class
             srv.setMappingTable(aliases);
		// setting properties for the server, and exchangable Acceptors
		java.util.Properties properties = new java.util.Properties();
		properties.put( MyServ.ARG_PORT, sPort);
		properties.put(MyServ.ARG_BINDADDRESS, sHost);
		
		//properties.setProperty(Acme.Serve.Serve.ARG_NOHUP, "nohup");
		srv.arguments = properties;
	
	//	srv.setHost("locahost");
	//	srv.addDefaultServlets(null); // optional file servlet
		InboxServlet is =  new InboxServlet();
		is.setService(this);
		
		srv.addServlet("/talk",is); // optional
		
		srv.serve();
	
		Log.i(TAG,srv.getServerInfo());

	}

	class MyServ extends Acme.Serve.Serve {
		// Overriding method for public access
                    public void setMappingTable(PathTreeDictionary mappingtable) { 
                          super.setMappingTable(mappingtable);
                    }
                    // add the method below when .war deployment is needed
                    public void addWarDeployer(String deployerFactory, String throttles) {
                          super.addWarDeployer(deployerFactory, throttles);
                    }
            };

            
        protected void showToolbarNotification (String title, String sender, String msg)
    	{
    		
    		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    		
    		CharSequence tickerText = sender + ": " + msg;
    		long when = System.currentTimeMillis();

    		Notification notification = new Notification(android.R.drawable.stat_notify_chat, tickerText, when);
    		
    		Context context = getApplicationContext();
    		
    		Intent nIntent = new Intent(this, OrbotTalkActivity.class);
    		nIntent.setAction("info.guardianproject.messenger.DISPLAY_MESSAGE");
    		nIntent.putExtra("sender", sender);
    		nIntent.putExtra("msg", msg);
    		
    		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, nIntent, 0);

    		notification.defaults |= Notification.DEFAULT_VIBRATE;
    		
    		notification.setLatestEventInfo(context, title, tickerText, contentIntent);

    		mNotificationManager.notify(NOTIFY_ID, notification);

    		

    	}
}
