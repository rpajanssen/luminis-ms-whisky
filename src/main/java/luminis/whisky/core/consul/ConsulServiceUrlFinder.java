package luminis.whisky.core.consul;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import luminis.whisky.client.ConsulClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ConsulServiceUrlFinder {
    // for now assume http and always a port
    private final String baseUrl = "http://%s:%s/";

    private final ConsulClient consulClient;

    private final LoadingCache<String, List<String>> uriCache;
    private final LoadingCache<String, List<ConsulServiceConfiguration>> configCache;

    @SuppressWarnings("unchecked")
    public ConsulServiceUrlFinder() {
        consulClient = new ConsulClient();

        CacheBuilder cacheBuilder =
                CacheBuilder.newBuilder().maximumSize(50).expireAfterAccess(10, TimeUnit.SECONDS);

        uriCache = cacheBuilder.build(
                new CacheLoader<String, List<String>>() {
                    @Override
                    public List<String> load(@Nullable final String serviceId) throws DyingServiceException {
                        return queryConsulForServiceUrl(serviceId);
                    }
                });

        configCache = cacheBuilder.build(
                new CacheLoader<String, List<ConsulServiceConfiguration>>() {
                    @Override
                    public List<ConsulServiceConfiguration> load(@Nullable final String serviceId) throws DyingServiceException {
                        return queryConsulForServiceConfiguration(serviceId);
                    }
                });
    }

    private List<String> queryConsulForServiceUrl(String serviceId) throws DyingServiceException {
        return new QueryConsulTemplate<String> () {

            @Override
            protected String buildResult(String address, String serviceAddress, String servicePort) {
                String url = String.format(baseUrl, ifNotNullAElseB(serviceAddress, address), servicePort);

                System.out.println(String.format("Found url %s for service %s", url, serviceId));

                return url;
            }
        }.queryConsulForServiceUrl(serviceId);
    }

    private List<ConsulServiceConfiguration> queryConsulForServiceConfiguration(String serviceId) throws DyingServiceException {
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

    public String findFirstAvailableServiceUrl(String serviceId) throws DyingServiceException {
        return findServiceUrl(serviceId).get(0);
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
     * @return a list of available base urls to the service
     *
     * @throws DyingServiceException if the requested service is not healthy
     */
    public List<String> findServiceUrl(String serviceId) throws DyingServiceException {
        return uriCache.getUnchecked(serviceId);
    }

    public ConsulServiceConfiguration findFirstAvailableServiceConfiguration(String serviceId) throws DyingServiceException {
        return findServiceConfiguration(serviceId).get(0);
    }

    public List<ConsulServiceConfiguration> findServiceConfiguration(String serviceId) throws DyingServiceException {
        return configCache.getUnchecked("csc-" + serviceId);
    }

    private boolean healthy(JSONArray checks, int checkIndex) {
        return !"warning".equalsIgnoreCase(checks.getJSONObject(checkIndex).getString("Status")) &&
                !"critical".equalsIgnoreCase(checks.getJSONObject(checkIndex).getString("Status"));
    }

    /**
     * Template capturing the boilerplate required to query Consul for the catalog of a service
     * for healthy urls information.
     *
     * @param <T> A result object capturing all the requested url info (can be any DTO)
     */
    abstract class QueryConsulTemplate<T> {
        protected abstract T buildResult(String address, String serviceAddress, String servicePort);

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

            throw new DyingServiceException(String.format("service %s unhealthy and not available", serviceId));
        }
    }

    private List<String> getHealthyNodes(String serviceId) {
        List<String> healthyNodes = new ArrayList<>();

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
                    healthyNodes.add(nodeInformation.getJSONObject("Node").getString("Node"));
                }
            }

        }

        return healthyNodes;
    }
}
