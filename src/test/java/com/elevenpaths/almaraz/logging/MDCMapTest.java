/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */
package com.elevenpaths.almaraz.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.MDC;

/**
 * Unit tests for {@link MDCMap}.
 *
 * @author Juan Hernando <juanantonio.hernandolabajo@telefonica.com>
 *
 */
public class MDCMapTest {
	
	@Test
	public void putAllWhenMapIsFilled() {
		Map<String, Object> map = new HashMap<>();
		map.put("key1", "string");
		map.put("key2", 1);
		map.put("key3", 1L);
		map.put("key4", true);
		map.put("key5", null);
		MDCMap.putAll(map);
		Map<String, String> context = MDC.getCopyOfContextMap();
		
		assertEquals("string", context.get("key1"));
		assertEquals("1", context.get("key2"));
		assertEquals("1", context.get("key3"));
		assertEquals("true", context.get("key4"));
		assertNull(context.get("key5"));
	} 

}
