package lk.ac.mrt.cse.hpn.ccnxmall;


import java.io.IOException;

import org.ccnx.ccn.CCNFilterListener;
import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.impl.support.Log;
import org.ccnx.ccn.profiles.security.KeyProfile;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.Interest;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

@SuppressWarnings("deprecation")
public class CCNxMallInterestListener implements CCNFilterListener {

	protected boolean _finished = false;
	protected CCNHandle _handle;
	protected ContentName _prefix; 
	protected ContentName _responseName = null;
	protected Interest  _interest;
	
	public CCNxMallInterestListener(String listeningPrefix) throws MalformedContentNameStringException, ConfigurationException, IOException{
		_prefix = ContentName.fromURI(listeningPrefix);
	
		_handle = CCNHandle.open();
		//set default response name
		_responseName = KeyProfile.keyName(null, _handle.keyManager().getDefaultKeyID());
	}
	
	public void start() throws IOException{
		Log.info("Starting Mall Prefix server");
		System.out.println("Starting Mall Prefix server");
		// All we have to do is say that we're listening on our main prefix.
		
		_handle.registerFilter(_prefix, this);
	}
	
    /**
     * Turn off everything.
     * @throws IOException 
     */
	public void shutdown() throws IOException {
		if (null != _handle) {
			_handle.unregisterFilter(_prefix, this);
			Log.info("Shutting down MallInterestHandler");
			System.out.println("Shutting down MallInterestHandler");
		}
		_finished = true;
	}
	
	public boolean finished() { 
		return _finished; 
	}

	@Override
	public boolean handleInterest(Interest interest) {
		
		// Alright, we've gotten an interest. Either it's an interest for a stream we're
		// already reading, or it's a request for a new stream.
		Log.info("CCNxMall main responder: got new interest: {0}", interest);
		System.out.println("CCNxMall main responder: got new interest:" + interest.toString());
		// Test to see if we need to respond to it.
		if (!_prefix.isPrefixOf(interest.name())) {
			Log.info("Unexpected: got an interest not matching our prefix (which is {0})", _prefix);
			System.out.println("Unexpected: got an interest not matching our prefix (which is " + interest.toString() + ")");
			
			return false;
		}
		
		return false;
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			CCNxMallInterestListener mallServer = new CCNxMallInterestListener("ccnx:/mall");
			
			// All we need to do now is wait until interrupted.
			mallServer.start();
			
			while (!mallServer.finished()) {
				// we really want to wait until someone ^C's us.
				try {
					Thread.sleep(100000);
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		} catch (Exception e) {
			Log.warning("Exception in ccnFileProxy: type: " + e.getClass().getName() + ", message:  "+ e.getMessage());
			Log.warningStackTrace(e);
			System.err.println("Exception in ccnFileProxy: type: " + e.getClass().getName() + ", message:  "+ e.getMessage());
			e.printStackTrace();
		}
	}
}
