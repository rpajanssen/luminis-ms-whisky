package luminis.whisky.util;

public class BoxFuseEnvironment {
    public static boolean isProd() {
        if(System.getProperty("BOXFUSE_ENV")==null) {
            return false;
        }

        return "prod".equalsIgnoreCase(System.getProperty("BOXFUSE_ENV"));
    }

    public static boolean isDevOrTest() {
        if(System.getProperty("BOXFUSE_ENV")==null) {
            return true;
        }

        return !"prod".equalsIgnoreCase(System.getProperty("BOXFUSE_ENV"));
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

}
