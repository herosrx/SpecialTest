package com.cocoatest.st.utils;

import javax.mail.*;

public class CustomizedAuthenticator extends Authenticator {
	String userName = null;
	String password = null;

	public CustomizedAuthenticator() {
	}

	public CustomizedAuthenticator(String username, String password) {
		this.userName = username;
		this.password = password;
	}

	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(userName, password);
	}
}
