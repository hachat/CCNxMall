package lk.ac.mrt.cse.hpn.ccnxmall.test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import junit.framework.Assert;

import lk.ac.mrt.cse.hpn.ccnxmall.CCNxMallContentStore;

import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;
import org.junit.Test;

public class CCNxMallContentStoreTest {

	@Test
	public void testGetContentList() {
		
		
		CCNxMallContentStore myStore = new CCNxMallContentStore("./../../../../mall_messages/");
		
		ArrayList<ContentName> contentList = myStore.getContentList();
		
		assertNotNull(contentList);
		for(ContentName name: contentList){
			try {
				if(name.equals(ContentName.fromNative("/1.txt")) || name.equals(ContentName.fromNative("/2.txt")) || 
					 name.equals(ContentName.fromNative("/3.txt")) || name.equals(ContentName.fromNative("/4.txt"))){
						assertTrue(true);
					}
				else{
					assertTrue(false);
				}
			} catch (MalformedContentNameStringException e) {
				fail("MalformedContentNameStringException");
				e.printStackTrace();
			}
		}
		
	}

	@Test
	public void testCheckAvailability() {
		
		
		CCNxMallContentStore myStore = new CCNxMallContentStore("./../../../../mall_messages/");
		
		assertTrue(myStore.checkAvailability("1.txt"));
		
		assertFalse(myStore.checkAvailability("175jhg.txt"));
		
	}

}
