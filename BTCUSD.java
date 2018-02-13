import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class BTCUSD {
	// @TODO: Add logs for errors. 
	// @TODO: Handle rate limits.
	// @TODO: Read response codes.
	// @TODO: Handle timeouts
	// @TODO: Arraylist of triple? As is, the keys are unique and the value is the most recent, so we're not guaranteed lowest price.
	// @TODO: Lots of code could probably be reused and broken out into shared methods.
	// @TODO: Add timestamps
	// @TODO: Add test cases. Maybe some mocking to test the sort function (JMockit?)

	public static void main(String[] args) {
		
		Map<Float, Float> pairs = new HashMap<Float, Float>();
		
		pairs.putAll(GetBitStamp());
		pairs.putAll(GetCex());
		pairs.putAll(GetBitfinex());
		
		Map<Float, Float> sortedPairs = SortPairs(pairs);//score to key
		
		System.out.println("Most profitable to sell on, to least profitable:");
		
		for(float f : sortedPairs.keySet()) {
			float key = sortedPairs.get(f);
			System.out.println("USD: " + key + " -> BTC: " + pairs.get(key));
		}
		
	}

	
	private static Map<Float, Float> GetBitStamp() {
		
		Map<Float, Float> pairs = new HashMap<Float, Float>();
		
		try {
			URL url = new URL("https://www.bitstamp.net/api/v2/order_book/btcusd/");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			BufferedReader in = new BufferedReader(
					  new InputStreamReader(con.getInputStream()));
			
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
			con.disconnect();
			
			List<String> trimContent = new ArrayList<String>();
			
			//Trim timestamp and all "ask" prices to leave only bids and remove non numeric data.

			for(String s : (content.toString().substring(content.indexOf("bids"), content.indexOf(", \"asks"))).split("\\,")){
				trimContent.add(s.replaceAll("[^\\d.]", "").toString());
			}
			
			String[] contentArray = trimContent.toArray(new String[trimContent.size()]);
			
			if(trimContent.size() %2 != 0) {
				throw new IOException("Key/Value pair size mismatch");
			}
			
			for(int i = 0; i < trimContent.size(); i++) {
				float key = Float.parseFloat(contentArray[i]);
				float value = Float.parseFloat(contentArray[++i]);
				pairs.put(key,  value);
		       }
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return pairs;

	}
	
	private static Map<Float, Float> GetCex() {
		Map<Float, Float> pairs = new HashMap<Float, Float>();
		
		try {
			URL url = new URL("https://cex.io/api/order_book/BTC/USD/");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.addRequestProperty("User-Agent",  "Mozilla/5.0 (Windows; U; Win98; en-US; rv:1.7.2) Gecko/20040803"); //Cex.IO doesn't like the default user agent.
			con.setRequestMethod("GET");
			BufferedReader in = new BufferedReader(
					  new InputStreamReader(con.getInputStream()));
			
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
			con.disconnect();
			
			List<String> trimContent = new ArrayList<String>();
			
			//Trim timestamp and all "ask" prices to leave only bids and remove non numeric data.

			//Note there is no space between the comma and "asks" in this api call, compared to BitStamp.
			for(String s : (content.toString().substring(content.indexOf("bids"), content.indexOf(",\"asks"))).split("\\,")){
				trimContent.add(s.replaceAll("[^\\d.]", "").toString());
			}
			
			String[] contentArray = trimContent.toArray(new String[trimContent.size()]);
			
			if(trimContent.size() %2 != 0) {
				throw new IOException("Key/Value pair size mismatch");
			}
			
			for(int i = 0; i < trimContent.size(); i++) {
				float key = Float.parseFloat(contentArray[i]);
				float value = Float.parseFloat(contentArray[++i]);
				pairs.put(key,  value);
		       }
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return pairs;
	}
	
	private static Map<Float, Float> GetBitfinex() {
		Map<Float, Float> pairs = new HashMap<Float, Float>();
		
		try {
			URL url = new URL("https://api.bitfinex.com/v2/book/tBTCUSD/P0");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			//con.addRequestProperty("User-Agent",  "Mozilla/5.0 (Windows; U; Win98; en-US; rv:1.7.2) Gecko/20040803"); //Cex.IO doesn't like the default user agent.
			con.setRequestMethod("GET");
			BufferedReader in = new BufferedReader(
					  new InputStreamReader(con.getInputStream()));
			
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
			con.disconnect();
			
			List<String> trimContent = new ArrayList<String>();
		
			for(String s : (content.toString().split("\\,"))){
				trimContent.add(s.replaceAll("[^\\d.]", "").toString());
			}
			
			String[] contentArray = trimContent.toArray(new String[trimContent.size()]);
			
			for(int i = 0; i < trimContent.size(); i++) {
				float key = Float.parseFloat(contentArray[i]);
				float value = Float.parseFloat(contentArray[i+=2]);
				pairs.put(key,  value);
		       }
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return pairs;
	}
	
	private static Map<Float, Float> SortPairs(Map<Float, Float> pairs) {
		Map<Float, Float> scoreToKey = new TreeMap<Float, Float>(); //order matters
		for(float f : pairs.keySet()) {
			scoreToKey.put(pairs.get(f)/f, f);
		}
		
		return scoreToKey;
		
	}

}
