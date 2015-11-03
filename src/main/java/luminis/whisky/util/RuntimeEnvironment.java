package luminis.whisky.util;

public class RuntimeEnvironment {
    public static boolean isProd() {
        if(System.getProperty("BOXFUSE_ENV")==null) {
            return false;
        }

        return "prod".equalsIgnoreCase(System.getProperty("BOXFUSE_ENV"));
    }

    public static boolean isTest() {
        if(System.getProperty("BOXFUSE_ENV")==null) {
            return true;
        }

        return "test".equalsIgnoreCase(System.getProperty("BOXFUSE_ENV"));
    }

    public static boolean isDev() {
        if(System.getProperty("BOXFUSE_ENV")==null) {
            return true;
        }

        return "dev".equalsIgnoreCase(System.getProperty("BOXFUSE_ENV"));
    }

    public static boolean isDevOrTest() {
        if(System.getProperty("BOXFUSE_ENV")==null) {
            return true;
        }

        return !"prod".equalsIgnoreCase(System.getProperty("BOXFUSE_ENV"));
    }

    public static String getEnv() {
        if(System.getProperty("BOXFUSE_ENV")==null) {
            return "test";
        }

        return System.getProperty("BOXFUSE_ENV");
    }

    public static int getHttpPort() {
        if(System.getProperty("BOXFUSE_PORTS_HTTP")==null) {
            return 80;
        }

        return Integer.valueOf(System.getProperty("BOXFUSE_PORTS_HTTP"));
    }

    public static int getForwardedHttpPort() {
        if(System.getProperty("BOXFUSE_PORTS_FORWARDED_HTTP")==null) {
            return 8888;
        }

        return Integer.valueOf(System.getProperty("BOXFUSE_PORTS_FORWARDED_HTTP"));
    }

    public static boolean isRunningOnAWS() {
        if(System.getProperty("BOXFUSE_PLATFORM_NAME")==null) {
            return false;
        }

        return "AWS".equalsIgnoreCase(System.getProperty("BOXFUSE_PLATFORM_NAME"));
    }

    public static boolean isRunningConsulServer() {
        if(System.getProperty("RUN_WITH_CONSUL_SERVER")==null) {
            return false;
        }

        return Boolean.valueOf(System.getProperty("RUN_WITH_CONSUL_SERVER"));
    }

    public static String getConsulServerAddress() {
        if(System.getProperty("CONSUL_SERVER_ADDRESS")==null) {
            return "";
        }

        return String.valueOf(System.getProperty("CONSUL_SERVER_ADDRESS"));
    }

    public static String getConsulServerPort() {
        if(System.getProperty("CONSUL_SERVER_PORT")==null) {
            return "";
        }

        return String.valueOf(System.getProperty("CONSUL_SERVER_PORT"));
    }

    public static String getAdvertiseAddress() {
        if(System.getProperty("CONSUL_ADVERTISE_ADDRESS")==null) {
            return null;
        }

        return String.valueOf(System.getProperty("CONSUL_ADVERTISE_ADDRESS"));
    }

    public static String getConsulAgentAddress() {
        if(System.getProperty("CONSUL_AGENT_ADDRESS")==null) {
            return "localhost";
        }

        return String.valueOf(System.getProperty("CONSUL_AGENT_ADDRESS"));
    }

    public static int getConsulAgentHttpPort() {
        if(System.getProperty("BOXFUSE_PORTS_CONSUL_HTTP")==null) {
            return 8500;
        }

        return Integer.valueOf(System.getProperty("BOXFUSE_PORTS_CONSUL_HTTP"));
    }

    public static boolean skipConsulDeployment() {
        if(System.getProperty("CONSUL_SKIP_DEPLOYMENT")==null) {
            return false;
        }

        return Boolean.valueOf(System.getProperty("CONSUL_SKIP_DEPLOYMENT"));
    }

    public static boolean withoutStubs() {
        if(System.getProperty("WITHOUT_STUBS")==null) {
            return false;
        }

        return Boolean.valueOf(System.getProperty("WITHOUT_STUBS"));
    }

    public static String getAccount() {
        return String.valueOf(System.getProperty("BOXFUSE_ACCOUNT"));
    }

    public static String getApp() {
        return String.valueOf(System.getProperty("BOXFUSE_APP"));
    }
}
