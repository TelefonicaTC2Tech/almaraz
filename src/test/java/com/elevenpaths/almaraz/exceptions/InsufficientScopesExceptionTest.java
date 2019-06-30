/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.exceptions;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

/**
 * Unit tests for {@link InsufficientScopesException}.
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
public class InsufficientScopesExceptionTest {

	@Test
	public void newInsufficientScopesException() {
		InsufficientScopesException e = new InsufficientScopesException();
		Assert.assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
		Assert.assertEquals(ErrorCodes.UNAUTHORIZED_CLIENT, e.getError());
		Assert.assertEquals("invalid token: insufficient scopes", e.getReason());
		Assert.assertNull(e.getCause());
		Assert.assertEquals(InsufficientScopesException.WWW_AUTHENTICATE_VALUE,
				e.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE));
		Assert.assertNull(e.getDetailMap());
	}

}
