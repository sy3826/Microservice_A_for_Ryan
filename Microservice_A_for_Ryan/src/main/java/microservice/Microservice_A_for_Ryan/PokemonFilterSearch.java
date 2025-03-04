package microservice.Microservice_A_for_Ryan;

import org.zeromq.*;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

public class PokemonFilterSearch extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField portNumField;
	private JTextField pathField;
	
	JLabel statusLab;
	JLabel portStatusLab;
	JLabel log;
	
	JScrollPane scrollPane;
	
	Log logText;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PokemonFilterSearch frame = new PokemonFilterSearch();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public PokemonFilterSearch() {
		logText = new Log();
		
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 850, 900);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		// Fields for info needed for connection
		portNumField = new JTextField();
		portNumField.setBounds(220, 179, 124, 51);
		portNumField.setFont(new Font("Serif", Font.PLAIN, 20));
		contentPane.add(portNumField);
		portNumField.setColumns(10);
		
		pathField = new JTextField();
		pathField.setColumns(10);
		pathField.setBounds(220, 259, 488, 51);
		pathField.setFont(new Font("Serif", Font.PLAIN, 20));
		contentPane.add(pathField);
		
		// Labels
		JLabel titleLab = new JLabel("Pokemon Filter Search");
		titleLab.setHorizontalAlignment(SwingConstants.CENTER);
		titleLab.setBounds(195, 87, 446, 41);
		titleLab.setFont(new Font("Serif", Font.BOLD, 35));
		contentPane.add(titleLab);
		
		JLabel portLab = new JLabel("Port:");
		portLab.setHorizontalAlignment(SwingConstants.TRAILING);
		portLab.setFont(new Font("Serif", Font.BOLD, 20));
		portLab.setBounds(10, 179, 185, 41);
		contentPane.add(portLab);
		
		JLabel pathLab = new JLabel("Path:");
		pathLab.setHorizontalAlignment(SwingConstants.TRAILING);
		pathLab.setFont(new Font("Serif", Font.BOLD, 20));
		pathLab.setBounds(10, 259, 185, 41);
		contentPane.add(pathLab);
		
		statusLab = new JLabel("");
		statusLab.setBounds(23, 473, 256, 41);
		statusLab.setHorizontalAlignment(SwingConstants.CENTER);
		statusLab.setFont(new Font("Serif", Font.BOLD, 20));
		contentPane.add(statusLab);
		
		portStatusLab = new JLabel("Listening on port: ");
		portStatusLab.setHorizontalAlignment(SwingConstants.CENTER);
		portStatusLab.setFont(new Font("Serif", Font.BOLD, 20));
		portStatusLab.setBounds(23, 535, 256, 41);
		contentPane.add(portStatusLab);
		
		log = new JLabel();
		log.setVerticalAlignment(SwingConstants.TOP);
		log.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		log.setFont(new Font("Serif", Font.PLAIN, 15));
		log.setBounds(300, 473, 408, 336);		
		
		scrollPane = new JScrollPane(log);
		scrollPane.setBounds(300, 473, 408, 336);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		contentPane.add(scrollPane);
		
		// Button
		JButton submitBtn = new JButton("Submit");
		submitBtn.setBounds(596, 381, 112, 41);
		submitBtn.setHorizontalAlignment(SwingConstants.CENTER);
		submitBtn.setFont(new Font("Serif", Font.BOLD, 20));
		submitBtn.addActionListener(this);
		contentPane.add(submitBtn);
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		// Start receiving a connection
		new Communication(this, this.portNumField.getText(), this.pathField.getText(), logText);
	}
}

// Wait for connections and communicate
class Communication {	
	int port;
	
	JSONArray array;
	JSONArray pokemonAll;
	JSONObject filters;
	
	// Filters
	String name;
	String type;
	String rarity;
	String collection;
	String set;
	String order;
	
	@SuppressWarnings("unchecked")
	public Communication(PokemonFilterSearch pfs, String portStr, String pathStr, Log log) {
		// Setup port and start listening
		while (true) {
			// Get port and path
			try {
				JSONParser parser = (JSONParser) new JSONParser();
				array = (JSONArray) parser.parse(new FileReader(pathStr));
				port = Integer.parseInt(portStr);
			} catch (Exception e) {
				pfs.statusLab.setText("Error in port number or path");
				e.printStackTrace();
				log.addLine(e.toString(), pfs);
				log.addLine("", pfs);
				this.reset(pfs);
				break;
			}
			
			// Communication
			try (ZContext context = new ZContext()) {
				ZMQ.Socket socket = context.createSocket(SocketType.REP);
				socket.bind("tcp://*:" + port);
				
				// Waiting
				pfs.statusLab.setText("Waiting...");
				pfs.portStatusLab.setText("Listening on port: " + port);
				this.reset(pfs);
				byte[] request = socket.recv();
				
				// If connection is being closed
				String check = new String(request, ZMQ.CHARSET);
				if (check.equals("exit")) {
					break;
				}
				
				// Convert request to json
				JSONParser parser = (JSONParser) new JSONParser();
				filters = (JSONObject) parser.parse(new String(request, ZMQ.CHARSET));
				
				// Extract the filters
				this.extractFilter(filters);
				log.addLine("Recieved filters to search Pokemons", pfs);
				log.addLine("Recieved: " + name + ", " + type + ", " + rarity + ", " + 
							collection + ", " + set + " " + order, pfs);
				this.reset(pfs);
				
				// Extract pokemons from array
				pokemonAll = new JSONArray();
				for (Object obj: array) {
					JSONObject tempObj = (JSONObject) obj;
					JSONArray tempArr = (JSONArray) tempObj.get("card_list");
					pokemonAll.addAll(tempArr);
				}
				
				// Find and return pokemons according to filter
				ArrayList<Pokemon> pokemons = new ArrayList<>();
				for (Object obj: pokemonAll) {
					JSONObject jsonObj = (JSONObject) obj;
					String objName = (String) jsonObj.get("card_name");
					String objType = (String) jsonObj.get("card_type");
					String objRarity = (String) jsonObj.get("card_rarity");
					String objCollection = (String) jsonObj.get("collection_name");
					String objSet = (String) jsonObj.get("set_name");
					String objPrice = (String) jsonObj.get("market_price");
					pokemons.add(new Pokemon(objName, objType, objRarity, objCollection, 
												objSet, Double.parseDouble(objPrice), 
												(String) jsonObj.get("iamge_path"), (String) jsonObj.get("comments")));
				}
				
				// Find matching
				log.addLine("Matching Pokemon(s):", pfs);
				ArrayList<Pokemon> matches = new ArrayList<>();
				for (Pokemon obj: pokemons) {
					if (name != "" && !name.equalsIgnoreCase(obj.name)) {
						continue;
					}
					if (type != "" && !type.equalsIgnoreCase(obj.type)) {
						continue;
					}
					if (rarity != "" && !rarity.equalsIgnoreCase(obj.rarity)) {
						continue;
					}
					if (collection != "" && !collection.equalsIgnoreCase(obj.collection)) {
						continue;
					}
					if (set != "" && !set.equalsIgnoreCase(obj.set)) {
						continue;
					}
					matches.add(obj);
					log.addLine(obj.name, pfs);
				}
				
				// Sort matches
				matches.sort((o1, o2) -> Double.compare(o1.price, o2.price));
				if (order.equals("decreasing")) {
					Collections.reverse(matches);
				}
				
				// Return json file with matching pokemons
				JSONArray result = new JSONArray();
				for (Pokemon obj: matches) {
					JSONObject match = new JSONObject();
					match.put("card_name", obj.name);
					match.put("colection_name", obj.collection);
					match.put("set_name", obj.set);
					match.put("card_type", obj.type);
					match.put("card_rarity", obj.rarity);
					match.put("market_price", Double.toString(obj.price));
					match.put("image_path", obj.path);
					match.put("comments", obj.comments);
					result.add(match);
				}
				
				// Send reply
				socket.send(result.toString().getBytes(ZMQ.CHARSET), 0);
				log.addLine("Completed request and replied", pfs);
				log.addLine("", pfs);
				
				
			} catch (Exception e) {
				pfs.statusLab.setText("Error in communication");
				e.printStackTrace();
				log.addLine(e.toString(), pfs);
				log.addLine("", pfs);
				this.reset(pfs);
				break;
			}
		}
	}
	
	// Extract the desired filters
	void extractFilter(JSONObject object) {
		name = (String) object.get("card_name");
		type = (String) object.get("card_type");
		rarity = (String) object.get("card_rarity");
		collection = (String) object.get("collection_name");
		set = (String) object.get("set_name");
		order = (String) object.get("order");
	}
	
	// Revalidate and repaint frame
	void reset(PokemonFilterSearch pfs) {
		pfs.revalidate();
		pfs.repaint();
	}	
}

// Keep a log
class Log {
	private String log;
	
	public Log() {
		log = "";
	}
	
	void addLine(String line, PokemonFilterSearch pfs) {
		log += "<br/>" + line;
		pfs.log.setText("<html>" + log + "</html");
		pfs.scrollPane.getVerticalScrollBar().setValue(pfs.scrollPane.getVerticalScrollBar().getMaximum());
	}
}

// Pokemon objects
class Pokemon {
	String name, type, rarity, collection, set, path, comments;
	double price;
	
	public Pokemon(String name, String type, String rarity, String collection, String set, 
					double price, String path, String comments) {
		this.name = name;
		this.type = type;
		this.rarity = rarity;
		this.collection = collection;
		this.set = set;
		this.price = price;
		this.path = path;
		this.comments = comments;
	}
}
