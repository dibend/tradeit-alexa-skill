package com.tradingticket.echo.types;

public class Buckets {
    public enum Bucket {
	WATCHLISTS("tradingticket");
	public String name;

	private Bucket(String name) {
	    this.name = name;
	}
    }
}
