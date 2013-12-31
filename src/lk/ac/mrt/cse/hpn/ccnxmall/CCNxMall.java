package lk.ac.mrt.cse.hpn.ccnxmall;

import java.io.IOException;
import java.util.ArrayList;

import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.impl.support.Log;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

public class CCNxMall {

	
	/**
	 * Domain of the hosting content.
	 */
	ContentName _namespace;
	
	/**
	 * Incoming CCNx request handler.
	 */
	protected CCNHandle _incomingHandle;
	
	/**
	 * Outgoing CCNx response handler.
	 */
	protected CCNHandle _outgoingHandle;
	
	/**
	 * Ones the other party knows that we have a particular file,
	 * it will request that file by name. this is the place where we 
	 * reply with the actual content for such requests
	 */
	protected CCNxMallInterestHandler _contentInterestResponder;
	
	
	/**
	 * Once we know that we don't have a particular file
	 * which is interesting, and it is available in the network,
	 * we request it an put it in our content store.
	 * _contentCollector is responsible for that.
	 */
	protected CCNxMallContentHandler _contentCollector;
	
	
	
	/**
	 * Content store keeps all messages available in this Node.
	 * It can provide ArrayList of ContentNames available, 
	 * and can give a named content.
	 * 
	 * When new content is found via the network, this should be
	 * updated with the given content, and register those back
	 * to be served.
	 */
	protected CCNxMallContentStore _contentStore;
	
	/**
	 * This handles all the network stuff.
	 */
	protected CCNxMallNE _networkHandler;
	
	/**
	 * For graceful termination.
	 */
	private volatile static boolean keepOn = true; 
	
	public CCNxMall(){
		Runtime.getRuntime().addShutdownHook(new RunWhenShuttingDown()); 
		
		_networkHandler = new CCNxMallNE();
	}
	
	public boolean initialize(String domain,String _rootFolder){
		boolean result;
		
		try {
			
			if (!domain.startsWith("/")){
				if(domain.startsWith("ccnx:")){
					domain = domain.substring(5);
				}
			}
			else{
				Log.severe("strings must begin with a / or ccnx:/");
				return false;
			}
			_namespace = ContentName.fromNative(domain);
		} catch (MalformedContentNameStringException e) {
			Log.severe("Could not register given domain" + domain);
			e.printStackTrace();
			return false;
		}
			
		try {
			_incomingHandle = CCNHandle.open();
			_outgoingHandle = CCNHandle.open();
		} catch (ConfigurationException e2) {
			Log.severe("ConfigurationException occured while opening handle");
			e2.printStackTrace();
		} catch (IOException e2) {
			Log.severe("IOException occured while opening handle");
			e2.printStackTrace();
		}
		
		//Create the content store. Which provides the list of available content
		_contentStore = new CCNxMallContentStore(_rootFolder);
		
		
		//Register Content Response Handler
		_contentInterestResponder = new CCNxMallInterestHandler(_incomingHandle,_namespace, _rootFolder);
		try {
			_incomingHandle.registerFilter(_namespace, _contentInterestResponder);
		} catch (IOException e1) {
			Log.severe("Could not register the _contentInterestResponder to the network");
			e1.printStackTrace();
			return false;
		}
		
		//Register NameEnumurator
		try {
			result = _networkHandler.setupNetwork(_incomingHandle,_outgoingHandle);
			if(!result){
				Log.severe("Could not setup Network");
				return false;
			}		
			_networkHandler.registerNames(_namespace,_contentStore.getContentList());

			
		} catch (IOException e) {
			Log.severe("Could not expose available content to the CCNx network.");
			e.printStackTrace();
			return false;
		}
		
		//Initialize _contentCollector
		_contentCollector = new CCNxMallContentHandler(_outgoingHandle,_rootFolder);

		Log.info("Registered Namespace {0} successfully",domain);
		return true;
	}
	
	
	public boolean shutdownNetwork(){
		Log.info("Starting shutdownNetwork");
		
		
		//Careful on _contentInterestResponder this should available for some time as a service
		
		if (null != _incomingHandle)
			_incomingHandle.close();
		if (null != _outgoingHandle)
			_outgoingHandle.close();
		
		//TODO: Never Used a key.!
		//KeyManager.closeDefaultKeyManager();
		
		Log.info("Completed shutdownNetwork");
		return true;
	}
	
	public void syncContentStore(String prefix){
		ArrayList<ContentName> names;
		
		//Get available names from the network
		names = _networkHandler.getContentListFromNetwork(prefix);
		if(names != null){
			for(ContentName name: names){
				System.out.println("Need to get: " + prefix + name);
				if(!_contentStore.checkAvailability(name.toString().substring(1))){
				//Grab each content and save it
				_contentCollector.getContentAndStore(prefix + name);
				}
			}
		}
		
		//Re-publish the content
		//TODO Look at any versioning with this request.
		try {
			_networkHandler.registerNames(_namespace,_contentStore.getContentList());
		} catch (IOException e) {
			Log.severe("IOException while re-registering names");
			e.printStackTrace();
		}

	}
	
	
	private static void printUsage() {
		// TODO Auto-generated method stub
		System.out.println("Usage: CCNxMall <URI> <MessagesDirectory>");
		System.out.println("       CCNxMall ccnx:/mall ~/mall_messages");
		System.out.println(" mall_messages directory should consist of messages saved in .txt files");
	}
	
 
	//~ Inner Classes ----------------------------------------------------------------------------- 

	public class RunWhenShuttingDown extends Thread { 
		public void run() { 
			System.out.println("Shutting down..."); 
			keepOn = false; 
			try { 
				Thread.sleep(2000); 
			} 
			catch (InterruptedException e) { 
				e.printStackTrace(); 
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		CCNxMall mall = new CCNxMall();

		if(args.length < 2){
			printUsage();
			return;
		}
		
		String directory = args[1];
		if(!directory.endsWith("/")){
			directory = directory.concat("/");
		}		
		System.out.println("Namespace:" + args[0]);
		System.out.println("Message Store:" + args[1]);
		
		mall.initialize(args[0],args[1]);
		
		while(keepOn){
		try {
			mall.syncContentStore(args[0]);
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			mall.shutdownNetwork();
			return;
		}
		}
		
	}

}
