/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.logging;

import java.util.function.Consumer;
import java.util.function.Predicate;

import org.slf4j.MDC;

import com.elevenpaths.almaraz.context.RequestContext;

import reactor.core.publisher.Signal;
import reactor.core.publisher.SignalType;
import reactor.util.context.Context;

/**
 * Reactive logger using MDC (Mapped Diagnostic Context) to generate log entries with contextual information
 * in a JSON format (although that is optional).
 *
 * Based on Simon Basle ideas (https://simonbasle.github.io/2018/02/contextual-logging-with-reactor-context-and-mdc/).
 *
 * The following example initializes the {@link RequestContext} in the reactive context and configures the logger
 * for signals: next, complete, and error. The method logOnNext is invoked twice (one per item in the reactive stream),
 * the method logOnComplete is invoked only once, and the method logOnError is not invoked because there is no error.
 *
 * <code>
 * Flux.just("test 1", "test 2")
 *   .doOnEach(ReactiveLogger.logOnNext(next -> log.info("Next: {}", next)))
 *   .doOnEach(ReactiveLogger.logOnComplete(() -> log.info("Complete")))
 *   .doOnEach(ReactiveLogger.logOnError(error -> log.error("Error", error)))
 *   .subscriberContext(Context.of(RequestContext.class, new RequestContext().setCorrelator("test-corr")))
 *   .subscribe();
 * </code>
 *
 * Note that the request context (for the MDC properties) must be stored in the reactive {@link Context} with the
 * key {@link RequestContext#getClass()} and must be an instance of the type (or subtype) {@link RequestContext}.
 *
 * The goal of this logger utilities is to configure MDC with the {@link RequestContext} properties while still using
 * a standard logger.
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
public class ReactiveLogger {

	/**
	 * Low level logger that considers the reactive signal to determine if the logger must be invoked or not.
	 * If the logger is to be invoked, then the {@link RequestContext} is extracted from the reactive context
	 * to update MDC with all the context properties. The MDC is cleared afterwards.
	 *
	 * @param isSignal
	 * @param log
	 * @return consumer of the reactive signal to log
	 */
	public static <T> Consumer<Signal<T>> logOnSignal(Predicate<Signal<T>> isSignal, Consumer<Signal<T>> log) {
		return signal -> {
			if (!isSignal.test(signal)) {
				return;
			}
			try {
				RequestContext logContext = signal.getContext().getOrDefault(RequestContext.class, new RequestContext());
				MDC.setContextMap(logContext.getContextMap());
				log.accept(signal);
			} finally {
				MDC.clear();
			}
		};
	}

	/**
	 * Logger triggered with the signal type {@link SignalType#ON_NEXT}.
	 *
	 * @param log
	 * @return consumer of the reactive signal to log
	 */
	public static <T> Consumer<Signal<T>> logOnNext(Consumer<T> log) {
		return logOnSignal(
				signal -> signal.getType() == SignalType.ON_NEXT,
				signal -> log.accept(signal.get()));
	}

	/**
	 * Logger triggered with the signal type {@link SignalType#ON_COMPLETE}.
	 *
	 * @param log
	 * @return consumer of the reactive signal to log
	 */
	public static <T> Consumer<Signal<T>> logOnComplete(Runnable log) {
		return logOnSignal(
				signal -> signal.getType() == SignalType.ON_COMPLETE,
				signal -> log.run());
	}

	/**
	 * Logger triggered with the signal type {@link SignalType#ON_ERROR}.
	 *
	 * @param log
	 * @return consumer of the reactive signal to log
	 */
	public static <T> Consumer<Signal<T>> logOnError(Consumer<Throwable> log) {
		return logOnSignal(
				signal -> signal.getType() == SignalType.ON_ERROR,
				signal -> log.accept(signal.getThrowable()));
	}

}
