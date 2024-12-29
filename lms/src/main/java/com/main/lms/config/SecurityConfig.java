// This is for disabling the login page for spring boot security so we can test
package com.main.lms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;

import com.main.lms.enums.UserRole;
import com.main.lms.services.UserService;
import org.springframework.web.filter.RequestContextFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final UserService userService;

    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, RequestContextFilter requestContextFilter) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Auth endpoints

                        .requestMatchers("/auth/login").permitAll()

                        .requestMatchers("/auth/register").permitAll()
                        // User endpoints
                        .requestMatchers("/users/me").authenticated()

                        .requestMatchers("/users/**")
                        .hasAuthority("ROLE_" + UserRole.ADMIN.name())

                        // Quiz endpoints
                        .requestMatchers("/quizzes/{quizId}")
                        .hasAnyAuthority("ROLE_" + UserRole.STUDENT.name(),
                                "ROLE_" + UserRole.INSTRUCTOR.name(), "ROLE_" + UserRole.ADMIN.name())

                        .requestMatchers("/quizzes/{quizId}/submit")
                        .hasAnyAuthority("ROLE_" + UserRole.STUDENT.name(), "ROLE_" + UserRole.ADMIN.name())

                        .requestMatchers("/quizzes")
                        .hasAnyAuthority("ROLE_" + UserRole.INSTRUCTOR.name(), "ROLE_" + UserRole.ADMIN.name())

                        .requestMatchers("/quizzes/courses/{courseId}").authenticated()

                        .requestMatchers("/quizzes/{quizId}/grades")
                        .hasAnyAuthority("ROLE_" + UserRole.INSTRUCTOR.name(), "ROLE_" + UserRole.ADMIN.name())

                        // Question endpoints
                        .requestMatchers("/questions/**")
                        .hasAnyAuthority("ROLE_" + UserRole.INSTRUCTOR.name(), "ROLE_" + UserRole.ADMIN.name())

                        // Assignment endpoints
                        .requestMatchers(HttpMethod.GET, "/assignments/{id}")
                        .authenticated()

                        .requestMatchers(HttpMethod.GET, "/assignments/courses/{courseId}")
                        .authenticated()

                        .requestMatchers("/assignments/**")
                        .hasAnyAuthority("ROLE_" + UserRole.INSTRUCTOR.name(), "ROLE_" + UserRole.ADMIN.name())

                        // Lessons endpoints
                        .requestMatchers("/lessons/courses/{courseId}")
                        .authenticated()

                        .requestMatchers("/lessons")
                        .hasAnyAuthority("ROLE_" + UserRole.INSTRUCTOR.name(), "ROLE_" + UserRole.ADMIN.name())

                        .requestMatchers("/lessons/**")
                        .authenticated()

                        // Student Assignment endpoints

                        .requestMatchers(HttpMethod.POST, "/student-assignments/submissions/{id}")
                        .hasAnyAuthority("ROLE_" + UserRole.STUDENT.name())

                        .requestMatchers(HttpMethod.POST, "/student-assignments")
                        .hasAnyAuthority("ROLE_" + UserRole.STUDENT.name())

                        .requestMatchers(HttpMethod.GET, "/student-assignments/{id}")
                        .authenticated()

                        .requestMatchers(HttpMethod.GET, "/student-assignments/courses/{courseId}")
                        .authenticated()

                        .requestMatchers(HttpMethod.GET, "/student-assignments/submissions/{id}")
                        .authenticated()

                        .requestMatchers(HttpMethod.DELETE, "/student-assignments/{id}")
                        .hasAnyAuthority("ROLE_" + UserRole.INSTRUCTOR.name(), "ROLE_" + UserRole.ADMIN.name())

                        .requestMatchers(HttpMethod.PUT, "/student-assignments/grade/{id}")
                        .hasAnyAuthority("ROLE_" + UserRole.INSTRUCTOR.name(), "ROLE_" + UserRole.ADMIN.name())

                        .requestMatchers("/student-assignments/users/**")
                        .authenticated()

                        // Student endpoints
                        .requestMatchers("/students/me/quiz-grades")
                        .hasAuthority("ROLE_" + UserRole.STUDENT.name())

                        .requestMatchers("/students/{studentId}/quiz-grades")
                        .hasAnyAuthority("ROLE_" + UserRole.INSTRUCTOR.name(), "ROLE_" + UserRole.ADMIN.name())

                        // Student Lesson endpoints
                        .requestMatchers("/student-lessons")
                        .hasAuthority("ROLE_" + UserRole.STUDENT.name())

                        .requestMatchers("/student-lessons/students/me/**")
                        .hasAuthority("ROLE_" + UserRole.STUDENT.name())

                        .requestMatchers("/student-lessons/students/{studentId}/**")
                        .hasAnyAuthority( "ROLE_" + UserRole.INSTRUCTOR.name(), "ROLE_" + UserRole.ADMIN.name())

                        // Course endpoints
                        .requestMatchers(HttpMethod.DELETE, "/courses/{id}")
                        .hasAnyAuthority("ROLE_" + UserRole.ADMIN.name(), "ROLE_" + UserRole.INSTRUCTOR.name())

                        .requestMatchers(HttpMethod.GET, "/courses/{id}/material")
                        .authenticated()

                        .requestMatchers(HttpMethod.POST, "/courses/{id}/material")
                        .hasAnyAuthority("ROLE_" + UserRole.INSTRUCTOR.name(), "ROLE_" + UserRole.ADMIN.name())

                        .requestMatchers(HttpMethod.POST, "/courses")
                        .hasAnyAuthority("ROLE_" + UserRole.INSTRUCTOR.name(), "ROLE_" + UserRole.ADMIN.name())

                        .requestMatchers(HttpMethod.GET, "/courses", "/courses/{id}")
                        .authenticated()

                        // Enrollment endpoints
                        .requestMatchers(HttpMethod.POST, "/enrollments/courses/{courseId}")
                        .hasAuthority("ROLE_" + UserRole.STUDENT.name())

                        .requestMatchers(HttpMethod.GET, "/enrollments")
                        .hasAuthority("ROLE_" + UserRole.ADMIN.name())

                        .requestMatchers("/enrollments/{id}")
                        .authenticated()

                        .requestMatchers(HttpMethod.GET, "/enrollments/courses/{courseId}")
                        .hasAuthority("ROLE_" + UserRole.INSTRUCTOR.name())

                        .requestMatchers(HttpMethod.GET, "/enrollments/students/{studentId}")
                        .authenticated()

                        .requestMatchers(HttpMethod.GET, "/enrollments/students/{studentId}/courses/{courseId}")
                        .authenticated()

                        // Notification endpoints
                        .requestMatchers("/notifications/**}")
                        .authenticated()

                        .requestMatchers("/analytics/**")
                        .hasAnyAuthority("ROLE_" + UserRole.INSTRUCTOR.name(), "ROLE_" + UserRole.ADMIN.name())

                        // Allow admin to access all endpoints
                        .requestMatchers("/**").hasAuthority("ROLE_" + UserRole.ADMIN.name())
                )

                .formLogin(form -> form
                        .disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .securityContext(context -> context.securityContextRepository(
                        new DelegatingSecurityContextRepository(
                                new RequestAttributeSecurityContextRepository(),
                                new HttpSessionSecurityContextRepository())));
        return http.build();
    }

    @Bean
    static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http
                .getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userService).passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

}
