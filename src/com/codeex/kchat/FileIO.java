package com.codeex.kchat;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class FileIO {

	private static final String file = ".KCHAT.txt";

	FileWriter outputStream;

	//FOR DEBUG PURPOSES...
	static String testUsername = "Tester121212";
	static String[] testServers = { "192.168.1.1", "192.168.1.2", "192.168.1.3", "192.168.1.4", "192.168.1.5" };
	static Integer[] testPorts = { 5491, 6849, 1234, 5647, 5555 };
	static String[] testBanlist = { "111.111.111.111", "222.222.222.222", "333.333.333.333", "444.444.444.444", "555.555.555.555" };

	//Constructor deletes previous file to avoid errors
	public FileIO() {
		File info = new File(file);
		if (info.exists()) {
			info.delete();
		}
	}

/*	public static void main(String[] args) {
		FileIO fio = new FileIO();
		fio.writeUsername(testUsername);
		fio.writeServers(testServers, testPorts);
		fio.writeBanlist(testBanlist);
		String[] test = fio.getData();
		for (String str : test) {
			System.out.println(str);
		}
	}*/

	public void writeUsername(String username) throws IOException {
		outputStream = new FileWriter(file);

		if (isWindows()) // If windows hide file
			Runtime.getRuntime().exec("attrib +H " + file);

		outputStream.write("USER:" + username);
		outputStream.write(" ");
		outputStream.close();

	}

	public void writeServers(String[] servers, Integer[] ports) throws IOException {
		outputStream = new FileWriter(file, true);

		if (isWindows()) // If windows hide file
			Runtime.getRuntime().exec("attrib +H " + file);

		for (int i = 0; i < servers.length; i++) {
			outputStream.write("FS:" + servers[i]);
			outputStream.write(" ");
			outputStream.write("P:" + ports[i].toString());
			outputStream.write(" ");
		}

		outputStream.close();
	}

	public void writeBanlist(String[] banlist) throws IOException {
		outputStream = new FileWriter(file, true);

		if (isWindows()) // If windows hide file
			Runtime.getRuntime().exec("attrib +H " + file);

		for (int i = 0; i < banlist.length; i++) {
			outputStream.write("BL:" + banlist[i]);
			outputStream.write(" ");
		}

		outputStream.close();
	}

	public String[] getData() {
		ArrayList<String> alData = new ArrayList<String>();
		String[] data;
		Scanner in = null;
		try {
			in = new Scanner(new FileInputStream(file));

			while (true) {
				if (in.hasNext()) {
					alData.add(in.next());
				} else
					break;
			}
			data = (String[]) alData.toArray(new String[alData.size()]);
		} catch (Exception e) {
			data = null;
		} finally {
			in.close();
		}
		return data;
	}

	//Simple method to check if os is windows
	private static boolean isWindows() {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.substring(0, 3).equals("win"))
			return true;
		else
			return false;
	}

}
