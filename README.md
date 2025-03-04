# Microservice_A_for_Ryan - PokemonFilterSearch

**Requesting Data**

To reqeust a data from the microservice, communication will need to be done through ZeroMq. First, a connection will need to be established with the socket type set to REQ. Then, after connecting to the microservice by using the port number that has been predetermined and entered into the microservice, the data would need to be sent. The data must be in JSON format, with one JSON object containing the values for the keys: card_name, collection_name, set_name, card_type, card_rarity, and order. Also, a json_path can be added to the JSONObject if the path to the file containing all of the Pokemon has not been explicitly inputed in the UI. However, before sending, the object must first be converted to bytes with the appropriate charset UTF-8. Then the data would be sent by using the send method within ZeroMq. This is shown by the following example snippet from Tester.java:
```
JSONObject match = new JSONObject();
match.put("card_name", name);
match.put("collection_name", collection);
match.put("set_name", set);
match.put("card_type", type);
match.put("card_rarity", rarity);
match.put("order", order);
match.put("json_path", path);

socket.send(match.toString().getBytes(ZMQ.CHARSET), 0);
```

**Receiving Data**

To receive data, the program will need wait for the microservice to send the data. This is done automatically when the recv method of ZeroMq is used. After receiving the data in a byte array, the array will need to be converted back to a String using the charset UTF-8, and then converted to a JSONArray containing all the received JSONObjects from the microservice. This array can then be used to fulfill whatever needs. The following example shows the process of receving all of the data sent by the microservice and having it in one JSONArray, in which each JSONObject in the array is accessed and displayed. This is a snippet from Tester.java:
```
byte[] data = socket.recv();
JSONParser parser = new JSONParser();JSONArray array = null;
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
```

**UML Sequence Diagram**
<img width="379" alt="UML" src="https://github.com/user-attachments/assets/7936a40a-633e-4a8e-a5b2-e2c452e207f9" />
