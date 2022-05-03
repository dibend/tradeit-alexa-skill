package com.tradingticket.echo.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Properties;

import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.net.HttpURLConnection;
import java.net.ProtocolException;

import org.json.JSONObject;
import org.json.JSONArray;

import java.lang.invoke.MethodHandles;

public class UtilsService {
    private static final Logger LOG = LoggerFactory.getLogger(UtilsService.class);
    private static UtilsService instance = null;
    private static String symbolLookupURL;
    protected UtilsService() {
        symbolLookupURL = getProp("ems.symbollookup.url");
    }

    public static UtilsService getInstance() {
	if(instance == null) {
	    instance = new UtilsService();
	}
	return instance;
    }

    public static String getProp(String property) {
	Properties prop = new Properties();
        InputStream input = null;
	String value = null;
        try {
            // load a properties file
            input = MethodHandles.lookup().lookupClass().getResourceAsStream("/config.properties");
            prop.load(input);
            value = prop.getProperty(property);
        } catch (IOException ex) {
            LOG.error("IOException at loading prop", ex);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
            	    LOG.error("IOException at closing stream", e);
                }
            }
	}
	return value;
    }

    public String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public String tickerSpeech(String ticker) {
	String name = getName(ticker);
	if(name != null)
	    return name;

        //put spaces in between ticker letters so they a read individually
        ticker = ticker.replaceAll(".(?=.)", "$0 ");
        if(ticker.contains("/")) {
            ticker = ticker.replace("/", "dot");
	}
        return ticker;
    }

    public String symbolLookup(String query) {
	URL url;
        try {
	    url = new URL(symbolLookupURL);
	} catch(MalformedURLException ex) {
            LOG.error("URL error", ex);
	    return "URL error";
	}
	
	if(query.contains(" dot ")) {
             query = query.replace(" dot ", "/");
	}
        Map<String,Object> params = new LinkedHashMap<>();
        params.put("query", query);

        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String,Object> param : params.entrySet()) {
            if (postData.length() != 0) {
		postData.append('&');
	    }

            String en;
            try {
		en = URLEncoder.encode(param.getKey(), "UTF-8");
	    } catch(UnsupportedEncodingException ex) {
                LOG.error("encoding error", ex);
		return "encoding error";
	    }

            postData.append(en);
            postData.append('=');

            try {
		en = URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8");
	    } catch(UnsupportedEncodingException ex) {
                LOG.error("encoding error", ex);
		return "encoding error";
	    }

            postData.append(en);
        }

        byte[] postDataBytes;
        try {
	    postDataBytes = postData.toString().getBytes("UTF-8");
	} catch(UnsupportedEncodingException ex) {
            LOG.error("encoding error");
	    return "encoding error";
	}

        HttpURLConnection conn;
        try {
	    conn = (HttpURLConnection)url.openConnection();
	} catch(IOException ex) {
            LOG.error("connection error", ex);
	    return "connection error";
	}

        try {
	    conn.setRequestMethod("POST");
	} catch(ProtocolException ex) {
            LOG.error("protocol error");
	    return "protocol error";
	}

        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);

        try {
	    conn.getOutputStream().write(postDataBytes);
	} catch(IOException ex) {
            LOG.error("io error", ex);
	    return "io error";
	}

        Reader in = null;
        try {
	    in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	} catch(IOException ex) {
            LOG.error("io error", ex);
	    return "io error";
	}

        String s = "";
        int c = 0;
        while( c >= 0 ) {
            try {
		c = in.read();
	    } catch(IOException ex) {
                LOG.error("io error", ex);
		return "io error";
	    }
            s += (char)c;
        }

        JSONObject obj = new JSONObject(s);
        JSONArray ar = obj.getJSONArray("results");
        String ticker = "";
        if(ar.length() > 0) {
             ticker = ar.getJSONObject(0).getString("symbol");
	}
        return ticker; 
    }

    //yahoo finance CSV API
    public String yahooFi (String ticker, String tag) {
        URL url;
        String s;

        //tradeit api uses rds/a, yahoo api uses rds-a        
        if(ticker.contains("/")) {
            ticker = ticker.replace("/", "-");
	}
        try { 
	    url = new URL("http://download.finance.yahoo.com/d/quotes.csv?s=" + ticker + "&f=" + tag);
	} catch(MalformedURLException ex) {
            LOG.error("url error", ex);
	    url = null;
	}

        try {
            InputStream is = url.openStream();
            s = convertStreamToString(is).trim();
        } catch(IOException ex) {
            LOG.error("io error", ex);
	    s = "";
	}

        return s;
    }

    //for yahoo finance JSON Symbol Lookup
    private static String getHTML(String urlToRead) throws Exception {
        StringBuilder result = new StringBuilder();

        URL url = new URL(urlToRead);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        String line;
        while ((line = rd.readLine()) != null) {
           result.append(line);
        }

        rd.close();
        return result.toString();
    }  
 
    //yahoo finance Symbol Lookup to try to see if ticker is an equity, and return the company name if it is, null if not or failed
    private static String getName(String ticker) {
	String s;
        String name = null;
        String good = "YAHOO.util.ScriptNodeDataSource.callbacks(";
        String url = "http://d.yimg.com/aq/autoc?query=" + ticker + "&region=US&lang=en-US&callback=YAHOO.util.ScriptNodeDataSource.callbacks";

        for(int trys = 0; trys < 50; trys ++) {
            try {
                s = getHTML(url);
            } catch(Exception ex) {
                LOG.error("yahoo symbol lookup error", ex);
                continue;
            }

            if(s.contains(good)) {
                s = s.replace(good, "").replace(");", "");
                JSONObject j = new JSONObject(s);
		JSONObject k;
                JSONArray ar = j.getJSONObject("ResultSet").getJSONArray("Result");
                String exch;
                for(int i = 0; i < ar.length(); i ++) {
		    k = ar.getJSONObject(i);
                    exch = k.getString("exchDisp");
                    if("NASDAQ".equals(exch) || "NYSE".equals(exch)) {
                        name = k.getString("name").replaceAll("Inc\\.|Incorporated|incorporated|Company|Corporation|Corp\\.|S\\.A\\.|Ltd\\.|,", "").trim();
                        break;
                    }
                }
                break;
            }
        }
	return name;
    }
}
