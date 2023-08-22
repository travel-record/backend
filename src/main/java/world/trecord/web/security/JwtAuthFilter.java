package world.trecord.web.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;
import world.trecord.web.controller.ApiResponse;
import world.trecord.web.service.users.UserService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static world.trecord.web.exception.CustomExceptionError.INVALID_TOKEN;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";
    private final JwtResolver jwtResolver;
    private final UserService userService;
    // TODO change type to ant matcher
    private final List<String> whitelist;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        try {
            String token = request.getHeader(AUTHORIZATION);

            if (token != null) {
                long userId = validateAndExtractUserIdWith(token);
                setAuthenticationWith(userId);
            } else {
                if (whitelist.stream().noneMatch(path::matches)) {
                    long userId = validateAndExtractUserIdWith(token);
                    setAuthenticationWith(userId);
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            ApiResponse apiResponse = ApiResponse.of(INVALID_TOKEN.getErrorCode(), INVALID_TOKEN.getErrorMsg(), null);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.print(new ObjectMapper().writeValueAsString(apiResponse));
            out.flush();
        }
    }

    private void setAuthenticationWith(long userId) {
        UserDetails userDetails = userService.loadUserByUsername(String.valueOf(userId));
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private long validateAndExtractUserIdWith(String token) {
        jwtResolver.validate(token);
        long userId = Long.parseLong(jwtResolver.extractUserIdFrom(token));
        return userId;
    }

}
