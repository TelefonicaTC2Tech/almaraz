/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.resolvers;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.BindingContext;

import com.elevenpaths.almaraz.exceptions.InvalidRequestException;
import com.elevenpaths.almaraz.exceptions.UnsupportedMediaTypeException;
import com.elevenpaths.almaraz.validation.JsonSchemaValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

/**
 * Unit tests for {@link ValidRequestBodyResolver}.
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidRequestBodyResolverTest {

	@Mock
	private JsonSchemaValidator validator;

	@Mock
	ValidRequestBody validRequestBody;

	@Mock
	MethodParameter methodParameter;

	@Mock
	BindingContext bindingContext;

	@After
	public void reset_mocks() {
		Mockito.reset(validator);
		Mockito.reset(methodParameter);
	}

	@Test
	public void supportsParameterWithValidRequestBody() {
		Mockito.when(methodParameter.getParameterAnnotation(ValidRequestBody.class)).thenReturn(validRequestBody);
		ValidRequestBodyResolver resolver = new ValidRequestBodyResolver(validator);
		Assert.assertTrue(resolver.supportsParameter(methodParameter));
	}

	@Test
	public void supportsParameterWithoutValidRequestBody() {
		Mockito.when(methodParameter.getParameterAnnotation(ValidRequestBody.class)).thenReturn(null);
		ValidRequestBodyResolver resolver = new ValidRequestBodyResolver(validator);
		Assert.assertFalse(resolver.supportsParameter(methodParameter));
	}

	@Test
	public void validateUnsupportedMediaType() {
		MockServerHttpRequest request = MockServerHttpRequest
				.post("/test")
				.contentType(MediaType.APPLICATION_PDF)
				.build();
		MockServerWebExchange exchange = MockServerWebExchange.from(request);

		Mockito.when(validRequestBody.value()).thenReturn("schema");
		Mockito.when(validRequestBody.multi()).thenReturn(false);
		Mockito.when(validRequestBody.query()).thenReturn(false);
		Mockito.when(methodParameter.getParameterAnnotation(ValidRequestBody.class)).thenReturn(validRequestBody);
		Mockito.doReturn(ReferenceType.class).when(methodParameter).getParameterType();

		ValidRequestBodyResolver resolver = new ValidRequestBodyResolver(validator);
		try {
			resolver.resolveArgument(methodParameter, bindingContext, exchange).block();
			Assert.fail();
		} catch (UnsupportedMediaTypeException e) {
			Assert.assertNotNull(e);
		}
	}

	@Test
	public void validateJsonBodyWithValidBody() {
		MockServerHttpRequest request = MockServerHttpRequest
				.post("/test")
				.contentType(MediaType.APPLICATION_JSON)
				.body("{\"str\": \"value\", \"bool\": true}");
		MockServerWebExchange exchange = MockServerWebExchange.from(request);

		Mockito.when(validRequestBody.value()).thenReturn("schema");
		Mockito.when(validRequestBody.multi()).thenReturn(false);
		Mockito.when(validRequestBody.query()).thenReturn(false);
		Mockito.when(methodParameter.getParameterAnnotation(ValidRequestBody.class)).thenReturn(validRequestBody);
		Mockito.doReturn(ReferenceType.class).when(methodParameter).getParameterType();

		ValidRequestBodyResolver resolver = new ValidRequestBodyResolver(validator);
		ReferenceType value = (ReferenceType) resolver.resolveArgument(methodParameter, bindingContext, exchange).block();
		Assert.assertEquals("value", value.str);
		Assert.assertTrue(value.bool);
	}

	@Test
	public void validateJsonBodyWithInvalidBody() {
		MockServerHttpRequest request = MockServerHttpRequest
				.post("/test")
				.contentType(MediaType.APPLICATION_JSON)
				.body("{\"str\": value\", \"bool\": true}");
		MockServerWebExchange exchange = MockServerWebExchange.from(request);

		Mockito.when(validRequestBody.value()).thenReturn("schema");
		Mockito.when(validRequestBody.multi()).thenReturn(false);
		Mockito.when(validRequestBody.query()).thenReturn(false);
		Mockito.when(methodParameter.getParameterAnnotation(ValidRequestBody.class)).thenReturn(validRequestBody);
		Mockito.doReturn(ReferenceType.class).when(methodParameter).getParameterType();

		ValidRequestBodyResolver resolver = new ValidRequestBodyResolver(validator);
		try {
			resolver.resolveArgument(methodParameter, bindingContext, exchange).block();
			Assert.fail();
		} catch (InvalidRequestException e) {
			Assert.assertEquals("invalid json body", e.getMessage());
		}
	}

	@Test
	public void validateJsonBodyWithoutBody() {
		MockServerHttpRequest request = MockServerHttpRequest
				.post("/test")
				.contentType(MediaType.APPLICATION_JSON)
				.build();
		MockServerWebExchange exchange = MockServerWebExchange.from(request);

		Mockito.when(validRequestBody.value()).thenReturn("schema");
		Mockito.when(validRequestBody.multi()).thenReturn(false);
		Mockito.when(validRequestBody.query()).thenReturn(false);
		Mockito.when(methodParameter.getParameterAnnotation(ValidRequestBody.class)).thenReturn(validRequestBody);
		Mockito.doReturn(ReferenceType.class).when(methodParameter).getParameterType();

		ValidRequestBodyResolver resolver = new ValidRequestBodyResolver(validator);
		try {
			resolver.resolveArgument(methodParameter, bindingContext, exchange).block();
			Assert.fail();
		} catch (InvalidRequestException e) {
			Assert.assertEquals("expected json body", e.getMessage());
		}
	}

	@Test
	public void validateUrlEncodedBodyWithValidBody() {
		MockServerHttpRequest request = MockServerHttpRequest
				.post("/test")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body("str=value&bool=true");
		MockServerWebExchange exchange = MockServerWebExchange.from(request);

		Mockito.when(validRequestBody.value()).thenReturn("schema");
		Mockito.when(validRequestBody.multi()).thenReturn(false);
		Mockito.when(validRequestBody.query()).thenReturn(false);
		Mockito.when(methodParameter.getParameterAnnotation(ValidRequestBody.class)).thenReturn(validRequestBody);
		Mockito.doReturn(ReferenceType.class).when(methodParameter).getParameterType();

		ValidRequestBodyResolver resolver = new ValidRequestBodyResolver(validator);
		ReferenceType value = (ReferenceType) resolver.resolveArgument(methodParameter, bindingContext, exchange).block();
		Assert.assertEquals("value", value.str);
		Assert.assertTrue(value.bool);
	}

	@Test
	public void validateUrlEncodedBodyWithInvalidBody() {
		MockServerHttpRequest request = MockServerHttpRequest
				.post("/test")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body("str=value&bool==&&=true");
		MockServerWebExchange exchange = MockServerWebExchange.from(request);

		Mockito.when(validRequestBody.value()).thenReturn("schema");
		Mockito.when(validRequestBody.multi()).thenReturn(false);
		Mockito.when(validRequestBody.query()).thenReturn(false);
		Mockito.when(methodParameter.getParameterAnnotation(ValidRequestBody.class)).thenReturn(validRequestBody);
		Mockito.doReturn(ReferenceType.class).when(methodParameter).getParameterType();

		ValidRequestBodyResolver resolver = new ValidRequestBodyResolver(validator);
		try {
			resolver.resolveArgument(methodParameter, bindingContext, exchange).block();
			Assert.fail();
		} catch (InvalidRequestException e) {
			Assert.assertEquals("invalid urlencoded body", e.getMessage());
		}
	}

	@Test
	public void validateUrlEncodedBodyWithoutBody() {
		MockServerHttpRequest request = MockServerHttpRequest
				.post("/test")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.build();
		MockServerWebExchange exchange = MockServerWebExchange.from(request);

		Mockito.when(validRequestBody.value()).thenReturn("schema");
		Mockito.when(validRequestBody.multi()).thenReturn(false);
		Mockito.when(validRequestBody.query()).thenReturn(false);
		Mockito.when(methodParameter.getParameterAnnotation(ValidRequestBody.class)).thenReturn(validRequestBody);
		Mockito.doReturn(ReferenceType.class).when(methodParameter).getParameterType();

		ValidRequestBodyResolver resolver = new ValidRequestBodyResolver(validator);
		ReferenceType value = (ReferenceType) resolver.resolveArgument(methodParameter, bindingContext, exchange).block();
		Assert.assertNull(value.str);
		Assert.assertFalse(value.bool);
	}

	@Test
	public void validateQueryParameters() {
		MockServerHttpRequest request = MockServerHttpRequest
				.get("/test?str=value&bool=true")
				.build();
		MockServerWebExchange exchange = MockServerWebExchange.from(request);

		Mockito.when(validRequestBody.value()).thenReturn("schema");
		Mockito.when(validRequestBody.multi()).thenReturn(false);
		Mockito.when(validRequestBody.query()).thenReturn(true);
		Mockito.when(methodParameter.getParameterAnnotation(ValidRequestBody.class)).thenReturn(validRequestBody);
		Mockito.doReturn(ReferenceType.class).when(methodParameter).getParameterType();

		ValidRequestBodyResolver resolver = new ValidRequestBodyResolver(validator);
		ReferenceType value = (ReferenceType) resolver.resolveArgument(methodParameter, bindingContext, exchange).block();
		Assert.assertEquals("value", value.str);
		Assert.assertTrue(value.bool);
	}

	@Test
	public void validateQueryParametersWithoutQuery() {
		MockServerHttpRequest request = MockServerHttpRequest
				.get("/test")
				.build();
		MockServerWebExchange exchange = MockServerWebExchange.from(request);

		Mockito.when(validRequestBody.value()).thenReturn("schema");
		Mockito.when(validRequestBody.multi()).thenReturn(false);
		Mockito.when(validRequestBody.query()).thenReturn(true);
		Mockito.when(methodParameter.getParameterAnnotation(ValidRequestBody.class)).thenReturn(validRequestBody);
		Mockito.doReturn(ReferenceType.class).when(methodParameter).getParameterType();

		ValidRequestBodyResolver resolver = new ValidRequestBodyResolver(validator);
		ReferenceType value = (ReferenceType) resolver.resolveArgument(methodParameter, bindingContext, exchange).block();
		Assert.assertNull(value.str);
		Assert.assertFalse(value.bool);
	}

	@Test
	public void toSingleValueMapWithRepetitions() {
		MultiValueMap<String, String> multivalue = new LinkedMultiValueMap<String, String>();
		multivalue.addAll("repeated", Arrays.asList("one", "two"));

		ValidRequestBodyResolver resolver = new ValidRequestBodyResolver(validator);
		try {
			resolver.toSingleValueMap(multivalue);
		} catch (InvalidRequestException e) {
			Assert.assertEquals("$.repeated: only one value is permitted", e.getMessage());
		}
	}

	@Test
	public void toSingleValueMapWithoutRepetitions() {
		MultiValueMap<String, String> multivalue = new LinkedMultiValueMap<String, String>();
		multivalue.addAll("first", Arrays.asList("one"));
		multivalue.addAll("second", Arrays.asList("two"));

		ValidRequestBodyResolver resolver = new ValidRequestBodyResolver(validator);
		Map<String, String> actual = resolver.toSingleValueMap(multivalue);
		Map<String, String> expected = new HashMap<>();
		expected.put("first", "one");
		expected.put("second", "two");
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void validateAndMarshalWithInvalidException() throws IOException {
		JsonNode node = getReferenceJsonNode();
		Mockito.doThrow(new InvalidRequestException("invalid")).when(validator).validate("schema", node);

		ValidRequestBodyResolver resolver = new ValidRequestBodyResolver(validator);
		try {
			resolver.validateAndMarshal("schema", node, Map.class);
			Assert.fail();
		} catch (InvalidRequestException e) {
			Assert.assertEquals("invalid", e.getMessage());
		}
	}

	@Test
	public void validateAndMarshalWithJsonNodeType() throws IOException {
		JsonNode node = getReferenceJsonNode();
		ValidRequestBodyResolver resolver = new ValidRequestBodyResolver(validator);
		Assert.assertEquals(node, resolver.validateAndMarshal("schema", node, JsonNode.class));
	}

	@Test
	public void validateAndMarshalWithReferenceType() throws IOException {
		JsonNode node = getReferenceJsonNode();
		ValidRequestBodyResolver resolver = new ValidRequestBodyResolver(validator);
		ReferenceType value = (ReferenceType) resolver.validateAndMarshal("schema", node, ReferenceType.class);
		Assert.assertEquals("value", value.str);
		Assert.assertTrue(value.bool);
	}

	protected JsonNode getReferenceJsonNode() throws IOException {
		return new ObjectMapper().readTree("{\"str\": \"value\", \"bool\": true}");
	}

	@Data
	static class ReferenceType {
		private String str;
		private boolean bool;
	}

}
