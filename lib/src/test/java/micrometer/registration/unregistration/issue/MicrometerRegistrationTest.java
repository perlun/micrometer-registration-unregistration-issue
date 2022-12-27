package micrometer.registration.unregistration.issue;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

class MicrometerRegistrationTest {
    private static final Logger logger = LoggerFactory.getLogger( MicrometerRegistrationTest.class );

    private MicrometerRegistryFactory registryFactory;
    private String prometheusHost;

    @BeforeEach
    void setUp() {
        Pair<MicrometerRegistryFactory, String> result = createMicrometerRegistryFactory();
        registryFactory = result.getFirst();
        prometheusHost = result.getSecond();
    }

    @AfterEach
    void tearDown() {
        registryFactory.close();
    }

    @RepeatedTest( 150 )
    void recorder_job_metric_is_published_using_expected_name() throws Exception {
        // Arrange
        MeterRegistry meterRegistry = registryFactory.registry( "centre.job.recorder.programs" );

        // Act
        Tags tags = Tags.of( "type", "processed" );
        Counter counter = meterRegistry.counter( "centre.job.recorder.programs", tags );

        counter.increment( 42 );

        // Assert
        List<String> programsTotalResult = getPrometheusMetrics( prometheusHost, "centre_job_recorder_programs_total" );

        if ( programsTotalResult.size() != 1 ) {
            logger.warn( "UNEXPECTED NUMBER OF ROWS RETURNED" );
        }

        assertThat( programsTotalResult ).hasSize( 1 );

        assertThat( programsTotalResult.get( 0 ) ).isEqualTo( "centre_job_recorder_programs_total{type=\"processed\",} 42.0" );
    }

    public static Pair<MicrometerRegistryFactory, String> createMicrometerRegistryFactory() {
        String prometheusHost = "127.0.0.1";

        Map<String, String> configuration = ImmutableMap.of(
                "prometheus_host",

                // By setting the port to zero here, we let the Java runtime find & bind to the first
                // available port in a concurrency/thread-safe way.
                prometheusHost + ":0"
        );

        MicrometerRegistryFactory registryFactory = new MicrometerRegistryFactory( configuration );
        registryFactory.initExporters();

        return Pair.of( registryFactory, prometheusHost + ":" + registryFactory.getPrometheusPort() );
    }

    public static List<String> getPrometheusMetrics( String prometheusHost, String metricName ) throws IOException {
        List<String> responseLines = getAllPrometheusMetrics( prometheusHost );

        return responseLines
                .stream()
                .filter( line -> line.startsWith( metricName + "{" ) || line.startsWith( metricName + " " ) )
                .collect( toList() );
    }

    public static List<String> getAllPrometheusMetrics( String prometheusHost ) throws IOException {
        URL url = new URL( "http://" + prometheusHost + "/metrics" );

        try {
            return ImmutableList.copyOf( Resources.readLines( url, Charsets.UTF_8 ) );
        }
        catch ( SocketException e ) {
            fail( "Error connecting to " + url, e );

            // Will never be reached
            return ImmutableList.of();
        }
    }
}
