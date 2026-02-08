package com.minzetsu.ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EcommerceApplicationTests {

	@Autowired
	private EcommerceApplication application;

	@Test
	void contextLoads() {
		assertThat(application).isNotNull();
	}

}
