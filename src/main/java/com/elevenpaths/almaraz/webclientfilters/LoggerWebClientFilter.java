/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.webclientfilters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import com.elevenpaths.almaraz.context.ContextField;
import com.elevenpaths.almaraz.logging.ReactiveLogger;

import reactor.core.publisher.Mono;

/**
 * {@link ExchangeFilterFunction} to log the request and response with contextual information.
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
public class LoggerWebClientFilter implements ExchangeFilterFunction {

	/**
	 * Logger.
	 */
	private static final Logger log = LoggerFactory.getLogger(LoggerWebClientFilter.class);

	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
		long start = System.currentTimeMillis();
		return Mono.empty()
				.doOnEach(ReactiveLogger.logOnComplete(() -> logRequest(request)))
				.then(next.exchange(request))
				.doOnEach(ReactiveLogger.logOnNext((response) -> logResponse(response, start)));
	}

	/**
	 * Log the request.
	 *
	 * @param request
	 */
	protected void logRequest(ClientRequest request) {
		MDC.put(ContextField.METHOD, request.method().name());
		MDC.put(ContextField.URL, request.url().toString());
		log.info("Client request");
	}

	/**
	 * Log the response.
	 *
	 * @param response
	 * @param start Timestamp when the request was received to calculate the latency.
	 */
	protected void logResponse(ClientResponse response, long start) {
		MDC.put(ContextField.STATUS, Integer.toString(response.rawStatusCode()));
		MDC.put(ContextField.LATENCY, Long.toString(System.currentTimeMillis() - start));
		log.info("Client response");
	}

}
