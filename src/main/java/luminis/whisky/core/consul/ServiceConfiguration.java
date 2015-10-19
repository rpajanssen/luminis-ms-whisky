package luminis.whisky.core.consul;

public class ServiceConfiguration {
    private String protocol = "http";
    private String address = "localhost";
    private int port = 80;

    ServiceConfiguration withProtocol(String protocol) {
        this.protocol = protocol;

        return this;
    }

    ServiceConfiguration withAddress(String address) {
        this.address = address;

        return this;
    }

    ServiceConfiguration withPort(int port) {
        this.port = port;

        return this;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "ServiceConfiguration{" +
                "protocol='" + protocol + '\'' +
                ", address='" + address + '\'' +
                ", port=" + port +
                '}';
    }
}
