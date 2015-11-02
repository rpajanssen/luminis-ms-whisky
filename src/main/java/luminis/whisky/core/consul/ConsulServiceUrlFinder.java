package luminis.whisky.core.consul;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import luminis.whisky.client.ConsulClient;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
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

    String ifNotNullAElseB(String a, String b) {
        if(!StringUtils.isEmpty(a)) {
            return a;
        }

        return b;
    }

    public String findFirstAvailableServiceUrl(String serviceId) throws DyingServiceException {
        return findServiceUrl(serviceId).get(0);
    }

    public List<String> findServiceUrl(String serviceId) throws DyingServiceException {
        return uriCache.getUnchecked(serviceId);
    }

    public ConsulServiceConfiguration findFirstAvailableServiceConfiguration(String serviceId) throws DyingServiceException {
        return findServiceConfiguration(serviceId).get(0);
    }

    public List<ConsulServiceConfiguration> findServiceConfiguration(String serviceId) throws DyingServiceException {
        return configCache.getUnchecked("csc-" + serviceId);
    }
}
