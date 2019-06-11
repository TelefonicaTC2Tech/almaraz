/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.webfilters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.elevenpaths.almaraz.context.RequestContext;
import com.elevenpaths.almaraz.logging.MDCServerWebExchange;
import com.elevenpaths.almaraz.logging.ReactiveLogger;

import reactor.core.publisher.Mono;

/**
 * Reactive {@link WebFilter} to log the request and response with contextual information.
 * The contextual information is stored as a {@link RequestContext} instance in the
 * reactive context.
 *
 * Note that errors are not logged by this web filter. This is delegated to the error handler
 * so that it is possible to log not only the exception but also the response status code. The
 * error handler is responsible for converting an exception into a HTTP response.
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
public class LoggerWebFilter implements WebFilter {

	/**
	 * Logger.
	 */
	private static final Logger log = LoggerFactory.getLogger(LoggerWebFilter.class);

	/**
	 * Field name with the HTTP method of the request.
	 */
	public static final String METHOD = "method";

	/**
	 * Field name with the HTTP path of the request.
	 */
	public static final String PATH = "path";

	/**
	 * Field name with the client address of the request.
	 */
	public static final String ADDRESS = "address";

	/**
	 * Field name with the HTTP status of the response.
	 */
	public static final String STATUS = "status";

	/**
	 * Field name with the latency (in milliseconds) to complete the response.
	 */
	public static final String LATENCY = "latency";

	/**
	 * Web filter implementation to write a log entry when the request is received and
	 * another log entry when the response is completed.
	 */
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		MDCServerWebExchange.addStartTimestamp(exchange);
		return Mono.just(exchange)
				.doOnEach(ReactiveLogger.logOnComplete(() -> logRequest(exchange)))
				.then(chain.filter(exchange))
				.doOnEach(ReactiveLogger.logOnComplete(() -> logResponse(exchange)));
	}

	/**
	 * Log the request with method, path, and remote address as contextual information.
	 *
	 * @param exchange
	 */
	protected void logRequest(ServerWebExchange exchange) {
		MDC.put(METHOD, MDCServerWebExchange.getMethod(exchange));
		MDC.put(PATH, MDCServerWebExchange.getPath(exchange));
		MDC.put(ADDRESS, MDCServerWebExchange.getRemoteAddress(exchange));
		log.info("Request");
	}

	/**
	 * Log the response with status and latency as contextual information.
	 * @param exchange
	 */
	protected void logResponse(ServerWebExchange exchange) {
		MDC.put(STATUS, MDCServerWebExchange.getStatusCode(exchange));
		MDC.put(LATENCY, MDCServerWebExchange.getLatency(exchange));
		log.info("Response");
	}

}
