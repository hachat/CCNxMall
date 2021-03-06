package lk.ac.mrt.cse.hpn.ccnxmall;

import java.io.IOException;
import java.util.ArrayList;

import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.impl.support.Log;
import org.ccnx.ccn.profiles.nameenum.BasicNameEnumeratorListener;
import org.ccnx.ccn.profiles.nameenum.CCNNameEnumerator;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

public class CCNxMallNE implements BasicNameEnumeratorListener{

	/**
	 * Incoming enumerator request handler.
	 */
	protected CCNNameEnumerator _putne;
	protected CCNHandle _putHandle;
	
	
	/**
	 * Outgoing enumerator response handler.
	 */
	protected CCNNameEnumerator _getne;
	protected CCNHandle _getHandle;

	/**
	 * Place to keep list of available names through the network.
	 * this will be filled by handleNameEnumerator method.
	 */
	protected ArrayList<ContentName> receivingNames;
		
	/**
	 * To lock the receiving buffers to avoid being used
	 * while being received.
	 */
	protected Object namesLock = new Object();
	
	
	public CCNxMallNE(){
		
		receivingNames = null;

	}
	
	public boolean setupNetwork(CCNHandle _incomingHandle,CCNHandle _outgoingHandle){
		Log.info("Starting setupNetwork");
				
		
		_putHandle = _outgoingHandle;
		_getHandle = _incomingHandle;
		try {
			_putHandle = CCNHandle.open();
			_getHandle = CCNHandle.open();
		} catch (ConfigurationException | IOException e) {
			Log.severe("Could not open CCNx network handles");
			e.printStackTrace();
			return false;
		}
		
		
		_putne = new CCNNameEnumerator(_putHandle, this);
		_getne = new CCNNameEnumerator(_getHandle, this);
		
		Log.info("Completed setupNetwork");
		return true;
	}

	
	/**
	 * Wrapper for getting list of available content from the network
	 * 
	 */
	public ArrayList<ContentName> getContentListFromNetwork(String prefix){
		Log.info("Startng getContentListFromNetwork");
		String nativePrefix;
		ArrayList<ContentName> list = new ArrayList<ContentName>();
		
		if(prefix.startsWith("ccnx:")){
			nativePrefix = prefix.substring(5);
		}
		else nativePrefix = prefix;
			
		cleanReceivingBuffers();
		
		try {
			Log.info("Registering prefix {0}",nativePrefix);
			registerPrefix(nativePrefix);
			
			waitCallbackFromRegisteredPrefix();
			
		} catch (IOException e) {
			Log.severe("Couldn't convert prefix properly");
			list = null;
			e.printStackTrace();
		}
		
		synchronized (namesLock) {
			if(receivingNames != null){
			for(ContentName name:receivingNames){
				//TODO Check weather this is a complete clone
				list.add(name);
			}
			}
		}
		
		try {
			_getne.cancelPrefix(ContentName.fromNative(nativePrefix));
		} catch (MalformedContentNameStringException e) {
			Log.severe("Couldn't convert prefix properly");
			e.printStackTrace();
		}
		
		Log.info("Completed getContentListFromNetwork");
		return list;
	}
	/**
	 * @param names : ArrayList<ContentName> names of content available
	 * Register the Content Names available in this Node, 
	 * which are ready to be served.
	 */
	public void registerNames(ContentName namespace,ArrayList<ContentName> names) throws IOException{
		Log.info("Starting registerNames");
		ContentName fullName = null;
		_putne.registerNameSpace(namespace);
		
		for(ContentName name: names ){
			fullName = namespace.append(name);
			Log.info("registering name {0}", fullName);
			_putne.registerNameForResponses(fullName);	
		}
		Log.info("registered all names in server");
		
		try{
			//Wait till even the last name is registered
			while(!_putne.containsRegisteredName(fullName)){
				Thread.sleep(10);
			}
			
			//the names are registered...
			Log.info("the names are now registered");
		}
		catch(InterruptedException e){
			Log.warning("Error waiting for names to be registered by name enumeration responder");
		}
		Log.info("Completed registerNames");
	}
	
	
	
	/**
	 * @param interestedPrefix : String e.g. /mall Do not put ccnx:
	 * 
	 * Check the content available in network
	 * Register the prefix which we are interested in enumerating.
	 * This will publish an basic enumeration interest to the given prefix.
	 * Prefix which is interested by the node.
	 * this is used in exploring content from the network
	 * nothing to do with the content available in the same place.
	 */
	public void registerPrefix(String interestedPrefix) throws IOException{
		Log.info("Starting registerPrefix");
		
	    ContentName prefix = null;
		try{
			prefix = ContentName.fromNative(interestedPrefix);
			_getne.registerPrefix(prefix);
		}
		catch(Exception e){
			Log.warning("Could not create ContentName from {0}",interestedPrefix);
		}
		Log.info("Completed registerPrefix");
	}
	
	
	/**
	 * Ones registerPrefix is called, this is the function to be called to make sure
	 * receivingNames are properly filled. then it is safe to read it.
	 */
	public void waitCallbackFromRegisteredPrefix(){
		Log.info("Starting waitCallbackFromRegisteredPrefix");

		int attempts = 1;
		try{
			synchronized (namesLock) {
				while (null == receivingNames && attempts < 500){
					namesLock.wait(50);
					attempts++;
				}
			}
			
			//we either broke out of loop or the names are here
			Log.info("done waiting for results to arrive: attempts " + attempts);
		} catch(InterruptedException e){
			Log.warning("error waiting for names to be registered by name enumeration responder");
		}

		synchronized (namesLock) {
			if(receivingNames != null){
				for (ContentName name: receivingNames){
					Log.info("got name: "+name.toString());
				}
			}
		}
		Log.info("Completed waitCallbackFromRegisteredPrefix");
	}
	
	/**
	 * Call this before publishing a new interest through registerPrefix
	 */
	public void cleanReceivingBuffers(){
		synchronized (namesLock) {
			receivingNames = null;
		}
	}
	
	/**
	 * Callback called when we get a collection matching a registered prefix,
	 * which is registered by void registerPrefix(String interestedPrefix)
	 * see original documentation for this, as per BasicNameEnumeratorListener
	 */
	@Override
	public int handleNameEnumerator(ContentName prefix,
			ArrayList<ContentName> names) {
		
		Log.info( "handleNameEnumerator got a callback!");

		synchronized (namesLock) {
			receivingNames = new ArrayList<ContentName>();
			for (ContentName name : names){
				receivingNames.add(name);				
			}
			namesLock.notify();
			Log.info("here are the returned names: ");
			Log.info("Name Count: {0}", receivingNames.size());
			for (ContentName name: receivingNames){
				Log.info(name.toString()+" ("+prefix.toString()+name.toString()+")");
			}
		}
		Log.info( "Completed handleNameEnumerator");
		return 0;
	}

}
