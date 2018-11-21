package com.theappsolutions.nanostream.gcs;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.gson.Gson;
import com.theappsolutions.nanostream.models.GCloudNotification;
import org.apache.beam.sdk.testing.TestPipeline;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.Mockito.*;

/**
 * Tests {@link GCSService} blob searching method
 */
public class GCSServiceTest {

    @Rule
    public final transient TestPipeline testPipeline = TestPipeline.create().enableAbandonedNodeEnforcement(true);
    private Gson gson;


    @Before
    public void setup() {
        gson = new Gson();
    }

    @Test
    public void testBlobSearchingByBlobId() throws IOException {
        String testBucketName = "bucket_name";
        String testBlobName = "blob_name";

        Storage mockStorage = mock(Storage.class);

        GCSService gcsService = new GCSService(mockStorage);

        GCloudNotification mockGCloudNotification = mock(GCloudNotification.class);
        when(mockGCloudNotification.getBucket()).thenReturn(testBucketName);
        when(mockGCloudNotification.getName()).thenReturn(testBlobName);

        gcsService.getBlobByGCloudNotificationData(mockGCloudNotification.getBucket(), mockGCloudNotification.getName());

        verify(mockStorage).get(BlobId.of(mockGCloudNotification.getBucket(), mockGCloudNotification.getName()));
    }
}
