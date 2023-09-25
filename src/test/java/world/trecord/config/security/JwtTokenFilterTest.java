package world.trecord.config.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import world.trecord.config.properties.JwtProperties;
import world.trecord.config.security.account.AnonymousContext;
import world.trecord.config.security.account.UserContext;
import world.trecord.infra.test.AbstractMockMvcTest;
import world.trecord.service.users.UserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

class JwtTokenFilterTest extends AbstractMockMvcTest {

    @Mock
    JwtTokenHandler jwtTokenHandler;

    @Mock
    UserService userService;

    @Mock
    SecurityContextHolder securityContextHolder;

    @Mock
    HttpServletRequest req;

    @Mock
    HttpServletResponse res;

    @Mock
    FilterChain filterChain;

    @Spy
    JwtProperties jwtProperties;

    JwtTokenFilter jwtTokenFilter;

    @Test
    @DisplayName("올바른 토큰을 가지고 요청하면 SecuriyContext에 인증 토큰을 저장한다")
    void doFilterInternalWithValidTokenTest() throws Exception {
        //given
        String validToken = "validToken";
        String whitelistPath = "/whitelist";

        jwtTokenFilter = new JwtTokenFilter(jwtProperties.getSecretKey(), jwtTokenHandler, userService, Map.of(whitelistPath, List.of(HttpMethod.GET)), new ArrayList<>());

        when(req.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(validToken);
        when(jwtTokenHandler.getUserIdFromToken(any(), any())).thenReturn(1L);
        when(userService.getUserContextOrException(any())).thenReturn(mock(UserContext.class));

        //when
        jwtTokenFilter.doFilterInternal(req, res, filterChain);

        //then
        verify(filterChain).doFilter(req, res);
        Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNotNull()
                .isInstanceOf(UsernamePasswordAuthenticationToken.class)
                .extracting("principal")
                .isInstanceOf(UserContext.class);
    }

    @Test
    @DisplayName("토큰 없이 화이트리스트 URL 리소스에 대해서 요청을 하면 SecuriyContext에 인증 토큰을 저장한다")
    void doFilterInternalWithoutTokenToWhitelistUrlTest() throws Exception {
        //given
        String whiteListPath = "/whitelist";
        jwtTokenFilter = new JwtTokenFilter(jwtProperties.getSecretKey(), jwtTokenHandler, userService, Map.of(whiteListPath, List.of(HttpMethod.GET)), new ArrayList<>());

        when(req.getHeader("Authorization")).thenReturn(null);
        when(req.getServletPath()).thenReturn(whiteListPath);
        when(req.getMethod()).thenReturn(HttpMethod.GET.toString());

        //when
        jwtTokenFilter.doFilterInternal(req, res, filterChain);

        //then
        verify(filterChain, times(1)).doFilter(req, res);
        Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNotNull()
                .isInstanceOf(UsernamePasswordAuthenticationToken.class)
                .extracting("principal")
                .isInstanceOf(AnonymousContext.class);
        ;
    }

    @Test
    @DisplayName("올바르지 않은 토큰으로 요청하면 SecuriyContext에 인증 토큰을 저장하지 않는다")
    void doFilterInternalWithInvalidTokenTest() throws Exception {
        //given
        jwtTokenFilter = new JwtTokenFilter(jwtProperties.getSecretKey(), jwtTokenHandler, userService, Map.of("/whitelist", List.of(HttpMethod.GET)), new ArrayList<>());

        String invalidToken = "invalidToken";
        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";

        ReflectionTestUtils.setField(jwtTokenFilter, "secretKey", secretKey);

        when(req.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(invalidToken);
        doThrow(new JwtException("invalid jwt exception")).when(jwtTokenHandler).verifyToken(secretKey, invalidToken);

        //when
        jwtTokenFilter.doFilterInternal(req, res, filterChain);

        //then
        verify(filterChain, times(1)).doFilter(req, res);
        Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("토큰 없이 보안 URL 리소스에 대해서 요청을 하면 SecuriyContext에 인증 토큰을 저장하지 않는다")
    void doFilterInternalWithoutTokenToSecuritylistUrlTest() throws Exception {
        //given
        jwtTokenFilter = new JwtTokenFilter(jwtProperties.getSecretKey(), jwtTokenHandler, userService, Map.of("/whitelist", List.of(HttpMethod.GET)), new ArrayList<>());

        String requestUri = "/security";
        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";

        when(req.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        when(req.getServletPath()).thenReturn(requestUri);
        doThrow(new JwtException("invalid jwt exception")).when(jwtTokenHandler).verifyToken(secretKey, null);

        //when
        jwtTokenFilter.doFilterInternal(req, res, filterChain);

        //then
        verify(filterChain, times(1)).doFilter(req, res);
        Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("요청 URL에 쿼리 파라미터에 올바른 토큰이 있으면 SecuriyContext에 인증 토큰을 저장한다")
    void isTokenInRequestQueryParamTest() throws Exception {
        //given
        String validToken = "validToken";

        String tokenInUrl = "/uri";
        jwtTokenFilter = new JwtTokenFilter(jwtProperties.getSecretKey(), jwtTokenHandler, userService, new HashMap<>(), List.of(tokenInUrl));

        when(req.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        when(req.getServletPath()).thenReturn(tokenInUrl + "?token=" + validToken);

        when(jwtTokenHandler.getUserIdFromToken(any(), any())).thenReturn(1L);
        when(userService.getUserContextOrException(any())).thenReturn(mock(UserContext.class));

        //when
        jwtTokenFilter.doFilterInternal(req, res, filterChain);

        //then
        verify(filterChain, times(1)).doFilter(req, res);
        Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNotNull()
                .isInstanceOf(UsernamePasswordAuthenticationToken.class)
                .extracting("principal")
                .isInstanceOf(UserContext.class);
    }

    @Test
    @DisplayName("요청 URL에 쿼리 파라미터에에 올바르지 않은 토큰이 있으면 SecuriyContext에 인증 토큰을 저장하지 않는다")
    void isTokenInRequestQueryParamWhenInvalidTokenTest() throws Exception {
        //given
        String invalidToken = "invalidToken";
        String tokenInUrl = "/uri";

        jwtTokenFilter = new JwtTokenFilter(jwtProperties.getSecretKey(), jwtTokenHandler, userService, new HashMap<>(), List.of(tokenInUrl));

        when(req.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        when(req.getServletPath()).thenReturn(tokenInUrl + "?token=" + invalidToken);

        doThrow(new JwtException("invalid jwt exception")).when(jwtTokenHandler).verifyToken(any(), any());


        //when
        jwtTokenFilter.doFilterInternal(req, res, filterChain);

        //then
        verify(filterChain, times(1)).doFilter(req, res);
        Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}