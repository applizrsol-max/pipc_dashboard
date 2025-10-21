package com.pipc.dashboard.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Utility class for JSON merge and change detection. Supports nested objects
 * and arrays.
 */
public class JsonUtils {

	private static final ObjectMapper mapper = new ObjectMapper();

	/**
	 * Recursively merges incoming JSON into existing JSON. Returns true if any
	 * changes are detected.
	 */
	public static boolean mergeAndDetectChanges(JsonNode existingNode, JsonNode incomingNode) {
		if (existingNode == null || existingNode.isNull()) {
			return true; // entire node is new
		}
		if (incomingNode == null || incomingNode.isNull()) {
			return false; // nothing to merge
		}

		boolean changed = false;

		if (existingNode.isObject() && incomingNode.isObject()) {
			ObjectNode existingObj = (ObjectNode) existingNode;
			ObjectNode incomingObj = (ObjectNode) incomingNode;

			for (String fieldName : iterable(incomingObj.fieldNames())) {
				JsonNode newValue = incomingObj.get(fieldName);
				JsonNode oldValue = existingObj.get(fieldName);

				if (oldValue == null || !oldValue.equals(newValue)) {
					if (oldValue != null && oldValue.isObject() && newValue.isObject()) {
						boolean nestedChange = mergeAndDetectChanges(oldValue, newValue);
						if (nestedChange)
							changed = true;
					} else if (oldValue != null && oldValue.isArray() && newValue.isArray()) {
						boolean arrayChange = mergeArray((ArrayNode) oldValue, (ArrayNode) newValue);
						if (arrayChange)
							changed = true;
					} else {
						existingObj.set(fieldName, newValue);
						changed = true;
					}
				}
			}
		} else if (existingNode.isArray() && incomingNode.isArray()) {
			changed = mergeArray((ArrayNode) existingNode, (ArrayNode) incomingNode);
		} else if (!existingNode.equals(incomingNode)) {
			changed = true;
		}

		return changed;
	}

	/** Merge arrays: replace elements if size differs or any element differs */
	private static boolean mergeArray(ArrayNode existingArray, ArrayNode incomingArray) {
		if (existingArray.size() != incomingArray.size()) {
			existingArray.removeAll();
			existingArray.addAll(incomingArray);
			return true;
		}

		boolean changed = false;
		for (int i = 0; i < existingArray.size(); i++) {
			JsonNode oldItem = existingArray.get(i);
			JsonNode newItem = incomingArray.get(i);

			if (oldItem.isObject() && newItem.isObject()) {
				boolean nestedChange = mergeAndDetectChanges(oldItem, newItem);
				if (nestedChange)
					changed = true;
			} else if (!oldItem.equals(newItem)) {
				existingArray.set(i, newItem);
				changed = true;
			}
		}

		return changed;
	}

	/** Convert Iterator to Iterable for for-each loop */
	private static <T> Iterable<T> iterable(final java.util.Iterator<T> iterator) {
		return () -> iterator;
	}

	/**
	 * Ensure the node is an ObjectNode. Wrap primitives or arrays as {"value": ...}
	 * for consistent merging.
	 */
	public static ObjectNode ensureObjectNode(JsonNode node) {
		if (node == null || node.isNull()) {
			return mapper.createObjectNode();
		}
		if (node.isObject())
			return (ObjectNode) node;

		ObjectNode obj = mapper.createObjectNode();
		obj.set("value", node);
		return obj;
	}
}
