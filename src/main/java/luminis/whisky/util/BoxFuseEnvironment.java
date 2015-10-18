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

}
