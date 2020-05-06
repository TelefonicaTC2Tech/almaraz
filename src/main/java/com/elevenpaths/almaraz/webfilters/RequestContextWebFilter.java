/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.webfilters;

import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.elevenpaths.almaraz.context.RequestContext;
import com.elevenpaths.almaraz.logging.ReactiveLogger;

import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * Reactive {@link WebFilter} to store a context instance, extended from {@link RequestContext}, in the
 * reactive {@link Context}. This context is used for contextual logging with {@link ReactiveLogger}
 * and {@link LoggerWebFilter}.
 *
 * The web filter also initializes the {@link RequestContext} with the correlator and transaction ID:
 * <ul>
 * <li><b>Transaction ID</b> - UUID for the request</li>
 * <li><b>Correlator</b> - Identifier to correlate a request or flow of requests in a microservices architecture.
 * The correlator can be specified by the client using a HTTP header (by default, "Unica-Correlator"). Web clients should include this
 * correlator in a HTTP header to maintain it during the rest of the web flow.</li>
 * </ul>
 *
 * Both correlator and transaction ID are logged as contextual information (MDC fields). It is possible to track all the log
 * entries of a request by transaction ID or all the log entries of a web flow by correlator with a log aggregator.
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
public class RequestContextWebFilter implements WebFilter {

	/**
	 * Default correlator header.
	 */
	public static final String DEFAULT_CORRELATOR_HEADER = "Unica-Correlator";

	/**
	 * Default context supplier. It creates a {@link RequestContext} instance.
	 */
	private static final Supplier<? extends RequestContext> DEFAULT_CONTEXT_SUPPLIER = () -> new RequestContext();

	/**
	 * Context supplier to build a {@link RequestContext} instance. Note that each request processed by the {@link WebFilter}
	 * requires to instantiate a {@link RequestContext} (or subclass). This {@link Supplier} is responsible for creating
	 * this instance.
	 */
	private final Supplier<? extends RequestContext> contextSupplier;

	/**
	 * HTTP header with the correlator.
	 */
	private final String correlatorHeader;

	/**
	 * Default constructor.
	 */
	public RequestContextWebFilter() {
		this(null, null);
	}

	/**
	 * Constructor with custom correlator header.
	 *
	 * @param correlatorHeader
	 */
	public RequestContextWebFilter(String correlatorHeader) {
		this(null, correlatorHeader);
	}

	/**
	 * Constructor with custom contextSupplier.
	 *
	 * @param contextSupplier
	 */
	public <T extends RequestContext> RequestContextWebFilter(Supplier<T> contextSupplier) {
		this(contextSupplier, null);
	}

	/**
	 * Constructor with custom contextSupplier and correlator header.
	 *
	 * @param contextSupplier
	 * @param correlatorHeader
	 */
	public <T extends RequestContext> RequestContextWebFilter(Supplier<T> contextSupplier, String correlatorHeader) {
		this.contextSupplier = contextSupplier == null ? DEFAULT_CONTEXT_SUPPLIER : contextSupplier;
		this.correlatorHeader = correlatorHeader == null ? DEFAULT_CORRELATOR_HEADER : correlatorHeader;
	}

	/**
	 * Implementation of the web filter that instantiates a {@link RequestContext} (or subclass) with
	 * {@link #contextSupplier}. It also updates the reactive context with the {@link RequestContext}
	 * instance.
	 * The web filter modifies neither the request nor the response.
	 */
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		RequestContext requestContext = buildRequestContext(request);
		exchange.getResponse().beforeCommit(() -> {
			exchange.getResponse().getHeaders().set(correlatorHeader, requestContext.getCorrelator());
			return Mono.empty();
		});
		return chain.filter(exchange)
				.subscriberContext(Context.of(RequestContext.class, requestContext));
	}

	/**
	 * Create a new {@link RequestContext} instance using {@link #newRequestContext()} and enrich
	 * this context with the transaction ID and correlator using {@link #initRequestContext(RequestContext, ServerHttpRequest)}.
	 *
	 * @param request
	 * @return {@link RequestContext}
	 */
	protected RequestContext buildRequestContext(ServerHttpRequest request) {
		RequestContext requestContext = newRequestContext();
		return initRequestContext(requestContext, request);
	}

	/**
	 * Create a new instance of {@link RequestContext} (or subclass) using the {@link #contextSupplier}.
	 *
	 * @return {@link RequestContext}
	 */
	protected RequestContext newRequestContext() {
		return contextSupplier.get();
	}

	/**
	 * Initializes the {@link RequestContext} with the transaction ID and correlator.
	 * The context instance includes a transaction ID (UUID) and a correlator (from the HTTP header of the request
	 * or the transaction ID if not available the header).
	 *
	 * @param requestContext
	 * @param request
	 * @return {@link RequestContext}
	 */
	protected RequestContext initRequestContext(RequestContext requestContext, ServerHttpRequest request) {
		String transactionId = getTransactionId();
		String correlator = getCorrelator(request, transactionId);
		return requestContext
				.setCorrelator(correlator)
				.setTransactionId(transactionId);
	}

	/**
	 * Get the transaction ID using a random UUID.
	 *
	 * @return transaction ID
	 */
	protected String getTransactionId() {
		return UUID.randomUUID().toString();
	}

	/**
	 * Get the correlator from the HTTP header with name {@link #correlatorHeader} from the request.
	 * If the request does not include a correlator header, then the defaultCorrelator is used.
	 *
	 * @param request
	 * @param defaultCorrelator
	 * @return correlator
	 */
	protected String getCorrelator(ServerHttpRequest request, String defaultCorrelator) {
		String correlator = request.getHeaders().getFirst(correlatorHeader);
		return (correlator == null) ? defaultCorrelator : correlator;
	}

}
