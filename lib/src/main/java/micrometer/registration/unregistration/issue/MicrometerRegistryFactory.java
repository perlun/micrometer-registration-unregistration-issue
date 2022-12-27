package micrometer.registration.unregistration.issue;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;

@VisibleForTesting
public class MicrometerRegistryFactory {
    private static final Logger logger = LoggerFactory.getLogger( MicrometerRegistryFactory.class );

    private final Map<String, CompositeMeterRegistry> meterRegistries = Maps.newConcurrentMap();
    private final CompositeMeterRegistry globalCompositeMeterRegistry = new CompositeMeterRegistry();

    private final String prometheusHost;

    @Nullable
    private PrometheusPublisher prometheusPublisher;

    @VisibleForTesting
    MicrometerRegistryFactory( Map<String, String> config ) {
        prometheusHost = config.getOrDefault( "prometheus_host", null );
    }

    public void close() {
        for ( CompositeMeterRegistry meterRegistry : meterRegistries.values() ) {
            meterRegistry.remove( globalCompositeMeterRegistry);
            globalCompositeMeterRegistry.remove( meterRegistry );
        }
    }

    /**
     * Initializes the Micrometer exporter(s). This method binds to the HTTP port(s) in question
     * and makes the metrics available to e.g. Prometheus.
     */
    public void initExporters() {
        // Cannot be performed in static initializer since we must be able to override the
        // Prometheus host/port for tests.
        if ( Strings.isNullOrEmpty( prometheusHost ) ) {
            logger.debug( "Prometheus metrics disabled" );
            prometheusPublisher = null;
        }
        else {
            prometheusPublisher = setupPrometheusPublisher( prometheusHost, globalCompositeMeterRegistry );
        }
    }

    public MeterRegistry registry( String identifier ) {
        return getOrCreateMeterRegistry( identifier );
    }

    private PrometheusPublisher setupPrometheusPublisher( String prometheusHost, CompositeMeterRegistry compositeMeterRegistry ) {
        String[] splitHostname = prometheusHost.split( ":" );

        if ( splitHostname.length != 2 ) {
            throw new RuntimeException( "invalid Prometheus hostname configured: " + prometheusHost );
        }
        else if ( tryParseInt( splitHostname[1], -1 ) == -1 ) {
            throw new RuntimeException( "invalid Prometheus port configured: " + prometheusHost );
        }

        String hostName = splitHostname[0];
        int port = Integer.parseInt( splitHostname[1] );

        PrometheusPublisher prometheusPublisher = new PrometheusPublisher( hostName, port, compositeMeterRegistry );
        prometheusPublisher.listen();

        Runtime.getRuntime().addShutdownHook( new Thread( this::closePrometheusPublisher ) );

        return prometheusPublisher;
    }

    @VisibleForTesting
    public int getPrometheusPort() {
        if ( prometheusPublisher != null ) {
            return prometheusPublisher.getPort();
        }
        else {
            throw new IllegalStateException( "The Prometheus listener is not enabled" );
        }
    }

    @VisibleForTesting
    public void closePrometheusPublisher() {
        try {
            if ( prometheusPublisher != null ) {
                prometheusPublisher.close();
            }
        }
        catch ( Exception ignore ) {
            // Not much we can do at this point; error is ignored.
        }
    }

    private MeterRegistry getOrCreateMeterRegistry( String identifier ) {
        MeterRegistry meterRegistry = meterRegistries.get( identifier );

        if ( meterRegistry != null ) {
            return meterRegistry;
        }
        else {
            synchronized ( Metrics.globalRegistry ) {
                return meterRegistries.computeIfAbsent( identifier, dummy -> {

                    // We need this extra level of indirection with an individual
                    // CompositeMeterRegistry to have a more fine-grained control over how the
                    // data is being published to JMX. (Otherwise, customization as seen above
                    // becomes impossible and all JMX metrics get registered under a common
                    // identifier.)
                    CompositeMeterRegistry newCompositeRegistry = new CompositeMeterRegistry();

                    newCompositeRegistry.add( globalCompositeMeterRegistry );

                    return newCompositeRegistry;
                } );
            }
        }
    }

    public static int tryParseInt( String intString, int defaultValue ) {
        if ( !Strings.isNullOrEmpty( intString ) ) {
            Integer num = Ints.tryParse( intString );
            if ( num != null ) {
                return num;
            }
        }
        return defaultValue;
    }
}
