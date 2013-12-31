package lk.ac.mrt.cse.hpn.ccnxmall;

import java.io.File;
import java.util.ArrayList;

import org.ccnx.ccn.impl.support.Log;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

public class CCNxMallContentStore {

	protected File _rootDirectory;
	protected ArrayList<ContentName> _contentList;
	
	public CCNxMallContentStore(String rootDirectory){
		
		_rootDirectory = new File(rootDirectory);
		
	}
	
	public ArrayList<ContentName> getContentList(){
		Log.info("Start getContentList");
		
		_contentList = new ArrayList<ContentName>();
		
		if (!_rootDirectory.exists() || !_rootDirectory.isDirectory()) {
			Log.info("nothing to enumerate in content store");
			return null;
		}
		
		String[] listOfFiles = _rootDirectory.list();
		try {
			for (String filename:listOfFiles){
				Log.info("Got content: {0}",filename);
				_contentList.add(ContentName.fromNative("/" + filename));
			}
		} catch (MalformedContentNameStringException e) {
			Log.warning("Content names malformed.");
			e.printStackTrace();
			return null;
		}
		
		Log.info("Complete getContentList");
		return _contentList;
	}
	
	public boolean checkAvailability(String filename){
		Log.info("Start checkAvailability {0}",filename);
		
		String[] listOfFiles = _rootDirectory.list();
		
		for(String file: listOfFiles){
			if(file.equals(filename)){
				Log.info("Complete checkAvailability {0}",true);
				return true;
			}
		}
		Log.info("Complete checkAvailability {0}",false);
		return false;
	}
}
