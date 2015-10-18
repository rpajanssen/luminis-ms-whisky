package luminis.whisky.util;

import luminis.whisky.client.StatsdClient;
import luminis.whisky.core.consul.ConsulServiceUrlFinder;
import luminis.whisky.core.consul.ServiceConfiguration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public class Metrics {
    private final ConsulServiceUrlFinder consulServiceUrlFinder;
    private StatsdClient statsdClient;

    public Metrics() {
        consulServiceUrlFinder = new ConsulServiceUrlFinder();
    }

    public Metrics(ConsulServiceUrlFinder consulServiceUrlFinder) {
        this.consulServiceUrlFinder = consulServiceUrlFinder;
    }

    public void increment(String serviceId) {
        if(statsdClient==null) {
            setup();
        }

        if(statsdClient!=null) {
            statsdClient.increment(serviceId);
        }
    }

    @PostConstruct
    public void setup() {
        try {
            ServiceConfiguration serviceConfiguration = consulServiceUrlFinder.findServiceConfiguration(ApplicationConstants.METRICS_SERVICE_ID);
            System.out.println("Found statsd ip and port: " + serviceConfiguration.getAddress() +":"+ serviceConfiguration.getPort());
            statsdClient = new StatsdClient(serviceConfiguration.getAddress(), serviceConfiguration.getPort());
        } catch(Exception e) {
            System.out.println("Unable to start StatsDClient : " + e.getMessage());
        }
    }

    @PreDestroy
    public void cleanup() {
        if(statsdClient!=null) {
            statsdClient.flush();
        }
    }
}
