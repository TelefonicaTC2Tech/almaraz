/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.exceptions;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

/**
 * Unit tests for {@link InvalidTokenException}.
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
public class InvalidTokenExceptionTest {

	@Test
	public void newInvalidTokenException() {
		InvalidTokenException e = new InvalidTokenException();
		Assert.assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
		Assert.assertEquals(ErrorCodes.UNAUTHORIZED_CLIENT, e.getError());
		Assert.assertEquals("invalid token: invalid access token", e.getReason());
		Assert.assertNull(e.getCause());
		Assert.assertEquals(InvalidTokenException.WWW_AUTHENTICATE_VALUE,
				e.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE));
		Assert.assertNull(e.getDetailMap());
	}

}
