package uc.mainframe.migration.regression.fetch;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/fetch")
public class FetchController {

    private final FetchService fetchService;
    private final BuildZipCreator buildZipCreator;

    public FetchController(
            FetchService fetchService,
            BuildZipCreator buildZipCreator) {

        this.fetchService = fetchService;
        this.buildZipCreator = buildZipCreator;
    }

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> fetchAssemblyResponses(
            @RequestParam("customerWorkbook")
            MultipartFile customerWorkbook) throws Exception {

        if (customerWorkbook.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "status", "FAILED",
                            "message",
                            "Customer workbook is empty"));
        }

        try {
            Path assemblyBuildFolder =
                    fetchService.fetchAssemblyResponses(
                            customerWorkbook.getInputStream());

            String buildId = assemblyBuildFolder
                    .getFileName()
                    .toString();

            byte[] zipContent =
                    buildZipCreator.createZip(
                            assemblyBuildFolder);

            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(
                    MediaType.parseMediaType(
                            "application/zip"));

            headers.setContentDisposition(
                    ContentDisposition
                            .attachment()
                            .filename(buildId + ".zip")
                            .build());

            headers.set(
                    "X-Build-Id",
                    buildId);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(zipContent);

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "status", "FAILED",
                            "message",
                            exception.getMessage()));

        } catch (IOException exception) {
            return ResponseEntity
                    .internalServerError()
                    .body(
                            Map.of(
                                    "status", "FAILED",
                                    "message",
                                    "Fetch operation could not be completed"));
        }
    }
}