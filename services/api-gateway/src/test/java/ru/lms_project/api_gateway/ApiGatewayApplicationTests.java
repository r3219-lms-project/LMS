package ru.lms_project.api_gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"auth.tokens.secret=mysuperlongsecretkey_for_dev_1234567890",
		"auth.tokens.issuer=http://localhost:8084",
		"auth.tokens.audience=lms-api"
})
class ApiGatewayApplicationTests {

	@Test
	void contextLoads() {
		// Context loads successfully
	}
}