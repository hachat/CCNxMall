package lk.ac.mrt.cse.hpn.ccnxmall;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.CCNInterestHandler;
import org.ccnx.ccn.impl.support.Log;
import org.ccnx.ccn.io.CCNFileOutputStream;
import org.ccnx.ccn.profiles.VersioningProfile;
import org.ccnx.ccn.protocol.CCNTime;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.Interest;

public class CCNxMallInterestHandler implements CCNInterestHandler{

	
	static int BUF_SIZE = 4096;
	
	protected ContentName _prefix;
	
	protected String _rootDirectory;
	
	/**
	 * Incoming content request handler.
	 */
	protected CCNHandle _putHandle;
	
	/**
	 * Outgoing enumerator response handler.
	 */
	protected CCNHandle _getHandle;
	
	CCNxMallInterestHandler(CCNHandle _incomingHandle,
			CCNHandle _outgoingHandle,
			ContentName _domainPrefix,
			String _contentFolder) {
		
		_prefix = _domainPrefix;
		_putHandle = _outgoingHandle;
		_getHandle = _incomingHandle;
		_rootDirectory = _contentFolder;
		
	}
	@Override
	public boolean handleInterest(Interest interest) {
		
		try {
			sendFileInResponse(interest);
		} catch (IOException e) {
			Log.warning("sendFileInResponse gave an IO exception");
			
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Send the Actual Content in the form of file to the network
	 *  should probably run in a separate thread.
	 * @param outstandingInterest
	 * @throws IOException 
	 */
	
	protected boolean sendFileInResponse(Interest outstandingInterest) throws IOException {
		
		File fileToWrite = ccnNameToFilePath(outstandingInterest.name());
		Log.info("extracted request for file: " + fileToWrite.getAbsolutePath() + " exists? ", fileToWrite.exists());
		if (!fileToWrite.exists()) {
			Log.warning("File {0} does not exist. Ignoring request.", fileToWrite.getAbsoluteFile());
			return false;
		}
		
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(fileToWrite);
		} catch (FileNotFoundException fnf) {
			Log.warning("Unexpected: file we expected to exist doesn't exist: {0}!", fileToWrite.getAbsolutePath());
			return false;
		}
		
		// Set the version of the CCN content to be the last modification time of the file.
		CCNTime modificationTime = new CCNTime(fileToWrite.lastModified());
		ContentName versionedName = 
			VersioningProfile.addVersion(new ContentName(_prefix, 
						outstandingInterest.name().postfix(_prefix).components()), modificationTime);

		// CCNFileOutputStream will use the version on a name you hand it (or if the name
		// is unversioned, it will version it).
		CCNFileOutputStream ccnout = new CCNFileOutputStream(versionedName, _putHandle);
		
		// We have an interest already, register it so we can write immediately.
		ccnout.addOutstandingInterest(outstandingInterest);
		
		byte [] buffer = new byte[BUF_SIZE];
		
		int read = fis.read(buffer);
		while (read >= 0) {
			ccnout.write(buffer, 0, read);
			read = fis.read(buffer);
		} 
		fis.close();
		ccnout.close(); // will flush
		
		return true;
	}
	
	protected File ccnNameToFilePath(ContentName name) {
		
		ContentName fileNamePostfix = name.postfix(_prefix);
		if (null == fileNamePostfix) {
			// Only happens if interest.name() is not a prefix of _prefix.
			Log.info("Unexpected: got an interest not matching our prefix (which is {0})", _prefix);
			return null;
		}

		File fileToWrite = new File(_rootDirectory, fileNamePostfix.toString());
		Log.info("file postfix {0}, resulting path name {1}", fileNamePostfix, fileToWrite.getAbsolutePath());
		return fileToWrite;
	}

}
