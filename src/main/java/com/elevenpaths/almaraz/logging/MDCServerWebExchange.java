/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.logging;

import org.springframework.web.server.ServerWebExchange;

/**
 * Utility class to extract attributes from a {@link ServerWebExchange} instance in {@link String} format
 * as required by MDC.
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
public class MDCServerWebExchange {

	/**
	 * Exchange attribute with the start timestamp to calculate the latency).
	 */
	public static final String START_TIMESTAMP = "startTimestamp";

	/**
	 * Private constructor.
	 * Only static functions.
	 */
	private MDCServerWebExchange() {
	}

	/**
	 * Get the method of the exchange request.
	 *
	 * @param exchange
	 * @return
	 */
	public static String getMethod(ServerWebExchange exchange) {
		try {
			return exchange.getRequest().getMethodValue();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Get the path of the exchange request.
	 *
	 * @param exchange
	 * @return
	 */
	public static String getPath(ServerWebExchange exchange) {
		try {
			return exchange.getRequest().getPath().value();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Get the remote address of the exchange request.
	 *
	 * @param exchange
	 * @return
	 */
	public static String getRemoteAddress(ServerWebExchange exchange) {
		try {
			return exchange.getRequest().getRemoteAddress().getAddress().toString();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Get the status code of the exchange response.
	 *
	 * @param exchange
	 * @return
	 */
	public static String getStatusCode(ServerWebExchange exchange) {
		try {
			return Integer.toString(exchange.getResponse().getStatusCode().value());
		} catch (Exception e) {
			return null;
		}
	}

}
