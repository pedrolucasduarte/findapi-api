package com.findapi.api;

import org.springframework.boot.SpringApplication;

public class TestFindapiApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(FindapiApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
