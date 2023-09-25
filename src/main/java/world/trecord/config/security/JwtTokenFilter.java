package world.trecord.config.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import world.trecord.config.security.account.AnonymousContext;
import world.trecord.config.security.account.UserContext;
import world.trecord.service.users.UserService;

import java.io.IOException;
import java.util.*;

@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

    private final String secretKey;
    private final JwtTokenHandler jwtTokenHandler;
    private final UserService userService;
    private final Map<RegexRequestMatcher, List<HttpMethod>> whitelistMap = new HashMap<>();
    private final Set<RequestMatcher> tokenInParamSet = new HashSet<>();

    public JwtTokenFilter(String secretKey,
                          JwtTokenHandler jwtTokenHandler,
                          UserService userService,
                          Map<String, List<HttpMethod>> whitelistMap,
                          List<String> tokenInParamUrls) {
        this.secretKey = secretKey;
        this.jwtTokenHandler = jwtTokenHandler;
        this.userService = userService;
        whitelistMap.forEach((url, methods) -> this.whitelistMap.put(new RegexRequestMatcher(url, null), methods));
        tokenInParamUrls.forEach(url -> this.tokenInParamSet.add(new AntPathRequestMatcher(url)));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        try {
            String token = req.getHeader(HttpHeaders.AUTHORIZATION);

            if (isTokenInRequestQueryParam(req)) {
                log.info("Request with {} check the query param", req.getRequestURI());
                if (req.getQueryString().contains("token")) {
                    token = req.getQueryString().split("=")[1].trim();
                }
            }

            if (Objects.isNull(token) && isWhitelistRequest(req) && isNotAuthenticated()) {
                AnonymousContext anonymousContext = AnonymousContext.of();
                Authentication authentication = new UsernamePasswordAuthenticationToken(anonymousContext, anonymousContext.getPassword(), anonymousContext.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                chain.doFilter(req, res);
                return;
            }

            jwtTokenHandler.verifyToken(secretKey, token);
            Long userId = jwtTokenHandler.getUserIdFromToken(secretKey, token);
            UserContext userContext = userService.getUserContextOrException(userId);
            setAuthentication(userContext);
        } catch (Exception e) {
            chain.doFilter(req, res);
            return;
        }
        chain.doFilter(req, res);
    }

    private boolean isWhitelistRequest(HttpServletRequest req) {
        return whitelistMap.entrySet().stream().anyMatch(it -> {
            RequestMatcher matcher = it.getKey();
            List<HttpMethod> allowedMethods = it.getValue();
            return matcher.matches(req) && allowedMethods.contains(HttpMethod.valueOf(req.getMethod()));
        });
    }

    private boolean isTokenInRequestQueryParam(HttpServletRequest req) {
        return tokenInParamSet.stream().anyMatch(r -> r.matches(req));
    }

    private boolean isNotAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Objects.isNull(authentication) || authentication instanceof AnonymousAuthenticationToken;
    }

    private void setAuthentication(UserContext userContext) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(userContext, userContext.getPassword(), userContext.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
