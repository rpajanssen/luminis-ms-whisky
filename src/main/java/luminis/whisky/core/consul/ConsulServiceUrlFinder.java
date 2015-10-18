package luminis.whisky.core.consul;

import luminis.whisky.client.ConsulClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

// todo error handling
// todo : perform heath check for returned services
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
     */
    public String findServiceUrl(String serviceId) {
        String jsonResponse = consulClient.catalogService(serviceId);

        JSONArray response = new JSONArray(jsonResponse);

        // simply pick first service instance for now
        String address = (String)((JSONObject)response.get(0)).get("Address");
        String serviceAddress = (String)((JSONObject)response.get(0)).get("ServiceAddress");
        String servicePort = String.valueOf(((JSONObject)response.get(0)).get("ServicePort"));

        return String.format(baseUrl, ifNotNullAElseB(serviceAddress, address), servicePort);
    }

    public ServiceConfiguration findServiceConfiguration(String serviceId) {
        String jsonResponse = consulClient.catalogService(serviceId);

        JSONArray response = new JSONArray(jsonResponse);
        if(response.length()>0) {

            // simply pick first service instance for now
            String address = (String) ((JSONObject) response.get(0)).get("Address");
            String serviceAddress = (String) ((JSONObject) response.get(0)).get("ServiceAddress");
            String servicePort = String.valueOf(((JSONObject) response.get(0)).get("ServicePort"));

            return new ServiceConfiguration()
                    .withAddress(ifNotNullAElseB(serviceAddress, address))
                    .withPort(Integer.valueOf(ifNotNullAElseB(servicePort, "80")));
        }

        System.out.println("Missing metrics configuration in Consul services configuration. Using defaults.");
        return new ServiceConfiguration();
    }

    private String ifNotNullAElseB(String a, String b) {
        if(!StringUtils.isEmpty(a)) {
            return a;
        }

        return b;
    }
}
