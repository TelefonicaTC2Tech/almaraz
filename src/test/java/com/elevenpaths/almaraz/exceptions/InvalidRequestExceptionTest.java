/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.exceptions;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;

/**
 * Unit tests for {@link InvalidRequestException}.
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
public class InvalidRequestExceptionTest {

	@Test
	public void newInvalidRequestException() {
		InvalidRequestException e = new InvalidRequestException("reason");
		Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
		Assert.assertEquals(ErrorCodes.INVALID_REQUEST, e.getError());
		Assert.assertEquals("reason", e.getReason());
		Assert.assertNull(e.getCause());
		Assert.assertNull(e.getHeaders());
		Assert.assertNull(e.getDetailMap());
	}
}
