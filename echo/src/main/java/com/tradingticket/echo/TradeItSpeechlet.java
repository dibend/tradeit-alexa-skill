/**
    Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.tradingticket.echo;

import com.tradingticket.echo.services.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;

// This Alexa Skill gets stock data from yahoo finance
// and lets users manage a watchlist saved in AWS S3

public class TradeItSpeechlet implements Speechlet {
    private static final Logger LOG = LoggerFactory.getLogger(TradeItSpeechlet.class);
    
    //my services
    private S3Service s3 = S3Service.getInstance();
    private WatchlistIntentService wlService = WatchlistIntentService.getInstance();   
    private QuoteIntentService quoteService = QuoteIntentService.getInstance(); 
    private UtilsIntentService utilsIntentService = UtilsIntentService.getInstance();
    private DefinitionIntentService defineService = DefinitionIntentService.getInstance();

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
        LOG.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any initialization Logic goes here
	String bucketName = "tradeit-watchlists";
        // Get UserId from session
        String userId = session.getUser().getUserId();
	String wl = s3.getObjectData(bucketName, userId);
	if(wl != null)
	    wlService.asked = new String(wl);
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {
        LOG.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
	
        return quoteService.getWelcomeResponse(session);
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {
        LOG.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        // Get intent from the request object.
        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        // Note: If the session is started with an intent, no welcome message will be rendered;
        // rather, the intent specific response will be returned.
        if ("GetQuoteIntent".equals(intentName)) {
            return quoteService.getQuote(intent);
        } else if ("GetPeIntent".equals(intentName)) {
            return quoteService.getPe(intent);
        } else if ("GetAskIntent".equals(intentName)) {
            return quoteService.getAsk(intent);
        } else if ("GetBidIntent".equals(intentName)) {
            return quoteService.getBid(intent);
        } else if ("GetOpenIntent".equals(intentName)) {
            return quoteService.getOpen(intent);
        } else if ("GetCloseIntent".equals(intentName)) {
            return quoteService.getClose(intent);
        } else if ("GetDividendIntent".equals(intentName)) {
            return quoteService.getDividend(intent);
        } else if ("GetYieldIntent".equals(intentName)) {
            return quoteService.getYield(intent);
        } else if ("GetTargetIntent".equals(intentName)) {
            return quoteService.getTarget(intent);
        } else if ("GetDaysRangeIntent".equals(intentName)) {
            return quoteService.getDaysRange(intent);
        } else if ("GetFTWkRangeIntent".equals(intentName)) {
            return quoteService.getFTWkRange(intent);
        } else if ("GetVolumeIntent".equals(intentName)) {
            return quoteService.getVolume(intent);
        } else if ("GetAvVolumeIntent".equals(intentName)) {
            return quoteService.getAvVolume(intent);
        } else if ("GetMktCapIntent".equals(intentName)) {
            return quoteService.getMktCap(intent);
        } else if ("GetEpsIntent".equals(intentName)) {
            return quoteService.getEps(intent);
        } else if ("GetEpsEstCrntYrIntent".equals(intentName)) {
            return quoteService.getEpsEstCrntYr(intent);
        } else if ("GetEpsEstNextYrIntent".equals(intentName)) {
            return quoteService.getEpsEstNextYr(intent);
        } else if ("GetEpsEstNextQtrIntent".equals(intentName)) {
            return quoteService.getEpsEstNextQtr(intent);
        } else if ("GetMktOverviewIntent".equals(intentName)) {
            return quoteService.getMktOverview();
        } else if ("GetEarningsIntent".equals(intentName)) {
            return quoteService.getEarnings();
        } else if ("GetExDivIntent".equals(intentName)) {
            return quoteService.getExDiv();
        } else if ("AddToWatchlistIntent".equals(intentName)) {
            return wlService.addToWatchlist(intent, session);
        } else if ("GetWatchlistIntent".equals(intentName)) {
            return wlService.getWatchlist(session);
        } else if ("RemoveFromWatchlistIntent".equals(intentName)) {
            return wlService.removeFromWatchlist(intent, session);
        } else if ("GetWatchlistQuotesIntent".equals(intentName)) {
            return wlService.getWatchlistQuotes(session);
        } else if ("GetWatchlistMostActiveIntent".equals(intentName)) {
            return wlService.getWatchlistMostActive(session);
        } else if ("GetWatchlistMostUpIntent".equals(intentName)) {
            return wlService.getWatchlistMostUp(session);
        } else if ("GetWatchlistMostDownIntent".equals(intentName)) {
            return wlService.getWatchlistMostDown(session);
        } else if ("ConfirmAddToWatchlistIntent".equals(intentName)) {
            return wlService.confirmAddToWatchlist(session);
        } else if ("DeclineAddToWatchlistIntent".equals(intentName)) {
            return wlService.declineAddToWatchlist();
        } else if ("WatchOutIntent".equals(intentName)) {
            return wlService.watchOut(session);
        } else if ("DefineQuoteIntent".equals(intentName)) {
            return defineService.defineQuote();
        } else if ("DefinePeIntent".equals(intentName)) {
            return defineService.definePe();
        } else if ("DefineAskIntent".equals(intentName)) {
            return defineService.defineAsk();
        } else if ("DefineBidIntent".equals(intentName)) {
            return defineService.defineBid();
        } else if ("DefineOpenIntent".equals(intentName)) {
            return defineService.defineOpen();
        } else if ("DefineCloseIntent".equals(intentName)) {
            return defineService.defineClose();
        } else if ("DefineDividendIntent".equals(intentName)) {
            return defineService.defineDividend();
        } else if ("DefineYieldIntent".equals(intentName)) {
            return defineService.defineYield();
        } else if ("DefineTargetIntent".equals(intentName)) {
            return defineService.defineTarget();
        } else if ("DefineRangeIntent".equals(intentName)) {
            return defineService.defineRange();
        } else if ("DefineVolumeIntent".equals(intentName)) {
            return defineService.defineVolume();
        } else if ("DefineAvVolumeIntent".equals(intentName)) {
            return defineService.defineAvVolume();
        } else if ("DefineMktCapIntent".equals(intentName)) {
            return defineService.defineMktCap();
        } else if ("DefineEpsIntent".equals(intentName)) {
            return defineService.defineEps();
        } else if ("DefineEpsEstIntent".equals(intentName)) {
            return defineService.defineEpsEst();
	} else if ("AMAZON.HelpIntent".equals(intentName)) {
            return utilsIntentService.getHelpResponse();
        } else if ("AMAZON.StopIntent".equals(intentName) || "AMAZON.CancelIntent".equals(intentName)) {
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText("Goodbye");
            return SpeechletResponse.newTellResponse(outputSpeech);
        } else {
            throw new SpeechletException("Invalid Intent");
        }
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        LOG.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any cleanup Logic goes here
	wlService.watchlistAsks = 0;
	wlService.asked = " ";
    }
}
