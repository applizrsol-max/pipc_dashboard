package com.pipc.dashboard.security.utility;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import static org.springframework.security.config.Customizer.withDefaults; // Required import

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtAuthFilter jwtAuthFilter; 

	public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
		this.jwtAuthFilter = jwtAuthFilter;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

    // 🛡️ The main security filter chain configuration
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
	    http
	        .csrf(csrf -> csrf.disable())
	        
	        // 🔑 THE FINAL, MOST ROBUST FIX: Explicitly configure CORS to use your custom bean.
            // This is grammatically correct for the builder pattern and resolves the IDE error.
	        .cors(cors -> cors.configurationSource(corsConfigurationSource())) 
	        
	        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	        
	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers(
	                "/pipc/dashboard/onboarding/register",
	                "/pipc/dashboard/onboarding/login",
	                "/pipc/dashboard/onboarding/refresh-token",
	                "/pipc/dashboard/onboarding/forgotPassword"
	            ).permitAll()
	            
	            .requestMatchers(HttpMethod.DELETE, "/pipc/dashboard/onboarding/deleteUser/**").hasRole("ADMIN")
	            
	            .anyRequest().authenticated()
	        )
	        
	        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

	    return http.build();
	}

    // 🌐 Your CUSTOM CORS Configuration Bean
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();

		// Set allowed origins (Updated for typical local ports)
		config.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "http://localhost:4200", 
            "https://pipc-dashboard.onrender.com"
        ));

		// Allowed methods, headers, and credentials
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("Authorization", "Content-Type", "correlationId", "businessCorrelationId"));
		config.setExposedHeaders(List.of("Authorization", "correlationId", "businessCorrelationId"));
		config.setAllowCredentials(true);

		// Register this config for all application paths
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}