package com.sc.sample.parsetojson.adapter.infrastructure.httprequest;

import com.sc.sample.parsetojson.adapter.infrastructure.filter.context.IpApiResponse;
import com.sc.sample.parsetojson.adapter.infrastructure.filter.context.RequestContextHolder;
import com.sc.sample.parsetojson.adapter.out.persistence.httprequest.RequestInfoRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Component
@Order(1)
public class RequestInfoBuilderFilter implements Filter {
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";

    private final IpInfoService ipInfoService;

    private final RequestInfoRepository requestInfoRepository;

    private final Executor executor;

    public RequestInfoBuilderFilter(
            IpInfoService ipInfoService,
            RequestInfoRepository requestInfoRepository,
            Executor executor) {
        this.ipInfoService = ipInfoService;
        this.requestInfoRepository = requestInfoRepository;
        this.executor = executor;
    }
    public RequestInfoBuilderFilter(
            IpInfoService ipInfoService,
            RequestInfoRepository requestInfoRepository) {
        this.ipInfoService = ipInfoService;
        this.requestInfoRepository = requestInfoRepository;
        ThreadFactory threadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread();
                thread.setName("save(httpRequestInfo)");
                thread.setDaemon(true);
                return thread;
            }
        };
        this.executor = Executors.newCachedThreadPool(threadFactory);
    }

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain)
            throws IOException, ServletException {
        Instant requestArriveTime = Instant.now();

        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        String clientIpAddress;
        IpApiResponse ipApiResponse = null;
        try {
//            clientIpAddress = "24.48.0.1"; // "24.38.0.1" - US and "24.48.0.1" - Canada

            clientIpAddress = extractClientIPAddress(httpRequest);
            ipApiResponse = ipInfoService.lookupIp(clientIpAddress);

            RequestContextHolder.set(ipApiResponse);

            // Also attach to request attributes for servlet-based filters
            httpRequest.setAttribute(
                    RequestContextHolder.IP_INFO_RESPONSE_KEY, ipApiResponse);

            filterChain.doFilter(servletRequest, servletResponse);

        } finally {
            RequestContextHolder.clear();
        }

        String id = UUID.randomUUID().toString();
        String requestUri = httpRequest.getRequestURI();
        int httpResponseCode = httpResponse.getStatus();
        String requesterCountryCode = ipApiResponse.countryCode();
        String ispProviderIp = ipApiResponse.isp();
        long timeLapsedMillis = Duration.between(
                requestArriveTime, Instant.now()).toMillis();

        HttpRequestInfo httpRequestInfo = new HttpRequestInfo(
                id, requestUri, requestArriveTime,
                httpResponseCode, clientIpAddress,
                requesterCountryCode, ispProviderIp, timeLapsedMillis);

        System.out.println("RequestInfoBuilderFilter.doFilter - "
                .concat("post filterChain.doFilter - httpRequestInfo:%s").
                formatted(httpRequestInfo));

        executor.execute(() -> requestInfoRepository.save(httpRequestInfo));
    }

    private String extractClientIPAddress(HttpServletRequest httpRequest) {
        String forwarded = httpRequest.getHeader(X_FORWARDED_FOR);
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return httpRequest.getRemoteAddr();
    }
}
