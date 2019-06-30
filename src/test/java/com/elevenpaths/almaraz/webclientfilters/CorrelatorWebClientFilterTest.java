/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.webclientfilters;

import java.net.URI;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import com.elevenpaths.almaraz.context.RequestContext;
import com.elevenpaths.almaraz.webfilters.RequestContextWebFilter;

import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * Unit tests for {@link CorrelatorWebClientFilter}.
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
public class CorrelatorWebClientFilterTest {

	@Test
	public void filter() {
		RequestContext requestContext = new RequestContext()
				.setCorrelator("test-corr");

		ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("http://localhost:8080")).build();
		ClientResponse response = Mockito.mock(ClientResponse.class);
		ExchangeFunction exchange = r -> {
			Assert.assertEquals("test-corr", r.headers().getFirst(RequestContextWebFilter.DEFAULT_CORRELATOR_HEADER));
			return Mono.just(response);
		};

		CorrelatorWebClientFilter filter = new CorrelatorWebClientFilter();
		ClientResponse actualResponse = filter.filter(request, exchange)
				.subscriberContext(Context.of(RequestContext.class, requestContext))
				.block();
		Assert.assertEquals(response, actualResponse);
	}

}
