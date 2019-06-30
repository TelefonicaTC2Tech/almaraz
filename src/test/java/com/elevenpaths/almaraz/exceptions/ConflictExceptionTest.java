/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.exceptions;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;

/**
 * Unit tests for {@link ConflictException}.
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
public class ConflictExceptionTest {

	@Test
	public void newConflictException() {
		ConflictException e = new ConflictException();
		Assert.assertEquals(HttpStatus.CONFLICT, e.getStatus());
		Assert.assertEquals(ErrorCodes.CONFLICT, e.getError());
		Assert.assertNull(e.getReason());
		Assert.assertNull(e.getCause());
		Assert.assertNull(e.getHeaders());
		Assert.assertNull(e.getDetailMap());
	}
}
