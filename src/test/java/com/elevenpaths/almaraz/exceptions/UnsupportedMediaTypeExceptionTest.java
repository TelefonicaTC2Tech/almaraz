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
public class UnsupportedMediaTypeExceptionTest {

	@Test
	public void newUnsupportedMediaTypeException() {
		UnsupportedMediaTypeException e = new UnsupportedMediaTypeException();
		Assert.assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, e.getStatus());
		Assert.assertNull(e.getError());
		Assert.assertNull(e.getReason());
		Assert.assertNull(e.getCause());
		Assert.assertNull(e.getHeaders());
		Assert.assertNull(e.getDetailMap());
	}
}
