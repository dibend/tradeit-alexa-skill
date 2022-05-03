package com.tradingticket.echo.services;

import com.tradingticket.echo.types.*;

import java.util.Set;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Date;
import java.text.SimpleDateFormat;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;

public class QuoteIntentService {
    private static QuoteIntentService instance = null;

    private S3Service s3 = S3Service.getInstance();
    private UtilsService utilsService = UtilsService.getInstance();
    private UtilsIntentService utilsIntentService = UtilsIntentService.getInstance();
    private WatchlistIntentService wlService = WatchlistIntentService.getInstance();

    private static ArrayList<String> DataPoints = new ArrayList<String>();
    private static final String BUCKET_NAME = Buckets.Bucket.WATCHLISTS.name;

    protected QuoteIntentService() {
	DataPoints.add("Quote");
	DataPoints.add("Price Earnings Ratio");
	DataPoints.add("Ask");
	DataPoints.add("Bid");
	DataPoints.add("Opening Price");
	DataPoints.add("Closing Price");
	DataPoints.add("Dividend");
	DataPoints.add("Dividend Yield");
	DataPoints.add("Target");
	DataPoints.add("Range");
	DataPoints.add("Volume");
	DataPoints.add("Average Daily Volume");
	DataPoints.add("Market Capitalization");
	DataPoints.add("Earnings Per Share");
	DataPoints.add("Earnings Per Share Estimate");
    }

    public static QuoteIntentService getInstance() {
	if(instance == null) {
	    instance = new QuoteIntentService();
	}
	return instance;
    }

    private String marketOverview() {
	String data;
	String text = "";
	LinkedHashMap ov = new LinkedHashMap();

	ov.put("DIA", "Dow Jones");
	ov.put("ONEQ", "Nasdaq");
	ov.put("SPY", "S&P");

	Set<String> keys = ov.keySet();
	for(String k:keys) {	

	    data = utilsService.yahooFi(k, "p2").replace("\"", "");
	    if(! "".equals(data) && ! "N/A".equals(data)) {
                if(data.contains("-")) {
                    data = data.replace("-", ", " + ov.get(k) + " is down ");
                } else if(data.contains("+")) {
                    data = data.replace("+", ", " + ov.get(k) + " is up ");
                } else {
                    data = "! no change in " + ov.get(k);
		}
                text += data;
            }
        }
	return text;
    }
    public SpeechletResponse getWelcomeResponse(Session session) {
        // Create the welcome message.
        String speechText = "Welcome to Trade It";
        String today = new SimpleDateFormat ("MM.dd.yy").format(new Date());
        String dateFile = "date." + session.getUser().getUserId();
        String day = s3.getObjectData(BUCKET_NAME, dateFile);

        if(day == null || ! day.equals(today)) {
            if(s3.putObjectData(BUCKET_NAME, dateFile, today) != 0) {
                speechText = "failed";
                return utilsIntentService.getSpeechletResponse(speechText, speechText, true);
            }
            speechText += marketOverview() + 
	    ". You can ask me for stock quotes, how the market is doing, definitions, manage your watchlist, and more";
        }
	speechText += ". What can I help you with?";
        return utilsIntentService.getSpeechletResponse(speechText, "", true);
    }

    private String getDataPoint() {
	String dataPoint = DataPoints.get(0);
	DataPoints.remove(0); 
	return dataPoint;
    }

    private String getRepromptText(String ticker) {
        String repromptText = "";
        if(wlService.watchlistAsks < wlService.MAX_WATCHLIST_ASKS && ! wlService.asked.contains(" " + ticker + " ")) {
            repromptText = "Would you like to add " + utilsService.tickerSpeech(ticker) + " to your watchlist?";
            wlService.currentTicker = ticker;
            wlService.asked += ticker + " ";
            wlService.watchlistAsks ++;
        } else if(! DataPoints.isEmpty()) {
	    String dataPoint = getDataPoint();
	    repromptText = "You can also ask me for " + dataPoint + " for " + utilsService.tickerSpeech(ticker) + 
	    ", or to define " + dataPoint + ". What else can I help you with?";
	}
        return repromptText;
    }

    public SpeechletResponse getQuote(final Intent intent) {
        String ticker = utilsIntentService.getTicker(intent);
        String quote = utilsService.yahooFi(ticker, "l1");

        String speechText;
        if(! "".equals(quote) && ! "N/A".equals(quote)) {
                speechText = "Price of " + utilsService.tickerSpeech(ticker) + " is $" + quote;
        } else {
            speechText = "Not Available";
            return utilsIntentService.getSpeechletResponse(speechText, speechText, true);
        }

        String change = utilsService.yahooFi(ticker, "p2").replace("\"", "");
        if(! "".equals(change) && ! "N/A".equals(change)) {
            if(change.contains("-")) {
                change = change.replace("-", ", down ");
            } else if(change.contains("+")) {
                change = change.replace("+", ", up ");
            } else {
                change = ", no change";
	    }
            speechText += change;
        }

	DataPoints.remove("Quote");
        return utilsIntentService.getSpeechletResponse(speechText, getRepromptText(ticker), true);
    }

    public SpeechletResponse getPe(final Intent intent) {
        String ticker = utilsIntentService.getTicker(intent);
        String pe = utilsService.yahooFi(ticker, "r");

        String speechText;
        if(! "".equals(pe) && ! "N/A".equals(pe)) {
                speechText = "Price Earnings Ratio for " + utilsService.tickerSpeech(ticker) + " is " + pe;
        } else {
            speechText = "Not Available";
        }

	DataPoints.remove("Price Earnings Ratio");
        return utilsIntentService.getSpeechletResponse(speechText, getRepromptText(ticker), true);
    }

    public SpeechletResponse getAsk(final Intent intent) {
        String ticker = utilsIntentService.getTicker(intent);
        String price = utilsService.yahooFi(ticker, "a");

        String speechText;
        if(! "".equals(price) && ! "N/A".equals(price)) {
                speechText = "Ask for " + utilsService.tickerSpeech(ticker) + " is $" + price;
        } else {
            speechText = "Not Available";
        }

	DataPoints.remove("Ask");
        return utilsIntentService.getSpeechletResponse(speechText, getRepromptText(ticker), true);
    }

    public SpeechletResponse getBid(final Intent intent) {
        String ticker = utilsIntentService.getTicker(intent);
        String price = utilsService.yahooFi(ticker, "b");

        String speechText;
        if(! "".equals(price) && ! "N/A".equals(price)) {
                speechText = "Bid for " + utilsService.tickerSpeech(ticker) + " is $" + price;
        } else {
            speechText = "Not Available";
        }

	DataPoints.remove("Bid");
        return utilsIntentService.getSpeechletResponse(speechText, getRepromptText(ticker), true);
    }

    public SpeechletResponse getOpen(final Intent intent) {
        String ticker = utilsIntentService.getTicker(intent);
        String open = utilsService.yahooFi(ticker, "o");

        String speechText;
        if(! "".equals(open) && ! "N/A".equals(open)) {
                speechText = "Opening Price for " + utilsService.tickerSpeech(ticker) + " was $" + open;
        } else {
            speechText = "Not Available";
        }

	DataPoints.remove("Opening Price");
        return utilsIntentService.getSpeechletResponse(speechText, getRepromptText(ticker), true);
    }

    public SpeechletResponse getClose(final Intent intent) {
        String ticker = utilsIntentService.getTicker(intent);
        String close = utilsService.yahooFi(ticker, "p");

        String speechText;
        if(! "".equals(close) && ! "N/A".equals(close)) {
                speechText = "Closing Price for " + utilsService.tickerSpeech(ticker) + " was $" + close;
        } else {
            speechText = "Not Available";
        }

	DataPoints.remove("Closing Price");
        return utilsIntentService.getSpeechletResponse(speechText, getRepromptText(ticker), true);
    }

    public SpeechletResponse getDividend(final Intent intent) {
        String ticker = utilsIntentService.getTicker(intent);
        String div = utilsService.yahooFi(ticker, "d");

        String speechText;
        if(! "".equals(div) && ! "N/A".equals(div)) {
                speechText = "Dividend for " + utilsService.tickerSpeech(ticker) + " was $" + div;
        } else {
            speechText = "Not Available";
        }

	DataPoints.remove("Dividend");
        return utilsIntentService.getSpeechletResponse(speechText, getRepromptText(ticker), true);
    }

    public SpeechletResponse getYield(final Intent intent) {
        String ticker = utilsIntentService.getTicker(intent);
        String yield = utilsService.yahooFi(ticker, "y");

        String speechText;
        if(! "".equals(yield) && ! "N/A".equals(yield)) {
                speechText = "Dividend Yield for " + utilsService.tickerSpeech(ticker) + " is " + yield + "%";
        } else {
            speechText = "Not Available";
        }

	DataPoints.remove("Dividend Yield");
        return utilsIntentService.getSpeechletResponse(speechText, getRepromptText(ticker), true);
    }

    public SpeechletResponse getTarget(final Intent intent) {
        String ticker = utilsIntentService.getTicker(intent);
        String target = utilsService.yahooFi(ticker, "t8");

        String speechText;
        if(! "".equals(target) && ! "N/A".equals(target)) {
                speechText = "1 year price target for " + utilsService.tickerSpeech(ticker) + " is $" + target;
        } else {
            speechText = "Not Available";
        }

	DataPoints.remove("Target");
        return utilsIntentService.getSpeechletResponse(speechText, getRepromptText(ticker), true);
    }

    public SpeechletResponse getDaysRange(final Intent intent) {
        String ticker = utilsIntentService.getTicker(intent);
        String range = utilsService.yahooFi(ticker, "m");

        String speechText;
        if(! "".equals(range) && ! "N/A".equals(range)) {
                range = range.replace("\"", "");
                range = range.replace("- ", "to $");
                speechText = "Day's Range for " + utilsService.tickerSpeech(ticker) + " is from $" + range;
        } else {
            speechText = "Not Available";
        }

	DataPoints.remove("Range");
        return utilsIntentService.getSpeechletResponse(speechText, getRepromptText(ticker), true);
    }
    //52 Week Range
    public SpeechletResponse getFTWkRange(final Intent intent) {
        String ticker = utilsIntentService.getTicker(intent);
        String range = utilsService.yahooFi(ticker, "w");

        String speechText;
        if(! "".equals(range) && ! "N/A".equals(range)) {
                range = range.replace("\"", "");
                range = range.replace("- ", "to $");
                speechText = "52 week Range for " + utilsService.tickerSpeech(ticker) + " is from $" + range;
        } else {
            speechText = "Not Available";
        }

	DataPoints.remove("Range");
        return utilsIntentService.getSpeechletResponse(speechText, getRepromptText(ticker), true);
    }

    public SpeechletResponse getVolume(final Intent intent) {
        String ticker = utilsIntentService.getTicker(intent);
        String volume = utilsService.yahooFi(ticker, "v");

        String speechText;
        if(! "".equals(volume) && ! "N/A".equals(volume)) {
                speechText = "Volume for " + utilsService.tickerSpeech(ticker) + " is " + volume;
        } else {
            speechText = "Not Available";
        }

	DataPoints.remove("Volume");
        return utilsIntentService.getSpeechletResponse(speechText, getRepromptText(ticker), true);
    }

    //Average Daily Volume
    public SpeechletResponse getAvVolume(final Intent intent) {
        String ticker = utilsIntentService.getTicker(intent);
        String volume = utilsService.yahooFi(ticker, "a2");

        String speechText;
        if(! "".equals(volume) && ! "N/A".equals(volume)) {
                speechText = "Average Daily Volume for " + utilsService.tickerSpeech(ticker) + " is " + volume;
        } else {
            speechText = "Not Available";
        }

	DataPoints.remove("Average Daily Volume");
        return utilsIntentService.getSpeechletResponse(speechText, getRepromptText(ticker), true);
    }

    public SpeechletResponse getMktCap(final Intent intent) {
        String ticker = utilsIntentService.getTicker(intent);
        String cap = utilsService.yahooFi(ticker, "j1");

        String speechText;
        if(! "".equals(cap) && ! "N/A".equals(cap)) {
            if(cap.contains("B")) {
                cap = cap.replace("B", " billion");
            } else if(cap.contains("M")) {
                cap = cap.replace("M", " million");
	    }
            speechText = "Market cap for " + utilsService.tickerSpeech(ticker) + " is " + cap;
        } else {
            speechText = "Not Available";
        }

	DataPoints.remove("Market Capitalization");
        return utilsIntentService.getSpeechletResponse(speechText, getRepromptText(ticker), true);
    }

    public SpeechletResponse getEps(final Intent intent) {
        String ticker = utilsIntentService.getTicker(intent);
        String eps = utilsService.yahooFi(ticker, "e");

        String speechText;
        if(! "".equals(eps) && ! "N/A".equals(eps)) {
                speechText = "Last 12 month's Earning per Share for " + utilsService.tickerSpeech(ticker) + " was $" + eps;
        } else {
            speechText = "Not Available";
        }

	DataPoints.remove("Earnings Per Share");
        return utilsIntentService.getSpeechletResponse(speechText, getRepromptText(ticker), true);
    }
    //Current Year EPS Estimate
    public SpeechletResponse getEpsEstCrntYr(final Intent intent) {
        String ticker = utilsIntentService.getTicker(intent);
        String est = utilsService.yahooFi(ticker, "e7");

        String speechText;
        if(! "".equals(est) && ! "N/A".equals(est)) {
                speechText = "Current Year's Earning per Share Estimate for " + utilsService.tickerSpeech(ticker) + " is $" + est;
        } else {
            speechText = "Not Available";
        }

	DataPoints.remove("Earnings Per Share Estimate");
        return utilsIntentService.getSpeechletResponse(speechText, getRepromptText(ticker), true);
    }
    //Next Year EPS Estimate
    public SpeechletResponse getEpsEstNextYr(final Intent intent) {
        String ticker = utilsIntentService.getTicker(intent);
        String est = utilsService.yahooFi(ticker, "e8");

        String speechText;
        if(! "".equals(est) && ! "N/A".equals(est)) {
                speechText = "Next Year's Earning per Share Estimate for " + utilsService.tickerSpeech(ticker) + " is $" + est;
        } else {
            speechText = "Not Available";
        }

	DataPoints.remove("Earnings Per Share Estimate");
        return utilsIntentService.getSpeechletResponse(speechText, getRepromptText(ticker), true);
    }
    //Next Quarter EPS Estimate
    public SpeechletResponse getEpsEstNextQtr(final Intent intent) {
        String ticker = utilsIntentService.getTicker(intent);
        String est = utilsService.yahooFi(ticker, "e9");

        String speechText;
        if(! "".equals(est) && ! "N/A".equals(est)) {
                speechText = "Next Quarter's Earning per Share Estimate for " + utilsService.tickerSpeech(ticker) + " is $" + est;
        } else {
            speechText = "Not Available";
        }

	DataPoints.remove("Earnings Per Share Estimate");
        return utilsIntentService.getSpeechletResponse(speechText, getRepromptText(ticker), true);
    }

    public SpeechletResponse getMktOverview() {
        String speechText = marketOverview();
        if("".equals(speechText)) {
            speechText = "Not Available";
	}
	String repromptText = "You can also ask me what's the price of a particular stock, what else can I help you with?";
        return utilsIntentService.getSpeechletResponse(speechText, repromptText, true);
    }

    public SpeechletResponse getEarnings() {
	String earn = s3.getObjectData(BUCKET_NAME, "earn.txt");
	String speechText = "";

	if(earn != null) {
	    for(String ticker:earn.split("\n")) {
	        speechText += utilsService.tickerSpeech(ticker) + ", ";
	    } 
	}

        if("".equals(speechText)) {
            speechText = "Not Available";
	} else {
	    speechText += "are releasing earnings today";
	}

	String repromptText = "You can add any of these to your watchlist";
        return utilsIntentService.getSpeechletResponse(speechText, repromptText, true);
    }

    public SpeechletResponse getExDiv() {
	String div = s3.getObjectData(BUCKET_NAME, "div.txt");
	String speechText = "";

	if(div != null) {
	    for(String ticker:div.split("\n")) {
	        speechText += utilsService.tickerSpeech(ticker) + ", ";
	    } 
	}

        if("".equals(speechText)) {
            speechText = "Not Available";
	} else {
	    speechText += "are going ex dividend today";
	}

	String repromptText = "You can add any of these to your watchlist";
        return utilsIntentService.getSpeechletResponse(speechText, repromptText, true);
    }
}
