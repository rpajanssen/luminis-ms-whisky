package luminis.whisky.client;

import luminis.whisky.command.RestGetCommand;
import luminis.whisky.core.consul.ConsulAgentConfiguration;
import luminis.whisky.util.Service;

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
        return get(CONSUL_API_SERVICES);
    }

    public String checks() {
        return get(CONSUL_API_CHECKS);
    }

    public String members() {
        return get(CONSUL_API_MEMBERS);
    }

    public String self() {
        return get(CONSUL_API_SELF);
    }

    public String join(final String address) {
        return get(String.format(CONSUL_API_JOIN, address));
    }

    public String forceLeave(final String node) {
        return get(String.format(CONSUL_API_FORCE_LEAVE, node));
    }

    public String catalogServices() {
        return get(CONSUL_API_CATALOG_SERVICES);
    }

    public String catalogService(final String service) {
        return get(String.format(CONSUL_API_CATALOG_SERVICE, service));
    }

    public String healthService(final String service) {
        return get(String.format(CONSUL_API_HEALTH_SERVICE, service));
    }

    private String get(String path) {
        RestGetCommand restGetCommand =
                new RestGetCommand(Service.CONSUL, ConsulAgentConfiguration.getInstance().getBaseUriString(), path);

        return restGetCommand.execute().readEntity(String.class);
    }
}
