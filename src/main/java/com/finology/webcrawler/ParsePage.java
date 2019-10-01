package com.finology.webcrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import com.finology.db.Sqlitedb;
/**
 * Processes a single page in a thread.
 */
public class ParsePage implements Callable<ParsePage> {
	static final int TIMEOUT = 60000;   // one minute
	//private static final String USER_AGENT =
    //     "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36";

	private URL url;
	private int depth;
	private Set<URL> urlList = new HashSet<>();

	public ParsePage(URL url, int depth) {
		this.url = url;
		this.depth = depth;
	}

	@Override
	public ParsePage call() throws Exception {
		Document document = null;
		System.out.println(Thread.currentThread().getName()+" Visiting (" + depth + ") :"+ url.toString());
		document = Jsoup.parse(url, TIMEOUT);
		//Document document = Jsoup.connect(url.toString()).userAgent(USER_AGENT).get();
		Elements productNameEl=document.select("[data-ui-id=page-title-wrapper][itemprop=name]");
        String name = productNameEl.text();
        
        // Here we check if there is no name then go to the next page
        if(!name.isEmpty()) {
			Elements productIDEl = document.select("input[type=hidden][name=item]");
			long productID = Long.parseLong(productIDEl.attr("value"));
			
			Elements productPriceEl = document.select("#product-price-"+ productID);
			String productPrice = productPriceEl.text();

			Elements descriptionEl = document.select("#description");
			String description = descriptionEl.text();

			Elements additionalEls = document.select("#additional tr");
			StringBuilder extrainfo = new StringBuilder();
			for (Element tr : additionalEls) {
				extrainfo.append(tr.text() + "|");
			}
			
			if (!Sqlitedb.findProductByOriginid(productID)) {
				Sqlitedb.insert(productID, name, productPrice, description, extrainfo.toString());
				print(name, productPrice, description, extrainfo.toString());
			}
			
        }

		processLinks(document.select("a[href]"));
		
		return this;
	}
	
	private void print(String name, String productPrice, String description, String extrainfo) {
		System.out.println("-------------------------------------------");
		System.out.println("Name: " + name);
		System.out.println("Price: " + productPrice);
		System.out.println("Description: " + description);
		System.out.println("Extra information: " + extrainfo);
		System.out.println("-------------------------------------------");
	}

	private void processLinks(Elements links) {
		links.stream().
				filter(link ->
				{
					String href = link.attr("href");
					return (!href.isEmpty()) && !href.startsWith("#");
				}).
				forEach(link -> {
					try {
						String href = link.attr("href");
						URL nextUrl = new URL(url, href);
						urlList.add(nextUrl);
					} catch (MalformedURLException e) { // ignore bad urls
					}
				});
	}

	public Set<URL> getUrlList() {
		return urlList;
	}

	public int getDepth() {
		return depth;
	}
}
