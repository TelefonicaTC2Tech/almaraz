/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.exceptions;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;

/**
 * Unit tests for {@link UnsupportedMediaTypeException}.
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
public class UnsupportedGrantTypeExceptionTest {

	@Test
	public void newUnsupportedGrantTypeException() {
		UnsupportedGrantTypeException e = new UnsupportedGrantTypeException("reason");
		Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
		Assert.assertEquals(ErrorCodes.UNSUPPORTED_GRANT_TYPE, e.getError());
		Assert.assertEquals("reason", e.getReason());
		Assert.assertNull(e.getCause());
		Assert.assertNull(e.getHeaders());
		Assert.assertNull(e.getDetailMap());
	}
}
