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
Unica-Correlator: f7c4e812-39ca-4533-abb6-da995f79b2ea
Location: http://localhost:8080/api/users/640e2e26-a314-470d-9344-57c1f02c77ff

{"id":"640e2e26-a314-470d-9344-57c1f02c77ff","name":"jorge","country":"es"}
```

Logs

```json
{"time":"2019-06-17T08:17:19.892+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","path":"/api/users","corr":"c310ccb6-46cc-4799-ae46-042c0d41d095","address":"/0:0:0:0:0:0:0:1","method":"POST","trans":"c310ccb6-46cc-4799-ae46-042c0d41d095","msg":"Request","svc":"auth-module"}
{"time":"2019-06-17T08:17:20.337+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","corr":"c310ccb6-46cc-4799-ae46-042c0d41d095","trans":"c310ccb6-46cc-4799-ae46-042c0d41d095","status":"201","latency":"453","msg":"Response","svc":"auth-module"}
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
Unica-Correlator: f7c4e812-39ca-4533-abb6-da995f79b2ea
Location: http://localhost:8080/api/users/640e2e26-a314-470d-9344-57c1f02c77ff

{"id":"e254c1fd-9a50-44cc-919e-791257603ef1","name":"jorge","country":"es","addressCode":3}
```

Logs

```json
{"time":"2019-06-17T08:19:38.467+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","path":"/api/users","corr":"36046bda-c2ac-44b3-9067-52875c83ea89","address":"/0:0:0:0:0:0:0:1","method":"POST","trans":"36046bda-c2ac-44b3-9067-52875c83ea89","msg":"Request","svc":"auth-module"}
{"time":"2019-06-17T08:19:38.485+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","corr":"36046bda-c2ac-44b3-9067-52875c83ea89","trans":"36046bda-c2ac-44b3-9067-52875c83ea89","status":"201","latency":"18","msg":"Response","svc":"auth-module"}
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
Unica-Correlator: 05dfd67e-5977-4fba-92ed-4053a979a7ec

[{"name":"jorge","country":"es"}
```

Logs

```json
{"time":"2019-06-17T08:25:03.709+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","path":"/api/users","corr":"d6689190-5a55-4da6-88fc-3f8dacedf7a1","address":"/0:0:0:0:0:0:0:1","method":"GET","trans":"d6689190-5a55-4da6-88fc-3f8dacedf7a1","msg":"Request","svc":"auth-module"}
{"time":"2019-06-17T08:25:03.713+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","corr":"d6689190-5a55-4da6-88fc-3f8dacedf7a1","trans":"d6689190-5a55-4da6-88fc-3f8dacedf7a1","status":"200","latency":"4","msg":"Response","svc":"auth-module"}
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
Unica-Correlator: d8587d6c-80f3-40db-970a-34f155bd8b2d
content-length: 126

{"error":"invalid_request","error_description":"$.country: does not have a value in the enumeration [es, uk, fr, us, br, ar]"}
```

Logs

```json
{"time":"2019-06-17T08:25:53.336+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","path":"/api/users","corr":"5d06cc25-0ac3-4c2f-ba81-6925151bcafa","address":"/0:0:0:0:0:0:0:1","method":"POST","trans":"5d06cc25-0ac3-4c2f-ba81-6925151bcafa","msg":"Request","svc":"auth-module"}
{"time":"2019-06-17T08:25:53.339+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.ErrorWebFilter","reason":"$.country: does not have a value in the enumeration [es, uk, fr, us, br, ar]","corr":"5d06cc25-0ac3-4c2f-ba81-6925151bcafa","error":"invalid_request","trans":"5d06cc25-0ac3-4c2f-ba81-6925151bcafa","msg":"Error","svc":"auth-module"}
{"time":"2019-06-17T08:25:53.339+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","corr":"5d06cc25-0ac3-4c2f-ba81-6925151bcafa","trans":"5d06cc25-0ac3-4c2f-ba81-6925151bcafa","status":"400","latency":"3","msg":"Response","svc":"auth-module"}
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
Unica-Correlator: 0b968ae5-70ba-4914-9666-0d784fb5cea7
content-length: 95

{"error":"invalid_request","error_description":"$.addressCode: string found, integer expected"}
```

Logs

```json
{"time":"2019-06-17T09:00:27.326+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","path":"/api/users","corr":"0b968ae5-70ba-4914-9666-0d784fb5cea7","address":"/0:0:0:0:0:0:0:1","method":"GET","trans":"0b968ae5-70ba-4914-9666-0d784fb5cea7","msg":"Request","svc":"auth-module"}
{"time":"2019-06-17T09:00:27.339+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.ErrorWebFilter","reason":"$.addressCode: string found, integer expected","corr":"0b968ae5-70ba-4914-9666-0d784fb5cea7","error":"invalid_request","trans":"0b968ae5-70ba-4914-9666-0d784fb5cea7","msg":"Error","svc":"auth-module"}
{"time":"2019-06-17T09:00:27.340+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","corr":"0b968ae5-70ba-4914-9666-0d784fb5cea7","trans":"0b968ae5-70ba-4914-9666-0d784fb5cea7","status":"400","latency":"15","msg":"Response","svc":"auth-module"}
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
Content-Length: 431
Unica-Correlator: 396ca542-8819-4035-8797-32bb75a88252

{"args":{},"data":"{\"name\":\"jorge\",\"country\":\"es\"}","files":{},"form":{},"headers":{"Accept":"application/json","Accept-Encoding":"gzip","Content-Length":"31","Content-Type":"application/json","Host":"httpbin.org","Unica-Correlator":"396ca542-8819-4035-8797-32bb75a88252","User-Agent":"ReactorNetty/0.8.8.RELEASE"},"json":{"country":"es","name":"jorge"},"origin":"83.47.41.98, 83.47.41.98","url":"https://httpbin.org/post"}
```

Logs

```json
{"time":"2019-06-17T08:26:24.480+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","path":"/api/httpbin","corr":"6ce836d8-3983-4cfb-8c60-c0b477a1c117","address":"/0:0:0:0:0:0:0:1","method":"POST","trans":"6ce836d8-3983-4cfb-8c60-c0b477a1c117","msg":"Request","svc":"auth-module"}
{"time":"2019-06-17T08:26:24.533+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webclientfilters.LoggerWebClientFilter","corr":"6ce836d8-3983-4cfb-8c60-c0b477a1c117","method":"POST","url":"http://httpbin.org/post","trans":"6ce836d8-3983-4cfb-8c60-c0b477a1c117","msg":"Client request","svc":"auth-module"}
{"time":"2019-06-17T08:26:25.501+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webclientfilters.LoggerWebClientFilter","corr":"6ce836d8-3983-4cfb-8c60-c0b477a1c117","trans":"6ce836d8-3983-4cfb-8c60-c0b477a1c117","status":"200","latency":"992","msg":"Client response","svc":"auth-module"}
{"time":"2019-06-17T08:26:25.549+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","corr":"6ce836d8-3983-4cfb-8c60-c0b477a1c117","trans":"6ce836d8-3983-4cfb-8c60-c0b477a1c117","status":"200","latency":"1069","msg":"Response","svc":"auth-module"}
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
{"time":"2019-06-17T08:37:41.443+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","path":"/api/httpbin","corr":"test-corr","address":"/0:0:0:0:0:0:0:1","method":"POST","trans":"df109688-a594-4158-92cd-9c8da575d573","msg":"Request","svc":"auth-module"}
{"time":"2019-06-17T08:37:41.452+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webclientfilters.LoggerWebClientFilter","corr":"test-corr","method":"POST","url":"http://httpbin.org/post","trans":"df109688-a594-4158-92cd-9c8da575d573","msg":"Client request","svc":"auth-module"}
{"time":"2019-06-17T08:37:41.886+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webclientfilters.LoggerWebClientFilter","corr":"test-corr","trans":"df109688-a594-4158-92cd-9c8da575d573","status":"200","latency":"437","msg":"Client response","svc":"auth-module"}
{"time":"2019-06-17T08:37:41.891+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","corr":"test-corr","trans":"df109688-a594-4158-92cd-9c8da575d573","status":"200","latency":"447","msg":"Response","svc":"auth-module"}
```

## Request with invalid remote server

If the remote server is configured incorrectly (e.g. configuration property `almaraz-example.httpbin-url` is `http://httpbinbug.org`), the web client throws a server error.

Request

```sh
curl -v -X POST -H 'Unica-Correlator: test-corr' -H 'Content-Type: application/json' -d '{"name": "jorge", "country": "es"}' http://localhost:8080/api/httpbin
```

Response

```http
HTTP/1.1 500 Internal Server Error
Content-Type: application/json;charset=UTF-8
Unica-Correlator: test-corr
content-length: 24

{"error":"server_error"}
```

Logs

```json
{"time":"2019-06-17T08:56:45.175+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","path":"/api/httpbin","corr":"test-corr","address":"/0:0:0:0:0:0:0:1","method":"POST","trans":"952d209f-fa3f-46e9-b3dd-59c280572015","msg":"Request","svc":"auth-module"}
{"time":"2019-06-17T08:56:45.178+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webclientfilters.LoggerWebClientFilter","corr":"test-corr","method":"POST","url":"http://httpbinbug.org/post","trans":"952d209f-fa3f-46e9-b3dd-59c280572015","msg":"Client request","svc":"auth-module"}
{"time":"2019-06-17T08:56:45.182+00:00","lvl":"ERROR","logger":"com.elevenpaths.almaraz.webfilters.ErrorWebFilter","corr":"test-corr","error":"server_error","trans":"952d209f-fa3f-46e9-b3dd-59c280572015","msg":"Error","exception":"java.net.UnknownHostException: httpbinbug.org\n\tat java.net.InetAddress.getAllByName0(InetAddress.java:1280)\n\t... 112 frames truncated\n","svc":"auth-module"}
{"time":"2019-06-17T08:56:45.182+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","corr":"test-corr","trans":"952d209f-fa3f-46e9-b3dd-59c280572015","status":"500","latency":"7","msg":"Response","svc":"auth-module"}
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
Unica-Correlator: 2c8ccc6a-160e-4208-a881-2f53e8fbc8e6
content-length: 0
```

Logs

```json
{"time":"2019-06-17T09:37:26.349+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","path":"/api/logs/invalidLogId","corr":"2c8ccc6a-160e-4208-a881-2f53e8fbc8e6","address":"/0:0:0:0:0:0:0:1","method":"GET","trans":"2c8ccc6a-160e-4208-a881-2f53e8fbc8e6","msg":"Request","svc":"auth-module"}
{"time":"2019-06-17T09:37:28.206+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.ErrorWebFilter","corr":"2c8ccc6a-160e-4208-a881-2f53e8fbc8e6","trans":"2c8ccc6a-160e-4208-a881-2f53e8fbc8e6","msg":"Error","svc":"auth-module"}
{"time":"2019-06-17T09:37:28.206+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","latency":"1857","status":"400","msg":"Response","svc":"auth-module"}
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
{"time":"2019-06-17T09:55:05.864+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","path":"/api/logs/4","corr":"64beaa3f-0756-475b-bf50-7597eea39078","address":"/0:0:0:0:0:0:0:1","method":"GET","trans":"64beaa3f-0756-475b-bf50-7597eea39078","msg":"Request","svc":"auth-module"}
{"time":"2019-06-17T09:55:05.944+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.example.Controller","op":"getLog","corr":"64beaa3f-0756-475b-bf50-7597eea39078","logId":"4","trans":"64beaa3f-0756-475b-bf50-7597eea39078","msg":"Receiving log request with logId 4","svc":"auth-module"}
{"time":"2019-06-17T09:55:05.945+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.example.Controller","op":"getLog","corr":"64beaa3f-0756-475b-bf50-7597eea39078","trans":"64beaa3f-0756-475b-bf50-7597eea39078","msg":"Processed log request successfully","svc":"auth-module"}
{"time":"2019-06-17T09:55:05.955+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","op":"getLog","corr":"64beaa3f-0756-475b-bf50-7597eea39078","trans":"64beaa3f-0756-475b-bf50-7597eea39078","status":"200","latency":"98","msg":"Response","svc":"auth-module"}
```

The log records for an odd logId:

```json
{"time":"2019-06-17T09:55:44.401+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","path":"/api/logs/5","corr":"a97b5fc0-d8dd-48cd-a3e9-db0a7c9b5f4d","address":"/0:0:0:0:0:0:0:1","method":"GET","trans":"a97b5fc0-d8dd-48cd-a3e9-db0a7c9b5f4d","msg":"Request","svc":"auth-module"}
{"time":"2019-06-17T09:55:44.403+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.example.Controller","op":"getLog","corr":"a97b5fc0-d8dd-48cd-a3e9-db0a7c9b5f4d","logId":"5","trans":"a97b5fc0-d8dd-48cd-a3e9-db0a7c9b5f4d","msg":"Receiving log request with logId 5","svc":"auth-module"}
{"time":"2019-06-17T09:55:44.410+00:00","lvl":"ERROR","logger":"com.elevenpaths.almaraz.example.Controller","op":"getLog","corr":"a97b5fc0-d8dd-48cd-a3e9-db0a7c9b5f4d","trans":"a97b5fc0-d8dd-48cd-a3e9-db0a7c9b5f4d","msg":"Error processing logRequest","exception":"java.lang.RuntimeException: logId cannot be an odd number\n\tat com.elevenpaths.almaraz.example.Controller.lambda$2(Controller.java:97)\n\t... 138 frames truncated\n","svc":"auth-module"}
{"time":"2019-06-17T09:55:44.491+00:00","lvl":"ERROR","logger":"com.elevenpaths.almaraz.webfilters.ErrorWebFilter","op":"getLog","corr":"a97b5fc0-d8dd-48cd-a3e9-db0a7c9b5f4d","error":"server_error","trans":"a97b5fc0-d8dd-48cd-a3e9-db0a7c9b5f4d","msg":"Error","exception":"java.lang.RuntimeException: logId cannot be an odd number\n\tat com.elevenpaths.almaraz.example.Controller.lambda$2(Controller.java:97)\n\t... 138 frames truncated\n","svc":"auth-module"}
{"time":"2019-06-17T09:55:44.491+00:00","lvl":"INFO","logger":"com.elevenpaths.almaraz.webfilters.LoggerWebFilter","op":"getLog","corr":"a97b5fc0-d8dd-48cd-a3e9-db0a7c9b5f4d","trans":"a97b5fc0-d8dd-48cd-a3e9-db0a7c9b5f4d","status":"500","latency":"91","msg":"Response","svc":"auth-module"}
```