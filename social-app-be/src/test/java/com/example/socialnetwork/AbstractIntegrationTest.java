package com.example.socialnetwork;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MSSQLServerContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // 1. Tạo một class tĩnh bên trong để giữ container
    static class MsSqlContainer extends MSSQLServerContainer<MsSqlContainer> {
        private static final String IMAGE_VERSION = "mcr.microsoft.com/mssql/server:2022-latest";
        private static MsSqlContainer container;

        private MsSqlContainer() {
            super(IMAGE_VERSION);
        }

        public static MsSqlContainer getInstance() {
            if (container == null) {
                container = new MsSqlContainer().acceptLicense();
            }
            return container;
        }

        @Override
        public void start() {
            super.start();
        }

        @Override
        public void stop() {
            // Không làm gì cả để container không bị dừng giữa các class test
        }
    }

    // 2. Lấy thể hiện singleton của container
    static {
        MsSqlContainer.getInstance().start();
    }

    // 3. Cấu hình các thuộc tính động
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> MsSqlContainer.getInstance().getJdbcUrl());
        registry.add("spring.datasource.username", () -> MsSqlContainer.getInstance().getUsername());
        registry.add("spring.datasource.password", () -> MsSqlContainer.getInstance().getPassword());
    }
}