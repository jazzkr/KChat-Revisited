package com.codeex.kchat;

import java.net.*;
import java.util.Scanner;
import java.io.PrintWriter;

public class Server extends Thread {

	Client client;
	ServerSocket ss;
	Socket s;
	Scanner in;
	PrintWriter out;


	public volatile boolean running = true;

	public Server(Client client) {
		this.client = client;
	}

	public void run() {
		
		try {
			ss = new ServerSocket(client.port);
			client.print("Server successfully created!");
		} catch (Exception e) {
			client.print("Error creating server!");
			client.print("Aborting server startup...");
			client.stop.doClick();
		}

		while (running) {
			
			try {
				s = ss.accept();
				client.print("Incoming connection...");
				in = new Scanner (s.getInputStream());
				out = new PrintWriter(s.getOutputStream());
			} catch (Exception e) {
				s = null;
				in = null;
				out = null;
				client.print("Incoming connection failed.");
				continue;
			}
			

			
		}
	}
}

