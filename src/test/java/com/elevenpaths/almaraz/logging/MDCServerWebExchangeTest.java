/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */
package com.elevenpaths.almaraz.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;

/**
 * Unit tests for {@link MDCServerWebExchange}.
 *
 * @author Juan Hernando <juanantonio.hernandolabajo@telefonica.com>
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class MDCServerWebExchangeTest {

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private ServerWebExchange exchange;

	@After
	public void reset_mocks() {
		Mockito.reset(exchange);
	}

	@Test
	public void getMethodTest() {
		Mockito.when(exchange.getRequest().getMethodValue()).then(answer-> {
			return "test";
		});
		String result = MDCServerWebExchange.getMethod(exchange);
		assertNotNull(result);
		assertEquals("test", result);
	}

	@Test
	public void getMethodExceptionTest() {
		Mockito.when(exchange.getRequest().getMethodValue()).then(answer-> {
			throw new Exception("error");
		});
		String result = MDCServerWebExchange.getMethod(exchange);
		assertNull(result);
	}

	@Test
	public void getPathTest() {
		Mockito.when(exchange.getRequest().getPath().value()).then(answer-> {
			return "test";
		});
		String result = MDCServerWebExchange.getPath(exchange);
		assertNotNull(result);
		assertEquals("test", result);
	}

	@Test
	public void getPathExceptionTest() {
		Mockito.when(exchange.getRequest().getPath().value()).then(answer-> {
			throw new Exception("error");
		});
		String result = MDCServerWebExchange.getPath(exchange);
		assertNull(result);
	}

	@Test
	public void getQueryParamsTest() {
		Mockito.when(exchange.getRequest().getQueryParams().toString()).then(answer-> {
			return "test=[test], test2=[test]";
		});
		String result = MDCServerWebExchange.getQueryParams(exchange);
		assertNotNull(result);
		assertEquals("test=[test], test2=[test]", result);
	}

	@Test
	public void getQueryParamsExceptionTest() {
		Mockito.when(exchange.getRequest().getQueryParams().toString()).then(answer-> {
			throw new Exception("error");
		});
		String result = MDCServerWebExchange.getQueryParams(exchange);
		assertNull(result);
	}

	@Test
	public void getRemoteAddressTest() {
		InetSocketAddress inetSocketAddress = new InetSocketAddress("localhost", 8080);
		Mockito.when(exchange.getRequest().getRemoteAddress()).then(answer-> {
			return inetSocketAddress;
		});

		String result = MDCServerWebExchange.getRemoteAddress(exchange);
		assertNotNull(result);
		assertEquals("localhost/127.0.0.1", result);
	}

	@Test
	public void getRemoteAddressFromXFFTest() {
		Mockito.when(exchange.getRequest().getHeaders()).then(answer-> {
			HttpHeaders headers = new HttpHeaders();
			headers.add("X-Forwarded-For", "127.0.0.2,127.0.0.1");
			return headers;
		});

		String result = MDCServerWebExchange.getRemoteAddress(exchange);
		assertNotNull(result);
		assertEquals("127.0.0.2", result);
	}

	@Test
	public void getRemoteAddressExceptionTest() {
		Mockito.when(exchange.getRequest().getRemoteAddress()).then(answer-> {
			throw new Exception("error");
		});
		String result = MDCServerWebExchange.getRemoteAddress(exchange);
		assertNull(result);
	}

	@Test
	public void getStatusCodeTest() {
		HttpStatus httpStatus = HttpStatus.CREATED;
		Mockito.when(exchange.getResponse().getStatusCode()).then(answer-> {
			return httpStatus;
		});
		String result = MDCServerWebExchange.getStatusCode(exchange);
		assertNotNull(result);
		assertEquals("201", result);
	}

	@Test
	public void getStatusCodeExceptionTest() {
		Mockito.when(exchange.getResponse().getStatusCode()).then(answer-> {
			throw new Exception("error");
		});
		String result = MDCServerWebExchange.getStatusCode(exchange);
		assertNull(result);
	}

}
