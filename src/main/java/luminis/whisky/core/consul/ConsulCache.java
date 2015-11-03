package luminis.whisky.core.consul;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import luminis.whisky.client.ConsulClient;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Wraps a cache around the Consul client so we don't have a Consul round trip for every delegated service
 * request.
 */
public class ConsulCache {
    // for now assume http and always a port
    private final String baseUrl = "http://%s:%s/";

    private final ConsulClient consulClient;

    private final LoadingCache<String, List<String>> uriCache;
    private final LoadingCache<String, List<ConsulServiceConfiguration>> configCache;

    @SuppressWarnings("unchecked")
    public ConsulCache() {
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
        return new QueryConsulTemplate<String> (consulClient) {

            @Override
            protected String buildResult(String address, String serviceAddress, String servicePort) {
                return String.format(baseUrl, ifNotNullAElseB(serviceAddress, address), servicePort);
            }
        }.queryConsulForServiceUrl(serviceId);
    }

    private List<ConsulServiceConfiguration> queryConsulForServiceConfiguration(String serviceId) throws DyingServiceException {
        return new QueryConsulTemplate<ConsulServiceConfiguration> (consulClient) {

            @Override
            protected ConsulServiceConfiguration buildResult(String address, String serviceAddress, String servicePort) {
                return new ConsulServiceConfiguration()
                        .withAddress(ifNotNullAElseB(serviceAddress, address))
                        .withPort(Integer.valueOf(ifNotNullAElseB(servicePort, "80")));
            }
        }.queryConsulForServiceUrl(serviceId);
    }

    private String ifNotNullAElseB(String a, String b) {
        if(!StringUtils.isEmpty(a)) {
            return a;
        }

        return b;
    }

    public List<String> getUriStrings(String serviceId) {
        return uriCache.getUnchecked(serviceId);
    }

    public List<ConsulServiceConfiguration> getServiceConfigurations(String serviceId) {
        return configCache.getUnchecked("csc-" + serviceId);
    }
}
