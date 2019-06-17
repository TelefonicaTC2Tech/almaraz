/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.webfilters;

import java.time.Duration;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import com.elevenpaths.almaraz.context.RequestContext;

import reactor.core.publisher.Mono;

/**
 * Unit tests for {@link RequestContextWebFilter}.
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
public class RequestContextWebFilterTest {

	@Test
	public void filterWithoutDefaultCorrelator() {
		RequestContextWebFilter filter = new RequestContextWebFilter();
		TestWebFilterChain chain = new TestWebFilterChain();
		MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/resources"));
		filter.filter(exchange, chain).block(Duration.ZERO);
		Assert.assertNotNull(chain.requestContext);
		Assert.assertEquals(chain.requestContext.getCorrelator(), chain.requestContext.getTransactionId());
		Assert.assertEquals(chain.requestContext.getCorrelator(), exchange.getResponse().getHeaders().getFirst(RequestContextWebFilter.DEFAULT_CORRELATOR_HEADER));
	}

	@Test
	public void filterWithoutCustomCorrelator() {
		RequestContextWebFilter filter = new RequestContextWebFilter("My-Correlator");
		TestWebFilterChain chain = new TestWebFilterChain();
		MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/resources"));
		filter.filter(exchange, chain).block(Duration.ZERO);
		Assert.assertNotNull(chain.requestContext);
		Assert.assertEquals(chain.requestContext.getCorrelator(), chain.requestContext.getTransactionId());
		Assert.assertEquals(chain.requestContext.getCorrelator(), exchange.getResponse().getHeaders().getFirst("My-Correlator"));
	}

	@Test
	public void filterWithDefaultCorrelator() {
		RequestContextWebFilter filter = new RequestContextWebFilter();
		TestWebFilterChain chain = new TestWebFilterChain();
		MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/resources").header(RequestContextWebFilter.DEFAULT_CORRELATOR_HEADER, "test-corr").build());
		filter.filter(exchange, chain).block(Duration.ZERO);
		Assert.assertNotNull(chain.requestContext);
		Assert.assertNotNull(chain.requestContext.getTransactionId());
		Assert.assertEquals("test-corr", chain.requestContext.getCorrelator());
		Assert.assertEquals("test-corr", exchange.getResponse().getHeaders().getFirst(RequestContextWebFilter.DEFAULT_CORRELATOR_HEADER));
	}

	@Test
	public void filterWithCustomCorrelator() {
		RequestContextWebFilter filter = new RequestContextWebFilter("My-Correlator");
		TestWebFilterChain chain = new TestWebFilterChain();
		MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/resources").header("My-Correlator", "test-corr").build());
		filter.filter(exchange, chain).block(Duration.ZERO);
		Assert.assertNotNull(chain.requestContext);
		Assert.assertNotNull(chain.requestContext.getTransactionId());
		Assert.assertEquals("test-corr", chain.requestContext.getCorrelator());
		Assert.assertEquals("test-corr", exchange.getResponse().getHeaders().getFirst("My-Correlator"));
	}

	@Test
	public void filterWithCustomContext() {
		RequestContextWebFilter filter = new RequestContextWebFilter(() -> new CustomContext());
		TestWebFilterChain chain = new TestWebFilterChain();
		MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/resources"));
		filter.filter(exchange, chain).block(Duration.ZERO);
		Assert.assertNotNull(chain.requestContext);
		Assert.assertEquals(chain.requestContext.getCorrelator(), chain.requestContext.getTransactionId());
		Assert.assertEquals(chain.requestContext.getCorrelator(), exchange.getResponse().getHeaders().getFirst(RequestContextWebFilter.DEFAULT_CORRELATOR_HEADER));
		Assert.assertEquals("test-value", chain.requestContext.getString("custom"));
	}

	private static class CustomContext extends RequestContext {
		public CustomContext() {
			super();
			put("custom", "test-value");
		}
	}

	private static class TestWebFilterChain implements WebFilterChain {

		private RequestContext requestContext;

		@Override
		public Mono<Void> filter(ServerWebExchange exchange) {
			return RequestContext.context()
					.map(ctxt -> {
						requestContext = ctxt;
						return ctxt;
					})
					.then(exchange.getResponse().setComplete());
		}

	}

}
