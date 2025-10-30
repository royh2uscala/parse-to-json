package com.sc.sample.parsetojson.adapter.in.rest;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sc.sample.parsetojson.application.port.in.textconvert.TextDataToDomainConverter;
import com.sc.sample.parsetojson.model.SomeBizDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;


public class DomainConvertControllerTest {
    static final String TEST_LINE_01 =
            "18148426-89e1-11ee-b9d1-0242ac120002|1X1D14|John Smith|Likes Apricots|Rides A Bike|6.2|12.1";
    static final String TEST_LINE_02 =
            "3ce2d17b-e66a-4c1e-bca3-40eb1c9222c7|2X2D24|Mike Smith|Likes Grape|Drives an SUV|35.0|95.5";
    static final String TEST_LINE_03 =
            "1afb6f5d-a7c2-4311-a92d-974f3180ff5e|3X3D35|Jenny Walters|Likes Avocados|Rides A Scooter|8.5|15.3";
    static final String TEST_BLANK_LINE = "";

    private MockMvc mockMvc;

    private TextDataToDomainConverter<SomeBizDomain> domainConverter;
    private DomainConvertController controller;

    private FileUploadValidator validator;

    @BeforeEach
    void setUp() {
        domainConverter = mock(TextDataToDomainConverter.class);
        validator = mock(FileUploadValidator.class);
        controller = new DomainConvertController(
                domainConverter, new ObjectMapper (), validator);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setValidator(validator)
                .build();
    }

    @Test
    void shouldReturnJsonFileResponse_WhenUploadIsSuccessful() throws Exception {
        String fileContent = TEST_LINE_01.concat("\n").concat(TEST_LINE_02);
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "file", "EntryFile.txt",
                "text/plain", fileContent.getBytes()
        );
        List<SomeBizDomain> domains = List.of(
                new SomeBizDomain(UUID.randomUUID(), "1X1D14", "John Smith",
                        "Likes Apricots", "Rides A Bike",
                        6.2D, 12.1D),
                new SomeBizDomain(UUID.randomUUID(), "2X2D24", "Mike Smith",
                        "Likes Grape", "Drives an SUV",
                        35.0D, 95.5D));

        when(domainConverter.convertDataTextLines(List.of(TEST_LINE_01, TEST_LINE_02)))
                .thenReturn(domains);
        MvcResult mvcResult = mockMvc.perform(multipart("/api/somebizdomain/upload")
                        .file(mockMultipartFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"OutcomeFile.json\""))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON.toString()))
                .andReturn();

        byte[] responseBytes = mvcResult.getResponse().getContentAsByteArray();
        String jsonResponse = new String(responseBytes, StandardCharsets.UTF_8);
        long countOfDomainRecordNotFound = domains.stream().filter(domain -> {
            boolean jsonResponseContainsDomain =
                    jsonResponse.contains(domain.name())
                            && jsonResponse.contains(domain.transport())
                            && jsonResponse.contains(String.valueOf(domain.topSpeed()));
            return !jsonResponseContainsDomain;
        }).count();

        System.out.println("countOfDomainRecordNotFound=" +countOfDomainRecordNotFound);
        assertThat(countOfDomainRecordNotFound).isEqualTo(0);
    }
}
