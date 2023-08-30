package world.trecord.web.security.jwt;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import world.trecord.web.controller.ApiResponse;
import world.trecord.web.security.UserContext;
import world.trecord.web.service.users.UserService;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static world.trecord.web.exception.CustomExceptionError.INVALID_TOKEN;

public class JwtTokenFilter extends OncePerRequestFilter {


    @Value("${jwt.secret-key}")
    private String secretKey;

    private final JwtTokenHandler jwtTokenHandler;
    private final UserService userService;
    private final Map<RequestMatcher, List<HttpMethod>> whitelistMap = new HashMap<>();

    public JwtTokenFilter(JwtTokenHandler jwtTokenHandler, UserService userService, Map<String, List<HttpMethod>> whitelistMap) {
        this.jwtTokenHandler = jwtTokenHandler;
        this.userService = userService;
        whitelistMap.forEach((url, methods) -> this.whitelistMap.put(new AntPathRequestMatcher(url), methods));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = request.getHeader(HttpHeaders.AUTHORIZATION);

            if (token == null && isRequestInWhitelist(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            jwtTokenHandler.verify(secretKey, token);

            Long userId = jwtTokenHandler.extractUserId(secretKey, token);

            UserContext userContext = userService.loadUserContextByUserId(userId);

            setAuthenticationWith(userContext);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            ApiResponse<Object> body = ApiResponse.of(INVALID_TOKEN.getErrorCode(), INVALID_TOKEN.getErrorMsg(), null);

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            // response.getWriter().write(new ObjectMapper().writeValueAsString(body));

            PrintWriter out = response.getWriter();
            out.print(new ObjectMapper().writeValueAsString(body));
            out.flush();
        }
    }

    private boolean isRequestInWhitelist(HttpServletRequest request) {
        return whitelistMap.entrySet().stream().anyMatch(entry -> {
            RequestMatcher matcher = entry.getKey();
            List<HttpMethod> allowedMethods = entry.getValue();
            return matcher.matches(request) && allowedMethods.contains(HttpMethod.valueOf(request.getMethod()));
        });
    }

    private void setAuthenticationWith(UserContext userContext) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(userContext, userContext.getPassword(), userContext.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
