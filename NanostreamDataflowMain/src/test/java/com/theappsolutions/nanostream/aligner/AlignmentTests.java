package com.theappsolutions.nanostream.aligner;

import com.theappsolutions.nanostream.aligner.AlignerHttpService;
import com.theappsolutions.nanostream.aligner.MakeAlignmentViaHttpFn;
import com.theappsolutions.nanostream.aligner.ParseAlignedDataIntoSAMFn;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.fastq.FastqRecord;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.StreamSupport;

import static com.google.common.base.Charsets.UTF_8;
import static org.mockito.Mockito.*;

/**
 * Set of tests for fastq data alignment functionality
 */
public class AlignmentTests implements Serializable {

    private final static String SAM_RESULT = "337f59ec-bdd2-49b2-b16a-f5a990f4d85c 4796b unmapped read.";
    private final static String SAM_REFERENCE = "*";

    @Rule
    public final transient TestPipeline testPipeline = TestPipeline.create().enableAbandonedNodeEnforcement(true);

    @Test
    public void testFastQHttpAlignment() {
        try {
            String[] fastqData = IOUtils.toString(
                    getClass().getClassLoader().getResourceAsStream("fasqQSourceData.txt"), UTF_8.name())
                    .split("\n");

            String fastqAlignmentResult = IOUtils.toString(
                    getClass().getClassLoader().getResourceAsStream("fastQAlignmentResult.txt"), UTF_8.name());
            String fastQPrepearedForAlignmentData = IOUtils.toString(
                    getClass().getClassLoader().getResourceAsStream("fastQPrepearedForAlignment.txt"), UTF_8.name());

            FastqRecord fastqRecord = new FastqRecord(fastqData[0], fastqData[1], fastqData[2], fastqData[3]);
            Iterable<FastqRecord> fastqRecordIterable = Collections.singletonList(fastqRecord);

            AlignerHttpService mockAlignerHttpService = mock(AlignerHttpService.class,
                    withSettings().serializable());
            when(mockAlignerHttpService.generateAlignData(fastQPrepearedForAlignmentData)).thenReturn(fastqAlignmentResult);

            PCollection<String> parsedFastQ = testPipeline
                    .apply(Create.<Iterable<FastqRecord>>of(fastqRecordIterable))
                    .apply(ParDo.of(new MakeAlignmentViaHttpFn(mockAlignerHttpService)));
            PAssert.that(parsedFastQ)
                    .satisfies((SerializableFunction<Iterable<String>, Void>) input -> {
                        Iterator<String> dataIterator = input.iterator();
                        Assert.assertTrue(dataIterator.hasNext());
                        String resultData = dataIterator.next();
                        Assert.assertFalse(dataIterator.hasNext());

                        Assert.assertEquals(fastqAlignmentResult, resultData);
                        return null;
                    });

            testPipeline.run();
        } catch (IOException | URISyntaxException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testAlignedDataParsing() {
        try {
            String fastqAlignmentResult = IOUtils.toString(
                    getClass().getClassLoader().getResourceAsStream("fastQAlignmentResult.txt"), UTF_8.name());
            String samStringResult = IOUtils.toString(
                    getClass().getClassLoader().getResourceAsStream("samStringResult.txt"), UTF_8.name());

            PCollection<KV<String, SAMRecord>> parsedFastQ = testPipeline
                    .apply(Create.of(fastqAlignmentResult))
                    .apply(ParDo.of(new ParseAlignedDataIntoSAMFn()));

            PAssert.that(parsedFastQ)
                    .satisfies((SerializableFunction<Iterable<KV<String, SAMRecord>>, Void>) input -> {
                        KV<String, SAMRecord> result = input.iterator().next();
                        Assert.assertEquals(1, StreamSupport.stream(input.spliterator(), false)
                                .count());
                        Assert.assertEquals(SAM_REFERENCE, result.getKey());
                        Assert.assertEquals(SAM_RESULT, result.getValue().toString());
                        Assert.assertEquals(samStringResult, result.getValue().getSAMString());
                        return null;
                    });

            testPipeline.run();
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }
}
