/*
 * Copyright (c) Telefonica I+D. All rights reserved.
 */

package com.elevenpaths.almaraz.example.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {

	private String id;
	private String name;
	private String country;
	private Integer addressCode;

}
