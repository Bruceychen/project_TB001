package com.bruceychen.tb001;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ProjectTb001ApplicationTests {

	@Test
	void contextLoads() {
	}

}
