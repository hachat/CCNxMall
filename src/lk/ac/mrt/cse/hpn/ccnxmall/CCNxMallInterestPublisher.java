package lk.ac.mrt.cse.hpn.ccnxmall;

import java.io.IOException;
import java.util.ArrayList;


import org.ccnx.ccn.CCNContentHandler;
import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.impl.support.Log;
import org.ccnx.ccn.profiles.CommandMarker;
import org.ccnx.ccn.profiles.nameenum.BasicNameEnumeratorListener;
import org.ccnx.ccn.profiles.nameenum.CCNNameEnumerator;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.ContentObject;
import org.ccnx.ccn.protocol.Interest;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;
public class CCNxMallInterestPublisher implements CCNContentHandler,Runnable,BasicNameEnumeratorListener{

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CCNxMallInterestPublisher client;
		
		
		try {
			client = new CCNxMallInterestPublisher();
			
			client.requestAvailableContentList();
				
			
		} catch (ConfigurationException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
//		Interest sample_interest = null;	
//		try {
//			sample_interest = new Interest("ccnx:/mall/%C1.E.be");
//			client.sendInterest(sample_interest);
//		} catch (MalformedContentNameStringException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}



	@Override
	public void run() {
		
		
		Interest sample_interest = null;	
		try {
			sample_interest = new Interest("ccnx:/mall");
			sendInterest(sample_interest);
		} catch (MalformedContentNameStringException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	protected CCNHandle _handle;
	protected CCNNameEnumerator _enumHanler;
	
	ArrayList<ContentName> names;
	Object namesLock = new Object();
	
	public CCNxMallInterestPublisher() throws ConfigurationException, IOException{
		_handle = CCNHandle.open();
		_enumHanler = new CCNNameEnumerator(_handle, this);
		
		
		
		
	}
	@Override
	public Interest handleContent(ContentObject data, Interest interest) {
		System.out.println("Got response" + data);
		
		if (interest.name().contains(CommandMarker.COMMAND_MARKER_BASIC_ENUMERATION.getBytes())) {
			//the NEMarker is in the name...  so, find what are the available links
			
		} else {
			//Not a COMMAND_MARKER_BASIC_ENUMERATION response.. so should be a content
		}
		
		return null;
	}

	public void sendInterest(Interest interest){
		try {
			_handle.expressInterest(interest, this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean requestAvailableContentList(){
		
		try {
			_enumHanler.registerPrefix(ContentName.fromNative("/mall"));
		} catch (MalformedContentNameStringException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int attempts = 0;
		try{
			synchronized (namesLock) {
				while(names==null && attempts < 100){
					namesLock.wait(50);
					attempts++;
				}
				//we either broke out of loop or the names are here
				Log.info( "done waiting for results to arrive");
			}
			try {
				_enumHanler.cancelPrefix(ContentName.fromNative("/mall"));
			} catch (MalformedContentNameStringException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		catch(InterruptedException e){
			Log.info("error waiting for names to be registered by name enumeration responder");
		}
		
		if(names ==  null){
			return false;
		}
		else return true;
	}

	@Override
	public int handleNameEnumerator(ContentName prefix,
			ArrayList<ContentName> names) {
		
		Log.info( "got a callback!");

		synchronized (namesLock) {
			names = new ArrayList<ContentName>();
			for (ContentName name : names)
				names.add(name);
			namesLock.notify();
			Log.info("here are the returned names: ");

			for (ContentName cn: names)
				Log.info(cn.toString()+" ("+prefix.toString()+cn.toString()+")");
		}

		return 0;
	}

}
