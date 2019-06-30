/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.exceptions;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;

/**
 * Unit tests for {@link InvalidClientException}.
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
public class InvalidClientExceptionTest {

	@Test
	public void newConflictException() {
		InvalidClientException e = new InvalidClientException("reason");
		Assert.assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
		Assert.assertEquals(ErrorCodes.INVALID_CLIENT, e.getError());
		Assert.assertEquals("reason", e.getReason());
		Assert.assertNull(e.getCause());
		Assert.assertNull(e.getHeaders());
		Assert.assertNull(e.getDetailMap());
	}
}
