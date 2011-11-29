/**
 * The test class ItemTest.  Generated by BlueJ.
 *
 * Copyright BlueJ authors
 * Copyright 2011 Jan Minar <rdancer@rdancer.org>  All rights reserved.
 *
 * @author BlueJ authors
 * @author Jan Minar <rdancer@rdancer.org> 
 */

import java.util.*;
import java.math.BigDecimal;

public class ItemTest extends junit.framework.TestCase
{
    /**
     * Default constructor for test class ItemTest
     */
    public ItemTest()
    {
    }

    /**
     * Sets up the test fixture.
     *
     * Called before every test case method.
     */
    protected void setUp()
    {
    }

    /**
     * Tears down the test fixture.
     *
     * Called after every test case method.
     */
    protected void tearDown()
    {
    }
    
    public void testCreateWithMap()
            throws Exception
    {
        Map<String,String> map = new HashMap<String,String>();
        
        String id = "some id 33",
                name = "some funny name",
                price = "423.44",
                end = (new Date()).toString();
                
        map.put("ID", id);
        map.put("NAME", name);
        map.put("PRICE", price);
        map.put("END", end);
        
        Item item = new Item(map);
        
        assertEquals(id, item.getId());
        assertEquals(name, item.getName());
        assertEquals(price, item.getPrice().toString());
        assertEquals(end, item.getAuctionEndTime().toString());
    }
    
}
