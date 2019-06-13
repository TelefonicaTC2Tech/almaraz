/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */
package com.elevenpaths.almaraz.validation;


import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.elevenpaths.almaraz.exceptions.InvalidRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Unit tests for {@link JsonSchemaValidator}.
 *
 * @author Juan Hernando <juanantonio.hernandolabajo@telefonica.com>
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class JsonSchemaValidatorTest {
	
	@Spy
	private JsonSchemaRepository jsonSchemaRepository = new JsonSchemaRepository();
	
	@Test
	public void validateWhenSchemaNull() {
		JsonSchemaValidator jsonSchemaValidator = new JsonSchemaValidator(jsonSchemaRepository);
		jsonSchemaValidator.validate(null, null);
		Mockito.verify(jsonSchemaRepository, Mockito.times(0)).getJsonSchema(null);
	}
	
	@Test
	public void validateWhenSchemaEmpty() {
		JsonSchemaValidator jsonSchemaValidator = new JsonSchemaValidator(jsonSchemaRepository);
		jsonSchemaValidator.validate("", null);
		Mockito.verify(jsonSchemaRepository, Mockito.times(0)).getJsonSchema(null);
	}
	
	@Test
	public void validateRightJsonAndSchema() {
		String personJson =
		        "{ \"firstName\" : \"John\", \"lastName\" : \"Doe\", \"age\": 30 }";

		ObjectMapper objectMapper = new ObjectMapper();
		try {
			JsonNode jsonNode = objectMapper.readValue(personJson, JsonNode.class);
			JsonSchemaValidator jsonSchemaValidator = new JsonSchemaValidator(jsonSchemaRepository);
			jsonSchemaValidator.validate("schema", jsonNode);
			Mockito.verify(jsonSchemaRepository, Mockito.times(1)).getJsonSchema("schema");
		} catch (IOException e) {
			Assert.assertTrue(false);
		}
	}
	
	@Test
	public void validateBadJsonAndSchema() {
		String personJson =
		        "{ \"firstName\" : \"John\", \"lastName\" : \"Doe\" }";

		ObjectMapper objectMapper = new ObjectMapper();
		try {
			JsonNode jsonNode = objectMapper.readValue(personJson, JsonNode.class);
			JsonSchemaValidator jsonSchemaValidator = new JsonSchemaValidator(jsonSchemaRepository);
			jsonSchemaValidator.validate("schema", jsonNode);
		} catch (IOException e) {
			Assert.assertTrue(false);
		} catch (InvalidRequestException e) {
			Mockito.verify(jsonSchemaRepository, Mockito.times(1)).getJsonSchema("schema");
			Assert.assertEquals("$.age: is missing but it is required", e.getMessage());
		}
	}

}
