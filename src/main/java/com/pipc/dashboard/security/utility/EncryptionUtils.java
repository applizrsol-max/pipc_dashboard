package com.pipc.dashboard.security.utility;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class EncryptionUtils {

	public static String sha512(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

			StringBuilder hexString = new StringBuilder();
			for (byte b : bytes) {
				String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();

		} catch (Exception e) {
			throw new RuntimeException("Error generating SHA-512 hash", e);
		}
	}
}
