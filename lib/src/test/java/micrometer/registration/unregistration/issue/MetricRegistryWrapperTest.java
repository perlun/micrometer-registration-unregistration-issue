package micrometer.registration.unregistration.issue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import com.google.common.collect.ImmutableMap;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

public class MetricRegistryWrapperTest {

    private MicrometerRegistryFactory registryFactory;

    @BeforeEach
    void setUp() {
        this.registryFactory = new MicrometerRegistryFactory( ImmutableMap.of() );
    }

    @AfterEach
    public void tearDown() {
        registryFactory.close();
    }

    @RepeatedTest( 250 )
    void recorder_job_metric_has_expected_jmx_characteristics() {
        // The purpose of this test is to provoke errors in the other test. It doesn't do anything useful in itself.
        MeterRegistry meterRegistry = registryFactory.registry( "centre.job.recorder.programs" );

        Tags tags = Tags.of( "type", "processed" );

        // This is the line which triggers the errors.
        meterRegistry.counter( "centre.job.recorder.programs", tags );
    }
}
