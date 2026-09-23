package uc.mainframe.migration.regression.fetch;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.springframework.stereotype.Service;

import uc.mainframe.migration.regression.authentication.JwtTokenClient;
import uc.mainframe.migration.regression.common.BuildFolderCreator;
import uc.mainframe.migration.regression.customer.CustomerListGenerator;
import uc.mainframe.migration.regression.customer.CustomerRecord;
import uc.mainframe.migration.regression.soap.SoapRequestClient;

@Service
public class FetchService {

    private static final String ASSEMBLY_SYSTEM_PREFIX = "ASM";
    private static final String ASSEMBLY_FILE_SUFFIX = "_assembly.xml";

    private final CustomerListGenerator customerListGenerator;
    private final BuildFolderCreator buildFolderCreator;
    private final JwtTokenClient jwtTokenClient;
    private final SoapRequestClient soapRequestClient;

    public FetchService(
            CustomerListGenerator customerListGenerator,
            BuildFolderCreator buildFolderCreator,
            JwtTokenClient jwtTokenClient,
            SoapRequestClient soapRequestClient) {

        this.customerListGenerator = customerListGenerator;
        this.buildFolderCreator = buildFolderCreator;
        this.jwtTokenClient = jwtTokenClient;
        this.soapRequestClient = soapRequestClient;
    }

    public Path fetchAssemblyResponses(
            InputStream workbookInputStream) throws Exception {

        List<CustomerRecord> customers =
                customerListGenerator.generateCustomerList(
                        workbookInputStream);

        String accessToken =
                jwtTokenClient.getAccessToken();

        Path assemblyBuildFolder =
                buildFolderCreator.createBuildFolder(
                        ASSEMBLY_SYSTEM_PREFIX);

        for (CustomerRecord customer : customers) {

            String assemblyResponse =
                    soapRequestClient.getCustomerResponse(
                            ASSEMBLY_SYSTEM_PREFIX,
                            customer.customerId(),
                            accessToken);

            saveAssemblyResponse(
                    assemblyBuildFolder,
                    customer.customerId(),
                    assemblyResponse);
        }

        System.out.println(
                "Assembly responses saved in: "
                        + assemblyBuildFolder);

        return assemblyBuildFolder;
    }

    private void saveAssemblyResponse(
            Path assemblyBuildFolder,
            String customerId,
            String assemblyResponse) throws IOException {

        String responseFileName =
                customerId + ASSEMBLY_FILE_SUFFIX;

        Path responseFile =
                assemblyBuildFolder.resolve(responseFileName);

        Files.writeString(
                responseFile,
                assemblyResponse,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW);
    }
}