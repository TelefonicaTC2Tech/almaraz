/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.exceptions;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Unit tests for {@link ResponseException}.
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
public class ResponseExceptionTest {

	@Test
	public void newResponseExceptionWithStatus() {
		ResponseException e = new ResponseException(HttpStatus.I_AM_A_TEAPOT);
		Assert.assertEquals(HttpStatus.I_AM_A_TEAPOT, e.getStatus());
		Assert.assertNull(e.getError());
		Assert.assertNull(e.getReason());
		Assert.assertNull(e.getCause());
		Assert.assertNull(e.getHeaders());
		Assert.assertNull(e.getDetailMap());
	}

	@Test
	public void hashcodeSimple() {
		Assert.assertEquals(
				new ResponseException(HttpStatus.I_AM_A_TEAPOT).hashCode(),
				new ResponseException(HttpStatus.I_AM_A_TEAPOT).hashCode());
	}

	@Test
	public void hashcodeComplex() {
		Throwable t = new Exception("test exception");
		ResponseException e1 = new ResponseException(HttpStatus.I_AM_A_TEAPOT, "error", "reason", t);
		ResponseException e2 = new ResponseException(HttpStatus.I_AM_A_TEAPOT, "error", "reason", t);
		Assert.assertEquals(e1, e2);
		Assert.assertEquals(e1.hashCode(), e2.hashCode());

		e1.addHeader("header1", "header1 value");
		e1.addDetail("detail1", "detail1 value");
		e2.addHeader("header1", "header1 value");
		e2.addDetail("detail1", "detail1 value");
		Assert.assertEquals(e1, e2);
		Assert.assertEquals(e1.hashCode(), e2.hashCode());
	}

	@Test
	public void string() {
		ResponseException e = new ResponseException(HttpStatus.I_AM_A_TEAPOT, "error", "reason");
		Assert.assertEquals(
				"ResponseException(status=418 I_AM_A_TEAPOT, error=error, reason=reason, detailMap=null, headers=null)",
				e.toString());
	}

	@Test
	public void newResponseExceptionWithReason() {
		ResponseException e = new ResponseException(HttpStatus.I_AM_A_TEAPOT, "error", "reason");
		Assert.assertEquals(HttpStatus.I_AM_A_TEAPOT, e.getStatus());
		Assert.assertEquals("error", e.getError());
		Assert.assertEquals("reason", e.getReason());
		Assert.assertNull(e.getCause());
		Assert.assertNull(e.getHeaders());
		Assert.assertNull(e.getDetailMap());
	}

	@Test
	public void newResponseExceptionWithThrowable() {
		Throwable t = new Exception("test exception");
		ResponseException e = new ResponseException(HttpStatus.I_AM_A_TEAPOT, "error", "reason", t);
		Assert.assertEquals(HttpStatus.I_AM_A_TEAPOT, e.getStatus());
		Assert.assertEquals("error", e.getError());
		Assert.assertEquals("reason", e.getReason());
		Assert.assertEquals(t, e.getCause());
		Assert.assertNull(e.getHeaders());
		Assert.assertNull(e.getDetailMap());
	}

	@Test
	public void addHeader() {
		ResponseException actualException = new ResponseException(HttpStatus.I_AM_A_TEAPOT);
		actualException.addHeader("header1", "header1 value");
		actualException.addHeader("header2", "header2 value");

		MultiValueMap<String, String> expectedHeaders = new LinkedMultiValueMap<>();
		expectedHeaders.add("header1", "header1 value");
		expectedHeaders.add("header2", "header2 value");

		Assert.assertEquals(expectedHeaders, actualException.getHeaders());

		ResponseException expectedException = new ResponseException(HttpStatus.I_AM_A_TEAPOT);
		expectedException.setHeaders(expectedHeaders);
		Assert.assertEquals(expectedException, actualException);
	}

	@Test
	public void addDetail() {
		ResponseException actualException = new ResponseException(HttpStatus.I_AM_A_TEAPOT);
		actualException.addDetail("detail1", "detail1 value");
		actualException.addDetail("detail2", 4L);

		Map<String, Object> expectedDetails = new HashMap<>();
		expectedDetails.put("detail1", "detail1 value");
		expectedDetails.put("detail2", 4L);

		Assert.assertEquals(expectedDetails, actualException.getDetailMap());

		ResponseException expectedException = new ResponseException(HttpStatus.I_AM_A_TEAPOT);
		expectedException.setDetailMap(expectedDetails);
		Assert.assertEquals(expectedException, actualException);
	}

}
