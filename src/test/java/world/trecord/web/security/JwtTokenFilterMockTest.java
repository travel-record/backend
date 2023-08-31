package world.trecord.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;
import world.trecord.web.properties.JwtProperties;
import world.trecord.web.service.users.UserService;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static world.trecord.web.exception.CustomExceptionError.INVALID_TOKEN;

@ExtendWith(MockitoExtension.class)
class JwtTokenFilterMockTest {

    @Mock
    JwtTokenHandler jwtTokenHandler;

    @Mock
    UserService userService;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    FilterChain filterChain;

    @Spy
    ObjectMapper objectMapper;

    @Spy
    JwtProperties jwtProperties;

    JwtTokenFilter jwtTokenFilter;

    @Test
    @DisplayName("올바른 토큰을 가지고 요청하면 filterchain.doFilter이 호출된다")
    void doFilterInternalWithValidTokenTest() throws Exception {
        //given
        jwtTokenFilter = new JwtTokenFilter(jwtProperties.getSecretKey(), jwtTokenHandler, userService, objectMapper, Map.of("/whitelist", List.of(HttpMethod.GET)));

        String validToken = "validToken";
        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";

        ReflectionTestUtils.setField(jwtTokenFilter, "secretKey", secretKey);

        when(request.getHeader("Authorization")).thenReturn(validToken);
        when(jwtTokenHandler.extractUserId(secretKey, validToken)).thenReturn(1L);
        when(userService.loadUserContext(1L)).thenReturn(mock(UserContext.class));

        //when
        jwtTokenFilter.doFilterInternal(request, response, filterChain);

        //then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("올바르지 않은 토큰으로 요청하면 601 에러 응답 코드를 반환한다")
    void doFilterInternalWithInvalidTokenTest() throws Exception {
        //given
        jwtTokenFilter = new JwtTokenFilter(jwtProperties.getSecretKey(), jwtTokenHandler, userService, objectMapper, Map.of("/whitelist", List.of(HttpMethod.GET)));

        String invalidToken = "invalidToken";
        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";

        ReflectionTestUtils.setField(jwtTokenFilter, "secretKey", secretKey);

        when(request.getHeader("Authorization")).thenReturn(invalidToken);
        doThrow(new JwtException("invalid jwt exception")).when(jwtTokenHandler).verify(secretKey, invalidToken);

        PrintWriter mockPrintWriter = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(mockPrintWriter);

        ArgumentCaptor<String> responseContentCaptor = ArgumentCaptor.forClass(String.class);

        //when
        jwtTokenFilter.doFilterInternal(request, response, filterChain);

        //then
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(mockPrintWriter).write(responseContentCaptor.capture());

        String responseBody = responseContentCaptor.getValue();
        Assertions.assertThat(responseBody).contains(String.valueOf(INVALID_TOKEN.getErrorCode()));
    }

    @Test
    @DisplayName("토큰 없이 화이트리스트 URL 리소스에 대해서 요청을 하면 filterchain.doFilter이 호출된다")
    void doFilterInternalWithoutTokenToWhitelistUrlTest() throws Exception {
        //given
        String whiteListPath = "/whitelist";
        jwtTokenFilter = new JwtTokenFilter(jwtProperties.getSecretKey(), jwtTokenHandler, userService, objectMapper, Map.of(whiteListPath, List.of(HttpMethod.GET)));

        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getServletPath()).thenReturn(whiteListPath);
        when(request.getMethod()).thenReturn(HttpMethod.GET.toString());

        //when
        jwtTokenFilter.doFilterInternal(request, response, filterChain);

        //then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("토큰 없이 보안 URL 리소스에 대해서 요청을 하면 601 에러 응답 코드를 반환한다")
    void doFilterInternalWithoutTokenToSecuritylistUrlTest() throws Exception {
        //given
        jwtTokenFilter = new JwtTokenFilter(jwtProperties.getSecretKey(), jwtTokenHandler, userService, objectMapper, Map.of("/whitelist", List.of(HttpMethod.GET)));

        String secretKey = "zOlJAgjm9iEZPqmzilEMh4NxvOfg1qBRP3xYkzUWpSE";

        ReflectionTestUtils.setField(jwtTokenFilter, "secretKey", secretKey);

        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getServletPath()).thenReturn("/security");
        doThrow(new JwtException("invalid jwt exception")).when(jwtTokenHandler).verify(secretKey, null);

        PrintWriter mockPrintWriter = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(mockPrintWriter);

        ArgumentCaptor<String> responseContentCaptor = ArgumentCaptor.forClass(String.class);

        //when
        jwtTokenFilter.doFilterInternal(request, response, filterChain);

        //then
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(mockPrintWriter).write(responseContentCaptor.capture());

        String responseBody = responseContentCaptor.getValue();
        Assertions.assertThat(responseBody).contains(String.valueOf(INVALID_TOKEN.getErrorCode()));
    }
}