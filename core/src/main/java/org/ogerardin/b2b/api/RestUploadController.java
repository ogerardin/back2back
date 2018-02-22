package org.ogerardin.b2b.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class RestUploadController {

    private final Logger logger = LoggerFactory.getLogger(RestUploadController.class);

    //Save the uploaded file to this folder
    private static final String UPLOADED_FOLDER = "temp-upload";

    static {
        try {
            Files.createDirectories(Paths.get(UPLOADED_FOLDER));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Single file upload
    @PostMapping("/api/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile uploadfile) {

        logger.debug("Single file upload!");

        if (uploadfile.isEmpty()) {
            return new ResponseEntity<>("Empty file", HttpStatus.BAD_REQUEST);
        }

        try {
            saveUploadedFiles(Collections.singletonList(uploadfile));
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("Successfully uploaded - " + uploadfile.getOriginalFilename(),
                new HttpHeaders(), HttpStatus.OK);

    }

    // Multiple file upload
    @PostMapping("/api/upload/multi")
    public ResponseEntity<?> uploadFileMulti(
            @RequestParam("extraField") String extraField,
            @RequestParam("files") MultipartFile[] uploadfiles) {

        logger.debug("Multiple file upload!");

        // Get file name
        String uploadedFileName = Arrays.stream(uploadfiles).map(MultipartFile::getOriginalFilename)
                .filter(x -> !StringUtils.isEmpty(x)).collect(Collectors.joining(" , "));

        if (StringUtils.isEmpty(uploadedFileName)) {
            return new ResponseEntity<>("please select a file!", HttpStatus.BAD_REQUEST);
        }

        try {

            saveUploadedFiles(Arrays.asList(uploadfiles));

        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("Successfully uploaded - "
                + uploadedFileName, HttpStatus.OK);

    }

/*
    // maps html form to a Model
    @PostMapping("/api/upload/multi/model")
    public ResponseEntity<?> multiUploadFileModel(@ModelAttribute UploadModel model) {

        logger.debug("Multiple file upload! With UploadModel");

        try {

            saveUploadedFiles(Arrays.asList(model.getFiles()));

        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity("Successfully uploaded!", HttpStatus.OK);

    }
*/

    //save file
    private void saveUploadedFiles(List<MultipartFile> files) throws IOException {

        for (MultipartFile file : files) {

            if (file.isEmpty()) {
                continue; //next pls
            }

            byte[] bytes = file.getBytes();
            Path path = Paths.get(UPLOADED_FOLDER, file.getOriginalFilename());
            Files.write(path, bytes);

        }

    }
}