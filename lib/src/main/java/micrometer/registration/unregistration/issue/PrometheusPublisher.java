package micrometer.registration.unregistration.issue;

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.Collector.MetricFamilySamples;

public class PrometheusPublisher {
    private static final Logger logger = LoggerFactory.getLogger( PrometheusPublisher.class );

    private static final String PATH = "metrics";

    static {
        // Note: sun.net.httpserver has these timeouts set to -1 by default (meaning requests and
        // responses can potentially block the single Prometheus publisher thread indefinitely).
        // Regretfully, these cannot be set using any HttpServer method; setting these system
        // properties is the only way to affect the instantiated HttpServer instance.
        System.setProperty( "sun.net.httpserver.maxReqTime", "60" );
        System.setProperty( "sun.net.httpserver.maxRspTime", "600" );
    }

    private final InetSocketAddress socketAddress;
    private final String hostname;

    private int port;
    private HttpServer server;

    private ExecutorService executorService;
    private PrometheusMeterRegistry registry;

    public PrometheusPublisher( String hostname, int port ) {
        this.hostname = hostname;
        socketAddress = new InetSocketAddress( hostname, port );
    }

    /**
     * Starts the Prometheus listener on the configured host and port, and registers a
     * PrometheusMeterRegistry in the Micrometer list of meter registries.
     */
    public void listen() {
        HttpServer server;

        try {
            server = HttpServer.create( socketAddress, 1 );
            InetSocketAddress address = server.getAddress();
            port = address.getPort();

            executorService = createExecutorService();

            server.setExecutor( executorService );
        }
        catch ( IOException e ) {
            throw new RuntimeException( "unable to start prometheus metrics server, listening at " + socketAddress, e );
        }

        Thread serverHandlerThread = new Thread( server::start, "prometheus-publisher" );
        serverHandlerThread.start();
        this.server = server;

        registry = new PrometheusMeterRegistry( new PrometheusMeterRegistryConfigImpl() );

        // Prometheus doesn't support time series with the same name but with a different set of
        // tags. By default (since version 1.6), Micrometer silently ignores any invalid time
        // series. Enabling hard failures is now opt-in. See
        // https://github.com/micrometer-metrics/micrometer/issues/2068 for more details.
        registry.throwExceptionOnRegistrationFailure();

        logger.info( "publishing Prometheus metrics at http://{}:{}/{}", hostname, port, PATH );
        this.server.createContext( "/" + PATH, httpExchange -> handleRequest( httpExchange, registry ) );

        Metrics.addRegistry( registry );
    }

    private void handleRequest( HttpExchange httpExchange, PrometheusMeterRegistry registry ) throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();

        // Quoting the sendResponseHeaders Javadocs: "If the response length parameter is zero, then
        // chunked transfer encoding is used and an arbitrary amount of data may be sent."
        httpExchange.sendResponseHeaders( 200, 0 );

        try ( OutputStream responseBody = httpExchange.getResponseBody() ) {
            try ( OutputStreamWriter writer = new OutputStreamWriter( responseBody ) ) {
                registry.scrape( writer );

                Enumeration<MetricFamilySamples> metricFamilySamples = registry.getPrometheusRegistry().metricFamilySamples();

                if ( !metricFamilySamples.hasMoreElements() ) {
                    logger.error( "NO METRICS! This is the error" );
                }

                while ( metricFamilySamples.hasMoreElements() ) {
                    logger.warn( "element: {}", metricFamilySamples.nextElement() );
                }
            }
        }

        logger.trace( "scraped Prometheus metrics for {} in {}", httpExchange.getRemoteAddress(), stopwatch );
    }

    public void close() {
        // If the publisher is already stopped, just ignore the method call
        if ( server == null ) {
            return;
        }

        server.stop( 0 );
        executorService.shutdown(); // Free any (parked/idle) threads in pool

        Metrics.removeRegistry( registry );

        logger.info( "Prometheus publisher stopped at http://{}:{}/{}", hostname, port, PATH );
        server = null;
    }

    @VisibleForTesting
    public int getPort() {
        checkState( port != 0, "The Prometheus listener has not yet been started" );

        return port;
    }

    private ExecutorService createExecutorService() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor( 1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), Executors.defaultThreadFactory() );
        executor.setMaximumPoolSize( 1 );
        executor.setCorePoolSize( 1 );
        return executor;
    }
}
