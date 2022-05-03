package com.tradingticket.echo.services;

import com.tradingticket.echo.types.*;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.slu.Intent;
import java.util.Map;

public class UtilsIntentService {
    private static UtilsIntentService instance = null;
    private UtilsService utilsService = UtilsService.getInstance();
    private static final String TICKER_SLOT_TYPE = Slots.Slot.TICKER.type;
    protected UtilsIntentService() {
	// Exists only to defeat instantiation.
    }

    public static UtilsIntentService getInstance() {
	if(instance == null) {
	    instance = new UtilsIntentService();
	}
	return instance;
    }

    public SpeechletResponse getHelpResponse() {
        String speechText = "You can ask me for Definitions, Stock Quotes, Price Earnings Ratios, Ask and Bid, Opening and Closing prices, " +
        "Dividend and Dividend Yield, 1 year price target, Day's Range and 52 week Range, Volume and Average Daily Volume, Market Cap, " +
        "Earnings per share, Current Year, Next Year, and Next Quarter EPS estimate, how the market is doing today, " +
	"who's releasing earnings today, who's going ex dividend today, or manage your watchlist. " + 
	"You can say things like what's the price of Amazon";
        return getSpeechletResponse(speechText, "", true);
    }

    /**
     * Returns a Speechlet response for a speech and reprompt text.
     */
    public SpeechletResponse getSpeechletResponse(String speechText, String repromptText,
            boolean isAskResponse) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("TradeIt");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        if (isAskResponse) {
            // Create reprompt
            PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
            repromptSpeech.setText(repromptText);
            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(repromptSpeech);

            return SpeechletResponse.newAskResponse(speech, reprompt, card);

        } else {
            return SpeechletResponse.newTellResponse(speech, card);
        }
    }

    public String getTicker(Intent intent) {
	// Get the slots from the intent.
        Map<String, Slot> slots = intent.getSlots();
        // Get the ticker slot from the list of slots.
        Slot tickerSlot = slots.get(TICKER_SLOT_TYPE);
        String input = tickerSlot.getValue();
        String ticker = utilsService.symbolLookup(input);
	return ticker;
    }
}
