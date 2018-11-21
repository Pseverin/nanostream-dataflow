package com.theappsolutions.nanostream.aligner;

import com.theappsolutions.nanostream.http.NanostreamHttpService;
import htsjdk.samtools.fastq.FastqRecord;
import org.apache.beam.sdk.transforms.DoFn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Makes alignment of fastq data via HTTP server and returnes string body of alignment HTTP response
 */
public class MakeAlignmentViaHttpFn extends DoFn<Iterable<FastqRecord>, String> {

    private final static String DATABASE_MULTIPART_KEY = "database";
    private final static String FASTQ_DATA_MULTIPART_KEY = "fastq";

    private Logger LOG = LoggerFactory.getLogger(MakeAlignmentViaHttpFn.class);

    private NanostreamHttpService nanostreamHttpService;
    private String database;
    private String endpoint;

    public MakeAlignmentViaHttpFn(NanostreamHttpService nanostreamHttpService,
                                  String database,
                                  String endpoint) {
        this.nanostreamHttpService = nanostreamHttpService;
        this.database = database;
        this.endpoint = endpoint;
    }

    @ProcessElement
    public void processElement(ProcessContext c) {
        Iterable<FastqRecord> data = c.element();

        Map<String, String> content = new HashMap<>();
        content.put(DATABASE_MULTIPART_KEY, database);
        content.put(FASTQ_DATA_MULTIPART_KEY, prepareFastQData(data));

        try {
            @Nonnull
            String responseBody = nanostreamHttpService.generateAlignData(endpoint, content);
            c.output(responseBody);
        } catch (URISyntaxException | IOException e) {
            LOG.error(e.getMessage());
        }
    }

    private String prepareFastQData(Iterable<FastqRecord> data) {
        StringBuilder fastqPostBody = new StringBuilder();
        for (FastqRecord fq : data) {
            fastqPostBody.append(fq.toFastQString()).append("\n");
        }
        return fastqPostBody.toString();
    }
}