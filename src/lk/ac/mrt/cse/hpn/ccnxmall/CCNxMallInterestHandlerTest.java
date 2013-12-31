package lk.ac.mrt.cse.hpn.ccnxmall;

import static org.junit.Assert.*;

import java.io.IOException;

import junit.framework.Assert;

import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.impl.support.Log;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;
import org.junit.Test;

public class CCNxMallInterestHandlerTest {

	@Test
	public void test() {
		
		
		CCNHandle _incomingHandle = null;
		CCNHandle _outgoingHandle = null;
		
		ContentName _namespace = null;
		
		CCNxMallInterestHandler _contentInterestResponder = null;
		
		try {
			_incomingHandle = CCNHandle.open();
			_outgoingHandle = CCNHandle.open();
		} catch (ConfigurationException e2) {
			Assert.fail();
			e2.printStackTrace();
		} catch (IOException e2) {
			Assert.fail();
			e2.printStackTrace();
		}
		
		try {
			_namespace = ContentName.fromNative("/testmall");
			
		} catch (MalformedContentNameStringException e) {
			assertTrue(false);
		}
		
		//Register Content Response Handler
		_contentInterestResponder = new CCNxMallInterestHandler(_incomingHandle, _outgoingHandle, _namespace, "../../../../mall_messages/");
		try {
			_incomingHandle.registerFilter(_namespace, _contentInterestResponder);
		} catch (IOException e1) {
			Log.severe("Could not register the _contentInterestResponder to the network");
			e1.printStackTrace();
			Assert.fail("Could not register the _contentInterestResponder");
		}
				
		while(true){
			//Here we assume ~/mall_messages/
			//                     ./1.txt
			//                     ./2.txt
			//                     ./3.txt
			//                     ./4.txt
			//
			//Okay!. now the content is available at the network
			//You can check it manually by running like this
			//from terminal, 
			//	~/ccngetfile ccnx:/testmall/1.txt ./deletethis
			// you will get the content of ./1.txt to ./deletethis file
			//
		}
		//TODO Occurs java.io.IOException: Put(s) with no matching interests - size is 2
		// should look in to it.
	}

}
