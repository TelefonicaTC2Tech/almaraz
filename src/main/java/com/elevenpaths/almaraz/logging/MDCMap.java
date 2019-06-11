/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.logging;

import java.util.Map;

import org.slf4j.MDC;

/**
 * Utilities to work with {@link MDC}.
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
public class MDCMap {

	/**
	 * Private constructor.
	 */
	private MDCMap() {
	}

	/**
	 * Put all the map elements in {@link MDC} converting each value to {@link String} due
	 * to MDC limitations.
	 *
	 * @param map
	 */
	public static void putAll(Map<String, Object> map) {
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String value = (entry.getValue() == null) ? null : entry.getValue().toString();
			MDC.put(entry.getKey(), value);
		}
	}

}
