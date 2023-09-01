package world.trecord.web.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import world.trecord.web.service.users.UserService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static world.trecord.web.exception.CustomExceptionError.INVALID_TOKEN;

public class JwtTokenFilter extends OncePerRequestFilter {

    private final String secretKey;
    private final JwtTokenHandler jwtTokenHandler;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final Map<RequestMatcher, List<HttpMethod>> whitelistMap = new HashMap<>();

    public JwtTokenFilter(String secretKey, JwtTokenHandler jwtTokenHandler, UserService userService, ObjectMapper objectMapper, Map<String, List<HttpMethod>> whitelistMap) {
        this.secretKey = secretKey;
        this.jwtTokenHandler = jwtTokenHandler;
        this.userService = userService;
        this.objectMapper = objectMapper;
        whitelistMap.forEach((url, methods) -> this.whitelistMap.put(new AntPathRequestMatcher(url), methods));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        try {
            String token = req.getHeader(HttpHeaders.AUTHORIZATION);

            if (token == null && isWhitelistRequest(req)) {
                chain.doFilter(req, res);
                return;
            }

            jwtTokenHandler.verify(secretKey, token);

            Long userId = jwtTokenHandler.extractUserId(secretKey, token);

            UserContext userContext = userService.loadUserContext(userId);

            setAuthentication(userContext);

            chain.doFilter(req, res);

        } catch (Exception e) {
            returnInvalidTokenError(res);
        }
    }

    private void returnInvalidTokenError(HttpServletResponse res) throws IOException {
        ApiResponse<Object> body = ApiResponse.of(INVALID_TOKEN.getErrorCode(), INVALID_TOKEN.getErrorMsg(), null);

        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private boolean isWhitelistRequest(HttpServletRequest req) {
        return whitelistMap.entrySet().stream().anyMatch(it -> {
            RequestMatcher matcher = it.getKey();
            List<HttpMethod> allowedMethods = it.getValue();
            return matcher.matches(req) && allowedMethods.contains(HttpMethod.valueOf(req.getMethod()));
        });
    }

    private void setAuthentication(UserContext userContext) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(userContext, userContext.getPassword(), userContext.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
