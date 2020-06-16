//Copyright (c) Telefonica I+D. All rights reserved.

package com.elevenpaths.almaraz.context.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;

import com.elevenpaths.almaraz.context.RequestContext;
import com.elevenpaths.almaraz.context.annotations.OperationRequestContext;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
* Aspects for OperationLog annotation.
*
* @author Juan Antonio Hernando
*
*/
@Component
@Aspect
public class OperationLogAspect {

	/**
	 * It wraps annotated methods with OperationLog and add the operation in the reactive logs.
	 *
	 * @param joinPoint
	 * @param operationRequestContext
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Around("@annotation(operationRequestContext)")
	public Object operationLog(ProceedingJoinPoint joinPoint, OperationRequestContext operationRequestContext) throws Throwable {
		String methodName = operationRequestContext.value().isEmpty() ? joinPoint.getSignature().getName() :
			operationRequestContext.value();
		Object result = joinPoint.proceed();
		if (!(result instanceof Publisher)) {
			return result;
		}

		Mono<RequestContext> monoRequestContext = RequestContext.context()
				.map(ctxt -> ctxt.setOperation(methodName));

		if (result instanceof Mono) {
			return monoRequestContext.then((Mono) result);
		}

		return monoRequestContext.thenMany((Flux) result);
	}

}
