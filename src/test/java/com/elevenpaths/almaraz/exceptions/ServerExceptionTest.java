/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.exceptions;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;

/**
 * Unit tests for {@link ServerException}.
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
public class ServerExceptionTest {

	@Test
	public void newServerExceptionWithReason() {
		ServerException e = new ServerException("reason");
		Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
		Assert.assertEquals(ErrorCodes.SERVER_ERROR, e.getError());
		Assert.assertEquals("reason", e.getReason());
		Assert.assertNull(e.getCause());
		Assert.assertNull(e.getHeaders());
		Assert.assertNull(e.getDetailMap());
	}

	@Test
	public void newServerExceptionWithThrowable() {
		Throwable t = new Exception("test exception");
		ServerException e = new ServerException(t);
		Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
		Assert.assertEquals(ErrorCodes.SERVER_ERROR, e.getError());
		Assert.assertNull(e.getReason());
		Assert.assertEquals(t, e.getCause());
		Assert.assertNull(e.getHeaders());
		Assert.assertNull(e.getDetailMap());
	}

	@Test
	public void newServerExceptionWithReasonAndThrowable() {
		Throwable t = new Exception("test exception");
		ServerException e = new ServerException("reason", t);
		Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
		Assert.assertEquals(ErrorCodes.SERVER_ERROR, e.getError());
		Assert.assertEquals("reason", e.getReason());
		Assert.assertEquals(t, e.getCause());
		Assert.assertNull(e.getHeaders());
		Assert.assertNull(e.getDetailMap());
	}

}
