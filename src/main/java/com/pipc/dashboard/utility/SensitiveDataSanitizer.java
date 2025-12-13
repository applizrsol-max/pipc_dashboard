package com.pipc.dashboard.utility;

import java.util.Iterator;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class SensitiveDataSanitizer {

	private final Set<String> sensitiveKeys = Set.of("password", "passwd", "pwd", "pass", "secret", "secretKey",
			"token", "accessToken", "refreshToken", "otp", "pin");

	public JsonNode sanitize(JsonNode node) {

		if (node == null)
			return null;

		if (node.isObject()) {
			ObjectNode obj = (ObjectNode) node;
			Iterator<String> fieldNames = obj.fieldNames();

			while (fieldNames.hasNext()) {
				String field = fieldNames.next();

				if (isSensitive(field)) {
					obj.put(field, "***"); // MASK SENSITIVE VALUE
				} else {
					sanitize(obj.get(field)); // recursive clean
				}
			}
		}

		if (node.isArray()) {
			for (JsonNode child : node) {
				sanitize(child);
			}
		}

		return node;
	}

	private boolean isSensitive(String key) {
		return sensitiveKeys.stream().anyMatch(k -> k.equalsIgnoreCase(key));
	}
}