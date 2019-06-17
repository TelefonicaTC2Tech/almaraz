/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.webclientfilters;

import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.elevenpaths.almaraz.context.RequestContext;
import com.elevenpaths.almaraz.webfilters.RequestContextWebFilter;

import reactor.core.publisher.Mono;

/**
 * {@link ExchangeFilterFunction} to insert the correlator header in the request sent
 * by a {@link WebClient}.
 * The correlator is obtained from the {@link RequestContext}.
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
public class CorrelatorWebClientFilter implements ExchangeFilterFunction {

	/**
	 * HTTP header with the correlator.
	 */
	private final String correlatorHeader;

	/**
	 * Default constructor. It uses the default correlator header.
	 */
	public CorrelatorWebClientFilter() {
		this(RequestContextWebFilter.DEFAULT_CORRELATOR_HEADER);
	}

	/**
	 * Constructor with custom correlator header.
	 *
	 * @param correlatorHeader
	 */
	public CorrelatorWebClientFilter(String correlatorHeader) {
		this.correlatorHeader = correlatorHeader;
	}

	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
		return RequestContext.context()
				.map(ctxt -> ClientRequest.from(request)
						.header(correlatorHeader, ctxt.getCorrelator())
						.build())
				.flatMap(next::exchange);
	}

}
