package lk.ac.mrt.cse.hpn.ccnxmall;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.ccnx.ccn.CCNContentHandler;
import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.impl.support.Log;
import org.ccnx.ccn.protocol.ContentObject;
import org.ccnx.ccn.protocol.Interest;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

public class CCNxMallContentHandler implements CCNContentHandler{

	
	/**
	 * Requested content handler.
	 */
	protected CCNHandle _clientHandle;
	
	protected String _rootDirectory;
	
	
	public CCNxMallContentHandler(CCNHandle _outgoingHandle, String _rootFolder) {
		
		_clientHandle = _outgoingHandle;
		_rootDirectory = _rootFolder;
	}

	@Override
	public Interest handleContent(ContentObject data, Interest interest) {
		
		String filename;
		Log.info("Start handleContent");

		Log.info("Got data response: " + interest + " data: " + data);

		filename = interest.getContentName().toURIString();
		filename = _rootDirectory + filename.substring(filename.lastIndexOf("/")+1);
		Log.info("File Name: " + filename);
		FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(filename);
				
				byte [] buffer;
				
				buffer = data.content();
				try {
					fos.write(buffer);
					fos.close();
				} catch (IOException e) {
					Log.info("File could not be written at" + filename);
					e.printStackTrace();
				}				 
			} catch (FileNotFoundException e) {
				Log.info("File could not be created as" + filename);
				e.printStackTrace();
			}
		
		
		Log.info("Completed handleContent");
		return null;
	}

    /**
     * @param _ccnxURI :String ccnx address of the content
     *  which is required to be picked and stored in the store 
     *  
     *  Once the missing content is being identified, request and store
     *  those in the COntent Store one by one using this method
     */
	
	public void getContentAndStore(String _ccnxURI) {
		Log.info("Start getContentAndStore");
		
		Interest _interest = null;
		try {
			_interest = new Interest(_ccnxURI);
		} catch (MalformedContentNameStringException e) {
			Log.severe(_ccnxURI + " is malformed");
			e.printStackTrace();
		}
		
		try {
			_clientHandle.expressInterest(_interest, this);
		} catch (IOException e) {
			Log.severe("IOException in expressing internet");
			e.printStackTrace();
		}
		Log.info("Completed getContentAndStore");
	}

}
