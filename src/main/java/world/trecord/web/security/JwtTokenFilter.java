package world.trecord.web.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import world.trecord.web.controller.ApiResponse;
import world.trecord.web.service.users.UserContext;
import world.trecord.web.service.users.UserService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static world.trecord.web.exception.CustomExceptionError.INVALID_TOKEN;

@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

    private final static List<String> TOKEN_IN_PARAM_URLS = List.of("/api/v1/notifications/subscribe");

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

            if (TOKEN_IN_PARAM_URLS.contains(req.getRequestURI())) {
                log.info("Request with {} check the query param", req.getRequestURI());
                token = req.getQueryString().split("=")[1].trim();
            }

            if (token == null && isWhitelistRequest(req)) {
                chain.doFilter(req, res);
                return;
            }

            jwtTokenHandler.verify(secretKey, token);

            Long userId = jwtTokenHandler.getUserId(secretKey, token);

            UserContext userContext = userService.getUserContextOrException(userId);

            setAuthentication(userContext);

            chain.doFilter(req, res);

        } catch (Exception e) {
            log.error("Error in [JwtTokenFilter] while processing the request. Cause: [{}]", e.getMessage());
            returnInvalidTokenError(res);
        }
    }

    private void returnInvalidTokenError(HttpServletResponse res) throws IOException {
        ApiResponse<Object> body = ApiResponse.of(INVALID_TOKEN.getErrorCode(), INVALID_TOKEN.getErrorMsg(), null);
        res.setStatus(HttpStatus.UNAUTHORIZED.value());
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
