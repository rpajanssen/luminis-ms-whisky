package luminis.whisky.util;

public enum Services {
    METRICS("metrics",null),
    RETURNS("returns", "/returns"),
    SHIPPING("shipping", "/shipments/returns"),
    BILLING("billing", "/bills/returns");

    private final String serviceID;
    private final String servicePath;

    Services(String serviceID, String servicePath) {
        this.serviceID = serviceID;
        this.servicePath = servicePath;
    }

    public String getServiceID() {
        return serviceID;
    }

    public String getServicePath() {
        return servicePath;
    }
}
