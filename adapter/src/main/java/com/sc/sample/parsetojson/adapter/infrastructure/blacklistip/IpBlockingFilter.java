package com.sc.sample.parsetojson.adapter.infrastructure.blacklistip;

import com.sc.sample.parsetojson.adapter.infrastructure.filter.context.IpApiResponse;
import com.sc.sample.parsetojson.adapter.infrastructure.filter.context.RequestContextHolder;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@Order(2)
public class IpBlockingFilter implements Filter {
    private final BlackListGeoConfig blackListService;
    private final List<String> blackListIspSearchTokens;
   public static final String X_FORWARDED_FOR = "X-Forwarded-For";

    public IpBlockingFilter(BlackListGeoConfig blackListService) {
        this.blackListService = blackListService;
        this.blackListIspSearchTokens = blackListService.getIspDataCenterVenues().stream()
                .map(wrapper -> wrapper.getVenue().getSearchTokens())  // Stream<List<String>>
                .flatMap(List::stream)                           // Stream<String>
                .map(String::toLowerCase)                        // lowercase each string
                .toList();                                       // collect into List<String>

        System.out.println("blackListIspSearchTokens:%s".formatted(blackListIspSearchTokens));
    }

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        IpApiResponse ipApiResponse = RequestContextHolder.get();
        if (ipApiResponse == null) {
            System.out.println(
                    "Internal System error instant of %s expected in filter class: %s"
                    .formatted(IpApiResponse.class.getSimpleName(),
                            IpBlockingFilter.class.getSimpleName()));

            httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal System error - IP information missing");
            return;
        }

        if (shouldBlockIp(ipApiResponse)) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Access is blocked: isp:%s or is blocked for country:%s"
                            .formatted(ipApiResponse.isp(), ipApiResponse.countryCode()));
            return;
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private boolean shouldBlockIp(IpApiResponse ipApiResponse) {
        if (!"success".equalsIgnoreCase(ipApiResponse.status())) return false;

        boolean blockIpByCountry = ipApiResponse.countryCode() != null
                && blackListService.getIsoCountries().contains(ipApiResponse.countryCode());

        boolean blockIpByISP = ipApiResponse.isp() != null
                        && blackListIspSearchTokens.stream()
                            .anyMatch(searchToken ->
                                    searchToken.contains(ipApiResponse.isp().toLowerCase()));

        if (blockIpByCountry || blockIpByISP)
            System.out.println(
                    "Access is blocked: isp:%s or is blocked for country:%s,"
                            .concat("blockIpByCountry:%s, blockIpByISP:%s")
                    .formatted(ipApiResponse.isp(), ipApiResponse.countryCode(),
                            blockIpByCountry, blockIpByISP));

        return blockIpByCountry || blockIpByISP;
    }
}
