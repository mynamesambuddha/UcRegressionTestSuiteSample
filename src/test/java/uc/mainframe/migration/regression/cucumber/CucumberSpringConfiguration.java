package uc.mainframe.migration.regression.cucumber;

import org.springframework.boot.test.context.SpringBootTest;

import io.cucumber.spring.CucumberContextConfiguration;
import uc.mainframe.migration.regression.UcMigrationRegressionTestSuiteSampleApplication;

@CucumberContextConfiguration
@SpringBootTest(classes = UcMigrationRegressionTestSuiteSampleApplication.class, 
	webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CucumberSpringConfiguration {
}