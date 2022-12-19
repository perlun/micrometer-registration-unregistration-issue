# README

This repository exists to reproduce an issue we are seeing with Micrometer registration and unregistration from tests.

### How to run these tests

```shell
$ ./gradlew test
```

This is expected to fail with output similar to this:

```
> Task :lib:test

MicrometerRegistrationTest > recorder_job_metric_is_published_using_expected_name() > micrometer.registration.unregistration.issue.MicrometerRegistrationTest.recorder_job_metric_is_published_using_expected_name()[1] FAILED
    java.lang.AssertionError at MicrometerRegistrationTest.java:65

MicrometerRegistrationTest > recorder_job_metric_is_published_using_expected_name() > micrometer.registration.unregistration.issue.MicrometerRegistrationTest.recorder_job_metric_is_published_using_expected_name()[2] FAILED
    java.lang.AssertionError at MicrometerRegistrationTest.java:65

MicrometerRegistrationTest > recorder_job_metric_is_published_using_expected_name() > micrometer.registration.unregistration.issue.MicrometerRegistrationTest.recorder_job_metric_is_published_using_expected_name()[3] FAILED
    java.lang.AssertionError at MicrometerRegistrationTest.java:65

MicrometerRegistrationTest > recorder_job_metric_is_published_using_expected_name() > micrometer.registration.unregistration.issue.MicrometerRegistrationTest.recorder_job_metric_is_published_using_expected_name()[4] FAILED
    java.lang.AssertionError at MicrometerRegistrationTest.java:65

MicrometerRegistrationTest > recorder_job_metric_is_published_using_expected_name() > micrometer.registration.unregistration.issue.MicrometerRegistrationTest.recorder_job_metric_is_published_using_expected_name()[5] FAILED
    java.lang.AssertionError at MicrometerRegistrationTest.java:65

MicrometerRegistrationTest > recorder_job_metric_is_published_using_expected_name() > micrometer.registration.unregistration.issue.MicrometerRegistrationTest.recorder_job_metric_is_published_using_expected_name()[6] FAILED
    java.lang.AssertionError at MicrometerRegistrationTest.java:65

MicrometerRegistrationTest > recorder_job_metric_is_published_using_expected_name() > micrometer.registration.unregistration.issue.MicrometerRegistrationTest.recorder_job_metric_is_published_using_expected_name()[7] FAILED
    org.opentest4j.AssertionFailedError at MicrometerRegistrationTest.java:67

MicrometerRegistrationTest > recorder_job_metric_is_published_using_expected_name() > micrometer.registration.unregistration.issue.MicrometerRegistrationTest.recorder_job_metric_is_published_using_expected_name()[8] FAILED
    java.lang.AssertionError at MicrometerRegistrationTest.java:65

MicrometerRegistrationTest > recorder_job_metric_is_published_using_expected_name() > micrometer.registration.unregistration.issue.MicrometerRegistrationTest.recorder_job_metric_is_published_using_expected_name()[9] FAILED
    java.lang.AssertionError at MicrometerRegistrationTest.java:65

MicrometerRegistrationTest > recorder_job_metric_is_published_using_expected_name() > micrometer.registration.unregistration.issue.MicrometerRegistrationTest.recorder_job_metric_is_published_using_expected_name()[10] FAILED
    org.opentest4j.AssertionFailedError at MicrometerRegistrationTest.java:67

MicrometerRegistrationTest > recorder_job_metric_is_published_using_expected_name() > micrometer.registration.unregistration.issue.MicrometerRegistrationTest.recorder_job_metric_is_published_using_expected_name()[11] FAILED
    java.lang.AssertionError at MicrometerRegistrationTest.java:65

MicrometerRegistrationTest > recorder_job_metric_is_published_using_expected_name() > micrometer.registration.unregistration.issue.MicrometerRegistrationTest.recorder_job_metric_is_published_using_expected_name()[12] FAILED
    java.lang.AssertionError at MicrometerRegistrationTest.java:65

MicrometerRegistrationTest > recorder_job_metric_is_published_using_expected_name() > micrometer.registration.unregistration.issue.MicrometerRegistrationTest.recorder_job_metric_is_published_using_expected_name()[13] FAILED
    java.lang.AssertionError at MicrometerRegistrationTest.java:65

MicrometerRegistrationTest > recorder_job_metric_is_published_using_expected_name() > micrometer.registration.unregistration.issue.MicrometerRegistrationTest.recorder_job_metric_is_published_using_expected_name()[14] FAILED
    java.lang.AssertionError at MicrometerRegistrationTest.java:65

MicrometerRegistrationTest > recorder_job_metric_is_published_using_expected_name() > micrometer.registration.unregistration.issue.MicrometerRegistrationTest.recorder_job_metric_is_published_using_expected_name()[15] FAILED
    java.lang.AssertionError at MicrometerRegistrationTest.java:65

MicrometerRegistrationTest > recorder_job_metric_is_published_using_expected_name() > micrometer.registration.unregistration.issue.MicrometerRegistrationTest.recorder_job_metric_is_published_using_expected_name()[16] FAILED
    java.lang.AssertionError at MicrometerRegistrationTest.java:65

MicrometerRegistrationTest > recorder_job_metric_is_published_using_expected_name() > micrometer.registration.unregistration.issue.MicrometerRegistrationTest.recorder_job_metric_is_published_using_expected_name()[17] FAILED
    java.lang.AssertionError at MicrometerRegistrationTest.java:65

MicrometerRegistrationTest > recorder_job_metric_is_published_using_expected_name() > micrometer.registration.unregistration.issue.MicrometerRegistrationTest.recorder_job_metric_is_published_using_expected_name()[18] FAILED
    java.lang.AssertionError at MicrometerRegistrationTest.java:65

MicrometerRegistrationTest > recorder_job_metric_is_published_using_expected_name() > micrometer.registration.unregistration.issue.MicrometerRegistrationTest.recorder_job_metric_is_published_using_expected_name()[19] FAILED
    java.lang.AssertionError at MicrometerRegistrationTest.java:65

MicrometerRegistrationTest > recorder_job_metric_is_published_using_expected_name() > micrometer.registration.unregistration.issue.MicrometerRegistrationTest.recorder_job_metric_is_published_using_expected_name()[20] FAILED
    java.lang.AssertionError at MicrometerRegistrationTest.java:65
```
