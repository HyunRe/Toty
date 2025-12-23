package com.toty.common.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.toty.common.baseException.UnknownTagException;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@Getter
@NoArgsConstructor
public enum Tag {
    // ------------------------ 프로그래밍 언어 ------------------------
    JAVA("Java"),
    JAVASCRIPT("JavaScript"),
    C("C"),
    C_SHARP("C#"),
    C_PLUS_PLUS("C++"),
    RUBY("Ruby"),
    GO("Go"),
    PHP("PHP"),
    SWIFT("Swift"),
    KOTLIN("Kotlin"),
    RUST("Rust"),

    // ------------------------ 기술 스택 ------------------------
    SPRING_BOOT("Spring Boot"),
    SPRING_SECURITY("Spring Security"),
    SPRING_DATA_JPA("Spring Data JPA"),
    SPRING_CLOUD("Spring Cloud"),
    SPRING_BATCH("Spring Batch"),
    SPRING_REST("Spring REST"),
    SPRING_WEB("Spring Web"),
    THYMELEAF("Thymeleaf"),
    JPA("JPA"),
    HIBERNATE("Hibernate"),
    MYSQL("MySQL"),
    POSTGRESQL("PostgreSQL"),
    MONGODB("MongoDB"),
    REDIS("Redis"),
    KAFKA("Kafka"),
    RABBITMQ("RabbitMQ"),
    AWS("AWS"),
    AZURE("Azure"),
    GOOGLE_CLOUD("Google Cloud"),
    DOCKER("Docker"),
    KUBERNETES("Kubernetes"),
    JENKINS("Jenkins"),
    MAVEN("Maven"),
    GRADLE("Gradle"),
    REST_API("REST API"),
    SOAP("SOAP"),
    JWT("JWT"),
    OAUTH2("OAuth2"),
    SWAGGER("Swagger"),
    LOGGING("Logging"),
    UNIT_TESTING("Unit Testing"),
    INTEGRATION_TESTING("Integration Testing");

    private String tag;

    Tag(String tag) {
        this.tag = tag;
    }

    @JsonCreator
    public static Tag fromString(String tag) {
        return Arrays.stream(Tag.values())
                .filter(t -> t.tag.equalsIgnoreCase(tag))
                .findFirst()
                .orElseThrow(() -> new UnknownTagException(tag));
    }
}