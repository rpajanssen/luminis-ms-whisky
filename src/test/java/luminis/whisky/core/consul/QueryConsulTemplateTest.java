package luminis.whisky.core.consul;

import luminis.whisky.client.ConsulClient;
import luminis.whisky.util.Service;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.StringUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QueryConsulTemplateTest {
    private final String baseUrl = "http://%s:%s/";

    private String consulHealthServiceResult = "[\n" +
            "  {\n" +
            "    \"Node\": {\n" +
            "      \"Node\": \"foobar\",\n" +
            "      \"Address\": \"10.1.10.12\"\n" +
            "    },\n" +
            "    \"Service\": {\n" +
            "      \"ID\": \"shipping\",\n" +
            "      \"Service\": \"shipping\",\n" +
            "      \"Tags\": null,\n" +
            "      \"Port\": 8000\n" +
            "    },\n" +
            "    \"Checks\": [\n" +
            "      {\n" +
            "        \"Node\": \"foobar\",\n" +
            "        \"CheckID\": \"shipping\",\n" +
            "        \"Name\": \"Shipping\",\n" +
            "        \"Status\": \"passing\",\n" +
            "        \"Notes\": \"\",\n" +
            "        \"Output\": \"\",\n" +
            "        \"ServiceID\": \"redis\",\n" +
            "        \"ServiceName\": \"redis\"\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "]";

    private String consulCatalogServiceResult = "[\n" +
            "  {\n" +
            "    \"Node\": \"foobar\",\n" +
            "    \"Address\": \"10.1.10.12\",\n" +
            "    \"ServiceID\": \"redis\",\n" +
            "    \"ServiceName\": \"redis\",\n" +
            "    \"ServiceTags\": null,\n" +
            "    \"ServiceAddress\": \"\",\n" +
            "    \"ServicePort\": 8000\n" +
            "  }\n" +
            "]";

    private String consulCatalogServiceWithServiceAddressResult = "[\n" +
            "  {\n" +
            "    \"Node\": \"foobar\",\n" +
            "    \"Address\": \"10.1.10.12\",\n" +
            "    \"ServiceID\": \"redis\",\n" +
            "    \"ServiceName\": \"redis\",\n" +
            "    \"ServiceTags\": null,\n" +
            "    \"ServiceAddress\": \"my.address\",\n" +
            "    \"ServicePort\": 8000\n" +
            "  }\n" +
            "]";

    private ConsulClient consulClient;
    private QueryConsulTemplate<String> underTest;

    @Before
    public void setup() {
        consulClient = mock(ConsulClient.class);
        when(consulClient.healthService(anyString())).then(invocationOnMock -> consulHealthServiceResult);
        when(consulClient.catalogService(anyString())).then(invocationOnMock -> consulCatalogServiceResult);

        underTest = new QueryConsulTemplate<String>(consulClient) {

            @Override
            protected String buildResult(String address, String serviceAddress, String servicePort) {
                return String.format(baseUrl, ifNotNullAElseB(serviceAddress, address), servicePort);
            }
        };
    }

    @Test
    public void should_return_consul_info() throws DyingServiceException {
        List<String> urls = underTest.queryConsulForServiceUrl(Service.SHIPPING.getServiceID());
        assertEquals("http://10.1.10.12:8000/", urls.get(0));
    }

    @Test
    public void should_return_consul_info_with_service_address() throws DyingServiceException {
        when(consulClient.catalogService(anyString())).then(invocationOnMock -> consulCatalogServiceWithServiceAddressResult);

        List<String> urls = underTest.queryConsulForServiceUrl(Service.SHIPPING.getServiceID());
        assertEquals("http://my.address:8000/", urls.get(0));
    }

    private String ifNotNullAElseB(String a, String b) {
        if(!StringUtils.isEmpty(a)) {
            return a;
        }

        return b;
    }
}
