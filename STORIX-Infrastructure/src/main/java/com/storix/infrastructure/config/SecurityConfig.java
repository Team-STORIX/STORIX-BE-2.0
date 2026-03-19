package com.storix.infrastructure.config;

import com.storix.infrastructure.global.security.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final OnboardingAuthenticationFilter onboardingFilter;
    private final ErrorHandlingFilter errorHandlingFilter;

    private final SecurityEntryPoint securityEntryPoint;
    private final SecurityDeniedHandler securityDeniedHandler;
    private final Environment environment;

    @Value("${swagger.user:}")
    private String swaggerUser;

    @Value("${swagger.password:}")
    private String swaggerPassword;

    private boolean isLocal() {
        return environment.acceptsProfiles(Profiles.of("local"));
    }

    @Bean
    @Order(1)
    public SecurityFilterChain swaggerFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/swagger-ui/**", "/v3/api-docs/**")
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable);

        if (isLocal()) {
            http.authorizeHttpRequests(requests -> requests
                    .anyRequest().permitAll()
            );
        } else {
            InMemoryUserDetailsManager swaggerUserDetailsManager = new InMemoryUserDetailsManager(
                    User.withUsername(swaggerUser)
                            .password(passwordEncoder().encode(swaggerPassword))
                            .roles("SWAGGER")
                            .build()
            );
            DaoAuthenticationProvider swaggerAuthProvider = new DaoAuthenticationProvider(swaggerUserDetailsManager);
            swaggerAuthProvider.setPasswordEncoder(passwordEncoder());

            http
                    .authenticationProvider(swaggerAuthProvider)
                    .httpBasic(basic -> basic
                            .authenticationEntryPoint(new BasicAuthenticationEntryPoint() {{
                                setRealmName("Swagger");
                                afterPropertiesSet();
                            }})
                    )
                    .authorizeHttpRequests(requests -> requests
                            .anyRequest().hasRole("SWAGGER")
                    );
        }

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http)  throws Exception {

        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(
                        (requests) -> requests
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers("/actuator/health").permitAll()

                                // [Onboarding]
                                .requestMatchers("/api/v1/onboarding/**").permitAll()
          
                                // [Auth]
                                .requestMatchers("/api/v1/auth/oauth/**").permitAll()
                                .requestMatchers("/api/v1/auth/nickname/valid").permitAll()
                                .requestMatchers("/api/v1/auth/tokens/refresh").permitAll()
                                .requestMatchers("/api/v1/auth/developer/signup").permitAll()
                                .requestMatchers("/api/v1/auth/developer/login").permitAll()
                                .requestMatchers("/api/v1/auth/developer/slack/callback").permitAll()
                                .requestMatchers("/api/v1/auth/developer/**").hasRole("ADMIN")

                                // [TopicRoom]
                                .requestMatchers("/ws-stomp/**").permitAll()

                                .anyRequest().hasRole("READER")
                )

                // jwt filter
                .addFilterBefore(errorHandlingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(onboardingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                // spring security exception handler
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(securityEntryPoint) // 401 error
                        .accessDeniedHandler(securityDeniedHandler) // 403 error
                );

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of(
                "https://storix.kr",
                "https://www.storix.kr",
                "https://api.storix.kr",
                "http://localhost:3000",
                "http://localhost:5173",
                "https://storix-fe-git-develop-kim-yunseongs-projects.vercel.app",
                "https://storix-fe-git-main-kim-yunseongs-projects.vercel.app"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        config.setAllowCredentials(true);
        config.addExposedHeader("Set-Cookie");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("ROLE_ADMIN > ROLE_READER");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
