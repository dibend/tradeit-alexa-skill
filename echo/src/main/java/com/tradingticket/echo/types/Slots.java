package com.tradingticket.echo.types;

public class Slots {
    public enum Slot {
	TICKER("Ticker");
	public String type;

	private Slot(String type) {
	    this.type = type;
	}
    }
}
