package luminis.whisky.client;

import luminis.whisky.core.consul.ConsulAgentConfiguration;
import org.glassfish.jersey.client.ClientConfig;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

public class ConsulClient {
    private static final String CONSUL_API_CATALOG_SERVICES = "/v1/catalog/services";
    private static final String CONSUL_API_CATALOG_SERVICE = "/v1/catalog/service/%s";

    private static final String CONSUL_API_SERVICES = "/v1/agent/services";
    private static final String CONSUL_API_CHECKS = "/v1/agent/checks";
    private static final String CONSUL_API_MEMBERS = "/v1/agent/members";
    private static final String CONSUL_API_SELF = "/v1/agent/self";
    private static final String CONSUL_API_JOIN = "/v1/agent/join/%s";
    private static final String CONSUL_API_FORCE_LEAVE = "/v1/agent/force-leave/%s";

    private static final String CONSUL_API_HEALTH_SERVICE =  "/v1/health/service/%s";

    public String services() {
        WebTarget target = getWebTarget();

        return get(target, CONSUL_API_SERVICES);
    }

    public String checks() {
        WebTarget target = getWebTarget();

        return get(target, CONSUL_API_CHECKS);
    }

    public String members() {
        WebTarget target = getWebTarget();

        return get(target, CONSUL_API_MEMBERS);
    }

    public String self() {
        WebTarget target = getWebTarget();

        return get(target, CONSUL_API_SELF);
    }

    public String join(final String address) {
        WebTarget target = getWebTarget();

        return get(target, String.format(CONSUL_API_JOIN, address));
    }

    public String forceLeave(final String node) {
        WebTarget target = getWebTarget();

        return get(target, String.format(CONSUL_API_FORCE_LEAVE, node));
    }

    public String catalogServices() {
        WebTarget target = getWebTarget();

        return get(target, CONSUL_API_CATALOG_SERVICES);
    }

    public String catalogService(final String service) {
        WebTarget target = getWebTarget();

        return get(target, String.format(CONSUL_API_CATALOG_SERVICE, service));
    }

    public String healthService(final String service) {
        WebTarget target = getWebTarget();

        return get(target, String.format(CONSUL_API_HEALTH_SERVICE, service));
    }

    private String get(WebTarget target, String path) {
        return target.path(path).request().accept(MediaType.APPLICATION_JSON).get(String.class);
    }

    private WebTarget getWebTarget() {
        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
        return client.target(ConsulAgentConfiguration.getInstance().getBaseURI());
    }
}
