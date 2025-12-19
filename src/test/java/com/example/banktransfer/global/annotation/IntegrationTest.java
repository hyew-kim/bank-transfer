package com.example.banktransfer.global.annotation;

import com.example.banktransfer.global.config.TestcontainerConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@ActiveProfiles(profiles = "integration")
@Import(TestcontainerConfiguration.class)
public @interface IntegrationTest {

}
