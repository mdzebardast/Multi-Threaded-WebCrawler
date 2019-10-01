package com.finology;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.finology.db.Sqlitedb;
import com.finology.webcrawler.ParsePage;


@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class,Sqlitedb.class})
public class ParsePageTest {

	final int TIMEOUT = 60000;
	
	@Before
    public void setUp() throws Exception {

    }
	
	@Test
	public void testParsing() throws Exception {

		/// create a real jsoup document using a test file
		String simpleHTML = new String(Files.readAllBytes(Paths.get(getClass().getResource("/Breathe-Easy_Tank.html").toURI())));
		Document document = Jsoup.parse(simpleHTML);

		// This is our pretend url.
		URL fakeUrl = new URL("http://magento-test.finology.com.my/breathe-easy-tank.html");

		// Invoke powermock to take care of our static Jsoup.connect
		PowerMockito.mockStatic(Jsoup.class);
		PowerMockito.when(Jsoup.parse(fakeUrl, TIMEOUT)).thenReturn(document);
		
		PowerMockito.mockStatic(Sqlitedb.class);
		PowerMockito.when(Sqlitedb.findProductByOriginid(1818L)).thenReturn(false);
		PowerMockito.when(Sqlitedb.insert(anyInt(), anyString(), anyString(), anyString(), anyString())).thenReturn(1);
		// Run the code to be tested
		ParsePage parsePage = new ParsePage(fakeUrl, 1);
		parsePage.call();

		// check the results.
		Set<URL> urls = parsePage.getUrlList();
		assertEquals(44, urls.size());
	}
}
