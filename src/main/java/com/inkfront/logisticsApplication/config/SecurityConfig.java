package com.inkfront.logisticsApplication.config;

import com.inkfront.logisticsApplication.security.CustomUserDetailsService;
import com.inkfront.logisticsApplication.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final LogoutSuccessHandler logoutSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authenticationProvider(authenticationProvider())

                .authorizeHttpRequests(auth -> auth

                        // =====================================================
                        // PUBLIC ENDPOINTS
                        // =====================================================
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/public/**",

                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/webjars/**",

                                "/actuator/health",

                                "/ws/**",
                                "/ws",

                                // Payment gateway callbacks
                                "/api/payment-webhooks/**"
                        ).permitAll()

                        // =====================================================
                        // CLIENT / CUSTOMER
                        // =====================================================
                        .requestMatchers(
                                "/api/orders/**",
                                "/api/users/**",
                                "/api/addresses/**",
                                "/api/payments/**",
                                "/api/reviews/**",
                                "/api/notifications/**"
                        ).hasAnyRole(
                                "CLIENT",
                                "ADMIN",
                                "SUPER_ADMIN",
                                "DRIVER",
                                "DISPATCHER",
                                "FLEET_MANAGER"

                        )

                        // =====================================================
                        // DRIVER SELF SERVICE
                        // =====================================================
                        .requestMatchers(
                                "/api/drivers/me/**",
                                "/api/tracking/me/**"
                        ).hasAnyRole(
                                "DRIVER",
                                "ADMIN",
                                "SUPER_ADMIN"
                        )

                        // =====================================================
                        // DRIVER / DISPATCH OPERATIONS
                        // =====================================================
                        .requestMatchers(
                                "/api/dispatch/**",
                                "/api/tracking/**"
                        ).hasAnyRole(
                                "DRIVER",
                                "DISPATCHER",
                                "ADMIN",
                                "SUPER_ADMIN"
                        )

                        // =====================================================
                        // FLEET MANAGEMENT
                        // =====================================================
                        .requestMatchers(
                                "/api/vehicles/**",
                                "/api/fleet/**",
                                "/api/vehicle-assignments/**",
                                "/api/vehicle-maintenance/**",
                                "/api/vehicle-inspections/**",
                                "/api/vehicle-documents/**",
                                "/api/vehicle-analytics/**"
                        ).hasAnyRole(
                                "FLEET_MANAGER",
                                "ADMIN",
                                "SUPER_ADMIN"
                        )

                        // =====================================================
                        // REPORTS & ANALYTICS
                        // =====================================================
                        .requestMatchers(
                                "/api/dashboard/**",
                                "/api/reports/**",
                                "/api/revenue/**",
                                "/api/tracking-analytics/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "SUPER_ADMIN"
                        )

                        // =====================================================
                        // ADMIN
                        // =====================================================
                        .requestMatchers(
                                "/api/admin/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "SUPER_ADMIN"
                        )

                        // =====================================================
                        // SUPER ADMIN
                        // =====================================================
                        .requestMatchers(
                                "/api/system/**",
                                "/api/audit/**",
                                "/api/super-admin/**"
                        ).hasRole("SUPER_ADMIN")

                        // =====================================================
                        // EVERYTHING ELSE
                        // =====================================================
                        .anyRequest().authenticated()
                )

                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(logoutSuccessHandler)
                        .deleteCookies(
                                "access_token",
                                "refresh_token"
                        )
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://localhost:3000"
                // Production
                // "https://yourdomain.com"
        ));

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With"
        ));

        configuration.setExposedHeaders(List.of(
                "Authorization"
        ));

        configuration.setAllowCredentials(true);

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}