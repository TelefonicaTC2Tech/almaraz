/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.exceptions;

/**
 * Error codes constants for {@link ResponseException}.
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
public final class ErrorCodes {

	public static final String SERVER_ERROR = "server_error";

	public static final String INVALID_CLIENT = "invalid_client";

	public static final String INVALID_GRANT = "invalid_grant";

	public static final String INVALID_REQUEST = "invalid_request";

	public static final String FORBIDDEN = "forbidden";

	public static final String INVALID_SCOPE = "invalid_scope";

	public static final String UNAUTHORIZED_CLIENT = "unauthorized_client";

	public static final String UNSUPPORTED_GRANT_TYPE = "unsupported_grant_type";

	public static final String CONFLICT = "conflict";

	private ErrorCodes() {
	}

}
