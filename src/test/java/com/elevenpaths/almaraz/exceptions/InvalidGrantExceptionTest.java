/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.exceptions;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;

/**
 * Unit tests for {@link InvalidGrantException}.
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
public class InvalidGrantExceptionTest {

	@Test
	public void newInvalidGrantException() {
		InvalidGrantException e = new InvalidGrantException("reason");
		Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
		Assert.assertEquals(ErrorCodes.INVALID_GRANT, e.getError());
		Assert.assertEquals("reason", e.getReason());
		Assert.assertNull(e.getCause());
		Assert.assertNull(e.getHeaders());
		Assert.assertNull(e.getDetailMap());
	}
}
