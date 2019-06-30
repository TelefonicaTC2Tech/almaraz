/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.exceptions;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

/**
 * Unit tests for {@link ExpiredTokenException}.
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
public class ExpiredTokenExceptionTest {

	@Test
	public void newExpiredTokenException() {
		ExpiredTokenException e = new ExpiredTokenException();
		Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
		Assert.assertEquals(ErrorCodes.UNAUTHORIZED_CLIENT, e.getError());
		Assert.assertEquals("invalid token: expired token", e.getReason());
		Assert.assertNull(e.getCause());
		Assert.assertEquals(ExpiredTokenException.WWW_AUTHENTICATE_VALUE,
				e.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE));
		Assert.assertNull(e.getDetailMap());
	}

}
