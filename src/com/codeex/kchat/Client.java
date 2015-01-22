package com.codeex.kchat;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.net.*;
import java.util.*;
import java.io.InputStream;
import java.io.PrintWriter;

public class Client extends JFrame implements ActionListener, KeyListener {
	private static final long serialVersionUID = 1L;

	public static final String VERSION = "0.1b";
	private static final String newline = System.getProperty("line.separator");
	private static final String MASTERSERVER = "192.168.0.129";
	private static final int MSPORT = 5555;
	
	private static Client client;
	private Server server;

	private JFrame splash;
	private JMenu menu;
	private DefaultListModel servers, favServers, clients;
	private JTextField username, username2, ipaddress, portField, consoleCommand;
	private JTabbedPane tabbedPane;
	private JTextArea console;
	private JButton start;
	public JButton stop;
	
	private JCheckBox masterServer, rememberMe, rememberMe2;
	private ImageIcon logo;

	private volatile boolean tryToConnect = false;
	private volatile boolean serverStartWithRegister = false;
	private volatile boolean portTaken = false;
	public int port = 5555;
	private String savedUsername = "nosave";
	private String lastConsoleCommand = "";
	private ArrayList<String> IpAddress;
	private ArrayList<Integer> Port;
	private ArrayList<String> FavouriteIps;
	private ArrayList<Integer> FavouritePorts;
	private ArrayList<String> BanList;

	public Client(String titleBar) {
		super(titleBar);

		logo = new ImageIcon(Client.class.getResource("/com/codeex/kchat/logo.png"));

		//SPLASH SCREEN START
		splash = new JFrame();
		splash.setSize(200, 100);
		splash.setLocationRelativeTo(null);
		splash.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		splash.setUndecorated(true);
		splash.setResizable(false);
		splash.setLayout(new BorderLayout());

		JLabel splashlogo = new JLabel(logo);
		splashlogo.setAlignmentX(CENTER_ALIGNMENT);
		splash.add(splashlogo, BorderLayout.CENTER);
		splash.setVisible(true);
		//SPLASH SCREEN END

		try {
			//Set system look and feel, so it looks normal and not like java
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		//Do these things before window is closed...
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				FileIO fio = new FileIO();
				try {
					fio.writeUsername(savedUsername);

					String[] servers = FavouriteIps.toArray(new String[FavouriteIps.size()]);
					Integer[] ports = FavouritePorts.toArray(new Integer[FavouritePorts.size()]);
					fio.writeServers(servers, ports);

					String[] banlistString = BanList.toArray(new String[BanList.size()]);
					fio.writeBanlist(banlistString);
					System.exit(0);
				} catch (Exception ex) {
					int n = JOptionPane.showConfirmDialog(null, "Error saving data, exit anyway?", "An Error Has Occurred", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null);
					if (n == JOptionPane.YES_OPTION) {
						System.exit(0);
					}
				}
			}
		});

		setSize(500, 400);
		setResizable(true);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());

		JMenuBar menuBar = new JMenuBar();

		menu = new JMenu("File");

		//JMenuItems go here...

		menuBar.add(menu);

		menu = new JMenu("Edit");

		//JMenuItems go here...

		menuBar.add(menu);

		menu = new JMenu("Options");

		//JMenuItems go here...

		menuBar.add(menu);

		menu = new JMenu("Help");

		//JMenuItems go here...

		menuBar.add(menu);

		setJMenuBar(menuBar);

		//Code for tabbed panes...
		tabbedPane = new JTabbedPane();
		JPanel serverlistPanel = new JPanel();
		JPanel ipconnectPanel = new JPanel();

		///////////////////////////////////////////////////////
		///////////////SERVERLISTPANEL TAB START///////////////
		///////////////////////////////////////////////////////

		//Server list on the left side
		servers = new DefaultListModel();
		JList serverList = new JList(servers);
		serverList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		serverList.setLayoutOrientation(JList.VERTICAL);
		JScrollPane listScroller = new JScrollPane(serverList);

		//Panel with various buttons etc on the right
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));
		rightPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
		rightPanel.setPreferredSize(new Dimension(300, 250));
		JLabel welcome = new JLabel("KChat " + VERSION);
		welcome.setAlignmentX(CENTER_ALIGNMENT);
		JLabel credits = new JLabel("Made by Krisztian Kurucz");
		credits.setAlignmentX(CENTER_ALIGNMENT);
		JTextPane instructions = new JTextPane();
		instructions.setText("To join a server please select one on the right, then specify a username, and finally press connect.");
		instructions.setBackground(null);
		instructions.setEditable(false);
		instructions.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
		instructions.setAlignmentX(CENTER_ALIGNMENT);

		//New JPanel to group username controls together, within usernameAndRememberMe
		JPanel usernamePanel = new JPanel();
		usernamePanel.setLayout(new BoxLayout(usernamePanel, BoxLayout.LINE_AXIS));
		JLabel userLabel = new JLabel(" Username:");
		username = new JTextField(20);
		username.setDocument(new TextFieldLimit(12));
		//username.setPreferredSize(new Dimension (20,20));
		username.setMaximumSize(new Dimension(100, 20));
		usernamePanel.add(userLabel);
		usernamePanel.add(Box.createRigidArea(new Dimension(3, 0)));
		usernamePanel.add(username);

		//JPanel with ALL username components (ie remember me checkbox)
		JPanel usernameAndRememberMe = new JPanel();
		usernameAndRememberMe.setLayout(new BoxLayout(usernameAndRememberMe, BoxLayout.PAGE_AXIS));
		rememberMe = new JCheckBox("Remember Me");
		rememberMe.setAlignmentX(CENTER_ALIGNMENT);
		usernameAndRememberMe.add(usernamePanel);
		usernameAndRememberMe.add(Box.createRigidArea(new Dimension(0, 5)));
		usernameAndRememberMe.add(rememberMe);
		usernameAndRememberMe.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null), BorderFactory.createEmptyBorder(1, 1, 1, 1)));

		JButton connect = new JButton("Connect!");
		connect.setAlignmentX(CENTER_ALIGNMENT);
		connect.addActionListener(this);
		//connect.setMaximumSize(new Dimension(80,20));

		//Add all components to rightPanel...
		rightPanel.add(welcome);
		rightPanel.add(credits);
		rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		rightPanel.add(instructions);
		rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		rightPanel.add(usernameAndRememberMe);
		rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		rightPanel.add(connect);

		//Splitpane to separate list and rightpanel from each other
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroller, rightPanel);
		splitPane.setDividerLocation(150);

		//////////////////////////////////////////////////////////////
		/////////////////////SERVERLISTPANEL TAB END ////////////////
		/////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////
		/////////////////IPCONNECT TAB START//////////////////////////
		//////////////////////////////////////////////////////////////

		//Panel on the left for favourite servers...
		JPanel favouritesPane = new JPanel();
		favouritesPane.setLayout(new BoxLayout(favouritesPane, BoxLayout.PAGE_AXIS));
		JLabel favourites = new JLabel("Favourites");
		favourites.setAlignmentX(CENTER_ALIGNMENT);
		//Set up a favourite server list on the left
		favServers = new DefaultListModel();
		JList favServerList = new JList(favServers);
		favServerList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		favServerList.setLayoutOrientation(JList.VERTICAL);
		JScrollPane favScroller = new JScrollPane(favServerList);
		favouritesPane.add(favourites);
		favouritesPane.add(Box.createRigidArea(new Dimension(0, 5)));
		favouritesPane.add(favScroller);

		//Panel on right for manual connect and adding to favourites
		JPanel manualPanel = new JPanel();
		manualPanel.setLayout(new BoxLayout(manualPanel, BoxLayout.PAGE_AXIS));
		manualPanel.setPreferredSize(new Dimension(300, 250));
		JLabel ipconn = new JLabel("Manual IP Address Connect");
		ipconn.setAlignmentX(CENTER_ALIGNMENT);
		JTextPane ipconninstructions = new JTextPane();
		ipconninstructions.setText("To join a server please specify one below along with port number, then specify a username, and finally press connect.");
		ipconninstructions.setBackground(null);
		ipconninstructions.setEditable(false);
		ipconninstructions.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
		ipconninstructions.setAlignmentX(CENTER_ALIGNMENT);
		JPanel IPandPort = new JPanel();
		IPandPort.setLayout(new BoxLayout(IPandPort, BoxLayout.PAGE_AXIS));
		IPandPort.setAlignmentX(CENTER_ALIGNMENT);
		IPandPort.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
		JPanel IP = new JPanel();
		IP.setLayout(new BoxLayout(IP, BoxLayout.LINE_AXIS));
		JLabel ipad = new JLabel("IP Address: ");
		ipaddress = new JTextField();
		ipaddress.setMaximumSize(new Dimension(100, 20));
		IP.add(ipad);
		IP.add(ipaddress);
		JPanel port = new JPanel();
		port.setLayout(new BoxLayout(port, BoxLayout.LINE_AXIS));
		JLabel portL = new JLabel("Port: ");
		portField = new JTextField(5);
		portField.setDocument(new TextFieldLimit(4));
		portField.setMaximumSize(new Dimension(50, 20));
		port.add(portL);
		port.add(portField);
		IPandPort.add(IP);
		IPandPort.add(Box.createRigidArea(new Dimension(0, 5)));
		IPandPort.add(port);

		//New JPanel to group username controls together, within usernameAndRememberMe
		JPanel usernamePanel2 = new JPanel();
		usernamePanel2.setLayout(new BoxLayout(usernamePanel2, BoxLayout.LINE_AXIS));
		JLabel userLabel2 = new JLabel(" Username:");
		username2 = new JTextField(15);
		username2.setDocument(new TextFieldLimit(12));
		//username2.setPreferredSize(new Dimension (20,20));
		username2.setMaximumSize(new Dimension(100, 20));
		usernamePanel2.add(userLabel2);
		usernamePanel2.add(Box.createRigidArea(new Dimension(3, 0)));
		usernamePanel2.add(username2);

		//JPanel with ALL username components (ie remember me checkbox)
		JPanel usernameAndRememberMe2 = new JPanel();
		usernameAndRememberMe2.setLayout(new BoxLayout(usernameAndRememberMe2, BoxLayout.PAGE_AXIS));
		rememberMe2 = new JCheckBox("Remember Me");
		rememberMe2.setAlignmentX(CENTER_ALIGNMENT);
		usernameAndRememberMe2.add(usernamePanel2);
		usernameAndRememberMe2.add(Box.createRigidArea(new Dimension(0, 5)));
		usernameAndRememberMe2.add(rememberMe2);
		usernameAndRememberMe2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null), BorderFactory.createEmptyBorder(1, 1, 1, 1)));

		JPanel favOrConnect = new JPanel();
		favOrConnect.setLayout(new BoxLayout(favOrConnect, BoxLayout.LINE_AXIS));
		favOrConnect.setAlignmentX(CENTER_ALIGNMENT);
		JButton favButton = new JButton("Add Favourite");
		favButton.addActionListener(this);
		JButton connect2 = new JButton("Connect!");
		connect2.setAlignmentX(CENTER_ALIGNMENT);
		connect2.addActionListener(this);
		favOrConnect.add(favButton);
		favOrConnect.add(Box.createRigidArea(new Dimension(5, 0)));
		favOrConnect.add(connect2);

		manualPanel.add(ipconn);
		manualPanel.add(ipconninstructions);
		manualPanel.add(IPandPort);
		manualPanel.add(usernameAndRememberMe2);
		manualPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		manualPanel.add(favOrConnect);

		JSplitPane splitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, favouritesPane, manualPanel);
		splitPane2.setDividerLocation(150);

		ipconnectPanel.add(splitPane2, BorderLayout.CENTER);

		///////////////////////////////////////////////////////
		////////////////IPCONNECT TAB END/////////////////////
		///////////////////////////////////////////////////////

		///////////////////////////////////////////////////////
		///////////////CREATE SERVER TAB START/////////////////
		///////////////////////////////////////////////////////

		JPanel createPanel = new JPanel();
		//Create a client list that shows people logged in to server...
		JPanel clientListPanel = new JPanel();
		clientListPanel.setLayout(new BoxLayout(clientListPanel, BoxLayout.PAGE_AXIS));
		JLabel clientsL = new JLabel("Clients");
		clientsL.setAlignmentX(CENTER_ALIGNMENT);
		clients = new DefaultListModel();
		JList clientList = new JList(clients);
		JScrollPane clientScroller = new JScrollPane(clientList);
		clientListPanel.add(clientsL);
		clientListPanel.add(clientScroller);

		//Create a right panel with various controls for server...
		JPanel consolePanel = new JPanel();
		consolePanel.setLayout(new BoxLayout(consolePanel, BoxLayout.PAGE_AXIS));
		JLabel createLabel = new JLabel("Create A Server");
		createLabel.setAlignmentX(CENTER_ALIGNMENT);
		console = new JTextArea();
		console.setEditable(false);
		console.setFont(new Font("Sans Serif", Font.PLAIN, 11));
		JScrollPane consoleScroller = new JScrollPane(console);
		consoleScroller.setPreferredSize(new Dimension(300,100));
		consoleCommand = new JTextField();
		consoleCommand.addKeyListener(this);
		JPanel consoleWindow = new JPanel();
		consoleWindow.setLayout(new BoxLayout(consoleWindow, BoxLayout.PAGE_AXIS));
		consoleWindow.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(null, "Console", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
		consoleWindow.add(consoleScroller, BorderLayout.CENTER);
		consoleWindow.add(consoleCommand, BorderLayout.PAGE_END);

		JPanel serverControls = new JPanel();
		serverControls.setLayout(new BoxLayout(serverControls, BoxLayout.PAGE_AXIS));
		serverControls.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(null, "Server Controls", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
		JPanel startAndStop = new JPanel();
		startAndStop.setLayout(new BoxLayout(startAndStop, BoxLayout.LINE_AXIS));
		start = new JButton("Start");
		start.addActionListener(this);
		stop = new JButton("Stop");
		stop.addActionListener(this);
		stop.setEnabled(false);
		JButton msCommand = new JButton("MS Command");
		msCommand.addActionListener(this);
		startAndStop.add(start);
		startAndStop.add(stop);
		startAndStop.add(msCommand);
		startAndStop.setAlignmentX(CENTER_ALIGNMENT);
		masterServer = new JCheckBox("Register with Master Server");
		masterServer.setAlignmentX(CENTER_ALIGNMENT);
		serverControls.add(startAndStop);
		serverControls.add(masterServer);

		JPanel clientControls = new JPanel();
		clientControls.setLayout(new BoxLayout(clientControls, BoxLayout.LINE_AXIS));
		clientControls.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(null, "Client Controls", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
		JButton kick = new JButton("Kick");
		kick.addActionListener(this);
		JButton ban = new JButton("Ban");
		ban.addActionListener(this);
		JButton banlist = new JButton("Ban List");
		clientControls.add(kick);
		clientControls.add(ban);
		clientControls.add(banlist);
		clientControls.setAlignmentX(CENTER_ALIGNMENT);

		consolePanel.add(createLabel);
		consolePanel.add(consoleWindow);
		consolePanel.add(serverControls);
		consolePanel.add(clientControls);

		JSplitPane splitPane3 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, clientListPanel, consolePanel);
		splitPane3.setDividerLocation(120);
		createPanel.add(splitPane3, BorderLayout.CENTER);

		///////////////////////////////////////////////////////
		///////////////CREDITS TAB START//////////////////////
		//////////////////////////////////////////////////////

		JPanel creditsPanel = new JPanel();
		creditsPanel.setLayout(new BoxLayout(creditsPanel, BoxLayout.PAGE_AXIS));
		JTextPane creditsText = new JTextPane();
		creditsText.setEditable(false);
		creditsText.setText("Coded by Krisztian Kurucz. Project was begun in November 2011, probably never to be completed. Thanks goes out to Alex Mitterhauser for moral support during the coding of this monstrosity. This is my first attempt at anything useful in Java, so go easy everybody. " + "My hope is that one day there will be a support website available for this where you all can report the inevitable bugs that will occur... If anyone uses this of course. Dated December 26th, 2011, marking the GUI completion milestone of version 0.1b (b for beta!).");
		creditsText.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(null, "Credits", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
		ImageIcon icon = new ImageIcon(Client.class.getResource("/com/codeex/kchat/logo.png"));
		JLabel logo = new JLabel(icon);
		logo.setAlignmentX(CENTER_ALIGNMENT);
		JLabel version = new JLabel("VERSION: " + VERSION);
		version.setForeground(Color.RED);
		version.setAlignmentX(CENTER_ALIGNMENT);
		creditsPanel.add(creditsText, BorderLayout.CENTER);
		creditsPanel.add(logo, BorderLayout.CENTER);
		creditsPanel.add(version);

		////////////////////////////////////////////////////////
		//////////////////CREDITS TAB END///////////////////////
		///////////////////////////////////////////////////////

		//Add everything to tabbed panes...
		serverlistPanel.add(splitPane, BorderLayout.CENTER);
		tabbedPane.addTab("Server List", serverlistPanel);
		tabbedPane.addTab("Manual Connect", ipconnectPanel);
		tabbedPane.addTab("Create Server", createPanel);
		tabbedPane.addTab("Credits", creditsPanel);

		add(tabbedPane);

	}

	public static void main(String[] args) {
		client = new Client("KChat " + VERSION);
		client.fillServerList();
		client.fillFavServerList();
		client.fillClientList();
		client.BanList = new ArrayList<String>();

		//Set checkboxes appropriately if already have saved username
		if (!client.savedUsername.equals("nosave")) {
			client.rememberMe.setSelected(true);
			client.rememberMe2.setSelected(true);
		}
		client.splash.setVisible(false);
		client.splash.dispose();
		client.setVisible(true);
		
	}

	public void print(String text) {
		console.append(newline + text);
		console.setCaretPosition(console.getDocument().getLength());
	}

	//Method that populates the DefaultListModel server with actual servers from master server
	private void fillServerList() {
		//Reset all when this is called
		try {
			servers.clear();
			IpAddress = new ArrayList<String>();
			Port = new ArrayList<Integer>();
		} catch (Exception e) {
		}

		Socket socket;
		Scanner in;
		PrintWriter out;
		try {
			socket = new Socket(InetAddress.getByName(MASTERSERVER), MSPORT);
			in = new Scanner(socket.getInputStream());
			out = new PrintWriter(socket.getOutputStream(), true);
		} catch (Exception e) {
			socket = null;
			JOptionPane.showMessageDialog(this, "Error retrieving server list!");
			servers.addElement("Error Connecting!");
			return;
		}

		out.println("/printclients"); // Request clients from master server
		String allClients = in.nextLine(); //Receives answer from master server in one string
		try {
			socket.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Socket to master server could not be closed!");
		}
		String[] stringIps = allClients.split(" ");

		for (String stringip : stringIps) { //Evaluate IP Addresses to separate ip & port starting at 1 because 0 is /clients
			if (stringip.equals("/clients")) {
				continue;
			}

			String[] tempStore;
			try {
				tempStore = stringip.split(":", 2);
				if (tempStore[0].equals("NoServersFound")) {
					throw new Exception();
				}
			} catch (Exception e) {
				//If it has no port to separate then it is null
				tempStore = null;
			}

			if (tempStore != null) {
				IpAddress.add(tempStore[0]);
				Port.add(Integer.parseInt(tempStore[1]));
			} else if (tempStore == null) {
				IpAddress.add(stringip);
			}
		}
		//Add elements from IpAddress arraylist to server list
		for (int i = 0; i < IpAddress.size(); i++) {
			servers.addElement(IpAddress.get(i));
		}

	}

	private void fillFavServerList() {
		FavouriteIps = new ArrayList<String>();
		FavouritePorts = new ArrayList<Integer>();
		//Code to load favourite servers from a file potentially...
		//would have to create an array of INetAddresses or plain string IP Addresses!!
		favServers.addElement("No favourites specified.");
	}

	private void fillClientList() {
		//Code that determines whether server is online and fills the list initially...
		clients.addElement("No clients connected");
	}

	//Internal code for checking validity of a entered username
	private boolean checkUsername(String username) {
		char[] invalidChars = { ' ', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '=', '+', '|', '.', ',', '~', '`', '/', '<', '>', ':', ';', '\'', '"', '?' };
		for (int i = 0; i < username.length(); i++) {
			for (int ii = 0; ii < invalidChars.length; ii++) {
				if (username.charAt(i) == invalidChars[ii]) {
					return false;
				}
			}
		}
		return true;
	}

	private String returnIP() {
		String serverIP;
		try {
			URL getIP = new URL("http://automation.whatismyip.com/n09230945.asp");
			InputStream input = getIP.openStream();
			Scanner in = new Scanner(input);
			serverIP = in.nextLine(); //Capture server IP in static variable for potential use later.
			input.close();
			return serverIP;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Could not determine IP Address!");
			return "Error";
		}
	}

	//Simple method to send a single message to a server/client that should make up the framework
	//of this chat program
	private String sendMsg(String msg, String ipAddress, int port) {
		Socket socket;
		PrintWriter out;
		Scanner in;
		String reply;
		try {
			socket = new Socket(InetAddress.getByName(ipAddress), port);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new Scanner(socket.getInputStream());
			out.println(msg);
			reply = in.nextLine();
			return reply;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error! Could not communicate.");
			return "Error connecting to master server!";
		}
	}

	private void newServer(boolean register) {
		//console.setText("");
		console.append("Welcome to KChat Server v1.00!");
		print("Server is now initializing...");
		print("Your IP Address is: " + returnIP() + ".");
		print("Server is listening on port "+port+".");

		if (register) {
			print("Registering with master server...");
			String reply = sendMsg("/register " + returnIP() + ":" + port, MASTERSERVER, MSPORT);
			print(reply);
		}
		
		server = new Server(client);
		server.start();
	}

	public void actionPerformed(ActionEvent e) {
		if (((JButton) e.getSource()).getText().equals("Connect!")) {
			
			//Check for valid username
			if (!checkUsername(username.getText())) {
				JOptionPane.showMessageDialog(this, "Username is not valid. No special characters are allowed.");
				return;
			}
			
			//Look to see if username should be remembered
			if (rememberMe.isSelected()) {
				savedUsername = username.getText();
			} else if (rememberMe2.isSelected()) {
				savedUsername = username2.getText();
			}
			
			tryToConnect = true;

		} else if (((JButton) e.getSource()).getText().equals("Add Favourite")) {
			//Only execute if both are filled out somehow
			if (!ipaddress.getText().equals("") && !portField.getText().equals("")) {
				//Get the ip and port from fields
				String serverIP = ipaddress.getText();
				String stringPort = portField.getText();
				int serverPort = 0;
				try {
					//Test if it is valid port
					serverPort = Integer.parseInt(stringPort); 
				}
				catch (Exception ex) {
					JOptionPane.showMessageDialog(this, "Port invalid!");
					return;
				}
				
				boolean duplicate = false;
				try {
					InetAddress.getByName(serverIP); //Test if it is valid IP
					

					//Test for duplicates
					for (int i = 0; i < favServers.size(); i++) {
						if (serverIP.equals(favServers.get(i))) {
							duplicate = true;
							JOptionPane.showMessageDialog(this, "Duplicate IP Address!");
						}
					}

					if (favServers.get(0).equals("No favourites specified.") && duplicate == false) {
						favServers.remove(0);
					}

					//Add it to favourites list if not a duplicate
					if (duplicate == false) {
						FavouriteIps.add(serverIP);
						FavouritePorts.add(serverPort);
						favServers.addElement(serverIP);
					}
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(this, "IP Address invalid!");
				}

			} else {
				JOptionPane.showMessageDialog(this, "No IP/Port Specified!");
			}

		} else if (((JButton) e.getSource()).getText().equals("Start")) {
			stop.setEnabled(true);
			start.setEnabled(false);
			masterServer.setEnabled(false);
			tabbedPane.setEnabled(false);

			if (masterServer.isSelected()) {
				newServer(true);
			} else {
				newServer(false);
			}

		} else if (((JButton) e.getSource()).getText().equals("Stop")) {
			print("Server is now shutting down...");
			start.setEnabled(true);
			stop.setEnabled(false);
			masterServer.setEnabled(true);
			tabbedPane.setEnabled(true);
			
			server.running = false;

		} else if (((JButton) e.getSource()).getText().equals("MS Command")) {

		} else if (((JButton) e.getSource()).getText().equals("Kick")) {

		} else if (((JButton) e.getSource()).getText().equals("Ban")) {

		} else if (((JButton) e.getSource()).getText().equals("Ban List")) {

		}
	}

	public void keyPressed(KeyEvent e) {

	}

	public void keyReleased(KeyEvent e) {
		String[] split;
		if (KeyEvent.getKeyText(e.getKeyCode()).equals("Enter")) {	
			//If its not empty and it starts with a slash split it
			if (!consoleCommand.getText().equals("") && consoleCommand.getText().charAt(0) == '/') {
				try {
					split = consoleCommand.getText().split(" ", 2);
				} catch (Exception ex) {
					split = null;
				}
				if (!split[1].isEmpty() && split[1].length() <= 4) {
					try {
						Integer.parseInt(split[1]);
						print("Port set successfully to "+split[1]+".");
						portTaken = true;
					} catch (Exception ex) {
						print("Port invalid.");
						portTaken = false;
					}
				}
			}
			if (!consoleCommand.getText().equals("")) {
			lastConsoleCommand = consoleCommand.getText();
			print(lastConsoleCommand);
			}
			consoleCommand.setText("");
		}
	}

	public void keyTyped(KeyEvent e) {

	}

}
