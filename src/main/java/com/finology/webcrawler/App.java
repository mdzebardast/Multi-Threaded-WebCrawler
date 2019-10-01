package com.finology.webcrawler;

import java.io.IOException;
import java.net.URL;
import java.util.function.BiPredicate;

import com.finology.db.Sqlitedb;

/**
 * Multi Thread Web Crawler!
 *
 */
public class App 
{
    private static int MAX_DEPTH = 5;
    private static int THREAD_COUNT = 5;
    
    public static void main( String[] args )
    {
        System.out.println( "Web Crawler has been started...!" );
        if(args.length >= 1)
        	MAX_DEPTH = Integer.parseInt(args[0]);
        if(args.length >= 2)
        	THREAD_COUNT = Integer.parseInt(args[1]);
        
        try {
        	Sqlitedb.initialiseDatabase();
			startService("http://magento-test.finology.com.my/breathe-easy-tank.html", THREAD_COUNT);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
    }
    
	public static void startService(String startingUrl, int THREAD_COUNT) throws IOException, InterruptedException, Exception {
        URL start = new URL(startingUrl);

        BiPredicate<URL, Integer>
                shouldVisit = (url, depth) -> url.getHost().equals(start.getHost());
        shouldVisit = shouldVisit.and( (url, depth) -> depth < MAX_DEPTH);

        ParseManager parseManager = new ParseManager(shouldVisit, THREAD_COUNT);

        parseManager.go(start);

        System.out.println("Found " + parseManager.getMasterList().size() + " urls");

    }
}
