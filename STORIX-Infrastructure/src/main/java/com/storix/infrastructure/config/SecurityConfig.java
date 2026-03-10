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
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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

                                // [Onboarding]
                                .requestMatchers("/api/v1/onboarding/**").permitAll()
          
                                // [Auth]
                                .requestMatchers("/api/v1/auth/oauth/**").permitAll()
                                .requestMatchers("/api/v1/auth/nickname/valid").permitAll()
                                .requestMatchers("/api/v1/auth/users/artist/login").permitAll()
                                .requestMatchers("/api/v1/auth/tokens/refresh").permitAll()
                                .requestMatchers("/api/v1/auth/developer/**").permitAll() // 추후 Admin 변경

                                // [Home]
                                .requestMatchers("/api/v1/home/**").permitAll()
          
                                // [Search]
                                .requestMatchers("/api/v1/search/**").permitAll()
          
                                // [Profile]
                                .requestMatchers("/api/v1/profile/reader/**").hasRole("READER")
                                .requestMatchers("/api/v1/profile/**").hasAnyRole("READER","ARTIST")

                                // [Plus]
                                .requestMatchers("/api/v1/plus/reader/**").hasRole("READER")
                                .requestMatchers("/api/v1/plus/artist/**").hasRole("ARTIST")

                                // [Image]
                                .requestMatchers("/api/v1/image/fan-board").hasRole("ARTIST")

                                // [Library]
                                .requestMatchers("/api/v1/library/**").hasRole("READER")

                                // [TopicRoom]
                                .requestMatchers("/api/v1/topic-rooms/popular").permitAll()
                                .requestMatchers("/api/v1/topic-rooms/today").permitAll()
                                .requestMatchers("/api/v1/topic-rooms/search").permitAll()
                                .requestMatchers("/api/v1/topic-rooms/**").hasRole("READER")
                                .requestMatchers("/ws-stomp/**").permitAll()

                                // [Works]
                                .requestMatchers(HttpMethod.PATCH,   "/api/v1/works/review/*").hasRole("READER")
                                .requestMatchers(HttpMethod.POST,   "/api/v1/works/review/*").hasRole("READER")
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/works/review/*").hasRole("READER")
                                .requestMatchers("/api/v1/works/review/*/like").hasRole("READER")
                                .requestMatchers("/api/v1/works/review/*/report").hasRole("READER")
                                .requestMatchers("/api/v1/works/**").permitAll()

                                // [Favorite]
                                .requestMatchers(HttpMethod.GET, "/api/v1/favorite/**")
                                .access(new WebExpressionAuthorizationManager("!hasRole('ARTIST')"))
                                .requestMatchers("/api/v1/favorite/**").hasRole("READER")

                                // [Hashtag]
                                .requestMatchers("/api/v1/hashtags/recommendations").permitAll()

                                // [Feed]
                                .requestMatchers("/api/v1/feed/**").hasRole("READER")

                                // [Preference]
                                .requestMatchers("/api/v1/preference/**").hasRole("READER")

                                .anyRequest().authenticated()
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
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
