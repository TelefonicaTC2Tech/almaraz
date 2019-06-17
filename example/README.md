# Almaraz example

This is a WebFlux application to demonstrated how to use Almaraz library.

The application was created with Spring Boot wizard. Then, it was added the Almaraz dependency in the pom.xml file.

The applications includes the following classes:

| Class | Description |
| ----- | ----------- |
| Application | SpringBootApplication as it was created with Spring Boot wizard. |
| ApplicationConfiguration | Spring Configuration extending AlmarazConfiguration to configure all the default middlewares in the server. It also configures the custom resolver as well as the JSON schema validation bean. |
| Controller | It provides a REST controller. |
| HttpbinWebClient | It is a WebClient, configured with client middlewares to add the correlator header and log the request and response to a remote service. |
| User | Domain class. |

The REST controller provides 3 endpoints (note that the base path is configured as `/api` with configuration property `almaraz-example.base-path`):

| Operation | Method | Path | Description |
| --------- | ------ | ---- | ----------- |
| createUser | POST | /users | The annotation `@ValidRequestBody` validates the request body against JSON schema `/schemas/user.json` and binds it to the domain entity `User`. Note that the request supports both application/json and application/x-www-form-urlencoded media types. The response includes the location header that is updated by CompleteLocationHeaderWebFilter. |
| findUsers | GET | /users | The annotation `@ValidRequestBody` validates the query parameters against JSON schema `/schemas/user.json` and binds it to the domain entity `User`. |
| proxy | POST | /httpbin | The annotation `@ValidRequestBody` validates the request body against JSON schema `/schemas/user.json` and binds it to the domain entity `User`. This request is forwarded to a remote server [httpbin](http://httpbin.org) using `HttpbinWebClient`. |

## Validating and binding a JSON request

Request

```sh
curl -v -X POST -H 'Content-Type: application/json' -d '{"name": "jorge", "country": "es"}' http://localhost:8080/api/users
```

Response

```http
HTTP/1.1 201 Created
Content-Type: application/json;charset=UTF-8
Content-Length: 75
Unica-Correlator: aef3619f-c6af-43ed-83f6-349ff3831e36
Location: http://localhost:8080/api/users/f57b4279-5109-44a8-8cad-4eee5be1b72a

{"id":"f57b4279-5109-44a8-8cad-4eee5be1b72a","name":"jorge","country":"es"}
```

Logs

```json
{"time":"2019-06-17T11:54:18.016+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","path":"/api/users","corr":"aef3619f-c6af-43ed-83f6-349ff3831e36","address":"/0:0:0:0:0:0:0:1","method":"POST","trans":"aef3619f-c6af-43ed-83f6-349ff3831e36","msg":"Request","svc":"almaraz-example","status":null,"latency":null}
{"time":"2019-06-17T11:54:18.390+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","corr":"aef3619f-c6af-43ed-83f6-349ff3831e36","trans":"aef3619f-c6af-43ed-83f6-349ff3831e36","msg":"Response","svc":"almaraz-example","status":201,"latency":379}
```

## Validating and binding a URL encoded request

Request

```sh
curl -v -X POST -H 'Content-Type: application/x-www-form-urlencoded' -d 'name=jorge&country=es&addressCode=3' http://localhost:8080/api/users
```

Response

```http
HTTP/1.1 201 Created
Content-Type: application/json;charset=UTF-8
Content-Length: 91
Unica-Correlator: e073affa-fbb6-41e4-84e1-c54e863cd420
Location: http://localhost:8080/api/users/5f7a56a8-e02d-4067-8dbc-620b76ce154a

{"id":"5f7a56a8-e02d-4067-8dbc-620b76ce154a","name":"jorge","country":"es","addressCode":3}
```

Logs

```json
{"time":"2019-06-17T11:55:13.662+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","path":"/api/users","corr":"e073affa-fbb6-41e4-84e1-c54e863cd420","address":"/0:0:0:0:0:0:0:1","method":"POST","trans":"e073affa-fbb6-41e4-84e1-c54e863cd420","msg":"Request","svc":"almaraz-example","status":null,"latency":null}
{"time":"2019-06-17T11:55:13.684+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","corr":"e073affa-fbb6-41e4-84e1-c54e863cd420","trans":"e073affa-fbb6-41e4-84e1-c54e863cd420","msg":"Response","svc":"almaraz-example","status":201,"latency":22}
```

## Validating and binding query parameters of the request

Request

```sh
curl -v 'http://localhost:8080/api/users?name=jorge&country=es'
```

Response

```http
HTTP/1.1 200 OK
transfer-encoding: chunked
Content-Type: application/json;charset=UTF-8
Unica-Correlator: 85fd01f9-1958-461a-a494-749b88b3bc88

[{"name":"jorge","country":"es"}]
```

Logs

```json
{"time":"2019-06-17T11:55:48.293+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","path":"/api/users","corr":"85fd01f9-1958-461a-a494-749b88b3bc88","address":"/0:0:0:0:0:0:0:1","method":"GET","trans":"85fd01f9-1958-461a-a494-749b88b3bc88","msg":"Request","svc":"almaraz-example","status":null,"latency":null}
{"time":"2019-06-17T11:55:48.314+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","corr":"85fd01f9-1958-461a-a494-749b88b3bc88","trans":"85fd01f9-1958-461a-a494-749b88b3bc88","msg":"Response","svc":"almaraz-example","status":200,"latency":21}
```

## Invalid request body (violation of JSON schema)

Request

```sh
curl -v -X POST -H 'Content-Type: application/json' -d '{"name": "jorge", "country": "spain"}' http://localhost:8080/api/users
```

Response

```http
HTTP/1.1 400 Bad Request
Content-Type: application/json;charset=UTF-8
Unica-Correlator: 13e30bd3-a9f0-499b-9058-bcc1dada65ef
content-length: 126

{"error":"invalid_request","error_description":"$.country: does not have a value in the enumeration [es, uk, fr, us, br, ar]"}
```

Logs

```json
{"time":"2019-06-17T11:56:27.070+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","path":"/api/users","corr":"13e30bd3-a9f0-499b-9058-bcc1dada65ef","address":"/0:0:0:0:0:0:0:1","method":"POST","trans":"13e30bd3-a9f0-499b-9058-bcc1dada65ef","msg":"Request","svc":"almaraz-example","status":null,"latency":null}
{"time":"2019-06-17T11:56:27.085+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.ErrorWebFilter","reason":"$.country: does not have a value in the enumeration [es, uk, fr, us, br, ar]","corr":"13e30bd3-a9f0-499b-9058-bcc1dada65ef","error":"invalid_request","trans":"13e30bd3-a9f0-499b-9058-bcc1dada65ef","msg":"Error","svc":"almaraz-example","status":null,"latency":null}
{"time":"2019-06-17T11:56:27.086+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","corr":"13e30bd3-a9f0-499b-9058-bcc1dada65ef","trans":"13e30bd3-a9f0-499b-9058-bcc1dada65ef","msg":"Response","svc":"almaraz-example","status":400,"latency":16}
```

## Invalid request query parameters (violation of JSON schema)

Request

```sh
curl -v 'http://localhost:8080/api/users?name=jorge&country=es&addressCode=street'
```

Response

```http
HTTP/1.1 400 Bad Request
Content-Type: application/json;charset=UTF-8
Unica-Correlator: b96c7959-9630-4cbd-8058-e65c3dbb4b1f
content-length: 95

{"error":"invalid_request","error_description":"$.addressCode: string found, integer expected"}
```

Logs

```json
{"time":"2019-06-17T11:57:01.981+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","path":"/api/users","corr":"b96c7959-9630-4cbd-8058-e65c3dbb4b1f","address":"/0:0:0:0:0:0:0:1","method":"GET","trans":"b96c7959-9630-4cbd-8058-e65c3dbb4b1f","msg":"Request","svc":"almaraz-example","status":null,"latency":null}
{"time":"2019-06-17T11:57:01.983+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.ErrorWebFilter","reason":"$.addressCode: string found, integer expected","corr":"b96c7959-9630-4cbd-8058-e65c3dbb4b1f","error":"invalid_request","trans":"b96c7959-9630-4cbd-8058-e65c3dbb4b1f","msg":"Error","svc":"almaraz-example","status":null,"latency":null}
{"time":"2019-06-17T11:57:01.983+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","corr":"b96c7959-9630-4cbd-8058-e65c3dbb4b1f","trans":"b96c7959-9630-4cbd-8058-e65c3dbb4b1f","msg":"Response","svc":"almaraz-example","status":400,"latency":2}
```

## Request using WebClient

The web client is configured with the middlewares to add the correlator header and to log the request and response. Note that the response is the remote server response (httpbin.org) that includes the request headers.

Request

```sh
curl -v -X POST -H 'Content-Type: application/json' -d '{"name": "jorge", "country": "es"}' http://localhost:8080/api/httpbin
```

Response

```http
HTTP/1.1 200 OK
Content-Type: application/json;charset=UTF-8
Content-Length: 433
Unica-Correlator: 89b06970-cf38-48ea-91e9-73c75603e87e

{"args":{},"data":"{\"name\":\"jorge\",\"country\":\"es\"}","files":{},"form":{},"headers":{"Accept":"application/json","Accept-Encoding":"gzip","Content-Length":"31","Content-Type":"application/json","Host":"httpbin.org","Unica-Correlator":"89b06970-cf38-48ea-91e9-73c75603e87e","User-Agent":"ReactorNetty/0.8.8.RELEASE"},"json":{"country":"es","name":"jorge"},"origin":"83.47.41.226, 83.47.41.226","url":"https://httpbin.org/post"}
```

Logs

```json
{"time":"2019-06-17T11:57:41.575+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","path":"/api/httpbin","corr":"89b06970-cf38-48ea-91e9-73c75603e87e","address":"/0:0:0:0:0:0:0:1","method":"POST","trans":"89b06970-cf38-48ea-91e9-73c75603e87e","msg":"Request","svc":"almaraz-example","status":null,"latency":null}
{"time":"2019-06-17T11:57:41.581+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webclientfilters.LoggerWebClientFilter","corr":"89b06970-cf38-48ea-91e9-73c75603e87e","method":"POST","url":"http://httpbin.org/post","trans":"89b06970-cf38-48ea-91e9-73c75603e87e","msg":"Client request","svc":"almaraz-example","status":null,"latency":null}
{"time":"2019-06-17T11:57:42.466+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webclientfilters.LoggerWebClientFilter","corr":"89b06970-cf38-48ea-91e9-73c75603e87e","trans":"89b06970-cf38-48ea-91e9-73c75603e87e","msg":"Client response","svc":"almaraz-example","status":200,"latency":886}
{"time":"2019-06-17T11:57:42.469+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","corr":"89b06970-cf38-48ea-91e9-73c75603e87e","trans":"89b06970-cf38-48ea-91e9-73c75603e87e","msg":"Response","svc":"almaraz-example","status":200,"latency":894}
```
## Request forcing a correlator

The client may force a custom correlator using the HTTP header `Unica-Correlator`. Note that the logs include this custom correlator, as well as the correlator is also sent by the web client to the remote server.

Request

```sh
curl -v -X POST -H 'Unica-Correlator: test-corr' -H 'Content-Type: application/json' -d '{"name": "jorge", "country": "es"}' http://localhost:8080/api/httpbin
```

Response

```http
HTTP/1.1 200 OK
Content-Type: application/json;charset=UTF-8
Content-Length: 406
Unica-Correlator: test-corr

{"args":{},"data":"{\"name\":\"jorge\",\"country\":\"es\"}","files":{},"form":{},"headers":{"Accept":"application/json","Accept-Encoding":"gzip","Content-Length":"31","Content-Type":"application/json","Host":"httpbin.org","Unica-Correlator":"test-corr","User-Agent":"ReactorNetty/0.8.8.RELEASE"},"json":{"country":"es","name":"jorge"},"origin":"83.47.41.226, 83.47.41.226","url":"https://httpbin.org/post"}
```

Logs

```json
{"time":"2019-06-17T11:59:00.850+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","path":"/api/httpbin","corr":"test-corr","address":"/0:0:0:0:0:0:0:1","method":"POST","trans":"12f775c8-59e7-479e-ba1d-c127bfba0687","msg":"Request","svc":"almaraz-example","status":null,"latency":null}
{"time":"2019-06-17T11:59:00.852+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webclientfilters.LoggerWebClientFilter","corr":"test-corr","method":"POST","url":"http://httpbin.org/post","trans":"12f775c8-59e7-479e-ba1d-c127bfba0687","msg":"Client request","svc":"almaraz-example","status":null,"latency":null}
{"time":"2019-06-17T11:59:01.527+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webclientfilters.LoggerWebClientFilter","corr":"test-corr","trans":"12f775c8-59e7-479e-ba1d-c127bfba0687","msg":"Client response","svc":"almaraz-example","status":200,"latency":675}
{"time":"2019-06-17T11:59:01.529+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","corr":"test-corr","trans":"12f775c8-59e7-479e-ba1d-c127bfba0687","msg":"Response","svc":"almaraz-example","status":200,"latency":679}
```

## Request with invalid remote server

If the remote server is configured incorrectly (e.g. configuration property `almaraz-example.httpbin-url` is `http://httpbinbug.org`), the web client throws a server error.

Request

```sh
curl -v -X POST -H 'Content-Type: application/json' -d '{"name": "jorge", "country": "es"}' http://localhost:8080/api/httpbin
```

Response

```http
HTTP/1.1 500 Internal Server Error
Content-Type: application/json;charset=UTF-8
Unica-Correlator: bc2c4c0b-305a-4424-bb00-554ff1ed2437
content-length: 24

{"error":"server_error"}
```

Logs

```json
{"time":"2019-06-17T12:02:35.481+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","path":"/api/httpbin","corr":"bc2c4c0b-305a-4424-bb00-554ff1ed2437","address":"/0:0:0:0:0:0:0:1","method":"POST","trans":"bc2c4c0b-305a-4424-bb00-554ff1ed2437","msg":"Request","svc":"almaraz-example","status":null,"latency":null}
{"time":"2019-06-17T12:02:35.770+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webclientfilters.LoggerWebClientFilter","corr":"bc2c4c0b-305a-4424-bb00-554ff1ed2437","method":"POST","url":"http://httpbinbug.org/post","trans":"bc2c4c0b-305a-4424-bb00-554ff1ed2437","msg":"Client request","svc":"almaraz-example","status":null,"latency":null}
{"time":"2019-06-17T12:02:36.312+00:00","lvl":"ERROR","logger":"com.elevenpaths.almaraz.webfilters.ErrorWebFilter","corr":"bc2c4c0b-305a-4424-bb00-554ff1ed2437","error":"server_error","trans":"bc2c4c0b-305a-4424-bb00-554ff1ed2437","msg":"Error","exception":"java.net.UnknownHostException: httpbinbug.org: nodename nor servname provided, or not known\n\tat java.net.Inet6AddressImpl.lookupAllHostAddr(Inet6AddressImpl.java)\n\t... 115 frames truncated\n","svc":"almaraz-example","status":null,"latency":null}
{"time":"2019-06-17T12:02:36.318+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","corr":"bc2c4c0b-305a-4424-bb00-554ff1ed2437","trans":"bc2c4c0b-305a-4424-bb00-554ff1ed2437","msg":"Response","svc":"almaraz-example","status":500,"latency":845}
```

## Request with invalid path type

For the logs resource, the controller binds the path `/logs/{logId}` to an integer. If the path parameter is not an integer, Spring generates a `org.springframework.web.server.ServerWebInputException`. This exception is converted into an Almaraz exception with the same status code.

Request

```sh
curl -v http://localhost:8080/api/logs/invalidLogId
```

Response

```http
HTTP/1.1 400 Bad Request
Unica-Correlator: 1d480d31-fbb0-4577-aa74-400d10fdb33d
content-length: 0
```

Logs

```json
{"time":"2019-06-17T12:00:11.803+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","path":"/api/logs/invalidLogId","corr":"1d480d31-fbb0-4577-aa74-400d10fdb33d","address":"/0:0:0:0:0:0:0:1","method":"GET","trans":"1d480d31-fbb0-4577-aa74-400d10fdb33d","msg":"Request","svc":"almaraz-example","status":null,"latency":null}
{"time":"2019-06-17T12:00:11.818+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.ErrorWebFilter","corr":"1d480d31-fbb0-4577-aa74-400d10fdb33d","trans":"1d480d31-fbb0-4577-aa74-400d10fdb33d","msg":"Error","svc":"almaraz-example","status":null,"latency":null}
{"time":"2019-06-17T12:00:11.821+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","msg":"Response","svc":"almaraz-example","status":400,"latency":18}
```

## Log enrichment

The `getLog` API resource includes as an example how to enrich the log context and how to log custom statements (apart from the log records generated by `LoggerWebFilter`).

```java
return RequestContext.context()
		.map(ctxt -> ctxt.setOperation("getLog"))
		.doOnEach(ReactiveLogger.logOnNext(ctxt -> {
			MDC.put("logId", Integer.toString(logId));
			log.info("Receiving log request with logId {}", logId);
		}))
		.map(ctxt -> {
			if (logId % 2 != 0) {
				throw new RuntimeException("logId cannot be an odd number");
			}
			return logId;
		})
		.doOnEach(ReactiveLogger.logOnComplete(() -> {
			log.info("Processed log request successfully");
		}))
		.doOnEach(ReactiveLogger.logOnError(t -> {
			log.error("Error processing logRequest", t);
		}));
```

To add custom properties to the log context initialized by `RequestContextWebFilter`, you need to retrieve the context. The easiest way is to use `RequestContext.context()`. Note that reactive context cannot be retrieved synchronously. The example adds the operation name to the `RequestContext`. The operation will appear in all the log records for this request from now on.

The example also logs the start of the operation adding contextual information to MDC. The main different with enriching the `RequestContext` is that MDC parameters will only be added in this log record, but not appear in next log records.

Finally, if logId is odd, it will throw an exception and it will be logged by `ReactiveLogger.logOnError`. Otherwise, it will be logged by `ReactiveLogger.logOnComplete`.

See the log records for an even logId:

```json
{"time":"2019-06-17T12:01:06.697+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","path":"/api/logs/4","corr":"bc3a9a02-e0ac-42ea-8495-5ff7da4ae0fb","address":"/0:0:0:0:0:0:0:1","method":"GET","trans":"bc3a9a02-e0ac-42ea-8495-5ff7da4ae0fb","msg":"Request","svc":"almaraz-example","status":null,"latency":null}
{"time":"2019-06-17T12:01:06.705+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.example.Controller","op":"getLog","corr":"bc3a9a02-e0ac-42ea-8495-5ff7da4ae0fb","logId":"4","trans":"bc3a9a02-e0ac-42ea-8495-5ff7da4ae0fb","msg":"Receiving log request with logId 4","svc":"almaraz-example","status":null,"latency":null}
{"time":"2019-06-17T12:01:06.706+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.example.Controller","op":"getLog","corr":"bc3a9a02-e0ac-42ea-8495-5ff7da4ae0fb","trans":"bc3a9a02-e0ac-42ea-8495-5ff7da4ae0fb","msg":"Processed log request successfully","svc":"almaraz-example","status":null,"latency":null}
{"time":"2019-06-17T12:01:06.706+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","op":"getLog","corr":"bc3a9a02-e0ac-42ea-8495-5ff7da4ae0fb","trans":"bc3a9a02-e0ac-42ea-8495-5ff7da4ae0fb","msg":"Response","svc":"almaraz-example","status":200,"latency":14}
```

The log records for an odd logId:

```json
{"time":"2019-06-17T12:01:21.760+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","path":"/api/logs/5","corr":"4cd3fe28-fe8d-45c4-be76-8ee24258d1e5","address":"/0:0:0:0:0:0:0:1","method":"GET","trans":"4cd3fe28-fe8d-45c4-be76-8ee24258d1e5","msg":"Request","svc":"almaraz-example","status":null,"latency":null}
{"time":"2019-06-17T12:01:21.761+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.example.Controller","op":"getLog","corr":"4cd3fe28-fe8d-45c4-be76-8ee24258d1e5","logId":"5","trans":"4cd3fe28-fe8d-45c4-be76-8ee24258d1e5","msg":"Receiving log request with logId 5","svc":"almaraz-example","status":null,"latency":null}
{"time":"2019-06-17T12:01:21.763+00:00","lvl":"ERROR","logger":"com.elevenpaths.almaraz.example.Controller","op":"getLog","corr":"4cd3fe28-fe8d-45c4-be76-8ee24258d1e5","trans":"4cd3fe28-fe8d-45c4-be76-8ee24258d1e5","msg":"Error processing logRequest","exception":"java.lang.RuntimeException: logId cannot be an odd number\n\tat com.elevenpaths.almaraz.example.Controller.lambda$2(Controller.java:97)\n\t... 138 frames truncated\n","svc":"almaraz-example","status":null,"latency":null}
{"time":"2019-06-17T12:01:21.765+00:00","lvl":"ERROR","logger":"com.elevenpaths.almaraz.webfilters.ErrorWebFilter","op":"getLog","corr":"4cd3fe28-fe8d-45c4-be76-8ee24258d1e5","error":"server_error","trans":"4cd3fe28-fe8d-45c4-be76-8ee24258d1e5","msg":"Error","exception":"java.lang.RuntimeException: logId cannot be an odd number\n\tat com.elevenpaths.almaraz.example.Controller.lambda$2(Controller.java:97)\n\t... 138 frames truncated\n","svc":"almaraz-example","status":null,"latency":null}
{"time":"2019-06-17T12:01:21.765+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","op":"getLog","corr":"4cd3fe28-fe8d-45c4-be76-8ee24258d1e5","trans":"4cd3fe28-fe8d-45c4-be76-8ee24258d1e5","msg":"Response","svc":"almaraz-example","status":500,"latency":6}
```