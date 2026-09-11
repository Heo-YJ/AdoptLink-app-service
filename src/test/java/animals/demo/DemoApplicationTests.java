package animals.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:adoptlink-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=adoptlink-test-only-secret-key-at-least-32-bytes",
        "coolsms.api-key=test-key",
        "coolsms.api-secret=test-secret",
        "coolsms.sender-phone=01000000000"
})
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}

}
