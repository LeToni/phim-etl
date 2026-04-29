package com.trung.phimetl;

import org.springframework.boot.SpringApplication;

public class TestPhimEtlApplication {

	public static void main(String[] args) {
		SpringApplication.from(PhimEtlApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
