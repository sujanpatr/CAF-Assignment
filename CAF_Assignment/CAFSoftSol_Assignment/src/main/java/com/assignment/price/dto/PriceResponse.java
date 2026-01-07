package com.assignment.price.dto;

/**
 * DTO for Price API response.
 * 
 * Response format:
 * - If price found: {"price": 101.0}
 * - If price not found: "NOT SET"
 */
public class PriceResponse {
    
    private final Object price;
    
    /**
     * Constructor for successful price lookup.
     * 
     * @param price The price value
     */
    public PriceResponse(double price) {
        this.price = price;
    }
    
    /**
     * Constructor for "NOT SET" response.
     */
    public PriceResponse() {
        this.price = "NOT SET";
    }
    
    /**
     * Gets the price value (can be Double or String "NOT SET").
     * 
     * @return Price value or "NOT SET"
     */
    public Object getPrice() {
        return price;
    }
    
    /**
     * Checks if price is set.
     * 
     * @return true if price is set, false if "NOT SET"
     */
    public boolean isPriceSet() {
        return price instanceof Double;
    }
    
    @Override
    public String toString() {
        return "PriceResponse{" +
                "price=" + price +
                '}';
    }
}

