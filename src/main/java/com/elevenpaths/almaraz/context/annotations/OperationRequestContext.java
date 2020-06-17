// Copyright (c) Telefonica I+D. All rights reserved.
package com.elevenpaths.almaraz.context.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to add the operation type in the log traces.
 *
 * @author Juan Antonio Hernando
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationRequestContext {
	public String value() default "";
}
