package lk.ac.mrt.cse.hpn.ccnxmall;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import junit.framework.Assert;

import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;
import org.junit.Test;

public class CCNxMallNETest {

	@Test
	public void testNameEnumeration() {
		
		CCNxMallNE _networkHandler = new CCNxMallNE();
		ArrayList<ContentName> _contentList = new ArrayList<ContentName>();
		
		CCNHandle incomingHandle = null;
		CCNHandle outgoingHandle = null;
		
		try {
			incomingHandle = CCNHandle.open();
			outgoingHandle = CCNHandle.open();
			
		} catch (ConfigurationException e2) {
			Assert.fail();
			e2.printStackTrace();
		} catch (IOException e2) {
			Assert.fail();
			e2.printStackTrace();
		}
		
		try {
			_contentList.add(ContentName.fromNative("/testmall/1.txt"));
			_contentList.add(ContentName.fromNative("/testmall/2.txt"));
			
			
		} catch (MalformedContentNameStringException e) {
			assertTrue(false);
		}
		assertTrue(_networkHandler.setupNetwork(incomingHandle,outgoingHandle));
		try {
			_networkHandler.registerNames(ContentName.fromNative("/testmall"),_contentList);
		} catch (MalformedContentNameStringException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		ArrayList<ContentName> names;
		names = _networkHandler.getContentListFromNetwork("/testmall");
		if(names != null){
			for(ContentName name: names){
				System.out.println("Received: " + name);
				Assert.assertTrue(name.toString().equals("/1.txt") || 
						name.toString().equals("/2.txt"));
			}
		}
	else{
		Assert.fail();
	}
			incomingHandle.close();
			outgoingHandle.close();
	}

}
