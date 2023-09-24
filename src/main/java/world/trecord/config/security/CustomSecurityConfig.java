package world.trecord.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import world.trecord.config.properties.JwtProperties;
import world.trecord.service.users.UserService;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpMethod.GET;

@EnableWebSecurity
@RequiredArgsConstructor
@Configuration
public class CustomSecurityConfig {

    private final JwtProperties jwtProperties;
    private final JwtTokenHandler jwtTokenHandler;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .httpBasic(HttpBasicConfigurer::disable)
                .csrf(CsrfConfigurer::disable)
                .cors(configurer -> configurer.configurationSource(corsConfigurationSource()))
                .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        registry -> registry
                                .requestMatchers("/", "/api/*/auth/google-login", "/api/*/auth/token").permitAll()
                                .requestMatchers("/api/**").authenticated()
                                .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(configurer -> configurer.authenticationEntryPoint(new CustomAuthenticationEntryPoint(objectMapper)))
                .build();
    }

    public JwtTokenFilter jwtAuthFilter() {
        Map<String, List<HttpMethod>> whitelistMap = Map.of(
                "/api/.+/users/\\d+", List.of(GET),
                "/api/.+/feeds/\\d+", List.of(GET),
                "/api/.+/feeds/\\d+/records", List.of(GET),
                "/api/.+/records/\\d+", List.of(GET),
                "/api/.+/records/\\d+/comments", List.of(GET),
                "/api/.+/comments/\\d+/replies", List.of(GET)
        );

        List<String> tokenInParamUrls = List.of("/api/*/notifications/subscribe");

        return new JwtTokenFilter(jwtProperties.getSecretKey(),
                jwtTokenHandler,
                userService,
                whitelistMap,
                tokenInParamUrls);
    }

    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setMaxAge(Duration.ofHours(1));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
