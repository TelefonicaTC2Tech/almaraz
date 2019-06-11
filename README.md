# almaraz

Almaraz is a set of components to build up a production-ready service with Spring WebFlux.

It provides the following functionality:
 - **Context**. It uses the reactive [context](https://projectreactor.io/docs/core/release/api/reactor/util/context/Context.html) to store orthogonal information. The context information is available to any consumer of the reactive stream. It can be used to write contextual logs.
 - **Logging**. Using the reactive context, it provides a library to log contextual information in JSON with [SLF4J](https://www.slf4j.org/) logger.
 - **Validation**. Validate requests and documents against a JSON schema.
 - **Middlewares**. Set of WebFlux webfilters to serve a REST API with production quality.
 - **Exceptions**. Hierarchy of exceptions with support to build an error response. These exceptions are used by all the features of the Almaraz library.

Almaraz is built on top of [Reactor](https://projectreactor.io/) and [Spring WebFlux](https://spring.io/).

## Context

Reactor provides a [context](https://projectreactor.io/docs/core/release/api/reactor/util/context/Context.html) where it is possible to store orthogonal information. For example, Spring Security uses the context to store the user identifier after the authentication process.

The context is relevant to share information all along the reactive stream to avoid passing this information to every method. Typically, this information was stored at thread level, but this is not possible in reactive programming because the same thread can be used by multiple streams at the same time.

The Reactor context is immutable. Every time the context is modified, it returns a new instance. According to reactor [information](https://projectreactor.io/docs/core/release/api/reactor/util/context/Context.html), it is recommended to use a dedicated mutable structure, instead of storing the information directly to the Reactor context.

The class `com.elevenpaths.almaraz.context.RequestContext` is designed according to MDC constraints to log contextual information with SLF4J. This class includes a `Map<String, String>` and it is possible to put/get the following types: `String`, `Boolean`, `Long`. Any value that is stored in the map is converted to `String`. Apart from a general map to store any contextual information, it states the following context elements:

| Name | Key | Type | Description |
| ---- | --- | ---- | ----------- |
| transactionId | trans | String | Unique identifier of a request/response flow. |
| correlator | corr | String | Correlator to track logs corresponding to a HTTP flow. |
| operation | op | String | Name of the operation (e.g. createUser). |
| service | svc | String | Service name. |
| component | comp | String | Component name. |
| user | user | String | User identifier. |
| realm | realm | String | Realm. |
| alarm | alarm | String | Alarm identifier to track the start/stop of an alarm. |

`RequestContext` also provides a method to retrieve it (associated to the current reactive stream):

```java
public static Mono<RequestContext> context() {
    return Mono.subscriberContext()
            .map(ctxt -> ctxt.getOrDefault(RequestContext.class, new RequestContext()));
}
```

Note that it is assumed that `RequestContext` is always stored in the Reactor context under the key `RequestContext.class`.

## Logging

Logging is based on the ideas provided by Simon Basle in [Contextual Logging with Reactor Context and MDC](https://simonbasle.github.io/2018/02/contextual-logging-with-reactor-context-and-mdc/). The implementation uses Reactor `doOnEach`:

```java
Mono<T> doOnEach(Consumer<? super Signal<T>> signalConsumer)
```

This function is executed whenever an item is emitted, fails with an error or completes successfully for both `Mono` and `Flux`. The consumer receives a `Signal` that provides access to the reactive context with `signal.getContext()`.

The class `com.elevenpaths.almaraz.logging.ReactiveLogger` provides the low-level method `logOnSignal`:

```java
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
```

This function executes the predicate `isSignal` to filter which signals are relevant for logging. If the predicate is true, then it gets the `RequestContext` from the reactive context (obtained from the signal), configures the contextual information with MDC, and executes the log consumer. Finally, the MDC is reset, so that the log context is not available for following log records.

`ReactiveLogger` also provides 3 high-level functions based on the signal type:

| Function | Signal | Description |
| -------- | ------ | ----------- |
| `logOnNext(Consumer<T> log)` | ON_NEXT | Log when an item is emitted passing the item to the log consumer. |
| `logOnComplete(Runnable log)` | ON_COMPLETE | Log when a reactive stream is completed successfully. |
| `logOnError(Consumer<Throwable> log)` | ON_ERROR | Log when an error is thrown passing the exception to the log consumer. |

The following example initializes the {@link RequestContext} in the reactive context and configures the logger for signals: next, complete, and error. The method logOnNext is invoked twice (one per item in the reactive stream), the method logOnComplete is invoked only once, and the method logOnError is not invoked because there is no error.

```java
Flux.just("test 1", "test 2")
    .doOnEach(ReactiveLogger.logOnNext(next -> log.info("Next: {}", next)))
    .doOnEach(ReactiveLogger.logOnComplete(() -> log.info("Complete")))
    .doOnEach(ReactiveLogger.logOnError(error -> log.error("Error", error)))
    .subscriberContext(Context.of(RequestContext.class, new RequestContext().setCorrelator("test-corr")))
    .subscribe();
```

## Validation

Almaraz recommends using JSON schema validation to validate inputs (e.g. request body or request query parameters).

It is provided the following classes:

 - `com.elevenpaths.almaraz.validation.JsonSchemaRepository` loads the JSON schemas available in directory `/schemas` under classpath. Each JSON schema is loaded with an schema name that results from removing the `.json` extension from the file name. The goal of this class is to cache the JSON schemas to speed up multiple validations against the same JSON schema.
 - `com.elevenpaths.almaraz.validation.JsonSchemaValidator` provides a method to validate: `void validate(String schemaName, JsonNode node)`. This method receives a schema name to retrieve the JSON schema from the repository, and a Jackson `JsonNode` with the document to be validated.

### @ValidRequestBody

The `JsonSchemaValidator` can be used directly to validate any `JsonNode`. However, Almaraz also provides an annotation `@ValidRequestBody` to decorate an argument of a controller method. This annotation will retrieve the request body, validates it against a JSON schema, and binds it to the type of the method argument. Note that it is equivalent to standard `@RequestBody` but with additional JSON schema validation. The reason to merge validation and binding in a single annotation is to validate it against the original data, because the binding to a Java type could lose information (e.g. elements available in the document but absent in the Java type).

The following controller uses this annotation to validate the request body against the JSON schema stored at `/schemas/json-schema.json` and bind it to the `TestType` argument:

```
@RestController
public class DemoController {
	@PostMapping(value = "/demo")
	public String demo(@ValidRequestBody("json-schema") TestType value) {
		...
	}
}
```

The annotation supports the following arguments:

| Argument | Default | Description |
| -------- | ------- | ----------- |
| value | "" | Name of the JSON schema to perform the validation |
| multi | false | Multivalue maps (only considered for urlencoded media type). If set to false (default), it is checked that MultiValueMap does not include multiple values for the same element. It is converted the MultiValueMap<String, String> into Map<String, String>. |
| query | false | Process the query parameters of the request, instead of the request body. |

