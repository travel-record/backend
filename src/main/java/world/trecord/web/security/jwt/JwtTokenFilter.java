package world.trecord.web.security.jwt;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import world.trecord.web.controller.ApiResponse;
import world.trecord.web.service.users.UserService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static world.trecord.web.exception.CustomExceptionError.INVALID_TOKEN;

public class JwtTokenFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";
    private static final String UTF_8 = "UTF-8";
    private static final String APPLICATION_JSON = "application/json";

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
            String token = request.getHeader(AUTHORIZATION);

            if (token == null && isRequestInWhitelist(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            jwtTokenHandler.verify(secretKey, token);
            String userId = jwtTokenHandler.extractUserId(secretKey, token);
            setAuthenticationWith(userId);
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            // TODO exception 구분
            ApiResponse<Object> body = ApiResponse.of(INVALID_TOKEN.getErrorCode(), INVALID_TOKEN.getErrorMsg(), null);

            response.setStatus(SC_BAD_REQUEST);
            response.setCharacterEncoding(UTF_8);
            response.setContentType(APPLICATION_JSON);

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

    private void setAuthenticationWith(String userId) {
        UserDetails userDetails = userService.loadUserByUsername(userId);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
