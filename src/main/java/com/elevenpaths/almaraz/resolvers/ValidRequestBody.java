// Copyright (c) Telefonica I+D. All rights reserved.

package com.elevenpaths.almaraz.resolvers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import org.springframework.util.MultiValueMap;

/**
 * Annotation to resolve a request body into a type (as {@literal @}RequestBody) or query parameters
 * but including validation against a JSON schema.
 *
 * For example:
 * <code>
 * {@literal @}RestController
 * public class DemoController {
 *  	{@literal @}PostMapping(value = "/demo")
 *  	public String demo(@ValidRequestBody("json-schema") TestType value) {
 *  		...
 *  	}
 * }
 * </code>
 *
 * The annotation supports 3 parameters:
 * <ul>
 * <li><b>value</b>. The JSON schema name to validate the body. If empty or unset, the body is not validated.</li>
 * <li><b>multi</b>. For MediaType.APPLICATION_FORM_URLENCODED or query params. If true, the {@link MultiValueMap} is
 * not converted to a {@link Map}. By default, multi is false.</li>
 * <li><b>query</b>. Process the query parameters instead of the request body.</li>
 * </ul>
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ValidRequestBody {
	/**
	 * Name of the JSON schema required to validate the body.
	 *
	 * @return json schema name
	 */
	String value() default "";

	/**
	 * Multivalue maps (only considered for MediaType.APPLICATION_FORM_URLENCODED).
	 * If set to false (default), it is checked that MultiValueMap does not include multiple values
	 * for the same element. It is converted the MultiValueMap<String, String> into Map<String, String>.
	 *
	 * @return false if {@link MultiValueMap} is converted to {@link Map}
	 */
	boolean multi() default false;

	/**
	 * Process the query parameters of the request, instead of the request body.
	 *
	 * @return true to validate query parameters
	 */
	boolean query() default false;
}
