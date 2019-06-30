/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.exceptions;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;

/**
 * Unit tests for {@link ForbiddenException}.
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
public class ForbiddenExceptionTest {

	@Test
	public void newForbiddenException() {
		ForbiddenException e = new ForbiddenException();
		Assert.assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
		Assert.assertEquals(ErrorCodes.FORBIDDEN, e.getError());
		Assert.assertNull(e.getReason());
		Assert.assertNull(e.getCause());
		Assert.assertNull(e.getHeaders());
		Assert.assertNull(e.getDetailMap());
	}
}
