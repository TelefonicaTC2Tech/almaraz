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
	 * X-Forwarded-For header name.
	 */
	public static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";

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
	 * @return request method
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
	 * @return request path
	 */
	public static String getPath(ServerWebExchange exchange) {
		try {
			return exchange.getRequest().getURI().getPath();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Get the query params of the exchange request.
	 *
	 * @param exchange
	 * @return request query params
	 */
	public static String getQueryParams(ServerWebExchange exchange) {
		try {
			return exchange.getRequest().getURI().getQuery();
		} catch (Exception e) {
			return null;
		} 
	}

	/**
	 * Get the remote address of the exchange request.
	 * If the request contains a X-Forwarded-For HTTP header, it returns the first address. Otherwise, it returns
	 * the remote address obtained from TCP/IP.
	 *
	 * @param exchange
	 * @return request remote address
	 */
	public static String getRemoteAddress(ServerWebExchange exchange) {
		String address = getRemoteAddressFromXFF(exchange);
		if (address == null) {
			address = getRemoteAddressFromTCP(exchange);
		}
		return address;
	}

	/**
	 * Get the remote address of the exchange request from TCP/IP protocol.
	 * Note that this address may not correspond to the client if there is any intermediary (e.g. a nginx proxy).
	 *
	 * @param exchange
	 * @return request remote address
	 */
	public static String getRemoteAddressFromTCP(ServerWebExchange exchange) {
		try {
			return exchange.getRequest().getRemoteAddress().getAddress().toString();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Get the remote address of the exchange request from the X-Forwarded-For HTTP header.
	 * If X-Forwarded-For header is not present, it returns null.
	 * If X-Forwarded-For header contains several elements, it returns the first element.
	 *
	 * @param exchange
	 * @return request remote address from X-Forwarded-For HTTP header
	 */
	public static String getRemoteAddressFromXFF(ServerWebExchange exchange) {
		try {
			return exchange.getRequest().getHeaders().getValuesAsList(X_FORWARDED_FOR_HEADER).get(0);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Get the status code of the exchange response.
	 *
	 * @param exchange
	 * @return response status code
	 */
	public static String getStatusCode(ServerWebExchange exchange) {
		try {
			return Integer.toString(exchange.getResponse().getStatusCode().value());
		} catch (Exception e) {
			return null;
		}
	}

}
