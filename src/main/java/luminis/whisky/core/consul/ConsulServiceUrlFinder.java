package luminis.whisky.core.consul;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import luminis.whisky.client.ConsulClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

public class ConsulServiceUrlFinder {
    // for now assume http and always a port
    private String baseUrl = "http://%s:%s/";

    private ConsulClient consulClient;

    private final LoadingCache<String, String> uriCache;
    private final LoadingCache<String, ConsulServiceConfiguration> configCache;

    @SuppressWarnings("unchecked")
    public ConsulServiceUrlFinder() {
        consulClient = new ConsulClient();

        CacheBuilder cacheBuilder =
                CacheBuilder.newBuilder().maximumSize(50).expireAfterAccess(10, TimeUnit.SECONDS);

        uriCache = cacheBuilder.build(
                new CacheLoader<String, String>() {
                    @Override
                    public String load(final String serviceId) throws DyingServiceException {
                        return queryConsulForServiceUrl(serviceId);
                    }
                });

        configCache = cacheBuilder.build(
                new CacheLoader<String, ConsulServiceConfiguration>() {
                    @Override
                    public ConsulServiceConfiguration load(final String serviceId) throws DyingServiceException {
                        return queryConsulForServiceConfiguration(serviceId);
                    }
                });
    }

    private String queryConsulForServiceUrl(String serviceId) throws DyingServiceException {
        return new QueryConsulTemplate<String> () {

            @Override
            protected String buildResult(String address, String serviceAddress, String servicePort) {
                String url = String.format(baseUrl, ifNotNullAElseB(serviceAddress, address), servicePort);

                System.out.println(String.format("Found url %s for service %s", url, serviceId));

                return url;
            }
        }.queryConsulForServiceUrl(serviceId);
    }

    private ConsulServiceConfiguration queryConsulForServiceConfiguration(String serviceId) throws DyingServiceException {
        return new QueryConsulTemplate<ConsulServiceConfiguration> () {

            @Override
            protected ConsulServiceConfiguration buildResult(String address, String serviceAddress, String servicePort) {
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
        }.queryConsulForServiceUrl(serviceId);
    }

    private String ifNotNullAElseB(String a, String b) {
        if(!StringUtils.isEmpty(a)) {
            return a;
        }

        return b;
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
        return uriCache.getUnchecked(serviceId);
    }

    public ConsulServiceConfiguration findServiceConfiguration(String serviceId) throws DyingServiceException {
        return configCache.getUnchecked("csc-" + serviceId);
    }

    // todo : pick one random
    private String getHealthyNode(String serviceId) {
        // get information about the service per node
        String jsonResponse = consulClient.healthService(serviceId);

        System.out.println("health[" + serviceId + "]=" + jsonResponse);

        // iterate over the node to find the first node with a healthy check for this service
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

    abstract class QueryConsulTemplate<T> {
        protected abstract T buildResult(String address, String serviceAddress, String servicePort);

        public T queryConsulForServiceUrl(String serviceId) throws DyingServiceException {
            String node = getHealthyNode(serviceId);
            if(node!=null) {
                String jsonResponse = consulClient.catalogService(serviceId);

                JSONArray response = new JSONArray(jsonResponse);
                for(int index=0; index < response.length(); index++) {
                    if(node.equals(((JSONObject) response.get(index)).get("Node"))) {
                        String address = (String) ((JSONObject) response.get(index)).get("Address");
                        String serviceAddress = (String) ((JSONObject) response.get(index)).get("ServiceAddress");
                        String servicePort = String.valueOf(((JSONObject) response.get(index)).get("ServicePort"));

                        return buildResult(address, serviceAddress, servicePort);
                    }
                }

                throw new IllegalStateException(String.format("information for service %s for node %s not found", serviceId, node));
            }

            throw new DyingServiceException(String.format("service %s unhealthy and not available", serviceId));
        }
    }
}
