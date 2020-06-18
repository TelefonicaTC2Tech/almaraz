// Copyright (c) Telefonica I+D. All rights reserved.
package com.elevenpaths.almaraz.context.aspects;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to add the operation to the RequestContext.
 * 
 * Usage:
 * <code>
 * {@literal @}RestController
 * public class DemoController {
 * 	{@literal @}OperationRequestContext("insert-user")
 * 	{@literal @}PostMapping(value = "/users")
 * 	public String insertUser(@ValidRequestBody("json-schema") TestType value) {
 * 		...
 * 	}
 * }
 * </code>
 * 
 * If the annotation is used without an argument ( {@literal @}OperationRequestContext ) the operation
 * name will be taken from the method name.
 *
 * @author Juan Antonio Hernando
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationRequestContext {
	/**
	 * Operation name.
	 *
	 * @return String
	 */
	public String value() default "";
}
