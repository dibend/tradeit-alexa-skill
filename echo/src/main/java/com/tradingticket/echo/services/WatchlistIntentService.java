package com.tradingticket.echo.services;

import com.tradingticket.echo.types.Buckets;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;

public class WatchlistIntentService {
    private static WatchlistIntentService instance = null;
    private static final String BUCKET_NAME = Buckets.Bucket.WATCHLISTS.name;
    private static final String REPROMPT_TEXT = "what else can I help you with?";

    private S3Service s3 = S3Service.getInstance();
    private UtilsService utilsService = UtilsService.getInstance();
    private UtilsIntentService utilsIntentService = UtilsIntentService.getInstance();

    public static String currentTicker = "";
    public static String asked = " ";
    public static int watchlistAsks = 0;
    public static final int MAX_WATCHLIST_ASKS = 3;

    protected WatchlistIntentService() {
	// Exists only to defeat instantiation.
    }

    public static WatchlistIntentService getInstance() {
	if(instance == null) {
	    instance = new WatchlistIntentService();
	}
	return instance;
    }

    public SpeechletResponse getWatchlist(final Session session) {
        // Get UserId from session
        String userId = session.getUser().getUserId();
        String speechText = "";
        String wl = s3.getObjectData(BUCKET_NAME, userId);

        if(wl == null || " ".equals(wl)) {
            speechText = "your watchlist is empty";
        } else {
            for(String ticker: wl.split(" ")) {
                if ("".equals(ticker)) {
                    continue;
		}
                speechText += utilsService.tickerSpeech(ticker) + ", ";
            }
        }
        return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
    }

    public SpeechletResponse addToWatchlist(final Intent intent, final Session session) {
        // Get UserId from session
        String userId = session.getUser().getUserId();
        String ticker = utilsIntentService.getTicker(intent);

        String speechText;
        if (ticker.length() < 1) {
            speechText = "Not Available";
            return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
        }

        String wl = s3.getObjectData(BUCKET_NAME, userId);

        //set up String to be written to S3
        if(wl == null) {
            wl = " " + ticker + " ";
        } else if (! wl.contains(" " + ticker + " ")) {
            wl += ticker + " ";
        } else {
            speechText = utilsService.tickerSpeech(ticker) + " is already in your watchlist";
            return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
        }

        if(! asked.contains(" " + ticker + " ")) {
            asked += ticker + " ";
	}

        if(s3.putObjectData(BUCKET_NAME, userId, wl) == 0) {
            speechText = "added " + utilsService.tickerSpeech(ticker) + " to your watchlist";
        } else {
            speechText = "failed";
	}

        return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
    }

    public SpeechletResponse confirmAddToWatchlist(final Session session) {
        // Get UserId from session
        String userId = session.getUser().getUserId();

        String speechText;
        if ("".equals(currentTicker)) {
            speechText = "";
            return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
        }

        String wl = s3.getObjectData(BUCKET_NAME, userId);

        //set up String to be written to S3
        if(wl == null) {
                wl = " " + currentTicker + " ";
        } else if(! wl.contains(" " + currentTicker + " ")) {
                wl += (currentTicker + " ");
        } else {
            speechText = utilsService.tickerSpeech(currentTicker) + " is already in your watchlist";
            currentTicker = "";
            return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
        }

        if(s3.putObjectData(BUCKET_NAME, userId, wl) == 0) {
            speechText = "added " + utilsService.tickerSpeech(currentTicker) + " to your watchlist";
        } else {
            speechText = "failed";
	}

        currentTicker = "";
        return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
    }

    public SpeechletResponse declineAddToWatchlist() {
        String speechText = "";

        if(! "".equals(currentTicker)) {
            currentTicker = "";
            speechText = "okay";
        }

        return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
    }

    public SpeechletResponse removeFromWatchlist(final Intent intent, final Session session) {
        // Get UserId from session
        String userId = session.getUser().getUserId();
	String ticker = utilsIntentService.getTicker(intent);

        String speechText;
        if (ticker.length() < 1) {
            speechText = "Not Available";
            return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
        }

        String wl = s3.getObjectData(BUCKET_NAME, userId);
        String toReplace = " " + ticker + " ";

        if(wl.contains(toReplace)) {
                wl = wl.replace(toReplace, " ");
        } else {
            speechText = utilsService.tickerSpeech(ticker) + " is not in your watchlist";
            return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
        }

        if(s3.putObjectData(BUCKET_NAME, userId, wl) == 0) {
            speechText = "removed " + utilsService.tickerSpeech(ticker) + " from your watchlist";
        } else {
            speechText = "failed";
	}

        return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
    }

    public SpeechletResponse getWatchlistQuotes(final Session session) {
        // Get UserId from session
        String userId = session.getUser().getUserId();
        String speechText = "";
        String wl = s3.getObjectData(BUCKET_NAME, userId);

        if(wl == null || " ".equals(wl)) {
            speechText = "your watchlist is empty";
            return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
        }

        String quote, change;
        for(String ticker: wl.split(" ")) {
            if ("".equals(ticker)) {
                continue;
	    }

            quote = utilsService.yahooFi(ticker, "l1");
            if(! "".equals(quote) && ! "N/A".equals(quote)) {
                speechText += utilsService.tickerSpeech(ticker) + " is at $" + quote + ", ";
                change = utilsService.yahooFi(ticker, "p2").replace("\"", "");
                if(! "".equals(change) && ! "N/A".equals(change)) {
                    if(change.contains("-")) {
                        change = change.replace("-", "down ");
                    } else if(change.contains("+")) {
                        change = change.replace("+", "up ");
                    } else {
                        change = ", no change";
		    }
                    speechText += change + ", ";
                }
            } else {
                speechText += "Quote for " + utilsService.tickerSpeech(ticker) + " is not available, ";
            }
        }

        return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
    }

    public SpeechletResponse getWatchlistMostActive(final Session session) {
        // Get UserId from session
        String userId = session.getUser().getUserId();
        String speechText = "Most active in your watchlist is ";
        String wl = s3.getObjectData(BUCKET_NAME, userId);

        if(wl == null || " ".equals(wl)) {
            speechText = "your watchlist is empty";
            return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
        }

        //Sorry to anyone reading this
        //I'm keeping the 3 most active stocks' volumes in one array, and the index of the ticker in the tickers array in another

        int[] indexes = new int [3];
        int[] max_volumes = {-1, -1, -1};
        int volume;
        int i = 0;

        String tmp;
        String[] tickers = wl.substring(1, wl.length()-1).split(" ");

        for(String ticker: tickers) {
            tmp = utilsService.yahooFi(ticker, "v");
            if(! "".equals(tmp) && ! "N/A".equals(tmp)) {
                volume = Integer.parseInt(tmp);
                if(volume >= max_volumes[0]) {
                    max_volumes[2] = max_volumes[1];
                    indexes[2] = indexes[1];
                    max_volumes[1] = max_volumes[0];
                    indexes[1] = indexes[0];
                    max_volumes[0] = volume;
                    indexes[0] = i;
                } else if(volume >= max_volumes[1]) {
                    max_volumes[2] = max_volumes[1];
                    indexes[2] = indexes[1];
                    max_volumes[1] = volume;
                    indexes[1] = i;
                } else if(volume >= max_volumes[2]) {
                    max_volumes[2] = volume;
                    indexes[2] = i;
                }
            }
            i ++;
        }

        if(i == 0) {
            speechText = "Not Available";
            return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
        }

        if(i > 3) {
            i = 3;
	}

        for(int j = 0; j < i; j ++) {
            speechText += utilsService.tickerSpeech(tickers[indexes[j]]) + " with a volume of " + max_volumes[j] + ", ";
	}
        return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
    }

    public SpeechletResponse getWatchlistMostUp(final Session session) {
        // Get UserId from session
        String userId = session.getUser().getUserId();
        String speechText = "Best performers in your watchlist are ";
        String wl = s3.getObjectData(BUCKET_NAME, userId);

        if(wl == null || " ".equals(wl)) {
            speechText = "your watchlist is empty";
            return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
        }

        //Sorry to anyone reading this
        //I'm keeping the 3 most up stocks' % change in one array, and the index of the ticker in the tickers array in another

        int[] indexes = new int [3];
        double[] max_p = {-101, -101, -101};
        double p;
        int i = 0;

        String tmp;
        String[] tickers = wl.substring(1, wl.length()-1).split(" ");

        for(String ticker: tickers) {
            tmp = utilsService.yahooFi(ticker, "p2");
            if(! "".equals(tmp) && ! "N/A".equals(tmp)) {
                //get rid of non alpha numeric characters besides "." and "-" and convert to double
                p = Double.parseDouble(tmp.replaceAll("[^A-Za-z0-9\\.\\-]", ""));
                if(p > max_p[0]) {
                    max_p[2] = max_p[1];
                    indexes[2] = indexes[1];
                    max_p[1] = max_p[0];
                    indexes[1] = indexes[0];
                    max_p[0] = p;
                    indexes[0] = i;
                } else if(p > max_p[1]) {
                    max_p[2] = max_p[1];
                    indexes[2] = indexes[1];
                    max_p[1] = p;
                    indexes[1] = i;
                } else if(p > max_p[2]) {
                    max_p[2] = p;
                    indexes[2] = i;
                }
            }
            i ++;
        }

        if(i == 0) {
            speechText = "Not Available";
            return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
        }

        if(i > 3) {
            i = 3;
	}

        String direction;
        double change;
        for(int j = 0; j < i; j ++) {
            change = max_p[j];
            if(change < 0) {
                direction = " down ";
                change *= -1;
            } else {
                direction = " up ";
	    }
            speechText += utilsService.tickerSpeech(tickers[indexes[j]]) + direction + change + "%, ";
        }

        return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
    }

    public SpeechletResponse getWatchlistMostDown(final Session session) {
        // Get UserId from session
        String userId = session.getUser().getUserId();
        String speechText = "Worst performers in your watchlist are ";
        String wl = s3.getObjectData(BUCKET_NAME, userId);

        if(wl == null || " ".equals(wl)) {
            speechText = "your watchlist is empty";
            return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
        }

        //Sorry to anyone reading this
        //I'm keeping the 3 most down stocks' % change in one array, and the index of the ticker in the tickers array in another

        int[] indexes = new int [3];
        double[] min_p = {99999, 99999, 99999};
        double p;
        int i = 0;

        String tmp;
        String[] tickers = wl.substring(1, wl.length()-1).split(" ");

        for(String ticker: tickers) {
            tmp = utilsService.yahooFi(ticker, "p2");
            if(! "".equals(tmp) && ! "N/A".equals(tmp)) {
                //get rid of non alpha numeric characters besides "." and "-" and convert to double
                p = Double.parseDouble(tmp.replaceAll("[^A-Za-z0-9\\.\\-]", ""));
                if(p < min_p[0]) {
                    min_p[2] = min_p[1];
                    indexes[2] = indexes[1];
                    min_p[1] = min_p[0];
                    indexes[1] = indexes[0];
                    min_p[0] = p;
                    indexes[0] = i;
                } else if(p < min_p[1]) {
                    min_p[2] = min_p[1];
                    indexes[2] = indexes[1];
                    min_p[1] = p;
                    indexes[1] = i;
                } else if(p < min_p[2]) {
                    min_p[2] = p;
                    indexes[2] = i;
                }
            }
            i ++;
        }

        if(i == 0) {
            speechText = "Not Available";
            return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
        }

        if(i > 3) {
            i = 3;
	}

        String direction;
        double change;
        for(int j = 0; j < i; j ++) {
            change = min_p[j];
            if(change < 0) {
                direction = " down ";
                change *= -1;
            } else {
                direction = " up ";
	    }
            speechText += utilsService.tickerSpeech(tickers[indexes[j]]) + direction + change + "%, ";
        }

        return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
    }

    public SpeechletResponse watchOut(final Session session) {
        String userId = session.getUser().getUserId();
	String wl = s3.getObjectData(BUCKET_NAME, userId);
        String div = s3.getObjectData(BUCKET_NAME, "div.txt");
        String earn = s3.getObjectData(BUCKET_NAME, "earn.txt");
	String speechText = "";

	if((wl != null && ! " ".equals(wl)) && (div != null || earn != null)) {  
            String[] tickers = wl.substring(1, wl.length()-1).split(" ");
            for(String ticker: tickers) {
	        if(earn != null && earn.contains(ticker)) {
		    speechText += utilsService.tickerSpeech(ticker) + " is releasing earnings today, ";
	        }
	        if(div != null && div.contains(ticker)) {
		    speechText += utilsService.tickerSpeech(ticker) + " is going ex dividend today, ";
	        }
	    }
        }
	if("".equals(speechText)) {
	    speechText = "Nothing happening in your watchlist";
	}

        return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
    }
}
