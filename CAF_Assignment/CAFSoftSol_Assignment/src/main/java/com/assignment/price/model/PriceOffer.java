package com.assignment.price.model;

import java.time.LocalTime;

/**
 * Model representing a price offer for a SKU.
 * 
 * Each offer has:
 * - SKU ID
 * - Start time (inclusive)
 * - End time (exclusive or inclusive based on requirement)
 * - Price
 * 
 * Example:
 * SKU: u00006541
 * StartTime: 10:00
 * EndTime: 10:15
 * Price: 101
 */
public class PriceOffer {
    
    private final String skuId;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final double price;
    
    /**
     * Constructor.
     * 
     * @param skuId The SKU identifier
     * @param startTime Start time (inclusive)
     * @param endTime End time (inclusive - offer is valid until this time)
     * @param price The price for this offer
     */
    public PriceOffer(String skuId, LocalTime startTime, LocalTime endTime, double price) {
        this.skuId = skuId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
    }
    
    /**
     * Checks if this offer is active at the given time.
     * 
     * @param time The time to check
     * @return true if offer is active at this time, false otherwise
     */
    public boolean isActiveAt(LocalTime time) {
        // Offer is active if time >= startTime AND time <= endTime
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }
    
    /**
     * Gets the SKU ID.
     * 
     * @return SKU ID
     */
    public String getSkuId() {
        return skuId;
    }
    
    /**
     * Gets the start time.
     * 
     * @return Start time
     */
    public LocalTime getStartTime() {
        return startTime;
    }
    
    /**
     * Gets the end time.
     * 
     * @return End time
     */
    public LocalTime getEndTime() {
        return endTime;
    }
    
    /**
     * Gets the price.
     * 
     * @return Price
     */
    public double getPrice() {
        return price;
    }
    
    /**
     * Checks if this offer overlaps with another offer.
     * 
     * @param other The other offer to check
     * @return true if offers overlap, false otherwise
     */
    public boolean overlaps(PriceOffer other) {
        if (!this.skuId.equals(other.skuId)) {
            return false;
        }
        // Overlaps if: this.start <= other.end AND this.end >= other.start
        return !this.startTime.isAfter(other.endTime) && !this.endTime.isBefore(other.startTime);
    }
    
    @Override
    public String toString() {
        return "PriceOffer{" +
                "skuId='" + skuId + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", price=" + price +
                '}';
    }
}

