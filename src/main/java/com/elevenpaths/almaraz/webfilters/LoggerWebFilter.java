/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.webfilters;

import org.slf4j.MDC;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.elevenpaths.almaraz.context.ContextField;
import com.elevenpaths.almaraz.context.RequestContext;
import com.elevenpaths.almaraz.logging.MDCServerWebExchange;
import com.elevenpaths.almaraz.logging.ReactiveLogger;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class LoggerWebFilter implements WebFilter {

	/**
	 * Web filter implementation to write a log entry when the request is received and
	 * another log entry when the response is completed.
	 */
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		long start = System.currentTimeMillis();
		exchange.getResponse().beforeCommit(() -> {
			return Mono.empty()
					.doOnEach(ReactiveLogger.logOnComplete(() -> logResponse(exchange, start)))
					.then();
		});
		return Mono.empty()
				.doOnEach(ReactiveLogger.logOnComplete(() -> logRequest(exchange)))
				.then(chain.filter(exchange));
	}

	/**
	 * Log the request with method, path, and remote address as contextual information.
	 *
	 * @param exchange
	 */
	protected void logRequest(ServerWebExchange exchange) {
		MDC.put(ContextField.METHOD, MDCServerWebExchange.getMethod(exchange));
		MDC.put(ContextField.PATH, MDCServerWebExchange.getPath(exchange));
		MDC.put(ContextField.QUERY, MDCServerWebExchange.getQueryParams(exchange));
		MDC.put(ContextField.ADDRESS, MDCServerWebExchange.getRemoteAddress(exchange));
		log.info("Request");
	}

	/**
	 * Log the response with status and latency as contextual information.
	 * @param exchange
	 */
	protected void logResponse(ServerWebExchange exchange, long start) {
		MDC.put(ContextField.STATUS, MDCServerWebExchange.getStatusCode(exchange));
		MDC.put(ContextField.LATENCY, Long.toString(System.currentTimeMillis() - start));
		log.info("Response");
	}

}
