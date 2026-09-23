package uc.mainframe.migration.regression.soap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SoapRequestClient {

    private static final String ASSEMBLY = "ASM";
    private static final String COBOL = "COB";

    private final RestClient restClient;
    private final String soapUrl;
    private final String assemblyNamespace;
    private final String cobolNamespace;

    public SoapRequestClient(
            RestClient.Builder restClientBuilder,
            @Value("${regression.soap.url}")
            String soapUrl,
            @Value("${regression.soap.assembly.namespace}")
            String assemblyNamespace,
            @Value("${regression.soap.cobol.namespace}")
            String cobolNamespace) {

        this.restClient = restClientBuilder.build();
        this.soapUrl = soapUrl;
        this.assemblyNamespace = assemblyNamespace;
        this.cobolNamespace = cobolNamespace;
    }

    public String getCustomerResponse(
            String systemPrefix,
            String customerId,
            String accessToken) {

        String namespace = getNamespace(systemPrefix);

        String soapRequest = createSoapRequest(
                namespace,
                customerId);

        String soapResponse = restClient
                .post()
                .uri(soapUrl)
                .contentType(MediaType.TEXT_XML)
                .header(
                        "Authorization",
                        "Bearer " + accessToken)
                .body(soapRequest)
                .retrieve()
                .body(String.class);

        if (soapResponse == null || soapResponse.isBlank()) {
            throw new IllegalStateException(
                    systemPrefix
                            + " SOAP service returned no response");
        }

        return soapResponse;
    }

    private String getNamespace(String systemPrefix) {
        if (ASSEMBLY.equalsIgnoreCase(systemPrefix)) {
            return assemblyNamespace;
        }

        if (COBOL.equalsIgnoreCase(systemPrefix)) {
            return cobolNamespace;
        }

        throw new IllegalArgumentException(
                "System prefix must be ASM or COB");
    }

    private String createSoapRequest(
            String namespace,
            String customerId) {

        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <soapenv:Envelope
                        xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:customer="%s">
                    <soapenv:Header/>
                    <soapenv:Body>
                        <customer:getCustomerRequest>
                            <customer:customerId>%s</customer:customerId>
                        </customer:getCustomerRequest>
                    </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(namespace, customerId);
    }
}