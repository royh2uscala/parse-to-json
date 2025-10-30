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
   static final String MISSING_IP_INFO_ERROR_MSG =
           "Internal System error - IP information missing";
   static final String BLOCKED_REQUEST_BY_COUNTRY_ERROR_MGS =
           "Access is blocked for country:%s";
    static final String BLOCKED_REQUEST_BY_ISP_ERROR_MGS =
            "Access is blocked for isp:%s";
    static final String BLOCKED_REQUEST_BY_COUNTRY_AND_ISP_ERROR_MGS =
            "Access is blocked for country:%s and for isp:%s";
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
                    MISSING_IP_INFO_ERROR_MSG);
            return;
        }

        BlockStatus blockStatus = blockIpStatus(ipApiResponse);

        if (BlockStatus.OK == blockStatus || BlockStatus.IP_QUERY_FAILED == blockStatus ) {
            System.out.println("ok - requestor IP is allowed blockStatus:%s"
                    .formatted(blockStatus));
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        } else {
            String errorMsg = switch (blockStatus) {
                case BLOCKED_BY_BOTH -> BLOCKED_REQUEST_BY_COUNTRY_AND_ISP_ERROR_MGS
                        .formatted(ipApiResponse.countryCode(), ipApiResponse.isp());
                case BLOCKED_BY_ISP -> BLOCKED_REQUEST_BY_ISP_ERROR_MGS
                        .formatted(ipApiResponse.isp());
                case BLOCK_BY_COUNTRY -> BLOCKED_REQUEST_BY_COUNTRY_ERROR_MGS
                        .formatted(ipApiResponse.countryCode());

                case OK, IP_QUERY_FAILED -> "";
            };
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, errorMsg);
            return;
        }
    }

    private BlockStatus blockIpStatus(IpApiResponse ipApiResponse) {
        if (!"success".equalsIgnoreCase(ipApiResponse.status()))
            return BlockStatus.IP_QUERY_FAILED;

        BlockStatus blockStatus = null;
        boolean blockIpByCountry = ipApiResponse.countryCode() != null
                && blackListService.getIsoCountries().contains(ipApiResponse.countryCode());

        boolean blockIpByISP = ipApiResponse.isp() != null
                        && blackListIspSearchTokens.stream()
                            .anyMatch(searchToken ->
                                    searchToken.contains(ipApiResponse.isp().toLowerCase()));

        if (blockIpByCountry && blockIpByISP)
            return BlockStatus.BLOCKED_BY_BOTH;
        if (blockIpByCountry)
            return BlockStatus.BLOCK_BY_COUNTRY;
        if (blockIpByISP)
            return BlockStatus.BLOCKED_BY_ISP;
        else
            return BlockStatus.OK;

//        if (blockIpByCountry || blockIpByISP)
//            System.out.println(
//                    "Access is blocked: isp:%s or is blocked for country:%s,"
//                            .concat("blockIpByCountry:%s, blockIpByISP:%s")
//                    .formatted(ipApiResponse.isp(), ipApiResponse.countryCode(),
//                            blockIpByCountry, blockIpByISP));
//
//        return blockIpByCountry || blockIpByISP;
    }

    private enum BlockStatus {BLOCK_BY_COUNTRY, BLOCKED_BY_ISP, BLOCKED_BY_BOTH, OK, IP_QUERY_FAILED}
}
