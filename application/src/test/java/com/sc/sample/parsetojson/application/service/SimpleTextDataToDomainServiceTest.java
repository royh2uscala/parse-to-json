package com.sc.sample.parsetojson.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sc.sample.parsetojson.application.port.in.textconvert.FailedTextToDomainConversionException;
import com.sc.sample.parsetojson.model.SomeBizDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SimpleTextDataToDomainServiceTest {
    static final String TEST_LINE_01 =
            "18148426-89e1-11ee-b9d1-0242ac120002|1X1D14|John Smith|Likes Apricots|Rides A Bike|6.2|12.1";
    static final String TEST_LINE_02 =
            "3ce2d17b-e66a-4c1e-bca3-40eb1c9222c7|2X2D24|Mike Smith|Likes Grape|Drives an SUV|35.0|95.5";
    static final String TEST_LINE_03 =
            "1afb6f5d-a7c2-4311-a92d-974f3180ff5e|3X3D35|Jenny Walters|Likes Avocados|Rides A Scooter|8.5|15.3";
    static final String TEST_BLANK_LINE = "";
    final SimpleTextDataToDomainService sut = new SimpleTextDataToDomainService();

    @Test
    void convertDataTextLinesSuccess() throws FailedTextToDomainConversionException {
        List<String> textDataLines = List.of(TEST_LINE_01, TEST_LINE_02, TEST_BLANK_LINE, TEST_LINE_03);
        int expectedRecordSize = 3;
        List<SomeBizDomain> records = sut.convertDataTextLines(textDataLines);
        assertThat(expectedRecordSize).isNotNull().isEqualTo(records.size());
        System.out.println(records);

    }


}
