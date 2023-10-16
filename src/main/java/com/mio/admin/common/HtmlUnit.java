package com.mio.admin.common;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.gargoylesoftware.css.parser.CSSErrorHandler;
import com.gargoylesoftware.css.parser.CSSException;
import com.gargoylesoftware.css.parser.CSSParseException;
import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;

public class HtmlUnit {

	/**
	 * <pre>
	 *	HtmlUnit API 을 통해 page 접근
	 * </pre>
	 * 
	 * @methodName : htmlunitCrawling
	 * @since : 2020. 1. 3.
	 * @author : khham
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public HtmlPage getHtmlPage(WebClient webClient, String url) {
		HtmlPage page = null;

		try {

			url = getUrlFilter(url);

			// htmlUnit 세팅
			// WebClient webClient = new WebClient(BrowserVersion.FIREFOX_60);
			setEventListener(webClient);
			webClient.waitForBackgroundJavaScript(1000);
			webClient.getOptions().setTimeout(5_000); // timeout 시간 지정
			webClient.getOptions().setUseInsecureSSL(true);
			webClient.getOptions().setJavaScriptEnabled(true); // eco호출을 위해 스크립트 불러옴
			webClient.getOptions().setCssEnabled(false); // style 속성파악을 위해 css호출
			webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
			webClient.getOptions().setThrowExceptionOnScriptError(false);

			page = webClient.getPage(url);
			// page lazy load(eco 호출에 필요한 시간 300ms)
			synchronized (webClient) {
				webClient.wait(3000);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return page;
	}

	public HtmlPage getHtmlPageNonCss(WebClient webClient, String url) throws Exception {
		Thread.sleep(2000);
		
		HtmlPage page = null;

		try {
			url = getUrlFilter(url);

			// htmlUnit 세팅
			// WebClient webClient = new WebClient(BrowserVersion.FIREFOX_60);
			setEventListener(webClient);
			webClient.waitForBackgroundJavaScript(1000);
			webClient.getOptions().setTimeout(3_000); // timeout 시간 지정
			webClient.getOptions().setUseInsecureSSL(true);
			webClient.getOptions().setJavaScriptEnabled(false); // eco호출을 위해 스크립트 불러옴
			webClient.getOptions().setCssEnabled(false); // style 속성파악을 위해 css호출
			webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
			webClient.getOptions().setThrowExceptionOnScriptError(false);

			page = webClient.getPage(url);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return page;
	}

	/**
	 * <pre>
	 *	ignore htmlunit eventListener
	 * </pre>
	 * 
	 * @methodName : setEventListener
	 * @since : 2020. 1. 16.
	 * @author : khham
	 * @param webClient
	 */
	private void setEventListener(WebClient webClient) {
		CSSErrorHandler cssErrorHandler = new CSSErrorHandler() {
			@Override
			public void warning(CSSParseException arg0) throws CSSException {
			}

			@Override
			public void fatalError(CSSParseException arg0) throws CSSException {
			}

			@Override
			public void error(CSSParseException arg0) throws CSSException {
			}
		};
		JavaScriptErrorListener javaScriptErrorListener = new JavaScriptErrorListener() {
			@Override
			public void scriptException(HtmlPage page, ScriptException scriptException) {
			}

			@Override
			public void timeoutError(HtmlPage page, long allowedTime, long executionTime) {
			}

			@Override
			public void malformedScriptURL(HtmlPage page, String url, MalformedURLException malformedURLException) {
			}

			@Override
			public void loadScriptError(HtmlPage page, URL scriptUrl, Exception exception) {
			}

			@Override
			public void warn(String message, String sourceName, int line, String lineSource, int lineOffset) {
			}
		};
		IncorrectnessListener incorrectnessListener = new IncorrectnessListener() {
			@Override
			public void notify(String message, Object origin) {
			}
		};
		webClient.setCssErrorHandler(cssErrorHandler);
		webClient.setJavaScriptErrorListener(javaScriptErrorListener);
		webClient.setIncorrectnessListener(incorrectnessListener);
	}

	/**
	 * url 형태 통일
	 * 
	 * @param url
	 * @return
	 */
	public String getUrlFilter(String url) {
		String urlFilter = url;

		urlFilter = urlFilter.indexOf("//") == 0 ? urlFilter.replace("//", "") : urlFilter;
		if ((urlFilter.toLowerCase()).indexOf("http://") == -1 && (urlFilter.toLowerCase()).indexOf("https://") == -1)
			urlFilter = "http://" + urlFilter;

		return urlFilter.replace(":80", "").replace(":443", "");
	}

	public int getHtmlPage_totalScan(String url) {
		int ResponseCode = 500;

		try {

			// url http protocol
			url = getUrlFilter(url);

			URL u = new URL(url);
			HttpURLConnection con = (HttpURLConnection) u.openConnection();
			// System.out.println("url:" + url + "| 응답코드 : " + con.getResponseCode());
			ResponseCode = con.getResponseCode();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseCode;
	}

}
