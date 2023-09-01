package world.trecord.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import world.trecord.web.properties.JwtProperties;
import world.trecord.web.service.users.UserService;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@EnableWebSecurity
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final JwtProperties jwtProperties;
    private final JwtTokenHandler jwtTokenHandler;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .httpBasic().disable()
                .csrf().disable()
                .cors()
                .configurationSource(corsConfigurationSource())
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeHttpRequests(
                        req -> req
                                .requestMatchers("/", "/api/**").permitAll()
                                .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public JwtTokenFilter jwtAuthFilter() {
        Map<String, List<HttpMethod>> whitelistMap = Map.of(
                "/", List.of(GET),
                "/api/*/auth/google-login", List.of(POST),
                "/api/*/auth/token", List.of(POST),
                "/api/*/users/{userId}", List.of(GET),
                "/api/*/feeds/{feedId}", List.of(GET),
                "/api/*/records/{recordId}", List.of(GET),
                "/api/*/records/{recordId}/comments", List.of(GET)
        );
        return new JwtTokenFilter(jwtProperties.getSecretKey(), jwtTokenHandler, userService, objectMapper, whitelistMap);
    }

    @Bean
    public FilterRegistrationBean<JwtTokenFilter> filter() {
        FilterRegistrationBean<JwtTokenFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(jwtAuthFilter());
        registrationBean.addUrlPatterns("/api/*");
        return registrationBean;
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
