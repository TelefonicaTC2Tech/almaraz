/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Configuration;

import com.elevenpaths.almaraz.AlmarazConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Spring {@link Configuration} extending {@link AlmarazConfiguration} to include the default middlewares
 * in the server.
 *
 * @author Jorge Lorenzo <jorge.lorenzogallardo@telefonica.com>
 *
 */
@Configuration
public class ApplicationConfiguration extends AlmarazConfiguration {

	public ApplicationConfiguration(
			@Value("${almaraz-example.base-path}") String basePath,
			ObjectMapper objectMapper,
			BuildProperties buildProperties) {
		super(basePath, objectMapper, buildProperties);
	}

}
