/**
 * Class Item implementation.
 * 
 * Copyright 2011 Jan Minar <rdancer@rdancer.org>  All rights reserved.
 *
 * @author Jan Minar <rdancer@rdancer.org> 
 */

import java.util.concurrent.atomic.AtomicLong;
import java.math.BigDecimal;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class Item
{
    private String id;
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    private String secret;
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    private String name;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    private BigDecimal price;
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    private String sellerId;
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    private String winningBidderId;
    public String getWinningBidderId() { return winningBidderId; }
    public void setWinningBidderId(String winningBidderId) { this.winningBidderId = winningBidderId; }
    private Date auctionEndTime;
    public Date getAuctionEndTime() { return auctionEndTime; }
    private boolean finished = false;
    public boolean finished() { return finished; }
    public void setFinished(boolean finished) { this.finished = finished; };

    private static AtomicLong uniqueIdCounter = new AtomicLong();

    /**
     * Generate new Item, getting all the properties from reading the console
     * 
     * @return new Item
     */
    public static Item itemFromConsole()
    {
        Item item = new Item();
        String s = "";
        Scanner consoleScanner = new Scanner(System.in);

        try
        {
            
            System.out.print("Item name: ");
            //s = "   Some silly item name \n";
            s = consoleScanner.nextLine();
            item.name = s.trim();
            
            for (;;)
            {
                System.out.print("Starting price: ");

                s = consoleScanner.nextLine();
                
                try
                {
                    item.price = new BigDecimal(s);
                    if (item.price.signum() < 0)
                    {
                        System.err.println("Non-negative price, please");
                        continue;
                    }
                    else if (item.price.scale() > 2)
                    {
                         System.err.println("No fractional pennies, please");
                         continue;
                    }
                    item.price = item.price.setScale(2);
                    break;
                }
                catch (NumberFormatException e)
                {
                    continue;
                }
            }
            
            
            do {
                System.out.print("Auction duration in seconds (defaults to 10 minutes if blank): ");
                s = consoleScanner.nextLine();
                s = s.trim();
                if (s.equals(""))
                {
                    long millisecondsPerMinute = 60 * 1000;
                    Date tenMinutesFromNow = new Date(System.currentTimeMillis()
                            + 10 * millisecondsPerMinute);
                    item.auctionEndTime = tenMinutesFromNow;
                }
                else
                {
                    try
                    {
                        Integer auctionDurationSeconds = Integer.parseInt(s);
                        if (!auctionDurationSeconds.toString().equals(s))
                                throw new Exception("Not an integer");
                        else if (auctionDurationSeconds <= 0)
                                throw new Exception("Duration must be positive");
                        else
                        {
                            long millisecondsPerSecond = 1000;
                            item.auctionEndTime = new Date(System.currentTimeMillis()
                                    + auctionDurationSeconds * millisecondsPerSecond);
                        }
                    }
                    catch (Exception e)
                    {
                        System.err.println("I have trouble parsing your input: " + e.getMessage());
                        continue;
                    }
                }
            } while (false);
        }
        catch (NoSuchElementException e)
        {
            throw new RuntimeException("End of file reading item description from console");
        }
        catch (IllegalStateException e)
        {
            throw new RuntimeException("End of file reading item description from console");
        }
        
        return item;
    }
    
    
    public Item()
    {
        id = uniqueItemId();
    }
    
    public Item(String id)
    {
        this.id = id;
    }

    public Item(Map<String,String> itemDescription)
            throws Exception
    {
        if (itemDescription.isEmpty())
                throw new Exception("Empty item description");
        if (itemDescription.containsKey("ID"))
                id = itemDescription.get("ID");
        if (itemDescription.containsKey("NAME"))
                name = itemDescription.get("NAME");
        if (itemDescription.containsKey("PRICE"))
                price = new BigDecimal(itemDescription.get("PRICE"));
        if (itemDescription.containsKey("END"))
                auctionEndTime = new Date(itemDescription.get("END"));
        if (itemDescription.containsKey("SELLER"))
                sellerId = itemDescription.get("SELLER");
        if (itemDescription.containsKey("BIDDER"))
                winningBidderId = itemDescription.get("BIDDER");
        if (itemDescription.containsKey("FINISHED"))
                finished = true;
                
        // Tacitly ignore unknown label-value pairs
    }
    
    public String toLabelValuePairsString()
    {
        String s = "";
        
        if (id != null)
                s += "ID " + id + "\n";
        if (name != null)
                s += "NAME " + name + "\n";
        if (price != null)
                s += "PRICE " + price + "\n";
        if (auctionEndTime != null)
                s += "END " + auctionEndTime + "\n";
        if (sellerId != null)
                s += "SELLER " + sellerId + "\n";
        if (winningBidderId != null)
                s += "BIDDER " + winningBidderId + "\n";
        if (finished)
                s += "FINISHED" + "\n";
                
        return s;
    }
    
    private static String uniqueItemId()
    {
        return "" + uniqueIdCounter.getAndIncrement();
    }

    public boolean placeBid(BigDecimal amount)
    {
        throw new RuntimeException("Unimplemented");
    }
    
    public String toString()
    {
        String s = "";
        
        s += "ID: " + getId() + "\n";
        s += "name: " + getName() + "\n";
        s += "price: " + (getPrice() == null ? "" : "GBP ") + getPrice() + "\n";
        if (finished())
        {
            s += "expired: yes\n";
            s += "auction ended: " + getAuctionEndTime() + "\n";
        }
        else
        {
            s += "expired: no\n";
            s += "auction ends: " + getAuctionEndTime() + "\n";
        }
        s += "winning bidder: " + (getWinningBidderId() == null ? "no bids" : getWinningBidderId()) + "\n";

        return s;
    }
    
    
    public static void main(String[] args)
    {        
        while (true)
        {
            Item item = itemFromConsole();
            System.out.println(item);
        }
    }
}