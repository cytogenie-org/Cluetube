package com.help;

import java.io.IOException;

public class Test {

	public static void main(String[] args) {
		String  userHomePath = System.getProperty("user.home");
		String cmd = "\"" + userHomePath + "\\Cluetube\\lib\\cluetube.jar\"";
		String[] arg = new String[2];
		args[0] = "open";
		args[1] = "/Applications/AutoGate.app/Contents/Resources/Java/AutoGate.app";
		try {
			Runtime.getRuntime().exec("java -jar " + cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
