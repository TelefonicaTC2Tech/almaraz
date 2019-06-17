/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */
package com.elevenpaths.almaraz.validation;


import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.elevenpaths.almaraz.validation.JsonSchemaRepository.JsonSchemaRepositoryException;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;


/**
 * Unit tests for {@link JsonSchemaRepository}.
 *
 * @author Juan Hernando <juanantonio.hernandolabajo@telefonica.com>
 *
 */
public class JsonSchemaRepositoryTest {

	private static JsonSchemaRepository jsonSchemaRepository;

	@BeforeClass
	public static void init() {
		jsonSchemaRepository = new JsonSchemaRepository();
	}

	@Test
	public void getJsonSchemaWithRightFile() {
		JsonSchema result = jsonSchemaRepository.getJsonSchema("schema");
		Assert.assertNotNull(result);
		JsonNode schemaNode = result.getSchemaNode();
		Assert.assertNotNull(schemaNode);
		JsonNode firstNameNode = schemaNode.get("properties").get("firstName");
		JsonNode lastNameNode = schemaNode.get("properties").get("lastName");
		JsonNode ageNode = schemaNode.get("properties").get("age");
		Assert.assertNotNull(firstNameNode);
		Assert.assertEquals("string", firstNameNode.get("type").asText());
		Assert.assertNotNull(lastNameNode);
		Assert.assertEquals("string", lastNameNode.get("type").asText());
		Assert.assertNotNull(ageNode);
		Assert.assertEquals("integer", ageNode.get("type").asText());
		Assert.assertEquals(0, ageNode.get("minimum").asInt());
	}

	@Test
	public void getJsonSchemaUseInternalHashMap() {
		JsonSchema result = jsonSchemaRepository.getJsonSchema("schema");
		Assert.assertNotNull(result);
		JsonNode schemaNode = result.getSchemaNode();
		Assert.assertNotNull(schemaNode);
		JsonNode firstNameNode = schemaNode.get("properties").get("firstName");
		JsonNode lastNameNode = schemaNode.get("properties").get("lastName");
		JsonNode ageNode = schemaNode.get("properties").get("age");
		Assert.assertNotNull(firstNameNode);
		Assert.assertEquals("string", firstNameNode.get("type").asText());
		Assert.assertNotNull(lastNameNode);
		Assert.assertEquals("string", lastNameNode.get("type").asText());
		Assert.assertNotNull(ageNode);
		Assert.assertEquals("integer", ageNode.get("type").asText());
		Assert.assertEquals(0, ageNode.get("minimum").asInt());
	}

	@Test(expected = JsonSchemaRepositoryException.class)
	public void getJsonSchemaServerExceptionUnknownSchema() {
		jsonSchemaRepository.getJsonSchema(null);
		Assert.assertTrue(false);
	}

}
