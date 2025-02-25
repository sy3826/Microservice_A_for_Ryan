package microservice.Microservice_A_for_Ryan;

import org.zeromq.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Tester {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try (ZContext context = new ZContext()) {
			ZMQ.Socket socket = context.createSocket(SocketType.REQ);
			socket.connect("tcp://localhost:5250");
			
			// Adjust these to simulate the filters
			// order can be either increasing or decreasing, increasing is default
			String name = "";
			String type = "";
			String rarity = "Rare";
			String collection = "";
			String set = "";
			String order = "";
			
			JSONObject match = new JSONObject();
			match.put("card_name", name);
			match.put("collection_name", collection);
			match.put("set_name", set);
			match.put("card_type", type);
			match.put("card_rarity", rarity);
			match.put("order", order);
		
			socket.send(match.toString().getBytes(ZMQ.CHARSET), 0);
			byte[] data = socket.recv();
			JSONParser parser = new JSONParser();
			JSONArray array = null;
			try {
				array = (JSONArray) parser.parse(new String(data, ZMQ.CHARSET));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for (Object obj: array) {
				JSONObject object = (JSONObject) obj;
				System.out.println(object.toJSONString());
			}
			System.out.println("enter exit to close connection");
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String input = "";
			try {
				input = reader.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			socket.send(input.getBytes(ZMQ.CHARSET), 0);
		}
	}
}
