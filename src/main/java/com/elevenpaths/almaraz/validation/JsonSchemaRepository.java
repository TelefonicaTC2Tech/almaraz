// Copyright (c) Telefonica I+D. All rights reserved.

package com.elevenpaths.almaraz.validation;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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

	/**
	 * Cache of {@link JsonSchema}.
	 */
	private final Map<String, JsonSchema> schemas;

	/**
	 * Constructor.
	 */
	public JsonSchemaRepository() {
		schemas= new HashMap<>();
	}

	/**
	 * Get the path to the JSON schema.
	 *
	 * @param schemaName
	 * @return path to the JSON schema files
	 */
	protected String getSchemaPath(String schemaName) {
		return String.format("/schemas/%s.json", schemaName);
	}

	/**
	 * Load the schema by reading the file at /schemas/{schemaName}.json in the classpath
	 * and parsing it into a {@link JsonSchema} instance.
	 *
	 * @param schemaName
	 * @return {@link JsonSchema}
	 */
	protected JsonSchema loadSchema(String schemaName) {
		String schemaPath = getSchemaPath(schemaName);
		try (InputStream is = getClass().getResourceAsStream(schemaPath)) {
			if (is == null) {
				throw new JsonSchemaRepositoryException("Schema not found: " + schemaPath);
			}
			return JsonSchemaFactory.getInstance().getSchema(is);
		} catch (Exception e) {
			throw new JsonSchemaRepositoryException("Invalid schema: " + schemaPath, e);
		}
	}

	/**
	 * Get a {@link JsonSchema} from the repository. The schemas are cached in memory
	 * to avoid parsing them multiple times.
	 *
	 * @param schemaName
	 * @return {@link JsonSchema}
	 */
	public JsonSchema getJsonSchema(String schemaName) {
		if (schemas.containsKey(schemaName)) {
			return schemas.get(schemaName);
		}
		JsonSchema schema = loadSchema(schemaName);
		schemas.put(schemaName, schema);
		return schema;
	}

	/**
	 * {@link RuntimeException} when the JSON schema cannot be read or parsed.
	 *
	 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
	 *
	 */
	public static class JsonSchemaRepositoryException extends RuntimeException {

		private static final long serialVersionUID = 3619308327861518187L;

		/**
		 * Constructor.
		 *
		 * @param message
		 */
		public JsonSchemaRepositoryException(String message) {
			super(message);
		}

		/**
		 * Constructor with exception.
		 *
		 * @param message
		 * @param t
		 */
		public JsonSchemaRepositoryException(String message, Throwable t) {
			super(message, t);
		}

	}
}
