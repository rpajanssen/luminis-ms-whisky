package luminis.whisky.core.consul;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConsulAgentConfigurationTest {
    private ConsulAgentConfiguration underTest;

    @Before
    public void setup() {
        underTest = ConsulAgentConfiguration.getInstance();
    }

    @Test
    public void should_return_default_uri() {
        assertEquals("http://localhost:8500", underTest.getBaseUriString());
    }

    @Test
    public void should_return_configured_uri() {
        underTest.setProtocol("https");
        underTest.setIp("luminis");
        underTest.setPort("8080");
        assertEquals("https://luminis:8080", underTest.getBaseUriString());
    }
}
