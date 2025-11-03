package com.sc.sample.parsetojson.bootstrap.end2end;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Fault;
import com.sc.sample.parsetojson.adapter.infrastructure.filter.context.IpApiResponse;
import com.sc.sample.parsetojson.adapter.infrastructure.filter.context.RequestContextHolder;
import com.sc.sample.parsetojson.adapter.infrastructure.httprequest.HttpRequestInfo;
import com.sc.sample.parsetojson.adapter.infrastructure.httprequest.RequestInfoBuilderFilter;
import com.sc.sample.parsetojson.adapter.out.persistence.httprequest.RequestInfoRepository;
import io.restassured.RestAssured;
import io.restassured.response.ResponseBody;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.net.ConnectException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("dev.integrationtest") //use application-dev.integrationtest.yml
public class IntegrationTest {
    @LocalServerPort() private int port;
    static final int IP_IPA_REST_ADDRESS_PORT = 8089;

    static final String UPLOAD_FILE_REST_URI = "/api/somebizdomain/upload";
    static final Path uploadFileDirPath  = Paths.get("src/test/resources");
    static final ObjectMapper objectMapper = new ObjectMapper();

    // WireMock container (optional) - or you can run standalone via @WireMockTest
    private static WireMockServer wireMockServer;

    @Autowired JdbcTemplate jdbcTemplate;

    @Autowired private RequestInfoRepository requestInfoRepository;

    //Postgresql Testcontainer
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("requestinfodb")
            .withUsername("devco01")
            .withPassword("devco01");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeAll
    static void setupWireMock() {
//        wireMockServer = new WireMockServer(IP_IPA_REST_ADDRESS_PORT);
//        wireMockServer.start();
//        WireMock.configureFor("localhost", IP_IPA_REST_ADDRESS_PORT);

    }

    @BeforeEach
    void beforeEach() {
        RequestContextHolder.clear();
        jdbcTemplate.execute("TRUNCATE TABLE request_info RESTART IDENTITY CASCADE");

        wireMockServer = new WireMockServer(IP_IPA_REST_ADDRESS_PORT);
        wireMockServer.start();
        WireMock.configureFor("localhost", IP_IPA_REST_ADDRESS_PORT);
    }
    @AfterEach
    void afterEach() {
        RequestContextHolder.clear();
        wireMockServer.stop();
        RequestContextHolder.clear();
    }

    @AfterAll
    static void teardownWireMock() {
//        wireMockServer.stop();
//        RequestContextHolder.clear();
    }

    static void mockIpIpaResponse(String jsonIpIpaResponse) {
        // mock IP info REST API response
        wireMockServer.stubFor(WireMock.get(WireMock.urlMatching("/json/.*"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonIpIpaResponse)
                        .withStatus(200)));
    }

    private static IpApiResponse whiteListIpApiResponse(String requestorsIP) {
        IpApiResponse ip = new IpApiResponse(requestorsIP, "success",
                "Canada", "CA", "Le Groupe Videotron Ltee",
                "Videotron Ltee", "AS5769 Videotron Ltee");
        return ip;
    }

    @Test
    void testWhiteListSuccess() throws JsonProcessingException, ConnectException {
        String requestorsIP = "16.32.0.1";
        IpApiResponse whiteList = whiteListIpApiResponse(requestorsIP);
        mockIpIpaResponse(objectMapper.writeValueAsString(whiteList));


        String uploadFilePath01 = uploadFileDirPath.toString() + "/" + "EntryFile.txt";
        File file = new File(uploadFilePath01);

        assertThat(file.exists());
        System.out.println("random port=" + port);


        ResponseBody responseBody = RestAssured.given()
                .port(port)
                .header(RequestInfoBuilderFilter.X_FORWARDED_FOR, requestorsIP)
                .multiPart("file", file)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .post(UPLOAD_FILE_REST_URI)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .response().body();

        String prettyString = responseBody.asPrettyString();
        System.out.println("prettyString=" + prettyString);
        List<HttpRequestInfo> requestInfos = requestInfoRepository.findAll();
        assertThat(requestInfos.size()).isEqualTo(1);
        HttpRequestInfo httpRequestInfo = requestInfos.get(0);
        assertThat(httpRequestInfo).isNotNull();
        assertThat(httpRequestInfo.getRequestUri()).isEqualTo(UPLOAD_FILE_REST_URI);
        assertThat(httpRequestInfo.getHttpResponseCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(httpRequestInfo.getRequesterCountryCode()).isEqualTo(whiteList.countryCode());
        assertThat(httpRequestInfo.getRequesterIp()).isEqualTo(whiteList.query());
    }

    private IpApiResponse blackListedByCountry(String ipRequestIp) {
        IpApiResponse ip = new IpApiResponse(ipRequestIp, "success",
                "United States", "US", "Cablevision Systems Corp.",
                "AAA OF NEW JERSEY", "AS6128 Cablevision Systems Corp.");
        return ip;
    }

    @Test
    void testBlockedBlackListByCountryRequestSaveUserRequest() throws JsonProcessingException, ConnectException {
        String requestorsIP = "67.20.103.6";
        IpApiResponse blackListedByCountry = blackListedByCountry(requestorsIP);

        mockIpIpaResponse(objectMapper.writeValueAsString(blackListedByCountry));


        String uploadFilePath01 = uploadFileDirPath.toString() + "/" + "EntryFile.txt";
        File file = new File(uploadFilePath01);

        assertThat(file.exists());
        System.out.println("random port=" + port);


        ResponseBody responseBody = RestAssured.given()
                .port(port)
                .header(RequestInfoBuilderFilter.X_FORWARDED_FOR, requestorsIP)
                .multiPart("file", file)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .post(UPLOAD_FILE_REST_URI)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .extract()
                .response().body();

        String prettyString = responseBody.asPrettyString();
        System.out.println("prettyString=" + prettyString);
        List<HttpRequestInfo> requestInfos = requestInfoRepository.findAll();
        assertThat(requestInfos.size()).isEqualTo(1);
        HttpRequestInfo httpRequestInfo = requestInfos.get(0);
        assertThat(httpRequestInfo).isNotNull();
        assertThat(httpRequestInfo.getRequestUri()).isEqualTo(UPLOAD_FILE_REST_URI);
        assertThat(httpRequestInfo.getHttpResponseCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(httpRequestInfo.getRequesterCountryCode()).isEqualTo(blackListedByCountry.countryCode());
        assertThat(httpRequestInfo.getRequesterIp()).isEqualTo(blackListedByCountry.query());
    }


    private IpApiResponse blackListedByISP(String ipRequestIp) {
        IpApiResponse ip = new IpApiResponse(ipRequestIp, "success",
                "Canada", "CA", "Google Cloud Provider",
                "Google Cloud Corp", "Google Cloud CA.");
        return ip;
    }

    @Test
    void testBlockedBlackListByISPRequestSaveUserRequest() throws JsonProcessingException, ConnectException {
        String requestorsIP = "18.26.0.14";
        IpApiResponse blackListedByISP = blackListedByISP(requestorsIP);
        mockIpIpaResponse(objectMapper.writeValueAsString(blackListedByISP));

        String uploadFilePath01 = uploadFileDirPath.toString() + "/" + "EntryFile.txt";
        File file = new File(uploadFilePath01);

        assertThat(file.exists());
        System.out.println("random port=" + port);

        ResponseBody responseBody = RestAssured.given()
                .port(port)
                .header(RequestInfoBuilderFilter.X_FORWARDED_FOR, requestorsIP)
                .multiPart("file", file)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .post(UPLOAD_FILE_REST_URI)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .extract()
                .response().body();

        String prettyString = responseBody.asPrettyString();
        System.out.println("prettyString=" + prettyString);
        List<HttpRequestInfo> requestInfos = requestInfoRepository.findAll();
        assertThat(requestInfos.size()).isEqualTo(1);
        HttpRequestInfo httpRequestInfo = requestInfos.get(0);
        assertThat(httpRequestInfo).isNotNull();
        assertThat(httpRequestInfo.getRequestUri()).isEqualTo(UPLOAD_FILE_REST_URI);
        assertThat(httpRequestInfo.getHttpResponseCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(httpRequestInfo.getRequesterCountryCode()).isEqualTo(blackListedByISP.countryCode());
        assertThat(httpRequestInfo.getIspProviderIp()).isEqualTo(blackListedByISP.isp());
        assertThat(httpRequestInfo.getRequesterIp()).isEqualTo(blackListedByISP.query());
    }

    private IpApiResponse failedQueryIpApiResponse(String requestorsIP) {
        return new IpApiResponse(requestorsIP, "fail",
                null, null, null, null, null);
    }

//    @Test
    void testFailedIpIpaQuery() throws JsonProcessingException, ConnectException {
        wireMockServer.stubFor(WireMock.get(WireMock.urlMatching("/json/.*"))
                .willReturn(WireMock.aResponse()
                        .withFault(Fault.CONNECTION_RESET_BY_PEER)));

        //mockIpIpaResponse(objectMapper.writeValueAsString(failedQueryIpApiResponse));
        String requestorsIP = "16.35.0.2";
        IpApiResponse failedQueryIpApiResponse = failedQueryIpApiResponse(requestorsIP);

        String uploadFilePath01 = uploadFileDirPath.toString() + "/" + "EntryFile.txt";
        File file = new File(uploadFilePath01);

        assertThat(file.exists());
        System.out.println("random port=" + port);

        ResponseBody responseBody = RestAssured.given()
                .port(port)
                .header(RequestInfoBuilderFilter.X_FORWARDED_FOR, requestorsIP)
                .multiPart("file", file)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .post(UPLOAD_FILE_REST_URI)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .response().body();

        String prettyString = responseBody.asPrettyString();
        System.out.println("prettyString=" + prettyString);

        List<HttpRequestInfo> requestInfos = requestInfoRepository.findAll();
        assertThat(requestInfos.size()).isEqualTo(1);
        HttpRequestInfo httpRequestInfo = requestInfos.get(0);
        assertThat(httpRequestInfo).isNotNull();
        assertThat(httpRequestInfo.getRequestUri()).isEqualTo(UPLOAD_FILE_REST_URI);
        assertThat(httpRequestInfo.getHttpResponseCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(httpRequestInfo.getRequesterCountryCode()).isNull();
        assertThat(httpRequestInfo.getIspProviderIp()).isNull();
        assertThat(httpRequestInfo.getRequesterIp()).isEqualTo(failedQueryIpApiResponse.query());
    }
}
