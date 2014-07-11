package cz.cuni.mff.ufal.textan.server;

import cz.cuni.mff.ufal.textan.server.commands.CommandInvoker;
import cz.cuni.mff.ufal.textan.server.configs.AppConfig;
import cz.cuni.mff.ufal.textan.server.nametagIntegration.NameTagServices;
import cz.cuni.mff.ufal.textan.textpro.ITextPro;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import java.io.File;

/**
 * Server entry point.
 * Provides methods for running of application in two ways: as a standard java program and as a service using
 * some service wrapper (tested <a href="http://commons.apache.org/proper/commons-daemon/procrun.html">Apache Commons Daemon</a>
 * wrapper for Windows (because linux doesn't need any wrapper for daemons)).
 *
 * @author Petr Fanta
 */
public class AppEntry {

    /** The constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AppEntry.class);
    /** The constant app. */
    private static AppEntry app = null;

    /** The constant NAMETAG_MODEL. */
//TODO: just for testing
    private static final File NAMETAG_MODEL = new File("models/czech-cnec2.0-140304.ner");

    /** The Server. */
    private Server server = null;
    /** The Invoker. */
    private CommandInvoker invoker = null;

    /**
     * Instantiates a new App entry.
     */
    private AppEntry() {}

    /**
     * Initialize and run application.
     * @param args the command line arguments
     */
    @SuppressWarnings("UnusedParameters")
    public void run(String[] args) {

        try {
            LOG.info("Initialize TextAn");
            //Create root application context
            AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
            context.registerShutdownHook();

            server = context.getBean(Server.class);
            invoker = context.getBean(CommandInvoker.class);
            NameTagServices nameTagServices = context.getBean(NameTagServices.class);
            ITextPro textPro = context.getBean(ITextPro.class);
            textPro.learn();

            LOG.info("Initialize named entity recognizer");
            nameTagServices.bindModel(NAMETAG_MODEL.toString());

            LOG.info("Start server");
            server.start();
            LOG.info("Server command invoker");
            invoker.start();
        } catch (Exception e) {
            LOG.error("Unexpected exception: ", e);
            System.exit(1);
        }
    }

    /**
     * Stops application.
     */
    public void destroy() {

        try {
            if (server != null) {
                server.stop();
            }

            if (invoker != null) {
                invoker.stop();
            }
        } catch (Exception e) {
            LOG.error("Unexpected stop exception: ", e);
            System.exit(2);
        }
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {

         app = new AppEntry();
         app.run(args);
    }

    /**
     * Starts application (the alternative entry point).
     * (Required by service wrappers.)
     *
     * @param args the command line arguments
     */
    public static void start(String[] args) {

        app = new AppEntry();
        new Thread(() -> app.run(args)).start();
    }

    /**
     * Stops application (the alternative entry point).
     * (Required by service wrappers.)
     *
     * @param args the command line arguments
     */
    @SuppressWarnings("UnusedParameters")
    public  static void stop(String[] args) {

        if (app != null) {
            app.destroy();
        }
    }
}
