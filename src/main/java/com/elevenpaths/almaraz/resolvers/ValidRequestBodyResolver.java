// Copyright (c) Telefonica I+D. All rights reserved.

package com.elevenpaths.almaraz.resolvers;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;

import com.elevenpaths.almaraz.exceptions.InvalidRequestException;
import com.elevenpaths.almaraz.exceptions.UnsupportedMediaTypeException;
import com.elevenpaths.almaraz.validation.JsonSchemaValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

/**
 * Spring resolver to validate the body and bind it to a Java type. It
 * extends @BodyRequest annotation to include validation against a JSON schema.
 * Note that the validation is done before the binding with the Java type.
 *
 * Currently, it supports application/json and application/x-www-form-urlencoded.
 *
 * For application/json bodies (default):
 *
 * <pre>
 * &#064;RestController
 * public class DemoController {
 *   &#064;RequestMapping(value="/demo", method=RequestMethod.POST)
 *   public String demo(&#064;ValidRequestBody("json-schema") TestType value) {
 *     ...
 *   }
 * }
 * </pre>
 *
 * In application/x-www-form-urlencoded bodies, it is possible to use the "multi" boolean
 * parameter to specify if the {@link MultiValueMap} obtained
 * from parsing the body is maintained (multi=true) or transformed into a
 * {@link Map} (multi=false). Note that default value for "multi" is false. The
 * binding class needs to implement the set methods with a List argument
 * (multi=true) or a {@link String} argument (multi=false).
 *
 * <pre>
 * &#064;RestController
 * public class DemoController {
 *   &#064;RequestMapping(value="/demo2", method=RequestMethod.POST, consumes=MediaType.APPLICATION_FORM_URLENCODED_VALUE)
 *	 public String demo2(&#064;ValidRequestBody("json-schema-2") TestType value) {
 *	   ...
 *	 }
 * }
 * </pre>
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
public class ValidRequestBodyResolver implements HandlerMethodArgumentResolver {

	private final JsonSchemaValidator validator;

	private final ObjectMapper objectMapper;

	/**
	 * Constructor.
	 *
	 * @param validator
	 */
	public ValidRequestBodyResolver(JsonSchemaValidator validator) {
		this(validator, new ObjectMapper());
	}

	/**
	 * Constructor.
	 *
	 * @param validator
	 * @param objectMapper
	 */
	public ValidRequestBodyResolver(JsonSchemaValidator validator, ObjectMapper objectMapper) {
		this.validator = validator;
		this.objectMapper = objectMapper;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterAnnotation(ValidRequestBody.class) != null;
	}

	@Override
	public Mono<Object> resolveArgument(MethodParameter parameter, BindingContext bindingContext,
			ServerWebExchange exchange) {
		String schemaName = parameter.getParameterAnnotation(ValidRequestBody.class).value();
		boolean multi = parameter.getParameterAnnotation(ValidRequestBody.class).multi();
		boolean query = parameter.getParameterAnnotation(ValidRequestBody.class).query();
		Class<?> valueType = parameter.getParameterType();
		if (query) {
			return resolveQueryParams(exchange, schemaName, valueType, multi);
		}
		MediaType contentType = exchange.getRequest().getHeaders().getContentType();
		if (MediaType.APPLICATION_JSON.includes(contentType)) {
			return resolveJsonBody(exchange, schemaName, valueType);
		} else if (MediaType.APPLICATION_FORM_URLENCODED.includes(contentType)) {
			return resolveUrlEncodedBody(exchange, schemaName, valueType, multi);
		} else {
			throw new UnsupportedMediaTypeException();
		}
	}

	/**
	 * Read the request url encoded body (url encoded), validate it agains a JSON schema, and resolve it into valueType.
	 *
	 * @param exchange
	 * @param schemaName
	 * @param valueType
	 * @param multi
	 * @return resolved body
	 */
	protected Mono<Object> resolveUrlEncodedBody(ServerWebExchange exchange, String schemaName, Class<?> valueType, boolean multi) {
		return exchange.getFormData().map(formData -> multi ? formData : toSingleValueMap(formData))
				.map(formData -> {
					try {
						JsonNode node = objectMapper.valueToTree(formData);
						return validateAndMarshal(schemaName, node, valueType);
					} catch (IOException e) {
						throw new InvalidRequestException("invalid urlencoded body");
					}
				});
	}

	/**
	 * Read the request JSON body, validate it agains a JSON schema, and resolve it into valueType.
	 *
	 * @param exchange
	 * @param schemaName
	 * @param valueType
	 * @return resolved body
	 */
	protected Mono<Object> resolveJsonBody(ServerWebExchange exchange, String schemaName, Class<?> valueType) {
		return getBodyInputStream(exchange).map(is -> {
			try {
				JsonNode node = objectMapper.readTree(is);
				if (node == null) {
					throw new InvalidRequestException("expected json body");
				}
				return validateAndMarshal(schemaName, node, valueType);
			} catch (IOException e) {
				throw new InvalidRequestException("invalid json body");
			}
		});
	}

	/**
	 * Read the request query parameters, validate it agains a JSON schema, and resolve it into valueType.
	 *
	 * @param exchange
	 * @param schemaName
	 * @param valueType
	 * @param multi
	 * @return resolved query parameters
	 */
	protected Mono<Object> resolveQueryParams(ServerWebExchange exchange, String schemaName, Class<?> valueType, boolean multi) {
		MultiValueMap<String, String> queryParams = exchange.getRequest().getQueryParams();
		try {
			JsonNode node = objectMapper.valueToTree(multi ? queryParams : toSingleValueMap(queryParams));
			return Mono.just(validateAndMarshal(schemaName, node, valueType));
		} catch (IOException e) {
			throw new InvalidRequestException("invalid query params");
		}

	}

	/**
	 * Convert a {@link MultiValueMap} into a {@link Map}.
	 *
	 * @param map
	 * @return {@link Map}
	 */
	protected Map<String, String> toSingleValueMap(MultiValueMap<String, String> map) {
		map.forEach((k, v) -> {
			if (v.size() != 1) {
				throw new InvalidRequestException(String.format("$.%s: only one value is permitted", k));
			}
		});
		return map.toSingleValueMap();
	}

	/**
	 * Validate a JSON node against a JSON schema and marshal it into valueType type.
	 *
	 * @param schemaName
	 * @param node
	 * @param valueType
	 * @return object after marshalling into valueType
	 * @throws IOException
	 */
	protected Object validateAndMarshal(String schemaName, JsonNode node, Class<?> valueType) throws IOException {
		validator.validate(schemaName, node);
		if (valueType == JsonNode.class) {
			return node;
		}
		return objectMapper.treeToValue(node, valueType);
	}

	/**
	 * Read the body from the {@link ServerWebExchange}.
	 *
	 * @param exchange
	 * @return reactive {@link InputStream}
	 */
	protected Mono<InputStream> getBodyInputStream(ServerWebExchange exchange) {
		return exchange.getRequest().getBody()
				.map(DataBuffer::asInputStream)
				.collectList()
				.map(isList -> {
					Enumeration<InputStream> isEnum = Collections.enumeration(isList);
					return new SequenceInputStream(isEnum);
				});
	}

}
