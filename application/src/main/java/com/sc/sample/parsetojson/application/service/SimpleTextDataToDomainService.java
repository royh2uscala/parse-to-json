package com.sc.sample.parsetojson.application.service;

import com.sc.sample.parsetojson.application.port.in.textconvert.FailedTextToDomainConversionException;
import com.sc.sample.parsetojson.application.port.in.textconvert.TextDataToDomainConverter;
import com.sc.sample.parsetojson.model.SomeBizDomain;

import static com.sc.sample.parsetojson.model.SomeBizDomain.SomeDataFieldMapping.*;


import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

public class SimpleTextDataToDomainService implements TextDataToDomainConverter<SomeBizDomain> {
    static final String DELIMITER = "\\|";
    @Override
    public List<SomeBizDomain> convertDataTextLines(List<String> textLines)
            throws FailedTextToDomainConversionException {
        try {
            return textLines.stream()
                    .filter(line -> !line.isBlank())
                    .map(SimpleTextDataToDomainService::convertDataTextLine)

                    .collect(Collectors.toList());
        } catch(Throwable ex) {
            throw new FailedTextToDomainConversionException(ex);
        }
    }

    /**
     * 18148426-89e1-11ee-b9d1-0242ac120002|1X1D14|John Smith|Likes Apricots|Rides A Bike|6.2|12.1
     * 3ce2d17b-e66a-4c1e-bca3-40eb1c9222c7|2X2D24|Mike Smith|Likes Grape|Drives an SUV|35.0|95.5
     * 1afb6f5d-a7c2-4311-a92d-974f3180ff5e|3X3D35|Jenny Walters|Likes Avocados|Rides A Scooter|8.5|15.3
     * @param textLine
     * @return
     */
    private static SomeBizDomain convertDataTextLine(String textLine) {
            String[] dataFields = textLine.split(DELIMITER);
            return new SomeBizDomain(
                    UUID.fromString(dataFields[UU_ID.getIndex()]),
                    dataFields[ID.getIndex()],
                    dataFields[NAME.getIndex()],
                    dataFields[LIKES.getIndex()],
                    dataFields[TRANSPORT.getIndex()],
                    Double.parseDouble(dataFields[AVG_SPEED.getIndex()]),
                    Double.parseDouble(dataFields[TOP_SPEED.getIndex()]));
    }
}
