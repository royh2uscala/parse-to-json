package com.sc.sample.parsetojson.adapter.infrastructure.httprequest;

import com.sc.sample.parsetojson.adapter.infrastructure.filter.context.IpApiResponse;
import com.sc.sample.parsetojson.adapter.infrastructure.filter.context.RequestContextHolder;
import com.sc.sample.parsetojson.adapter.out.persistence.httprequest.RequestInfoRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;


import java.time.Duration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


public class RequestInfoBuilderFilterTest {

    private IpInfoService ipInfoService;
    private RequestInfoRepository requestInfoRepository;
    private RequestInfoBuilderFilter filter;

    @BeforeEach
    void setUp() {
        ipInfoService = mock(IpInfoService.class);
        requestInfoRepository = mock(RequestInfoRepository.class);
        filter = new RequestInfoBuilderFilter(
                ipInfoService,
                requestInfoRepository,
                Runnable::run);
    }

    @Test
    void doFilter_shouldCallIpAndSaveRequestInfo() throws Exception {
        String testIp = "10.156.20.1";
        IpApiResponse ipApiResponse = new IpApiResponse(
                testIp, "success", "United States", "US",
                "Cablevision Systems Corp.", "AAA OF NEW JERSEY",
                "AS6128 Cablevision Systems Corp.");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader(RequestInfoBuilderFilter.X_FORWARDED_FOR)).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(testIp);
        when(ipInfoService.lookupIp(testIp)).thenReturn(ipApiResponse);

        when(request.getRequestURI()).thenReturn("/test-endpoint");
        when(response.getStatus()).thenReturn(HttpStatus.OK.value());


        //test
        filter.doFilter(request, response, filterChain);

        verify(request, times(1))
                .setAttribute(eq(RequestContextHolder.IP_INFO_RESPONSE_KEY), eq(ipApiResponse));

        //Wait briefly for async save() call
        await().atMost(Duration.ofSeconds(3))
                .untilAsserted(() -> verify(requestInfoRepository).save(any(HttpRequestInfo.class)));


        //Assert
        verify(filterChain, times(1))
                .doFilter(any(ServletRequest.class), any(ServletResponse.class));

        ArgumentCaptor<HttpRequestInfo> captor =
                ArgumentCaptor.forClass(HttpRequestInfo.class);

        verify(requestInfoRepository, atLeastOnce()).save(captor.capture());

        HttpRequestInfo savedHttpRequestInfo = captor.getValue();
        assertThat(savedHttpRequestInfo.getRequesterCountryCode()).isEqualTo("US");
        assertThat(savedHttpRequestInfo.getIspProviderIp()).isEqualTo("Cablevision Systems Corp.");
        assertThat(savedHttpRequestInfo.getRequesterIp()).isEqualTo(testIp);
    }

    @Test
    void doFilter_shouldCallIpAndSaveRequestInfoWithForward() throws Exception {
        String testIp = "203.0.113.42, 198.51.100.17, 10.0.0.5";
        String finalTestIp = "203.0.113.42";

        IpApiResponse ipApiResponse = new IpApiResponse(
                testIp, "success", "United States", "US",
                "Cablevision Systems Corp.", "AAA OF NEW JERSEY",
                "AS6128 Cablevision Systems Corp.");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader(RequestInfoBuilderFilter.X_FORWARDED_FOR)).thenReturn(testIp);

        when(ipInfoService.lookupIp(finalTestIp)).thenReturn(ipApiResponse);

        when(request.getRequestURI()).thenReturn("/test-endpoint");
        when(response.getStatus()).thenReturn(HttpStatus.OK.value());

        //test
        filter.doFilter(request, response, filterChain);

        verify(request, times(1))
                .setAttribute(eq(RequestContextHolder.IP_INFO_RESPONSE_KEY), eq(ipApiResponse));

        //Assert
        verify(filterChain, times(1))
                .doFilter(any(ServletRequest.class), any(ServletResponse.class));

        ArgumentCaptor<HttpRequestInfo> captor =
                ArgumentCaptor.forClass(HttpRequestInfo.class);

        verify(requestInfoRepository, atLeastOnce()).save(captor.capture());

        HttpRequestInfo savedHttpRequestInfo = captor.getValue();
        assertThat(savedHttpRequestInfo.getRequesterCountryCode()).isEqualTo("US");
        assertThat(savedHttpRequestInfo.getIspProviderIp()).isEqualTo("Cablevision Systems Corp.");
        assertThat(savedHttpRequestInfo.getRequesterIp()).isEqualTo(finalTestIp);
    }
}
