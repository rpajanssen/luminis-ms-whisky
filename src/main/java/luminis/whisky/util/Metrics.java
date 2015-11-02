package luminis.whisky.util;

import luminis.whisky.client.StatsdClient;
import luminis.whisky.core.consul.ConsulServiceConfiguration;
import luminis.whisky.core.consul.ConsulServiceUrlFinder;
import org.joda.time.DateTime;
import org.joda.time.Minutes;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public class Metrics {
    private final ConsulServiceUrlFinder consulServiceUrlFinder;
    private StatsdClient statsdClient;
    private DateTime lastTry;

    public Metrics(ConsulServiceUrlFinder consulServiceUrlFinder) {
        this.consulServiceUrlFinder = consulServiceUrlFinder;
    }

    public boolean increment(String serviceId) {
        if(statsdClient==null && waitIntervalIsOver()) {
            setup();
        }

        if(statsdClient!=null) {
            return statsdClient.increment(serviceId);
        }

        return false;
    }

    private boolean waitIntervalIsOver() {
        DateTime now = new DateTime();

        if(lastTry==null || Minutes.minutesBetween(lastTry, now).isGreaterThan(Minutes.minutes(1))) {
            lastTry = now;

            return true;
        }

        return false;
    }

    @PostConstruct
    public void setup() {
        try {
            ConsulServiceConfiguration serviceConfiguration = consulServiceUrlFinder.findFirstAvailableServiceConfiguration(Service.METRICS.getServiceID());
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
