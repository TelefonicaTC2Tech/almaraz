/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.webfilters;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

/**
 * Unit tests for {@link CompleteLocationHeaderWebFilter}.
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
public class CompleteLocationHeaderWebFilterTest {

	@Test
	public void filterWithLocationHeader() {

		List<TestCase> tcs = Arrays.asList(
				new TestCase("3", "/api/resources/3"),
				new TestCase("xxx-4237-dsfsd", "/api/resources/xxx-4237-dsfsd"),
				new TestCase("4/res/3", "/api/resources/4/res/3"),
				new TestCase("/3", "/3"),
				new TestCase("/test/demo", "/test/demo"),
				new TestCase("http://server.com/3", "http://server.com/3")
		);

		CompleteLocationHeaderWebFilter filter = new CompleteLocationHeaderWebFilter();
		for (TestCase tc : tcs) {
			MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/resources"));
			filter.filter(exchange, new TestWebFilterChain(tc.location)).block(Duration.ZERO);
			Assert.assertEquals(tc.expectedLocation, exchange.getResponse().getHeaders().getLocation().toASCIIString());
		}
	}

	@Test
	public void filterWithoutLocation() {
		CompleteLocationHeaderWebFilter filter = new CompleteLocationHeaderWebFilter();
		MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/resources"));
		filter.filter(exchange, new TestWebFilterChain(null)).block(Duration.ZERO);
		Assert.assertNull(exchange.getResponse().getHeaders().getLocation());
	}

	private static class TestCase {
		String location;
		String expectedLocation;
		public TestCase(String location, String expectedLocation) {
			this.location = location;
			this.expectedLocation = expectedLocation;
		}
	}

	private static class TestWebFilterChain implements WebFilterChain {

		private final String locationHeader;

		public TestWebFilterChain(String locationHeader) {
			this.locationHeader = locationHeader;
		}

		@Override
		public Mono<Void> filter(ServerWebExchange exchange) {
			if (locationHeader != null) {
				exchange.getResponse().getHeaders().setLocation(URI.create(locationHeader));
			}
			return exchange.getResponse().setComplete();
		}

	}

}
