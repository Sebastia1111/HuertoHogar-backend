package com.huertohogar.usuarios;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "eureka.client.enabled=false")
@ActiveProfiles("h2")
class UsuariosApplicationTests {

	@Test
	void contextLoads() {
	}

}
