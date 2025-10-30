package com.sc.sample.parsetojson.adapter.infrastructure.httprequest;


import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.sc.sample.parsetojson.adapter.infrastructure.filter.context.IpApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;


import static org.mockito.Mockito.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@WireMockTest(httpPort = 8088)
public class IpInfoServiceTest {

    private IpInfoService ipInfoService;

    @BeforeEach
    void setUp() {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:8088/json")
                .build();

        ipInfoService = new IpInfoService(restClient);
        WireMock.reset();
    }

    @Test
    void lookUpIp_shouldReturnMockedResponse() {
        String testIp = "10.156.20.1";
        String mockIpApiResponseJson = """
                {
                "query": "10.156.20.1",
                "status": "success",
                "countryCode": "US",
                "isp": "Cablevision Systems Corp.",
                "org": "AAA OF NEW JERSEY",
                "as": "AS6128 Cablevision Systems Corp."                           
                }
                """;
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/json/" + testIp))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockIpApiResponseJson)
                        .withStatus(HttpStatus.OK.value())));

        //test
        IpApiResponse response = ipInfoService.lookupIp(testIp);

        assertThat(response.query()).isEqualTo("10.156.20.1");
        assertThat(response.status()).isEqualTo("success");
        assertThat(response.countryCode()).isEqualTo("US");
        assertThat(response.isp()).isEqualTo("Cablevision Systems Corp.");
        assertThat(response.org()).isEqualTo("AAA OF NEW JERSEY");
        assertThat(response.as()).isEqualTo("AS6128 Cablevision Systems Corp.");
    }

    @Test
    void lookUpIp_shouldHandleThrowableGracefully() {
        String testIp = "7.8.7.8";

        //Simulate ip-api REST API service throwing an http error (500)
        WireMock.stubFor((WireMock.get(WireMock.urlEqualTo("/json/" + testIp))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withBody("Internal Server Error"))));

        // test
        IpApiResponse response = ipInfoService.lookupIp(testIp);
        //Assert
        assertThat(response.query()).isEqualTo(testIp);
        assertThat(response.status()).isEqualTo("fail");
        assertThat(response.countryCode()).isNull();
        assertThat(response.isp()).isNull();
        assertThat(response.org()).isNull();
        assertThat(response.as()).isNull();
    }

    @Test
    void refreshIpCache_shouldReturnCacheSize() {

        int noOfCacheElementsRefreshed = ipInfoService.refreshIpCache();
        assertThat(noOfCacheElementsRefreshed).isEqualTo(0);

        String testIp = "10.156.20.1";
        String mockIpApiResponseJson = """
                {
                "query": "10.156.20.1",
                "status": "success",
                "countryCode": "US",
                "isp": "Cablevision Systems Corp.",
                "org": "AAA OF NEW JERSEY",
                "as": "AS6128 Cablevision Systems Corp."                           
                }
                """;
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/json/" + testIp))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockIpApiResponseJson)
                        .withStatus(HttpStatus.OK.value())));

        //test
        IpApiResponse response = ipInfoService.lookupIp(testIp);

        noOfCacheElementsRefreshed = ipInfoService.refreshIpCache();
        assertThat(noOfCacheElementsRefreshed).isEqualTo(1);
    }
}
