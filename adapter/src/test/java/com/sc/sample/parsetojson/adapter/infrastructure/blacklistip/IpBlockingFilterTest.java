package com.sc.sample.parsetojson.adapter.infrastructure.blacklistip;

import com.sc.sample.parsetojson.adapter.infrastructure.filter.context.IpApiResponse;
import com.sc.sample.parsetojson.adapter.infrastructure.filter.context.RequestContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.sc.sample.parsetojson.adapter.infrastructure.blacklistip.BlackListGeoConfig.IspDataCenterVenueWrapper;
import static com.sc.sample.parsetojson.adapter.infrastructure.blacklistip.BlackListGeoConfig.Venue;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

public class IpBlockingFilterTest {
    private BlackListGeoConfig blackListGeoConfig;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;
    private IpBlockingFilter filter;

    @BeforeEach
    void setUp() {
        blackListGeoConfig = buildDefaultBlackListConfig();
        filter  = new IpBlockingFilter(blackListGeoConfig);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
        RequestContextHolder.clear();
    }
    @AfterEach
    void tearDown() {
        RequestContextHolder.clear();
    }

    @Test
    void doFilter_willAcceptRequestAndReturnOk() throws ServletException, IOException {
        IpApiResponse whiteListIpApiResponse = whiteListIpApiResponse();
        RequestContextHolder.set(whiteListIpApiResponse);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }
    @Test
    void doFilter_willAcceptRequest_for_FailedIpQueryAndReturnOk() throws ServletException, IOException {
        IpApiResponse failedQueryIpApiResponse = failedQueryIpApiResponse();
        RequestContextHolder.set(failedQueryIpApiResponse);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void doFilter_willBlockRequestByCountryAndReturnSC_FORBIDDEN() throws ServletException, IOException {
        IpApiResponse blackListedIpApiResponse = blackListedByCountry();
        assertThat(blackListGeoConfig.getIsoCountries()
                .contains(blackListedIpApiResponse.countryCode()));

  //      assertThat(blackListGeoConfig.ispDataVenueBlackListed(venue));

        RequestContextHolder.set(blackListedIpApiResponse);

        filter.doFilter(request, response, chain);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN),
                eq(IpBlockingFilter.BLOCKED_REQUEST_BY_COUNTRY_ERROR_MGS
                        .formatted(blackListedIpApiResponse.countryCode())));
        verify(chain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    void doFilter_willBlockRequestByISPAndReturnSC_FORBIDDEN() throws ServletException, IOException {
        IpApiResponse blackListedByISP = blackListedByISP();
//        assertThat(blackListGeoConfig.getIsoCountries()
//                .contains(blackListedIpApiResponse.countryCode()));

        //      assertThat(blackListGeoConfig.ispDataVenueBlackListed(venue));

        RequestContextHolder.set(blackListedByISP);

        filter.doFilter(request, response, chain);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN),
                eq(IpBlockingFilter.BLOCKED_REQUEST_BY_ISP_ERROR_MGS
                        .formatted(blackListedByISP.isp())));
        verify(chain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    void doFilter_willBlockRequestByISPAndCountryAndReturnSC_FORBIDDEN()
            throws ServletException, IOException {
        IpApiResponse blackListedByISPAndCountry = blackListedByISPAndCountry();
//        assertThat(blackListGeoConfig.getIsoCountries()
//                .contains(blackListedIpApiResponse.countryCode()));

        //      assertThat(blackListGeoConfig.ispDataVenueBlackListed(venue));

        RequestContextHolder.set(blackListedByISPAndCountry);

        filter.doFilter(request, response, chain);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN),
                eq(IpBlockingFilter.BLOCKED_REQUEST_BY_COUNTRY_AND_ISP_ERROR_MGS
                        .formatted(
                                blackListedByISPAndCountry.countryCode(),
                                blackListedByISPAndCountry.isp())));
        verify(chain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }
    @Test
    void doFilter_willFailDueNullIpAPIResponseAndReturnInternalSystemErrorResponse()
            throws ServletException, IOException {
        RequestContextHolder.set(null);
        filter.doFilter(request, response, chain);

        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
                eq(IpBlockingFilter.MISSING_IP_INFO_ERROR_MSG));
        verify(chain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    private IpApiResponse failedQueryIpApiResponse() {
        return new IpApiResponse("16.35.0.2", "fail",
                null, null, null, null, null);
    }

    private IpApiResponse whiteListIpApiResponse() {
        IpApiResponse ip = new IpApiResponse("16.32.0.1", "success",
                "Canada", "CA", "Le Groupe Videotron Ltee",
                "Videotron Ltee", "AS5769 Videotron Ltee");
        return ip;
    }

    private IpApiResponse blackListedByCountry() {
        IpApiResponse ip = new IpApiResponse("67.20.103.6", "success",
                "United States", "US", "Cablevision Systems Corp.",
                "AAA OF NEW JERSEY", "AS6128 Cablevision Systems Corp.");
        return ip;
    }

    private IpApiResponse blackListedByISP() {
        IpApiResponse ip = new IpApiResponse("18.26.0.14", "success",
                "Canada", "CA", "Google Cloud Provider",
                "Google Cloud Corp", "Google Cloud CA.");
        return ip;
    }

    private IpApiResponse blackListedByISPAndCountry() {
        IpApiResponse ip = new IpApiResponse("11.23.0.19", "success",
                "United States", "US", "Azure Microsoft",
                "Azure Microsoft Corp", "Azure Microsoft US.");
        return ip;
    }

    private BlackListGeoConfig buildDefaultBlackListConfig() {
        BlackListGeoConfig bl = new BlackListGeoConfig();
        bl.setIsoCountries(List.of("ES", "US", "CN"));
        bl.setIspDataCenterVenues(buildDefaultBlackListedVenues());
        return bl;
    }

    private List<IspDataCenterVenueWrapper> buildDefaultBlackListedVenues() {
        List<IspDataCenterVenueWrapper> venues = new ArrayList<>();
        venues.add(buildVenue("AWS", List.of("AWS", "Amazon", "Amazon Web Service")));
        venues.add(buildVenue("GCP", List.of("GCP", "Google", "Google Cloud Provider")));
        venues.add(buildVenue("AZURE", List.of("AZURE", "Azure Microsoft", "Microsoft Azure")));
        return venues;
    }

    private IspDataCenterVenueWrapper buildVenue(
            String code, List<String> searchTokens) {
        return new IspDataCenterVenueWrapper(new Venue(code, searchTokens));
    }

}
