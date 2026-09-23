package uc.mainframe.migration.regression.cucumber.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.core.io.ClassPathResource;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import uc.mainframe.migration.regression.customer.CustomerListGenerator;
import uc.mainframe.migration.regression.customer.CustomerRecord;
import uc.mainframe.migration.regression.fetch.FetchService;

public class AssemblyFetchStepDefinitions {

    private static final String CUSTOMER_WORKBOOK_PATH =
            "test-data/customerInput.xlsx";

    private final FetchService fetchService;
    private final CustomerListGenerator customerListGenerator;

    private int expectedCustomerCount;
    private Path assemblyBuildFolder;

    public AssemblyFetchStepDefinitions(
            FetchService fetchService,
            CustomerListGenerator customerListGenerator) {

        this.fetchService = fetchService;
        this.customerListGenerator = customerListGenerator;
    }

    @Given("a valid customer workbook is available")
    public void validCustomerWorkbookIsAvailable()
            throws Exception {

        ClassPathResource workbookResource =
                new ClassPathResource(
                        CUSTOMER_WORKBOOK_PATH);

        assertTrue(
                workbookResource.exists(),
                "Customer workbook was not found");

        try (InputStream workbookInputStream =
                     workbookResource.getInputStream()) {

            List<CustomerRecord> customers =
                    customerListGenerator.generateCustomerList(
                            workbookInputStream);

            expectedCustomerCount = customers.size();
        }
    }

    @When("the Assembly Fetch operation is executed")
    public void assemblyFetchOperationIsExecuted()
            throws Exception {

        ClassPathResource workbookResource =
                new ClassPathResource(
                        CUSTOMER_WORKBOOK_PATH);

        try (InputStream workbookInputStream =
                     workbookResource.getInputStream()) {

            assemblyBuildFolder =
                    fetchService.fetchAssemblyResponses(
                            workbookInputStream);
        }
    }

    @Then("a new Assembly build folder should be created")
    public void assemblyBuildFolderShouldBeCreated() {

        assertTrue(
                Files.exists(assemblyBuildFolder),
                "Assembly build folder was not created");

        assertTrue(
                Files.isDirectory(assemblyBuildFolder),
                "Assembly build path is not a directory");

        assertTrue(
                assemblyBuildFolder
                        .getFileName()
                        .toString()
                        .startsWith("ASM_"),
                "Assembly build folder must start with ASM_");
    }

    @Then("an Assembly XML response should be saved for every customer")
    public void assemblyResponseShouldBeSavedForEveryCustomer()
            throws Exception {

        long xmlFileCount;

        try (Stream<Path> buildFiles =
                     Files.list(assemblyBuildFolder)) {

            xmlFileCount = buildFiles
                    .filter(Files::isRegularFile)
                    .filter(file -> file
                            .getFileName()
                            .toString()
                            .endsWith("_assembly.xml"))
                    .count();
        }

        assertEquals(
                expectedCustomerCount,
                xmlFileCount,
                "The number of Assembly XML files is incorrect");
    }
}
