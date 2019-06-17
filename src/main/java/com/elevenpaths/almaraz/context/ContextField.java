/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.context;

/**
 * Predefined context fields.
 * Note that context fields can be customized.
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
public class ContextField {

	/**
	 * Field name for the transaction id.
	 */
	public static final String TRANSACTION_ID = "trans";

	/**
	 * Field name for the correlator.
	 */
	public static final String CORRELATOR = "corr";

	/**
	 * Field name for the operation.
	 */
	public static final String OPERATION = "op";

	/**
	 * Field name for the service.
	 */
	public static final String SERVICE = "svc";

	/**
	 * Field name for the component.
	 */
	public static final String COMPONENT = "comp";

	/**
	 * Field name for the user.
	 */
	public static final String USER = "user";

	/**
	 * Field name for the realm.
	 */
	public static final String REALM = "realm";

	/**
	 * Field name for the error code.
	 */
	public static final String ERROR = "error";

	/**
	 * Field name for the reason (error description).
	 */
	public static final String REASON = "reason";

	/**
	 * Field name for the alarm.
	 */
	public static final String ALARM = "alarm";

	/**
	 * Field name with the HTTP method of the request.
	 */
	public static final String METHOD = "method";

	/**
	 * Field name with the HTTP path of the request.
	 */
	public static final String PATH = "path";

	/**
	 * Request URL.
	 * Unlike {@link #PATH}, it includes the whole URL. This is useful for web clients.
	 */
	public static final String URL = "url";

	/**
	 * Field name with the client address of the request.
	 */
	public static final String ADDRESS = "address";

	/**
	 * Field name with the HTTP status of the response.
	 */
	public static final String STATUS = "status";

	/**
	 * Field name with the latency (in milliseconds) to complete the response.
	 */
	public static final String LATENCY = "latency";

}
