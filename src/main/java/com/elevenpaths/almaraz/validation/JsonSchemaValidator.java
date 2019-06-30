// Copyright (c) Telefonica I+D. All rights reserved.

package com.elevenpaths.almaraz.validation;

import java.util.Set;

import com.elevenpaths.almaraz.exceptions.InvalidRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;

/**
 * JSON schema validator.
 *
 * It uses {@link JsonSchemaRepository} to retrieve a JSON schema by its name,
 * and it validates a Jackson {@link JsonNode} using {@link JsonSchema}.
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
public class JsonSchemaValidator {

	private final JsonSchemaRepository repository;

	public JsonSchemaValidator(JsonSchemaRepository repository) {
		this.repository = repository;
	}

	public void validate(String schemaName, JsonNode node) {
		if (schemaName == null || schemaName.isEmpty()) {
			return;
		}
		JsonSchema jsonSchema = repository.getJsonSchema(schemaName);
		Set<ValidationMessage> errors = jsonSchema.validate(node);
		if (errors != null && !errors.isEmpty()) {
			// Only process the first error
			ValidationMessage validationMessage = errors.iterator().next();
			throw new InvalidRequestException(validationMessage.toString());
		}
	}

}
