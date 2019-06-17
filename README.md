# Almaraz

Almaraz is a set of components to build up a production-ready service with Spring WebFlux.

It provides the following functionality:
 - **Context**. It uses the reactive [context](https://projectreactor.io/docs/core/release/api/reactor/util/context/Context.html) to store orthogonal information. The context information is available to any consumer of the reactive stream. It can be used to write contextual logs.
 - **Logging**. Using the reactive context, it provides a library to log contextual information in JSON with [SLF4J](https://www.slf4j.org/) logger.
 - **Validation**. Validate requests and documents against a JSON schema.
 - **Middlewares**. Set of WebFlux webfilters to serve a REST API with production quality.
 - **Exceptions**. Hierarchy of exceptions with support to build an error response. These exceptions are used by all the features of the Almaraz library.

Almaraz is built on top of [Reactor](https://projectreactor.io/) and [Spring WebFlux](https://spring.io/).

See the [example](example) to know how to use Almaraz in a real WebFlux application.

Almaraz receives the name from a Spanish nuclear power plant. This is a reference of the **reactive** nature of this library.

![Almaraz](doc/almaraz.jpeg)

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

Finally, it is required to configure the logger to generate contextual information in JSON. This is really convenient to process this information with a log aggregator. The following file configures the logback logger to write to console and include the MDC parameters:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <appender name="CONSOLE"
    class="ch.qos.logback.core.ConsoleAppender">
    <encoder
      class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
      <providers>
        <timestamp>
          <fieldName>time</fieldName>
          <timeZone>UTC</timeZone>
        </timestamp>
        <logLevel>
          <fieldName>lvl</fieldName>
        </logLevel>
        <loggerName>
          <fieldName>logger</fieldName>
        </loggerName>
        <mdc>
          <excludeMdcKeyName>status</excludeMdcKeyName> 
          <excludeMdcKeyName>latency</excludeMdcKeyName> 
        </mdc>
        <pattern>
          <omitEmptyFields>true</omitEmptyFields>
          <pattern>
            {
            "svc": "auth-module",
            "status": "#asLong{%mdc{status}}",
            "latency": "#asLong{%mdc{latency}}"
            }
          </pattern>
        </pattern>
        <message>
          <fieldName>msg</fieldName>
        </message>
        <stackTrace>
          <fieldName>exception</fieldName>
          <throwableConverter class="net.logstash.logback.stacktrace.ShortenedThrowableConverter">
            <maxDepthPerThrowable>1</maxDepthPerThrowable>
            <rootCauseFirst>true</rootCauseFirst>
            <exclude>sun\.reflect\..*\.invoke.*</exclude>
          </throwableConverter>
        </stackTrace>
      </providers>
    </encoder>
  </appender>
  <root level="INFO">
    <appender-ref ref="CONSOLE" />
  </root>
</configuration>
```

## Validation

Almaraz recommends using JSON schema validation to validate inputs (e.g. request body or request query parameters).

It is provided the following classes:

 - `com.elevenpaths.almaraz.validation.JsonSchemaRepository` loads the JSON schemas available in directory `/schemas` under classpath. Each JSON schema is loaded with an schema name that results from removing the `.json` extension from the file name. The goal of this class is to cache the JSON schemas to speed up multiple validations against the same JSON schema.
 - `com.elevenpaths.almaraz.validation.JsonSchemaValidator` provides a method to validate: `void validate(String schemaName, JsonNode node)`. This method receives a schema name to retrieve the JSON schema from the repository, and a Jackson `JsonNode` with the document to be validated.

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

## Middlewares

### Server middlewares

Almaraz provides a set of Spring WebFlux [WebFilters](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/server/WebFilter.html) to comply with frequent requirements. These webfilters are located in the package `com.elevenpaths.almaraz.webfilters`.

| Middleware | Order | Description |
| ---------- | ----- | ----------- |
| RequestContextWebFilter | 10 | It initializes the `RequestContext` with the correlator and transactionId. This instance is stored in the reactive context. |
| LoggerWebFilter | 20 |  It logs the request and response with contextual log information. |
| ErrorWebFilter | 30 |  It handles any exception to build up an error response. |
| CompleteLocationHeaderWebFilter | 40 |  If the response contains a location header with a relative path, then it modifies the header to make it absolute. This webfilter simplifies the controllers so that they only need to add the resource identifier in the location header when the resource is created. |
| BasePathWebFilter | 50 |  It supports the configuration of a base path (aka context path). The controllers would process the request path without the base path. |

These WebFilters can be executed in a chain (pipeline). The class `com.elevenpaths.almaraz.AlmarazConfiguration` is a pragmatic WebFlux configuration that provides a pipeline of Almaraz middlewares which is suitable for most REST servers. Note that `AlmarazConfiguration` also provides the validation beans: `JsonSchemaRepository` and `JsonSchemaValidator`.

The following picture represents the **order of execution** for each webfilter. It is possible to insert additional webfilters in the pipeline:

![WebFilters](doc/webfilters.png)

The way to create all the Almaraz beans is to create a configuration class that extends `AlmarazConfiguration` in your application. In the following example, it is achieved passing the basePath using the configuration property `server.basePath`:

```java
@Configuration
public class WebConfig extends AlmarazConfiguration {

	public WebConfig(@Value("${server.basePath}") String basePath) {
		super(basePath);
	}

}
```

It is also possible to customize which beans are instantiated without using `AlmarazConfiguration`. The following example configures only two webfilters:

```java
@Configuration
public class WebConfig {

	@Order(10)
	@Bean
	public RequestContextWebFilter getContextWebFilter() {
		return new RequestContextWebFilter();
	}

	@Order(20)
	@Bean
	public LoggerWebFilter getLoggerWebFilter() {
		return new LoggerWebFilter();
	}

}
```

### WebClient middlewares

Almaraz provides a set of Spring WebFlux [ExchangeFilterFunctions](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/reactive/function/client/ExchangeFilterFunction.html) to comply with frequent requirements in a [WebClient](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/reactive/function/client/WebClient.html). These middlewares are located in the package `com.elevenpaths.almaraz.webclientfilters`.

| Middleware | Description |
| ---------- | ----------- |
| CorrelatorWebClientFilter | It adds a correlator header in the request of the WebClient. |
| LoggerWebClientFilter | It logs the request and the response of the WebClient. |

The following code configures a [WebClient](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/reactive/function/client/WebClient.html) with both middlewares:

```java
WebClient.builder()
		.baseUrl("http://example.com")
		.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
		.filter(new CorrelatorWebClientFilter())
		.filter(new LoggerWebClientFilter())
		.build();
```

## Exceptions

Almaraz provides a hierarchy of exceptions to build custom error responses. Every Almaraz exception is a `RuntimeException`. This is convenient because it is possible to throw the exception and it will be automatically encapsulated as a reactive error.

The base exception is `com.elevenpaths.almaraz.exceptions.ResponseException`. This exception contains the following attributes:

| Attribute | Type | Optional | Description |
| --------- | ---- | -------- | ----------- |
| status | HttpStatus | no | HTTP status of the error response. |
| error | String | yes | Error identifier. See the list of available error identifiers. |
| reason | String | yes | Error description. |
| headers | Map<String, String> | yes | Map of HTTP headers. Some error responses may require to include HTTP headers (e.g. `www-authenticate` header). |

The `ErrorWebFilter` is responsible for converting an exception into an error response. If the exception extends `ResponseException`, then the error response will contain all the relevant information to generate the response. The error response may contain a JSON body (if error is not null), but some errors are self-descriptive with the status code (e.g. 404 - not found).

The format of the response body complies with the error format defined by [OAuth2 standard](https://tools.ietf.org/html/rfc6749#section-5.2):

```json
{
    "error": "invalid_request",
    "error_description": "$.invalid: is not defined in the schema and the schema does not allow additional properties"
}
```

The list of predefined error identifiers are:

| error |
| ----- |
| invalid_grant |
| invalid_request |
| invalid_scope |
| unauthorized_client |
| unsupported_grant_type |
| invalid_client |
| forbidden |
| conflict |
| server_error |

Almaraz provides a hierarchy of exceptions that implements these errors:

| exception | error | status | description |
| --------- | ----- | ------ | ----------- |
| InvalidGrantException | invalid_grant | 400 | The authorization code (or user’s password for the password grant type) is invalid or expired. This is also the error you would return if the redirect URL given in the authorization grant does not match the URL provided in this access token request. |
| InvalidRequestException | invalid_request | 400 | The request is invalid or malformed. For example, if the request body does not comply with JSON schema. |
| InvalidScopeException | invalid_scope | 400 | For access token requests that include a scope (password or client_credentials grants), this error indicates an invalid scope value in the request. |
| UnauthorizedClientException | unauthorized_client | 400 | The client is unauthorized. For example, when the client requests a scope which is not allowed. |
| ExpiredTokenException | unauthorized_client | 400 | The client is unauthorized because the access token is expired. |
| UnsupportedGrantTypeException | unsupported_grant_type | 400 | If a grant type is requested that the authorization server doesn’t recognize. |
| InvalidClientException | invalid_client | 401 | Client authentication failed, such as if the request contains invalid credentials. |
| InvalidTokenException | unauthorized_client | 401 | Client/Application requested a protected OAuth resource with an invalid token. |
| ForbiddenException | forbidden | 403 | The access to a resource is forbidden. |
| InsufficientScopesException | unauthorized_client | 403 | The client is unauthorized because, although the access token is valid, it does not fulfil all the required scopes to access the protected resource. |
| NotFoundException | - | 404 | Resource not found. |
| ConflictException | conflict | 409 | Error due to a conflict. For example, when it is not possible to create a resource in database due to a violation of the uniqueness of a field. |
| UnsupportedMediaTypeException | - | 415 | Unsupported media type. |
| ServerException | server_error | 500 | Internal error due to unhandled exception or bad integration with external systems. |

## License

Copyright 2019 [Telefónica Investigación y Desarrollo, S.A.U](http://www.tid.es)

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
