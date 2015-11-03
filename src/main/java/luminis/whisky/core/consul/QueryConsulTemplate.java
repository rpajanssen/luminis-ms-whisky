package luminis.whisky.core.consul;

import luminis.whisky.client.ConsulClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Template capturing the boilerplate required to query Consul for the catalog of a service
 * for healthy urls information.
 *
 * @param <T> A result object capturing all the requested url info (can be any DTO)
 */
public abstract class QueryConsulTemplate<T> {
    private final ConsulClient consulClient;

    protected QueryConsulTemplate(ConsulClient consulClient) {
        this.consulClient = consulClient;
    }

    protected abstract T buildResult(String address, String serviceAddress, String servicePort);

    /**
     * Consul catalog service returns an answer like this:
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
     * @return a list of available base urls to the service
     *
     * @throws DyingServiceException if the requested service is not healthy
     */
    public List<T> queryConsulForServiceUrl(String serviceId) throws DyingServiceException {
        List<String> nodes = getHealthyNodes(serviceId);
        if(!nodes.isEmpty()) {
            List<T> urls = new ArrayList<>();

            String jsonResponse = consulClient.catalogService(serviceId);

            JSONArray response = new JSONArray(jsonResponse);
            for(int index=0; index < response.length(); index++) {
                for(String node : nodes) {
                    if (node.equals(((JSONObject) response.get(index)).get("Node"))) {
                        String address = (String) ((JSONObject) response.get(index)).get("Address");
                        String serviceAddress = (String) ((JSONObject) response.get(index)).get("ServiceAddress");
                        String servicePort = String.valueOf(((JSONObject) response.get(index)).get("ServicePort"));

                        urls.add(buildResult(address, serviceAddress, servicePort));
                    }
                }
            }

            if(urls.isEmpty()) {
                throw new IllegalStateException(String.format("no information for service %s", serviceId));
            }

            return urls;
        }

        throw new DyingServiceException(serviceId);
    }

    /**
     * Consul health service returns something like:
     * [
     *    {
     *       "Node": {
     *       "Node": "foobar",
     *       "Address": "10.1.10.12"
     *    },
     *        "Service": {
     *        "ID": "redis",
     *        "Service": "redis",
     *        "Tags": null,
     *        "Port": 8000
     *    },
     *    "Checks": [
     *            {
     *                "Node": "foobar",
     *                "CheckID": "service:redis",
     *                "Name": "Service 'redis' check",
     *                "Status": "passing",
     *                "Notes": "",
     *                "Output": "",
     *                "ServiceID": "redis",
     *                "ServiceName": "redis"
     *            },
     *            {
     *                "Node": "foobar",
     *                "CheckID": "serfHealth",
     *                "Name": "Serf Health Status",
     *                "Status": "passing",
     *                "Notes": "",
     *                "Output": "",
     *                "ServiceID": "",
     *                "ServiceName": ""
     *            }
     *        ]
     *    }
     *  ]
     *
     * @param serviceId the service-id to get the info from
     * @return the list of healthy nodes
     */
    private List<String> getHealthyNodes(String serviceId) {
        List<String> healthyNodes = new ArrayList<>();

        // get information about the service per node
        String jsonResponse = consulClient.healthService(serviceId);

        // iterate over the node to find the first node with a healthy check for this service
        JSONArray nodes = new JSONArray(jsonResponse);
        for(int nodeIndex=0; nodeIndex < nodes.length(); nodeIndex++) {
            JSONObject nodeInformation = nodes.getJSONObject(nodeIndex);

            // verify the checks for this service on this node
            JSONArray checks = nodeInformation.getJSONArray("Checks");
            for(int checkIndex=0; checkIndex < checks.length(); checkIndex++) {
                if (healthy(checks, checkIndex)) {
                    healthyNodes.add(nodeInformation.getJSONObject("Node").getString("Node"));
                }
            }

        }

        return healthyNodes;
    }

    private boolean healthy(JSONArray checks, int checkIndex) {
        return !"warning".equalsIgnoreCase(checks.getJSONObject(checkIndex).getString("Status")) &&
                !"critical".equalsIgnoreCase(checks.getJSONObject(checkIndex).getString("Status"));
    }
}

