package luminis.whisky.core.consul;

public final class ConsulAgentConfiguration {
    private static final ConsulAgentConfiguration instance = new ConsulAgentConfiguration();

    private static final String CONSUL_BASE_URI = "%s://%s:%s";

    private String protocol = "http";
    private String ip = "localhost";
    private String port = "8500";

    private ConsulAgentConfiguration() {}

    public static ConsulAgentConfiguration getInstance() {
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

    public String getBaseUriString() {
        return String.format(CONSUL_BASE_URI, getProtocol(), getIp(), getPort());
    }
}
