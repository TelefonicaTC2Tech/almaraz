// Copyright (c) Telefonica I+D. All rights reserved.

package com.elevenpaths.almaraz.validation;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.elevenpaths.almaraz.exceptions.ServerException;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;

/**
 * Repository of JSON schemas in the directory /schemas of the classpath.
 * Note that schemas must have the file extension ".json", but this extension is
 * removed in the schema name.
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
public class JsonSchemaRepository {
	private Map<String, JsonSchema> schemas;

	public JsonSchemaRepository() {
		schemas= new HashMap<>();
	}

	protected String getSchemaPath(String schemaName) {
		return String.format("/schemas/%s.json", schemaName);
	}

	protected JsonSchema loadSchema(String schemaName) {
		String schemaPath = getSchemaPath(schemaName);
		try (InputStream is = getClass().getResourceAsStream(schemaPath)) {
			return JsonSchemaFactory.getInstance().getSchema(is);
		} catch (Exception e) {
			throw new ServerException(e);
		}
	}

	public JsonSchema getJsonSchema(String schemaName) {
		if (schemas.containsKey(schemaName)) {
			return schemas.get(schemaName);
		}
		JsonSchema schema = loadSchema(schemaName);
		schemas.put(schemaName, schema);
		return schema;
	}
}
