package com.sc.sample.parsetojson.adapter.in.rest;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileUploadValidator implements Validator {

    static final int ONE_MEGA_BIT = 1024;

    @Value("${app.validation.file-upload-enabled:true}")
    private boolean validationEnabled;

    @Value("${app.validation.file-upload-max-size-mbs}")
    private int fileUploadMaxSizeMbs;

    final String uploadFileName = "EntryFile.txt";

    @Override
    public boolean supports(Class<?> clazz) {
        return FileUploadRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (!validationEnabled) {
            System.out.println("File upload validation is DISABLED.");
            return; // ✅ Skip validation entirely
        } else
            System.out.println(
                    "File upload validation is ENABLED. fileUploadMaxSizeMbs:%d"
                    .formatted(fileUploadMaxSizeMbs));


        FileUploadRequest request = (FileUploadRequest) target;
        MultipartFile file = request.getFile();

        if (file == null || file.isEmpty()) {
            errors.rejectValue(
                    "file",
                    "file.isEmpty",
                    "File does not exist or is empty");
            return;
        }
        System.out.println(
                "validate -> originalFileName:%s, isEmpty:%s, isReadable:%s, file size:%d"
                .formatted(file.getOriginalFilename(), file.isEmpty(),
                        file.getResource().isReadable(), file.getSize()));

        if (!file.getOriginalFilename().equalsIgnoreCase(uploadFileName)) {
            errors.rejectValue(
                    "file", "file.invalidFileName",
                    "Invalid upload File Name -> expected file name:%s"
                            .formatted(uploadFileName));
        }

        if (file.getSize() > fileUploadMaxSizeMbs * ONE_MEGA_BIT) {
            errors.rejectValue(
                    "file", "file.tooLarge",
                    "File exceeds Max file size %d KB limit"
                            .formatted(fileUploadMaxSizeMbs * ONE_MEGA_BIT));
        }
    }
}
