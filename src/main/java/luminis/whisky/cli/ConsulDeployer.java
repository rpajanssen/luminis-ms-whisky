package luminis.whisky.cli;

import luminis.whisky.util.RuntimeEnvironment;
import luminis.whisky.util.StreamGobbler;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsulDeployer {
    //private static String consul_address = "http://10.1.17.188:8301";

    public static void deployAndRun() {
        boolean deployed = deployConsul();
        runConsul(deployed);
    }

    private static boolean deployConsul() {
        try {
            // deploy consul binary
            boolean result = deployArtifact("consul/binary/consul", "consul/binary", "consul/binary/consul", Permission.EXECUTE);

            // deploy consul configuration
            if(RuntimeEnvironment.isProd()) {
                result = result && deployArtifact("consul/config/prod_config.json", "consul/config", "consul/config/config.json", Permission.READ);
            } else {
                // todo : local with local external server
                result = result && deployArtifact("consul/config/test_config.json", "consul/config", "consul/config/config.json", Permission.READ);
            }

            // deploy consul ui
            deployArtifact("consul/dist/index.html", "consul/dist", "consul/dist/index.html", Permission.READ);
            deployArtifact("consul/dist/static/application.min.js", "consul/dist/static", "consul/dist/static/application.min.js", Permission.READ);
            deployArtifact("consul/dist/static/base.css", "consul/dist/static", "consul/dist/static/base.css", Permission.READ);
            deployArtifact("consul/dist/static/base.css.map", "consul/dist/static", "consul/dist/static/base.css.map", Permission.READ);
            deployArtifact("consul/dist/static/bootstrap.min.css", "consul/dist/static", "consul/dist/static/bootstrap.min.css", Permission.READ);
            deployArtifact("consul/dist/static/consul-logo.png", "consul/dist/static", "consul/dist/static/consul-logo.png", Permission.READ);
            deployArtifact("consul/dist/static/favicon.png", "consul/dist/static", "consul/dist/static/favicon.png", Permission.READ);
            deployArtifact("consul/dist/static/loading-cylon-purple.svg", "consul/dist/static", "consul/dist/static/loading-cylon-purple.svg", Permission.READ);

            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean deployArtifact(String sourceFilePath, String targetFolder, String targetFilePath, Permission permission) throws IOException {
        if (createFolder(targetFolder, targetFilePath)) return false;

        File reference = copyFile(sourceFilePath, targetFilePath);
        setFileSystemPermissions(targetFilePath, permission, reference);

        return true;
    }

    private static boolean createFolder(String targetFolder, String targetFilePath) {
        File folder = new File("./" + targetFolder);
        if(!folder.exists()) {
            System.out.println("creating folder " + targetFilePath);
            if(!folder.mkdirs()) {
                System.out.println("unable to create folder " + targetFilePath);
                return true;
            }
        }
        return false;
    }

    private static File copyFile(String sourceFilePath, String targetFilePath) throws IOException {
        File agent = new File("./" + targetFilePath);
        FileCopyUtils.copy(new ClassPathResource(sourceFilePath).getInputStream(), new FileOutputStream(agent));

        File reference = new File("./" + targetFilePath);
        if(!reference.exists()) {
            throw new RuntimeException(sourceFilePath + " not copied");
        }

        System.out.println(sourceFilePath + " copied");
        return reference;
    }

    private static void setFileSystemPermissions(String targetFilePath, Permission permission, File reference) {
        switch(permission) {
            case READ:
                if(!reference.canRead()) {
                    if(reference.setReadable(true)) {
                        System.out.println("set read permissions on " + targetFilePath);
                    }
                }
                break;
            case EXECUTE:
                if(!reference.canExecute()) {
                    if(reference.setExecutable(true)) {
                        System.out.println("set execute permissions on " + targetFilePath);
                    }
                }
                break;
        }
    }

    // todo : specify ports so we can run multiple vm's without clashing ports on one machine
    private static void runConsul(boolean deployed) {
        if (deployed) {
            System.out.println("starting deployed consul");
            if(RuntimeEnvironment.isProd()) {
                // run with agent only
                System.out.println("... run Consul agent only on prod");
                runAsyncCommand(new String[]{"/bin/sh", "-c", "./consul/binary/consul agent -data-dir=./consul -config-dir=./consul/config"});
            } else {
                if (RuntimeEnvironment.isRunningConsulServer()) {
                    // run with agent as server
                    System.out.println("... run Consul server and agent on local/dev/test");
                    runAsyncCommand(new String[]{"/bin/sh", "-c", "./consul/binary/consul agent -data-dir=./consul -config-dir=./consul/config -server -bootstrap-expect 1 -ui-dir ./consul/dist"});
                } else {
                    // todo : configurable ip address
                    // with advertise of local agents ip address ( luminis office, local vm)
                    System.out.println("... run Consul agent only with advertising on local/dev/test");
                    runAsyncCommand(new String[]{"/bin/sh", "-c", "./consul/consul agent -advertise 10.1.17.180 -data-dir=./consul -config-dir=./consul/config"});
                }
            }
        } else {
            System.out.println("crossing our fingers that consul is available from the command line");
            runAsyncCommand(new String[]{"consul", "agent", "-data-dir=/tmp/consul -config-dir=./consul/config"});
        }

    }

    private static void runAsyncCommand(final String[] command) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> runCommand(command));
    }

    private static void runCommand(final String[] command) {
        try {
            Process process = Runtime.getRuntime().exec(command);

            printOutput(process);

            // any error???
            int exitVal = process.waitFor();
            System.out.print("Exec result = " + exitVal);

        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.out.println();
        }
    }

    private static void printOutput(Process process) throws IOException {
        // any error message?
        StreamGobbler errorGobbler = new
                StreamGobbler(process.getErrorStream(), "ERROR");

        // any output?
        StreamGobbler outputGobbler = new
                StreamGobbler(process.getInputStream(), "OUTPUT");

        // kick them off
        errorGobbler.start();
        outputGobbler.start();
    }

    private enum Permission {
        READ, EXECUTE
    }
}
