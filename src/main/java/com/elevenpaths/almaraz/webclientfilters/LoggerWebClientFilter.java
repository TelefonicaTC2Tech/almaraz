/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.webclientfilters;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import com.elevenpaths.almaraz.context.ContextField;
import com.elevenpaths.almaraz.logging.ReactiveLogger;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * {@link ExchangeFilterFunction} to log the request and response with contextual information.
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
@Slf4j
public class LoggerWebClientFilter implements ExchangeFilterFunction {

	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
		long start = System.currentTimeMillis();
		String transactionId = UUID.randomUUID().toString();
		return Mono.empty()
				.doOnEach(ReactiveLogger.logOnComplete(() -> logRequest(request, transactionId)))
				.then(next.exchange(request))
				.doOnEach(ReactiveLogger.logOnNext((response) -> logResponse(response, start, transactionId)));
	}

	/**
	 * Log the request.
	 *
	 * @param request
	 */
	protected void logRequest(ClientRequest request, String transactionId) {
		MDC.put(ContextField.METHOD, request.method().name());
		MDC.put(ContextField.URL, request.url().toString());
		MDC.put(ContextField.TRANSACTION_ID, transactionId);
		log.info("Client request");
	}

	/**
	 * Log the response.
	 *
	 * @param response
	 * @param start Timestamp when the request was received to calculate the latency.
	 */
	protected void logResponse(ClientResponse response, long start, String transactionId) {
		MDC.put(ContextField.STATUS, Integer.toString(response.rawStatusCode()));
		MDC.put(ContextField.LATENCY, Long.toString(System.currentTimeMillis() - start));
		MDC.put(ContextField.TRANSACTION_ID, transactionId);
		log.info("Client response");
	}

}
