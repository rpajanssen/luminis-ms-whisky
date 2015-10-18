package luminis.whisky.core.consul;

import luminis.whisky.util.StreamGobbler;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// todo : dev/test/prod
//        cleanup
public class ConsulDeployer {
    //private static String consul_address = "http://10.1.17.188:8301";

    public static void deployAndRun() {
        boolean deployed = deployConsul();
        runConsul(deployed);
    }

    private static boolean deployConsul() {
        try {
            boolean deployed =  deployConsulBinary();
            if(deployed) {
                deployConsulConfiguration();
                deployConsulUI();
            }

            return deployed;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean deployConsulBinary() throws IOException {
        File folder = new File("./consul");
        if(!folder.exists()) {
            System.out.println("creating consul folder");
            if(!folder.mkdir()) {
                System.out.println("unable to create consul folder");
                return false;
            }

            File agent = new File("./consul/consul");
            FileCopyUtils.copy(new ClassPathResource("consul/binary/consul").getInputStream(), new FileOutputStream(agent));

            File reference = new File("./consul/consul");
            if(!reference.exists()) {
                throw new RuntimeException("consul not copied");
            }

            System.out.println("consul copied");

            if(!reference.canExecute()) {
                if(reference.setExecutable(true)) {
                    System.out.println("execute permissions on consul set");
                }
            }

            return true;
        } else {
            System.out.println("consul folder already exists");
            return false;
        }
    }

    private static void deployConsulConfiguration() throws IOException {
        File folder = new File("./consul/config");
        if(!folder.exists()) {
            System.out.println("creating consul config folder");
            if(!folder.mkdir()) {
                System.out.println("unable to create consul config folder");
                return;
            }

            File config = new File("./consul/config/basic_config.json");
            FileCopyUtils.copy(new ClassPathResource("consul/config/basic_config.json").getInputStream(), new FileOutputStream(config));

            File reference = new File("./consul/config/basic_config.json");
            if(!reference.exists()) {
                throw new RuntimeException("consul config not copied");
            }

            System.out.println("consul config copied");

            if(!reference.canRead()) {
                if(reference.setReadable(true)) {
                    System.out.println("read permissions on consul config set");
                }
            }
        } else {
            System.out.println("consul config folder already exists");
        }
    }

    private static void deployConsulUI() {
        try {
            File folder = new File("./consul/dist");
            if(!folder.exists()) {
                System.out.println("creating consul dist folder");
                if(!folder.mkdir()) {
                    System.out.println("unable to create consul dist folder");
                    return;
                }

                File config = new File("./consul/dist");
                FileSystemUtils.copyRecursively(new ClassPathResource("consul/dist/index.html").getFile().getParentFile(), config);

                File reference = new File("./consul/dist");
                if(!reference.exists()) {
                    throw new RuntimeException("consul dist not copied");
                }

                System.out.println("consul dist copied");

                if(!reference.canRead()) {
                    if(reference.setReadable(true)) {
                        System.out.println("read permissions on consul dist set");
                    }
                }
            } else {
                System.out.println("consul config folder already exists");
            }
        } catch (IOException e) {
            System.out.println("Unable to deploy consul ui, you have to live without it!");
            System.out.print(e.getMessage());
            System.out.println();
        }

    }

    // todo : dev/test + prod
    // todo : specify config dir with configuration
    // todo : specify ports so we can run multiple vm's without clashing ports on one machine
    private static void runConsul(boolean deployed) {
        if (deployed) {
            System.out.println("starting deployed consul");
            // with advertise of local agents ip address ( luminis office, local vm)
            //runAsyncCommand(new String[]{"/bin/sh", "-c", "./consul/consul agent -advertise 10.1.17.180 -data-dir=./consul -config-dir=./consul/config"});

            // run local with agent as server
            //consul agent -data-dir=/tmp/consul -server -bootstrap-expect 1 -ui-dir ./consul/dist
            runAsyncCommand(new String[]{"/bin/sh", "-c", "./consul/consul agent -data-dir=./consul -config-dir=./consul/config -server -bootstrap-expect 1 -ui-dir ./consul/dist"});
        } else {
            System.out.println("crossing our fingers that consul is available");
            runAsyncCommand(new String[]{"consul", "agent", "-data-dir=/tmp/consul -config-dir=./consul/config"});
        }

    }

    private static void runAsyncCommand(final String[] command) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            public void run() {
                runCommand(command);
            }
        });
    }

    private static void runCommand(final String[] command) {
        try {
            Process process = Runtime.getRuntime().exec(command);

            printOutput(process);

            // any error???
            int exitVal = process.waitFor();
            System.out.print("Exec result = " + exitVal);

        } catch (IOException | InterruptedException e) {
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
}
