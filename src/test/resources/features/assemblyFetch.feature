@assemblyFetch
Feature: Fetch Assembly customer responses

  Scenario: Capture Assembly responses using a valid customer workbook
  	Given a valid customer workbook is available
  	When the Assembly Fetch operation is executed
  	Then a new Assembly build folder should be created
  	And an Assembly XML response should be saved for every customer