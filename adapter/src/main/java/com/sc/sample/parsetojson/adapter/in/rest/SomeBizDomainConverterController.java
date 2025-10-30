package com.sc.sample.parsetojson.adapter.in.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sc.sample.parsetojson.application.port.in.textconvert.FailedTextToDomainConversionException;
import com.sc.sample.parsetojson.application.port.in.textconvert.TextDataToDomainConverter;
import com.sc.sample.parsetojson.model.SomeBizDomain;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/somebizdomain")
public class SomeBizDomainConverterController {
    private static final String OUTPUT_JSON_FILE_NAME = "OutcomeFile.json";
    private final TextDataToDomainConverter<SomeBizDomain> someBizDomainConverter;
    private final ObjectMapper objectMapper;

    private final FileUploadValidator fileUploadValidator;

    public SomeBizDomainConverterController(
            TextDataToDomainConverter<SomeBizDomain> someBizDomainConverter,
            ObjectMapper objectMapper,
            FileUploadValidator fileUploadValidator) {
        this.someBizDomainConverter = someBizDomainConverter;
        this.objectMapper = objectMapper;
        this.fileUploadValidator = fileUploadValidator;
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(fileUploadValidator);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> upload(
            @Valid @ModelAttribute("file") FileUploadRequest fileUploadRequest,
            BindingResult uploadFileErrorResult) {

        if(checkFileUploadErrors(uploadFileErrorResult).isPresent()) {
            throw RestErrorHandler.restControllerException(
                    HttpStatus.BAD_REQUEST,
                    "Error in posting file upload :%s"
                            .formatted(uploadFileErrorResult.getAllErrors()));
        }

        List<String> readDataLines =
                readUploadedFile(fileUploadRequest.getFile());
        List<SomeBizDomainJsonView> jsonViewRecords =
                covertDataFileToDomain(readDataLines);
        return buildJsonFileResonseEntity(jsonViewRecords);
    }

    private Optional<ResponseEntity> checkFileUploadErrors(
            BindingResult uploadFileErrorResult) {
        if (uploadFileErrorResult.hasErrors()) {
            return Optional.of(ResponseEntity.badRequest().body(
                    uploadFileErrorResult.getAllErrors().stream()
                            .map(error -> error.getDefaultMessage())
                            .toList()));
        } else {
            return Optional.empty();
        }
    }

    private String domainToJsonString(SomeBizDomainJsonView jsonViewRecord) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(jsonViewRecord);
        } catch (JsonProcessingException e) {
            throw RestErrorHandler.restControllerException(
                    HttpStatus.BAD_REQUEST,
                    "Error in coverting domain to json String uuid:%s"
                            .formatted(String.valueOf(jsonViewRecord)));
        }
    }

    private List<String> readUploadedFile(MultipartFile dataFile) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        dataFile.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().toList();
        } catch (IOException e1) {
            throw RestErrorHandler.restControllerException(
                    HttpStatus.BAD_REQUEST, "Error in reading uploaded file");
        }
    }

    private List<SomeBizDomainJsonView> covertDataFileToDomain(
            List<String> textDataLines) {
        try {
            List<SomeBizDomain> records =
                    someBizDomainConverter.convertDataTextLines(textDataLines);

            System.out.println("covertDataFileToDomain ->" + records);

            List<SomeBizDomainJsonView> jsonViewRecords =
                    records.stream().map(SomeBizDomainJsonView::transitFrom)
                            .collect(Collectors.toList());

       //     System.out.println("covertDataFileToDomain ->" + jsonViewRecords);
            return jsonViewRecords;
        } catch (FailedTextToDomainConversionException e1) {
            throw RestErrorHandler.restControllerException(
                HttpStatus.BAD_REQUEST,
                "Error in parsing and converting text data lines to domain record");
        }
    }

    private ResponseEntity<byte[]> buildJsonFileResonseEntity(
            List<SomeBizDomainJsonView> jsonViewRecords) {
        try {
            String jsonString = jsonViewRecords.stream()
                    .parallel()
                    .map(this::domainToJsonString)
                    .collect(Collectors.joining(",\n"));

            System.out.println(jsonString);

            byte[] jsonBytes = jsonString.getBytes(StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDisposition(
                    ContentDisposition.attachment()
                            .filename(OUTPUT_JSON_FILE_NAME)
                            .build());
            return new ResponseEntity<>(jsonBytes, headers, HttpStatus.OK);
        } catch(Throwable ex) {
            throw RestErrorHandler.restControllerException(
                HttpStatus.BAD_REQUEST,
                "Error in building json string from model"
                        .concat("or failure in generating Json file Response Entity"));
        }
    }
}
