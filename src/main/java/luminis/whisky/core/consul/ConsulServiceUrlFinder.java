package luminis.whisky.core.consul;

import java.util.List;

public class ConsulServiceUrlFinder {

    private final ConsulCache cache;

    public ConsulServiceUrlFinder() {
        cache = new ConsulCache();
    }

    public String findFirstAvailableServiceUrl(String serviceId) throws DyingServiceException {
        return findServiceUrl(serviceId).get(0);
    }

    public List<String> findServiceUrl(String serviceId) throws DyingServiceException {
        return cache.getUriStrings(serviceId);
    }

    public ConsulServiceConfiguration findFirstAvailableServiceConfiguration(String serviceId) throws DyingServiceException {
        return findServiceConfiguration(serviceId).get(0);
    }

    public List<ConsulServiceConfiguration> findServiceConfiguration(String serviceId) throws DyingServiceException {
        return cache.getServiceConfigurations(serviceId);
    }
}
