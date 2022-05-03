package com.tradingticket.echo.services;

import com.amazon.speech.speechlet.SpeechletResponse;

public class DefinitionIntentService {
    private static DefinitionIntentService instance = null;
    private static final String REPROMPT_TEXT = "What else can I help you with?";
    private UtilsIntentService utilsIntentService = UtilsIntentService.getInstance();

    protected DefinitionIntentService() {
	// Exists only to defeat instantiation.
    }

    public static DefinitionIntentService getInstance() {
	if(instance == null) {
	    instance = new DefinitionIntentService();
	}
	return instance;
    }

    public SpeechletResponse defineQuote() {
	String speechText = "A stock quote is the price of a stock as quoted on an exchange";
	return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
    }

    public SpeechletResponse definePe() {
	String speechText = "The price earnings ratio is the current market price of" 
	+ " a company share divided by the earnings per share of the company";
	return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
    }

    public SpeechletResponse defineAsk() {
	String speechText = "Ask is the price a seller is willing to accept for a security";
	return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
    }

    public SpeechletResponse defineBid() {
	String speechText = "A bid is an offer made by someone to buy a security";
	return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
    }

    public SpeechletResponse defineOpen() {
	String speechText = "The opening price is the price of a security at the start of a trading day";
	return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
    }

    public SpeechletResponse defineClose() {
	String speechText = "The closing price is the price of a security at the end of a trading day";
	return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
    }

    public SpeechletResponse defineDividend() {
	String speechText = "A dividend is a distribution of a portion of a company's earnings";
	return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
    }

    public SpeechletResponse defineYield() {
	String speechText = "Dividend yield is a dividend expressed as a percentage of a current share price";
	return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
    }

    public SpeechletResponse defineTarget() {
	String speechText = "A price target is a projected price level as stated by an analyst or advisor";
	return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
    }

    public SpeechletResponse defineRange() {
	String speechText = "A range reflects the lowest and highest price of a stock for a given time period";
	return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
    }

    public SpeechletResponse defineVolume() {
	String speechText = "Volume is the number of shares traded in a security";
	return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
    }

    public SpeechletResponse defineAvVolume() {
	String speechText = "Average daily volume is the average number of shares traded in a security each trading day";
	return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
    }

    public SpeechletResponse defineMktCap() {
	String speechText = "Market Capitalization is the market value of a company's outstanding shares";
	return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
    }

    public SpeechletResponse defineEps() {
	String speechText = "Earnings per share is the portion of a company's"
	+ " profit allocated to each outstanding share";
	return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
    }

    public SpeechletResponse defineEpsEst() {
	String speechText = "An earnings per share estimate is an analyst's estimate for"
	+ " a company's future quarterly or annual earnings per share";
	return utilsIntentService.getSpeechletResponse(speechText, REPROMPT_TEXT, true);
    }

}
