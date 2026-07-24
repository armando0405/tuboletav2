package com.tuboleta.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Se desactiva el scheduler ({@code scheduler.enabled=false}) para este
 * contexto: el dispatcher es {@code @ConditionalOnProperty} y no debe correr
 * ticks reales durante la suite de tests (Task 6).
 */
@SpringBootTest
@TestPropertySource(properties = "scheduler.enabled=false")
class MainTests {

	@Test
	void contextLoads() {
	}

}
