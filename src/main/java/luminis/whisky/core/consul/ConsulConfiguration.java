package luminis.whisky.core.consul;


import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public final class ConsulConfiguration {
    private static final ConsulConfiguration instance = new ConsulConfiguration();

    private static final String CONSUL_BASE_URI = "%s://%s:%s";

    private String protocol = "http";
    private String ip = "localhost";
    private String port = "8500";

    private ConsulConfiguration() {}

    public static ConsulConfiguration getInstance() {
        return instance;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public URI getBaseURI() {
        return UriBuilder.fromUri(String.format(CONSUL_BASE_URI, getProtocol(), getIp(), getPort())).build();
    }
}
