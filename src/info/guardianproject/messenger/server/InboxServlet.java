package info.guardianproject.messenger.server;


import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import android.util.Log;

public class InboxServlet extends HttpServlet {

	private OrbotTalkService mService = null;
	
	private final static String TAG = "ORTALK";

	
	public OrbotTalkService getService() {
		return mService;
	}

	public void setService(OrbotTalkService mService) {
		this.mService = mService;
	}

	@Override
	protected long getLastModified(HttpServletRequest req) {
		// TODO Auto-generated method stub
		return super.getLastModified(req);
	}

	@Override
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.service(arg0, arg1);
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		Log.i(TAG,"got get request: " + req.getRemoteAddr() + ":" + req.getRequestURL().toString());

		
		String sender = req.getParameter("sender");
		String msg = req.getParameter("msg");
		
		if (sender != null)
		{
			mService.showToolbarNotification("New Hidden Message", sender, msg);
		}
		
		resp.setContentType("text/plain");	
		resp.getOutputStream().println("ack");
		resp.getOutputStream().flush();
		
		
		
	}
	
	

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doPost(req, resp);
	}

	@Override
	public ServletConfig getServletConfig() {
		// TODO Auto-generated method stub
		return super.getServletConfig();
	}

	@Override
	public String getServletInfo() {
		// TODO Auto-generated method stub
		return super.getServletInfo();
	}

	@Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		super.init();
	}

	
}
