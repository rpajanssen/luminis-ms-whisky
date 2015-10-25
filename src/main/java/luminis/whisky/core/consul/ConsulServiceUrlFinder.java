package luminis.whisky.core.consul;

import luminis.whisky.client.ConsulClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

public class ConsulServiceUrlFinder {
    // for now assume http and always a port
    private String baseUrl = "http://%s:%s/";

    private ConsulClient consulClient;

    public ConsulServiceUrlFinder() {
        consulClient = new ConsulClient();
    }

    /**
     * Consul returns an answer like this:
     *  [
     *    {
     *      "Node": "foobar",
     *      "Address": "10.1.10.12",
     *      "ServiceID": "redis",
     *      "ServiceName": "redis",
     *      "ServiceTags": null,
     *      "ServiceAddress": "",
     *      "ServicePort": 8000
     *    }
     *  ]
     *
     * @param serviceId the service-id Consul is using
     * @return the base url to the service
     *
     * @throws DyingServiceException if the requested service is not healthy
     */
    public String findServiceUrl(String serviceId) throws DyingServiceException {
        String node = getHealthyNode(serviceId);
        if(node!=null) {
            String jsonResponse = consulClient.catalogService(serviceId);

            JSONArray response = new JSONArray(jsonResponse);

            for(int index=0; index < response.length(); index++) {
                if(node.equals(((JSONObject) response.get(index)).get("Node"))) {
                    String address = (String) ((JSONObject) response.get(index)).get("Address");
                    String serviceAddress = (String) ((JSONObject) response.get(index)).get("ServiceAddress");
                    String servicePort = String.valueOf(((JSONObject) response.get(index)).get("ServicePort"));

                    String url = String.format(baseUrl, ifNotNullAElseB(serviceAddress, address), servicePort);

                    System.out.println(String.format("Found url %s for service %s", url, serviceId));

                    return url;
                }
            }

            throw new IllegalStateException(String.format("information for service %s for node %s not found", serviceId, node));
        }

        throw new DyingServiceException(String.format("service %s unhealthy and not available", serviceId));
    }

    public ConsulServiceConfiguration findServiceConfiguration(String serviceId) throws DyingServiceException {
        String node = getHealthyNode(serviceId);
        if(node!=null) {
            String jsonResponse = consulClient.catalogService(serviceId);

            JSONArray response = new JSONArray(jsonResponse);
            if (response.length() > 0) {
                for(int index=0; index < response.length(); index++) {
                    if (node.equals(((JSONObject) response.get(index)).get("Node"))) {
                        String address = (String) ((JSONObject) response.get(0)).get("Address");
                        String serviceAddress = (String) ((JSONObject) response.get(0)).get("ServiceAddress");
                        String servicePort = String.valueOf(((JSONObject) response.get(0)).get("ServicePort"));

                        ConsulServiceConfiguration serviceConfiguration =
                                new ConsulServiceConfiguration()
                                .withAddress(ifNotNullAElseB(serviceAddress, address))
                                .withPort(Integer.valueOf(ifNotNullAElseB(servicePort, "80")));

                        System.out.println(
                                String.format("Found service configuration %s for service %s",
                                        serviceConfiguration.toString(),
                                        serviceId)
                        );

                        return serviceConfiguration;
                    }
                }

                throw new IllegalStateException(String.format("information for service %s for node %s not found", serviceId, node));
            }

            System.out.println(String.format("Missing service configuration [%s] in Consul services configuration. Using defaults.", serviceId));

            return new ConsulServiceConfiguration();
        }

        throw new DyingServiceException(serviceId);
    }

    private String getHealthyNode(String serviceId) {
        // get information about the service per node
        String jsonResponse = consulClient.healthService(serviceId);

        System.out.println("health[" + serviceId + "]=" + jsonResponse);

        // iterate over the node to find a node with a healthy check for this service
        JSONArray nodes = new JSONArray(jsonResponse);
        for(int nodeIndex=0; nodeIndex < nodes.length(); nodeIndex++) {
            JSONObject nodeInformation = nodes.getJSONObject(nodeIndex);

            // verify the checks for this service on this node
            JSONArray checks = nodeInformation.getJSONArray("Checks");
            for(int checkIndex=0; checkIndex < checks.length(); checkIndex++) {
                if (healthy(checks, checkIndex)) {
                    return nodeInformation.getJSONObject("Node").getString("Node");
                }
            }

        }

        // we did not find any healthy service instance on any node
        return null;
    }

    private boolean healthy(JSONArray checks, int checkIndex) {
        return !"warning".equalsIgnoreCase(checks.getJSONObject(checkIndex).getString("Status")) &&
                !"critical".equalsIgnoreCase(checks.getJSONObject(checkIndex).getString("Status"));
    }

    private String ifNotNullAElseB(String a, String b) {
        if(!StringUtils.isEmpty(a)) {
            return a;
        }

        return b;
    }
}
