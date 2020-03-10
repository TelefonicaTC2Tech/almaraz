/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.example;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.elevenpaths.almaraz.webclientfilters.CorrelatorWebClientFilter;
import com.elevenpaths.almaraz.webclientfilters.LoggerWebClientFilter;

import reactor.core.publisher.Mono;

@Component
public class HttpbinWebClient {

	/**
	 * Web client to httpbin server.
	 */
	private final WebClient webClient;

	/**
	 * Constructor that creates a {@link WebClient} configured with 2 filters provided by almaraz.
	 */
	public HttpbinWebClient(@Value("${almaraz-example.httpbin-url}") String url) {
		webClient = WebClient.builder()
				.baseUrl(url)
				.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.filter(new CorrelatorWebClientFilter())
				.filter(new LoggerWebClientFilter())
				.build();
	}

	/**
	 * Send a post request to httpbin.org.
	 *
	 * @param user
	 * @return
	 */
	public Mono<Map<String, Object>> post(User user) {
		return webClient.post()
			.uri("/post")
			.contentType(MediaType.APPLICATION_JSON)
			.body(BodyInserters.fromObject(user))
			.retrieve()
			.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
	}

}
