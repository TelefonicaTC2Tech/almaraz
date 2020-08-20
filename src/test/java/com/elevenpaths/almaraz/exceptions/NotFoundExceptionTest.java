/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.exceptions;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;

/**
 * Unit tests for {@link NotFoundException}.
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
public class NotFoundExceptionTest {

	@Test
	public void newNotFoundException() {
		NotFoundException e = new NotFoundException();
		Assert.assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
		Assert.assertNull(e.getError());
		Assert.assertNull(e.getReason());
		Assert.assertNull(e.getCause());
		Assert.assertNull(e.getHeaders());
		Assert.assertNull(e.getDetailMap());
	}

	@Test
	public void newNotFoundExceptionWithReason() {
		NotFoundException e = new NotFoundException("reason");
		Assert.assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
		Assert.assertEquals(ErrorCodes.NOT_FOUND, e.getError());
		Assert.assertEquals("reason", e.getReason());
		Assert.assertNull(e.getCause());
		Assert.assertNull(e.getHeaders());
		Assert.assertNull(e.getDetailMap());
	}

	@Test
	public void newNotFoundExceptionWithReasonAndException() {
		Throwable t = new Exception("test");
		NotFoundException e = new NotFoundException("reason", t);
		Assert.assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
		Assert.assertEquals(ErrorCodes.NOT_FOUND, e.getError());
		Assert.assertEquals("reason", e.getReason());
		Assert.assertEquals(t, e.getCause());
	}
}
