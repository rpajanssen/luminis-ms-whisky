package luminis.whisky.core.consul;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConsulServiceConfigurationTest {
    private ConsulServiceConfiguration underTest;

    @Before
    public void setup() {
        underTest = new ConsulServiceConfiguration();
    }
    
    @Test
    public void should_return_default_uri() {
        assertEquals("http", underTest.getProtocol());
        assertEquals("localhost", underTest.getAddress());
        assertEquals(80, underTest.getPort());
    }

    @Test
    public void should_return_configured_uri() {
        underTest.withProtocol("https");
        underTest.withAddress("luminis");
        underTest.withPort(8080);

        assertEquals("https", underTest.getProtocol());
        assertEquals("luminis", underTest.getAddress());
        assertEquals(8080, underTest.getPort());
    }
}
