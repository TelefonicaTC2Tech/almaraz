/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Resource not found (404 response).
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
public class NotFoundException extends ResponseException {

	private static final long serialVersionUID = 4758686274252544211L;

	/**
	 * Default constructor.
	 */
	public NotFoundException() {
		super(HttpStatus.NOT_FOUND, null, null);
	}

	/**
	 * Constructor with error reason.
	 *
	 * @param reason reason
	 */
	public NotFoundException(String reason) {
		super(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND, reason);
	}

	/**
	 * Constructor with error reason and exception.
	 *
	 * @param reason reason
	 * @param t exception
	 */
	public NotFoundException(String reason, Throwable t) {
		super(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND, reason, t);
	}

}
