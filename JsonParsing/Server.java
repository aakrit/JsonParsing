package JsonParsing;

/**
 * Created with IntelliJ IDEA.
 * User: aakritprasad
 * Date: 1/18/14
 * Time: 4:08 PM
 * To change this template use File | Settings | File Templates.
 */

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
//import com.oracle.javafx.jmx.json.JSONDocument;
//import com.sun.xml.internal.xsom.impl.scd.Iterators;
//import net.sf.json.*;
import org.apache.commons.io.IOUtils;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;
import java.lang.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class Server extends HttpServlet
{
    private String userMessage = null;
    private int[] messages;
    private int totalRoutes = 0;
    private java.util.ArrayList<String> recipientList = new java.util.ArrayList<String>();   //list of phone numbers
    private static final String ip = "ip", recipients = "recipients", message = "message", routes = "routes";
    private static final String error = "JSON INPUT FORMAT ERROR";
    private static final String testJsonString = "{\"message\":\"Sendhub Rocks!\",\"recipients\":[\"111111111\",\"122222222\",\"133333333\"]}";

    MessageType m1 = new MessageType("SMALL", "10.0.1.", 1, 0.01);
    MessageType m2 = new MessageType("MEDIUM", "10.0.2.", 5, 0.05);
    MessageType m3 = new MessageType("LARGE", "10.0.3.", 10, 0.10);
    MessageType m4 = new MessageType("SUPER", "10.0.4.", 25, 0.25);

    public void init(){
        m1 = new MessageType("SMALL", "10.0.1.", 1, 0.01);
        m2 = new MessageType("MEDIUM", "10.0.2.", 5, 0.05);
        m3 = new MessageType("LARGE", "10.0.3.", 10, 0.10);
        m4 = new MessageType("SUPER", "10.0.4.", 25, 0.25);

    }
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException
    {
        PrintWriter out = res.getWriter();
        out.println("Please use the following format to request JSON input:");
        out.println(testJsonString);
        out.flush();
        out.close();
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException
    {
        BufferedReader br = new BufferedReader( new InputStreamReader( req.getInputStream(), "UTF-8" ) );
        String line = null;
        StringBuffer input = new StringBuffer();

        while( (line = br.readLine()) != null )
        {
            input.append(line);
        }
        String inputAsString = input.toString();
        String retJSON = null;
        PrintWriter out = res.getWriter();
        String check = processJSONPostInput(inputAsString);
        if(check.equals(error)){
            out.println("Incorrect JSON FORMAT was Provided, Could not Parse");
            out.println("Please use the following format");
            out.println(testJsonString);
            out.flush();
            out.close();
            return;
        }
        try {
            retJSON = marshalJavaStringFromJSONForDoPostResponse(recipientList.size());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        if(retJSON.equals(null))
            out.print("IMPROPER JSON FORMAT per Request! \n Please retry");
        else
            out.print(retJSON);
        out.flush();
        out.close();

    }
    //Read Json to extract Java Objects
    private String processJSONPostInput(String jsonInput) throws com.google.gson.JsonSyntaxException
    {
        Gson g = new Gson();
        //convert json string into expected java object
        JSONInputRequest jir;
        try{
            jir = g.fromJson(jsonInput, JSONInputRequest.class);
            if((jir.message == null) || jir.recipients == null){
                return error;
            }

        } catch(Exception e){
            return error;
        }

        userMessage = jir.message;
        for(String s : jir.recipients){
            recipientList.add(s);
        }
        return jir.toString();
    }
    //create JSON response
    private String marshalJavaStringFromJSONForDoPostResponse(int numOfRespients){
        if(numOfRespients == 0) return "Error, no recipients were in the list";
        messages = optimizeMessagesByCategoryies(numOfRespients);
        for(int l : messages) totalRoutes += l;
        System.out.println("totalRoutes: "+totalRoutes);

        Gson g = new Gson();
        JSONOutputRequest jor = new JSONOutputRequest();
        jor.message = userMessage;
        jor.routes = buildRouteArray(jor.routes, messages);

//        JsonArray routeArray = new JsonArray();
//        JsonObject retJSON = new JsonObject();//
//        retJSON.addProperty(message, userMessage);
//        retJSON.addProperty(routes, g.toJson(routeArray));

        return g.toJson(jor);

    }
    private ArrayList<Route> buildRouteArray(ArrayList<Route> routeArray, int[] messages)
    {
        int routeArrayIterator = 0;
        StringBuffer sb = new StringBuffer();
        if(messages[0] > 0){   //less than 5 recipients
            int j = 1, i = messages[0];
            while(j <= i) {
                String[] numbers = new String[m1.messageCap];  //allocate space for num of recipients
                numbers[0] = recipientList.get(0);
//                sb.append(recipientList.get(0));
                recipientList.remove(0);
                String st = new Integer(j).toString();

                Route r1 = new Route();
                r1.ip = m1.relayIP+st;
                for(String ns : numbers) r1.recipients.add(ns);
                routeArray.add(r1);
//                JsonObject j1 = new JsonObject();
//                j1.addProperty(ip, (m1.relayIP)+st);
//                j1.addProperty(recipients, new Gson().toJson(numbers));
//                routeArray.add(j1);
//                routeArrayIterator++;
//                sb.delete(0, sb.capacity());
                j++;
            }
        }
        if(messages[1] > 0){     //less than 10 recipients
            int j = 1, i = messages[1];
            while(j <= i) {
                int k = m2.messageCap; // k = 5
                String[] numbers = new String[k];
                while(k > 0){
                    numbers[k-1] = recipientList.get(0);
                    recipientList.remove(0);
                    k--;
                }
                String st = new Integer(j).toString();
                Route r2 = new Route();
                r2.ip = m2.relayIP+st;
                for(String ns : numbers) r2.recipients.add(ns);
                routeArray.add(r2);
                j++;
            }
        }
        if(messages[2] > 0) {  //less than 25 recipients
            int j = 1, i = messages[2];
            while(j <= i) {
                int k = m3.messageCap; // k = 10
                String[] numbers = new String[k];
                while(k > 0){
                    numbers[k-1] = recipientList.get(0);
                    recipientList.remove(0);
                    k--;
                }
                String st = new Integer(j).toString();
                Route r3 = new Route();
                r3.ip = m3.relayIP+st;
                for(String ns : numbers) r3.recipients.add(ns);
                routeArray.add(r3);
                j++;
            }
        }
        if(messages[3] > 0){ //more than 25 recipients
            int j = 1, i = messages[3];
            while(j <= i) {
                int k = m4.messageCap; // k = 10
                String[] numbers = new String[k];
                while(k > 0){
                    numbers[k-1] = recipientList.get(0);
                    recipientList.remove(0);
                    k--;
                }
                String st = new Integer(j).toString();
                Route r4 = new Route();
                r4.ip = m4.relayIP+st;
                for(String ns : numbers) r4.recipients.add(ns);
                routeArray.add(r4);
                j++;
            }
        }
        return routeArray;
    }

    private int[] optimizeMessagesByCategoryies(int size){
        int[] ret = new int[4];
        for(int i : ret) ret[i] = 0;//initialize to 0's
        if(size > 24){     //more than 25 reciepients
            ret[3] = size/ m4.messageCap;
            size -= ret[3]*m4.messageCap;
        }
        if(size > 10){
            ret[2] = size/ m3.messageCap;
            size -= ret[2]*m3.messageCap;
        }
        if(size > 5){
            ret[1] = size/ m2.messageCap;
            size -= ret[1]*m2.messageCap;
        }
        if(size > 0){
            ret[0] = size/m1.messageCap;
            size -= ret[0];
        }
        return ret;
    }
    private void print(String s){System.out.println(s);}

    public static void main(String[] argv) {
        System.out.println("Testing SendHub Server from main\n");

//      TESTING JSON INPUT
        Server ss = new Server();
        String testJsonString = "{\"message\":\"Sendhub Rocksout!\",\"recipients\":[\"55555551\",\"55555552\",\"55555553\"]}";
        for(int i = 1; i < 30; i++){
            ss.print("Sending to "+i+" recipients");
            test(i, ss);
            i += 3; //test every fourth until 30
        }

    }
    private static void test(int k, Server ss){
        Gson g = new Gson();
        JSONInputRequest test = new JSONInputRequest();

        int totalRecipients = k;
        String[] st = new String[totalRecipients];
        for(int i = 1; i <= totalRecipients; i++){
            String j = new Integer(i).toString();
            st[i-1] = "5555555"+ j;
        }
        String whatToSay = "SendHub Rocksout!";

        test.message = whatToSay;
        for(String s : st) test.recipients.add(s);

        String jsonReq = g.toJson(test);
//        ss.print(jsonReq);
        ss.print("Input Request:");
        ss.print(ss.processJSONPostInput(jsonReq));
        ss.print("Output Request:");
        ss.print(ss.marshalJavaStringFromJSONForDoPostResponse(ss.recipientList.size()));
//        ss.print(ss.recipientList.toString());
//        for(int ko : ss.messages) ss.print(new Integer(ko).toString());
//        System.out.println("\n\n");
    }
}

class MessageType
{
    public String categoryName;
    public String relayIP;
    public int messageCap;
    public double messageCost;

    MessageType(String name, String IP, int size, double pricePer){
        this.categoryName = name;
        relayIP = IP;
        messageCap = size;
        messageCost = pricePer;
    }
}

class Container{
    public List<JSONInputRequest> items;
}

class JSONInputRequest implements Serializable
{
    public String message = null;
    public java.util.List<String> recipients = new java.util.ArrayList<String>();
    @Override
    public String toString(){    //return as JSON
        return "{\"message=" + message+ ", \"recipients=" + recipients + "]}";
    }

}

class JSONOutputRequest implements Serializable{
    public String message = null;
    public ArrayList<Route> routes = new ArrayList<Route>();
}
class Route{
    public String ip = null;
    public ArrayList<String> recipients = new ArrayList<String>();
}
