/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.example;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.elevenpaths.almaraz.context.RequestContext;
import com.elevenpaths.almaraz.logging.ReactiveLogger;
import com.elevenpaths.almaraz.resolvers.ValidRequestBody;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class Controller {

	private final HttpbinWebClient httpbinWebClient;

	/**
	 * Constructor. It injects the {@link HttpbinWebClient}.
	 *
	 * @param httpbinWebClient
	 */
	public Controller(HttpbinWebClient httpbinWebClient) {
		this.httpbinWebClient = httpbinWebClient;
	}

	/**
	 * This resources validates the request body against the JSON schema "schemas/user.json"
	 * and binds it to a {@link User} instance.
	 *
	 * This resource supports both application/json and application/x-www-form-urlencoded media
	 * types.
	 *
	 * @param user
	 * @return
	 */
	@PostMapping("/users")
	public Mono<ResponseEntity<User>> createUser(@ValidRequestBody("user") User user) {
		user.setId(UUID.randomUUID().toString());
		return Mono.just(ResponseEntity.created(URI.create(user.getId())).body(user));
	}

	/**
	 * This resource validates the query parameters against the JSON schema "schemas/user.json" and
	 * binds it to a {@link User} instance.
	 *
	 * @param user
	 * @return
	 */
	@GetMapping("/users")
	public Flux<User> findUsers(@ValidRequestBody(value = "user", query = true) User user) {
		return Flux.just(user);
	}

	/**
	 * Forward the request to httpbin server to test a {@link WebClient}.
	 *
	 * @param user
	 * @return
	 */
	@PostMapping("/httpbin")
	public Mono<Map<String, Object>> proxy(@ValidRequestBody("user") User user) {
		return httpbinWebClient.post(user);
	}

	/**
	 * Log resource that works with context enrichment and adding log statements.
	 *
	 * @param logId
	 * @return
	 */
	@GetMapping("/logs/{logId}")
	public Mono<Integer> getLog(@PathVariable int logId) {
		return RequestContext.context()
				.map(ctxt -> ctxt.setOperation("getLog"))
				.doOnEach(ReactiveLogger.logOnNext(ctxt -> {
					MDC.put("logId", Integer.toString(logId));
					log.info("Receiving log request with logId {}", logId);
				}))
				.map(ctxt -> {
					if (logId % 2 != 0) {
						throw new RuntimeException("logId cannot be an odd number");
					}
					return logId;
				})
				.doOnEach(ReactiveLogger.logOnComplete(() -> {
					log.info("Processed log request successfully");
				}))
				.doOnEach(ReactiveLogger.logOnError(t -> {
					log.error("Error processing logRequest", t);
				}));
	}

}
