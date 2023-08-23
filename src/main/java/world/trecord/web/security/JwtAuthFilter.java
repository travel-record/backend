package world.trecord.web.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import java.util.ArrayList;
import java.util.List;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static world.trecord.web.exception.CustomExceptionError.INVALID_TOKEN;

public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";
    public static final String UTF_8 = "UTF-8";
    public static final String APPLICATION_JSON = "application/json";

    private final JwtResolver jwtResolver;
    private final UserService userService;
    private final List<RequestMatcher> whitelist = new ArrayList<>();

    public JwtAuthFilter(JwtResolver jwtResolver, UserService userService, List<String> whiteListUrlList) {
        this.jwtResolver = jwtResolver;
        this.userService = userService;
        for (String url : whiteListUrlList) {
            whitelist.add(new AntPathRequestMatcher(url));
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = request.getHeader(AUTHORIZATION);

            if (token == null && whitelist.stream().anyMatch(requestMatcher -> requestMatcher.matches(request))) {
                filterChain.doFilter(request, response);
                return;
            }

            long userId = verifyTokenAndExtractUserId(token);
            setAuthenticationWith(userId);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            ApiResponse<Object> body = ApiResponse.of(INVALID_TOKEN.getErrorCode(), INVALID_TOKEN.getErrorMsg(), null);

            response.setStatus(SC_BAD_REQUEST);
            response.setCharacterEncoding(UTF_8);
            response.setContentType(APPLICATION_JSON);

            PrintWriter out = response.getWriter();
            out.print(new ObjectMapper().writeValueAsString(body));
            out.flush();
        }
    }

    private void setAuthenticationWith(long userId) {
        UserDetails userDetails = userService.loadUserByUsername(String.valueOf(userId));
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private long verifyTokenAndExtractUserId(String token) {
        jwtResolver.validate(token);
        return Long.parseLong(jwtResolver.extractUserIdFrom(token));
    }
}
