/**
 * Class Client implementation.
 * 
 * Copyright 2011 Jan Minar <rdancer@rdancer.org>  All rights reserved.
 *
 * @author Jan Minar <rdancer@rdancer.org> 
 */

import java.net.*;
import java.util.*;
import java.io.*;
import java.math.BigDecimal;


public class Client
        extends ClientServer
{
    protected static final boolean DEFAULT_DEBUG_VALUE = false;
    
    private static String clientId;
    //private String clientSecret;
    
    public Client(String host, int tcpPortNumber)
            throws IOException, Exception
    {
        Socket ioSocket;


        ioSocket = new Socket(host, tcpPortNumber);
        System.out.println("Connection established.");

        socketOut = new PrintWriter(ioSocket.getOutputStream(), true);
        socketIn = new BufferedReader(new InputStreamReader(
                ioSocket.getInputStream()));
    
                
        String request = "";
        request += "HELLO " + Protocol.PROTOCOL_NAME_AND_VERSION + "\r\n";
        request += "ID " + clientId + "\r\n";
        // XXX out.println("PASS " + clientPassPhrase + "\r\n");
        request += "THANKS\r\n";
        
        Map<String,String> response = sendToServer(request);
    
        if (response.containsKey("ERROR"))
        {
            throw new Exception("Error connecting to server: \"" + response.get("ERROR") + "\"");
        }
        else
        {
            System.out.println("Logged in!");
        }
    }
    
    protected String readWholeMessage()
    {
        String message = readWholeMessage(socketIn);
        Scanner messageScanner = new Scanner(message);

        while (messageScanner.hasNextLine())
                log("<<< " + messageScanner.nextLine());        
        
        return message;
    }    
    
    private void commandLoop()
    {
        System.out.println("Type \"help\" for the list of available commands");                    
        String line;
        
        System.out.print("> ");  // prompt

        Scanner in = new Scanner(System.in);
 
        while (in.hasNextLine())
        {
           
            line = in.nextLine();
            Scanner lineScanner = new Scanner(line.trim());
            
            if (lineScanner.hasNext())
            {
                String command = lineScanner.next();
                
                List<String> arguments = new ArrayList<String>();
                while (lineScanner.hasNext())
                        arguments.add(lineScanner.next());


                if (command.equals("quit") || command.equals("exit"))
                {
                    return;
                }
                else if (command.equals("list") && arguments.isEmpty())
                {
                    System.out.println("Getting list of auctions...");
                    Collection<Item> itemsForSale = itemsForSale();
                    System.out.println(itemsForSale.size() + " auction"
                            + (itemsForSale.size() == 1 ? "" : "s"));
                    for (Item item : itemsForSale)
                            System.out.println(item);
                }
                else if (command.equals("list"))
                {
                    System.out.println("Printing out specific auctions...");                    
                    for (Item item : itemsForSale())
                            if (arguments.contains(item.getId()))
                                    System.out.println(item);
                }
                else if (command.equals("sell"))
                {
                    try
                    {
                        sellItem(Item.itemFromConsole());
                    }
                    catch (Exception e)
                    {
                        System.err.println("Error: " + e.getMessage());
                    }
                }
                else if (command.equals("bid"))
                {
                    String itemId = null; 
                    BigDecimal amount = null;
                    
                    try
                    {
                        itemId = arguments.get(0);
                        amount = new BigDecimal(arguments.get(1));
                        if (arguments.size() > 2) throw new Exception("Too many arguments");
                    }
                    catch (Exception e)
                    {
                        System.err.println("Usage: bid <ITEM_ID> <AMOUNT>");
                    }
                    
                    try
                    {
                        bid(new Item(itemId), amount);
                    }
                    catch (Exception e)
                    {
                        System.err.println("Error: " + e.getMessage());
                    }
                }
                else if (command.equals("cancel"))
                {
                    if (arguments.isEmpty())
                    {
                        System.err.println("Usage: cancel <ITEM_ID>[, ...]");
                        continue;
                    }
                    
                    for (String itemId : arguments)
                    {
                        Item item;
                        String token;
                        
                        try
                        {
                            Map<String,Item> tokenAndItemTuple = requestCancelItem(new Item(itemId));
                            token = (String)(tokenAndItemTuple.keySet().toArray())[0];  // this is horrible
                            item = tokenAndItemTuple.get(token);
                            
                            log("Cancellation token received: " + token);
                            //System.out.println("item: " + item);
                        }
                        catch (Exception e)
                        {
                            //throw new Error(e);
                            System.err.println("Error: " + e.getMessage());
                            continue;
                        }
                        
                        System.out.print(item);
                        String questionPrompt = "Remove item? [yes/NO] ";
                        System.out.println(questionPrompt);
                        while (in.hasNextLine())
                        {
                            String yesOrNo = in.nextLine().trim().toLowerCase();
                            if (yesOrNo.matches("yes|y|yup|remove"))
                            {
                                try
                                {
                                    confirmAction(token);
                                }
                                catch (Exception e)
                                {
                                    System.err.println("Error: " + e.getMessage());
                                }
                                break;
                            }
                            else if (yesOrNo.matches("no|n|nope|do not remove|don't|don't remove"))
                            {
                                break;
                            }
                            else
                            {
                                System.out.print(questionPrompt);
                            }
                        }
                    }
                }
                else if (command.equals("verbose") && arguments.isEmpty())
                {
                    System.out.println("I am being " + (debug ? "verbose" : "quiet"));                    
                }
                else if (command.equals("verbose") && arguments.size() == 1
                        && arguments.get(0).matches("^(on|off)$"))
                {
                    debug = arguments.get(0).equals("on");
                }
                else if (command.toLowerCase().matches("help|h|\\?|hilfe"))
                {
                    System.out.println("quit                     Terminate program");
                    System.out.println("list [<ITEM_ID>[, ...]]  Show specified items (or all items if none apecified)");
                    System.out.println("sell                     Place an item up for sale");
                    System.out.println("bid <ITEM_ID> <AMOUNT>   Bid GBP AMOUNT on item ITEM_ID");
                    System.out.println("cancel <ITEM_ID>[, ...]  Remove one or more items from sale");
                    System.out.println("verbose [on|off]         Show chatter with server and other extra info");
                }
                else if (command.equals("ping"))
                {
                    String request = "PING";
                    
                    for (String argument : arguments)
                            request += " " + argument;
                    
                    try {
                        sendToServer(request + "\r\nTHANKS\r\n");
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    // Note: enable verbose (verbose on) to see the server reply
                }
                else
                {
                    System.out.println("Unknown command: " + line);
                    System.out.println("Type \"help\" for the list of available commands");                    
                }
            }
            System.out.print("> ");  // prompt
        }
        

    }
    
    
   
    public static void main(String[] args)
            throws IOException
    {
        debug = DEFAULT_DEBUG_VALUE; // bit of a kludge
        
        String hostNameOrIPAddress;
        int tcpPortNumber;
        
        try {
            hostNameOrIPAddress = args[0];
            tcpPortNumber = new Integer(args[1]);
            clientId = args[2];
        }
        catch (Exception e)
        {
            System.err.println("Usage: Client <HOST> <PORT> <CLIENT_ID>");
            return;
        }
        
        Client client;
        
        System.out.println("Connecting to " + hostNameOrIPAddress + ":" + tcpPortNumber + "...");
        try
        {
            client = new Client(hostNameOrIPAddress, tcpPortNumber);
        }
        catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            if (debug) throw new Error(e);
            else return;
        }
        
        client.commandLoop();
    }
    
    private void sellItem(Item item)
            throws Exception
    {
        String request = "";
        String name = item.getName().replaceAll("[\r\n]", ""),
                price = item.getPrice().toString(),
                end = item.getAuctionEndTime().toString();
        
        // Sanity checks
        mustNotContainNewlines(name);
        mustNotContainNewlines(price);
        mustNotContainNewlines(end);
    
        request += "SELL\r\n";
        request += "NAME " + name + "\r\n";
        request += "PRICE " + price + "\r\n";
        request += "END " + end + "\r\n";
        request += "THANKS\r\n";
        
        Map<String,String> response = sendToServer(request);
        
        if (response.containsKey("ID"))
                System.out.println("Auction created, with ID: " + response.get("ID"));
        else if (response.containsKey("ERROR"))
                throw new Exception("Failed to create auction"
                        + " -- server said: " + response.get("ERROR"));
        else
                throw new Exception("Failed to create auction");
    }

    public Map<String,String> sendToServer(String request)
            throws Exception
    {
        log(request.replaceAll("^|(\n)(.)", "$1>>> $2"));
        socketOut.println(request.trim() + "\r");
        String response = readWholeMessage();
        return parseResponse(response);
    }
    
    private Map<String,Item> requestCancelItem(Item item)
            throws Exception
    {
        String request = "";
        String id = item.getId();
        //String secret = item.getSecret();
        
        mustNotContainNewlines(id);
        //mustNotContainNewlines(secret);
        
        request += "CANCEL\r\n";
        request += "ID " + id + "\r\n";
        //request += "SECRET " + secret + "\r\n";
        request += "THANKS\r\n";
        
        Map<String,String> response = sendToServer(request);
        
        if (response.containsKey("ID") && response.containsKey("TOKEN"))
        {
            System.out.println("Confirmation requested");
            Map<String,Item> map = new HashMap<String,Item>();
            Item returnedItem;

            try {
                returnedItem = new Item(response);
            } catch (IllegalArgumentException e) {
                throw new Exception("Malformed server response"); // most probably malformed auction end time
            }
            
            map.put(response.get("TOKEN"), returnedItem);
            
            return map;
        }
        else if (response.containsKey("ERROR"))
                throw new Exception("Failed to request canceling auction " + id
                        + " -- server said: " + response.get("ERROR"));
        else
                throw new Exception("Failed to request canceling auction " + id);
    }

    private void confirmAction(String token)
            throws Exception
    {
        log("Confirming action with token " + token + "...");
        
        String request = "";
        
        mustNotContainNewlines(token);
        
        request += "CONFIRM " + token + "\r\n";
        request += "THANKS\r\n";
        
        Map<String,String> response = sendToServer(request);
        
        if (response.containsKey("OK"))
                System.out.println("Action confirmed");
        else if (response.containsKey("ERROR"))
                throw new Exception("Failed to confirm action"
                        + " -- server said: " + response.get("ERROR"));
        else
                throw new Exception("Failed to confirm action");
    }

    
    /**
     * Note: negative bid amount is perfectly valid -- it is up to the server to enforce
     * bid range restrictions.
     */
    private void bid(Item item, BigDecimal amount)
            throws Exception
    {
        String request = "";
        
        request += "BID\r\n";
        request += "ID " + item.getId() + "\r\n";
        request += "PRICE " + amount + "\r\n";
        request += "THANKS\r\n";
        
        
        Map<String,String> response = sendToServer(request);
        
        if (response.containsKey("OK"))
                System.out.println("Bid accepted");
        else if (response.containsKey("ERROR"))
                throw new Exception("Bid not accepted: " + response.get("ERROR"));
        else
                throw new Exception("Bid not accepted");
    }    

    public static void test()
            throws Exception
    {
        main(new String[]{ "localhost", "" + Protocol.DEFAULT_PORT_NUMBER, "" + Math.random() * Integer.MAX_VALUE });
    }
    
    private List<Item> itemsForSale()
    {
        List<Item> itemList = new ArrayList<Item>();

        String request = "BROWSE\r\nTHANKS\r";

        log(request.replaceAll("^|(\n)(.)", "$1>>> $2"));                
        socketOut.println(request);
        String response = readWholeMessage();
        
        Scanner scanner = new Scanner(response);
        
        while (scanner.hasNextLine())
        {
            String line = scanner.nextLine();
            if (line.matches("^ITEM\\b"))
            {
                String itemDescription = "";
                while(scanner.hasNextLine() && !(line = scanner.nextLine()).matches("^ENDITEM\\b"))
                        itemDescription += line + "\n";
                                    
                Map<String,String> labelValuePairs = parseResponse(itemDescription);
                
                if (!labelValuePairs.isEmpty())
                {
                    try
                    {
                        itemList.add(new Item(labelValuePairs));
                    }
                    catch (Exception e)
                    {
                        System.err.println("Bad item description: \"" + itemDescription + "\"");
                    }
                }
            }
        }
        
        return itemList;
    }
    
}