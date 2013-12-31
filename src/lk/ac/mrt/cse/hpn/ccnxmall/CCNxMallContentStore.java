package lk.ac.mrt.cse.hpn.ccnxmall;

import java.io.File;
import java.util.ArrayList;

import org.ccnx.ccn.impl.support.Log;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

public class CCNxMallContentStore {

	protected File _rootDirectory;
	protected ArrayList<ContentName> _contentList;
	
	public ArrayList<ContentName> getContentList(){
		
		_contentList = new ArrayList<ContentName>();
		
		try {
			_contentList.add(ContentName.fromNative("/mall/1.txt"));
			_contentList.add(ContentName.fromNative("/mall/2.txt"));
			_contentList.add(ContentName.fromNative("/mall/3.txt"));
			_contentList.add(ContentName.fromNative("/mall/4.txt"));
			
		} catch (MalformedContentNameStringException e) {
			Log.warning("Content names malformed.");
			e.printStackTrace();
			return null;
		}
	
		return _contentList;
	}
}
