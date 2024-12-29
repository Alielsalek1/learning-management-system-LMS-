plugins {
	java
    id("org.springframework.boot") version "3.4.0"
	id("io.spring.dependency-management") version "1.1.6"
}

group = "com.main"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.spring.io/release")
    }
    maven {
        url = uri("https://repo.spring.io/milestone")
    }
    maven { 
        url = uri("https://mvnrepository.com/artifact/net.kemitix/sqlite-dialect")
    }
}

dependencies {
    implementation("org.jfree:jfreechart:1.5.3") // For generating charts and graphs
    implementation("org.mockito:mockito-core:4.8.0")
    implementation("org.mockito:mockito-junit-jupiter:4.8.0")
    implementation("org.mockito:mockito-inline:4.8.0")
    implementation("org.powermock:powermock-api-mockito2:2.0.9")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa") // For JPA support
    implementation("org.springframework.boot:spring-boot-starter-mail") // For email support
    implementation("org.springframework.boot:spring-boot-starter-security") // For security/authentication
    implementation("org.springframework.boot:spring-boot-starter-validation") // For validation support
    implementation("org.springframework.boot:spring-boot-starter-web") // For building web services (REST APIs)
    // implementation("org.xerial:sqlite-jdbc:3.39.2.0") // For SQLite database support
    implementation("mysql:mysql-connector-java:8.0.33") // For MySQL database support
    implementation("org.apache.poi:poi-ooxml:5.2.3") // For Excel file generation (POI OOXML)
    implementation("org.apache.poi:poi:5.2.3") // For legacy POI support (if needed)
    compileOnly("org.projectlombok:lombok") // For Lombok (reducing boilerplate code)
    developmentOnly("org.springframework.boot:spring-boot-devtools") // For development tools (automatic restart, etc.)
    annotationProcessor("org.projectlombok:lombok") // Annotation processing for Lombok
    testImplementation("org.springframework.boot:spring-boot-starter-test") // For Spring Boot tests
    testImplementation("org.springframework.security:spring-security-test") // For security-related tests
    testRuntimeOnly("org.junit.platform:junit-platform-launcher") // For test execution
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation ("org.powermock:powermock-module-junit4:2.0.9")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation ("org.powermock:powermock-api-mockito2:2.0.9")
}


tasks.withType<Test> {
	useJUnitPlatform()
}
