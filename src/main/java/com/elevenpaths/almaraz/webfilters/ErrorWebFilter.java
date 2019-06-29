/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.webfilters;

import org.slf4j.MDC;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.elevenpaths.almaraz.context.ContextField;
import com.elevenpaths.almaraz.context.RequestContext;
import com.elevenpaths.almaraz.exceptions.ResponseException;
import com.elevenpaths.almaraz.exceptions.ServerException;
import com.elevenpaths.almaraz.logging.ReactiveLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
public class ErrorWebFilter implements WebFilter {

	/**
	 * Jackson {@link ObjectMapper} to generate the JSON of the response body.
	 */
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	/**
	 * Web filter implementation to write a log entry when the request is received and
	 * another log entry when the response is completed.
	 */
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		return chain.filter(exchange)
				.onErrorResume(t -> buildErrorResponse(exchange, t));
	}

	/**
	 * Convert the exception into a {@link ResponseException} and render it as
	 * a body response.
	 *
	 * @param exchange
	 * @param t
	 * @return a completed {@link Mono}
	 */
	protected Mono<Void> buildErrorResponse(ServerWebExchange exchange, Throwable t) {
		ResponseException e = getResponseException(t);
		HttpStatus status = e.getStatus();
		MultiValueMap<String, String> headers = null;
		byte[] bodyBytes = null;
		try {
			bodyBytes = marshalErrorResponseBody(e);
			headers = e.getHeaders();
		} catch (JsonProcessingException e1) {
			log.error("Error marshalling exception", e1);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return Mono.empty()
				.doOnEach(ReactiveLogger.logOnComplete(() -> logError(e)))
				.then(renderErrorResponse(exchange, status, bodyBytes, headers));
	}

	/**
	 * Serialize the error response body into a byte array.
	 *
	 * @param e
	 * @return byte array of the error response
	 * @throws JsonProcessingException
	 */
	protected byte[] marshalErrorResponseBody(ResponseException e) throws JsonProcessingException {
		if (e.getError() == null) {
			return null;
		}
		ObjectNode node = JsonNodeFactory.instance.objectNode();
		node.put("error", e.getError());
		if (e.getReason() != null) {
			node.put("error_description", e.getReason());
		}
		if (e.getDetailMap() != null) {
			node.set("error_details", OBJECT_MAPPER.valueToTree(e.getDetailMap()));
		}
		return OBJECT_MAPPER.writer().writeValueAsBytes(node);
	}

	/**
	 * Render a body response with status, body (as byte array) and headers.
	 *
	 * @param exchange
	 * @param status
	 * @param bodyBytes
	 * @param headers
	 * @return a completed {@link Mono}
	 */
	protected Mono<Void> renderErrorResponse(
			ServerWebExchange exchange, HttpStatus status, byte[] bodyBytes, MultiValueMap<String, String> headers) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(status);
		if (headers != null) {
			response.getHeaders().addAll(headers);
		}
		if (bodyBytes == null) {
			return Mono.empty();
		}
		response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
		DataBuffer buffer = response.bufferFactory().wrap(bodyBytes);
		return response.writeWith(Mono.just(buffer));
	}

	/**
	 * Map the exception to a {@link ResponseException} so that it is possible to render
	 * the error response.
	 * The {@link ResponseStatusException} already contains the status code for the error.
	 * Unhandled exceptions are mapped to {@link ServerException}.
	 *
	 * @param t
	 * @return {@link ResponseException} mapped from t exception
	 */
	protected ResponseException getResponseException(final Throwable t) {
		if (t instanceof ResponseException) {
			return (ResponseException) t;
		}
		if (t instanceof ResponseStatusException) {
			ResponseStatusException responseStatusException = (ResponseStatusException)t;
			return new ResponseException(responseStatusException.getStatus());
		}
		return new ServerException(t);
	}

	/**
	 * Log an error. It uses the {@link ContextField#ERROR} to save the error identifier.
	 *
	 * @param e
	 */
	protected void logError(ResponseException e) {
		MDC.put(ContextField.ERROR, e.getError());
		MDC.put(ContextField.REASON, e.getReason());
		if (e instanceof ServerException) {
			log.error("Error", e.getCause());
		} else {
			log.info("Error");
		}
	}

}
