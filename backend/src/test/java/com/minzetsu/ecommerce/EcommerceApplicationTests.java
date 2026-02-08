package com.minzetsu.ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.SecurityFilterChain;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EcommerceApplicationTests {

	@Autowired
	private EcommerceApplication application;

	@Autowired
	private SecurityFilterChain securityFilterChain;

	@Test
	void contextLoads() {
		assertThat(application).isNotNull();
		assertThat(securityFilterChain).isNotNull();
	}

}
