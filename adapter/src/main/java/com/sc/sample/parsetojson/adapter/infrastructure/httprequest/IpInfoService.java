package com.sc.sample.parsetojson.adapter.infrastructure.httprequest;


import com.sc.sample.parsetojson.adapter.infrastructure.filter.context.IpApiResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

@Service
public class IpInfoService implements RefreshIpIPCache{
    private final RestClient ipProviderRestClient;
    private final Map<String, IpApiResponse> ipCache = new ConcurrentHashMap<>();

    private static final long IP_REFRESH_SLEEP_PAUSE_NANO =
            TimeUnit.MILLISECONDS.toNanos(100);

    public IpInfoService(RestClient ipProviderRestClient) {
        this.ipProviderRestClient = ipProviderRestClient;
    }

    public IpApiResponse lookupIp(String clientIpAddress) {
        return ipCache.computeIfAbsent(clientIpAddress, this::lookupIpRestCall);
    }

    private final AtomicInteger refreshCounter = new AtomicInteger(0);
    private final AtomicInteger noOfCacheElementsRefreshed = new AtomicInteger(0);
    @Override
    public int refreshIpCache() {
        ipCache.forEach((ip, ipApiResponseOld) -> {
            ipCache.replace(ip, ipApiResponseOld, lookupIpRestCall(ip));
            noOfCacheElementsRefreshed.incrementAndGet();

            // Give normal user REST request a change run
            LockSupport.parkNanos(IP_REFRESH_SLEEP_PAUSE_NANO);
        });
        final int noOfCacheElements = noOfCacheElementsRefreshed.get();
        noOfCacheElementsRefreshed.set(0);
        System.out.println("""
           "%s -> refreshIpCache called - %d times, and " 
           "No of Cached elements refreshed this time:%d"
                        """.formatted(Thread.currentThread().getName(),
                        refreshCounter.incrementAndGet(),
                        noOfCacheElements));
        return noOfCacheElements;
    }

    private IpApiResponse lookupIpRestCall(String clientIpAddress) {
        try {
            return ipProviderRestClient.get()
                        .uri("/{ip}", clientIpAddress)
                        .retrieve()
                        .body(IpApiResponse.class);
        } catch (Throwable e) {
            System.out.println("lookupIp failed for IP:%s, exception:%s"
                    .formatted(clientIpAddress, e.getMessage()));
            // Fallback response when API is unavailable
            return new IpApiResponse(clientIpAddress, "fail",
                    null, null, null, null, null);
        }
    }
}
