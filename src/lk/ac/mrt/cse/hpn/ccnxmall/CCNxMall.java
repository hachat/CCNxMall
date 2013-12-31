package lk.ac.mrt.cse.hpn.ccnxmall;

import java.io.IOException;
import java.util.ArrayList;

import org.ccnx.ccn.impl.support.Log;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

public class CCNxMall {

	
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
	
	public CCNxMall(){
		_contentStore = new CCNxMallContentStore();
		_networkHandler = new CCNxMallNE();
	}
	
	public boolean initialize(String domain){
		boolean result;
		try {
			result = _networkHandler.setupNetwork();
			if(!result){
				Log.severe("Could not setup Network");
				return false;
			}
			ContentName namespace;
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
				namespace = ContentName.fromNative(domain);
				_networkHandler.registerNames(namespace,_contentStore.getContentList());
			} catch (MalformedContentNameStringException e) {
				Log.severe("Could not register given domain" + domain);
				e.printStackTrace();
				return false;
			}
			
			
		} catch (IOException e) {
			Log.severe("Could not expose available content to the CCNx network.");
			e.printStackTrace();
			return false;
		}
		Log.info("Registered Namespace {0} successfully",domain);
		return true;
	}
	
	public void syncContentStore(String prefix){
		ArrayList<ContentName> names;
		names = _networkHandler.getContentListFromNetwork(prefix);
		if(names != null){
			for(ContentName name: names){
				System.out.println("Received: " + name);
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		CCNxMall mall = new CCNxMall();
		

		mall.initialize("ccnx:/mall");
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mall.syncContentStore("ccnx:/mall");
	}

}
