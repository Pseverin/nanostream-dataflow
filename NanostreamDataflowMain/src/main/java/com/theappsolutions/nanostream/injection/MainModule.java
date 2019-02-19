package com.theappsolutions.nanostream.injection;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.theappsolutions.nanostream.aligner.MakeAlignmentViaHttpFn;
import com.theappsolutions.nanostream.geneinfo.LoadGeneInfoTransform;
import com.theappsolutions.nanostream.http.NanostreamHttpService;
import com.theappsolutions.nanostream.kalign.ProceedKAlignmentFn;
import com.theappsolutions.nanostream.other.Constants;
import com.theappsolutions.nanostream.output.PrepareSequencesStatisticToOutputDbFn;
import com.theappsolutions.nanostream.output.WriteSequencesBodiesToFirestoreDbFn;
import com.theappsolutions.nanostream.output.WriteSequencesStatisticToFirestoreDbFn;
import com.theappsolutions.nanostream.taxonomy.GetSpeciesTaxonomyDataFn;
import com.theappsolutions.nanostream.util.EntityNamer;
import com.theappsolutions.nanostream.util.HttpHelper;

import static com.theappsolutions.nanostream.other.Constants.SEQUENCES_STATISTIC_DOCUMENT_NAME_BASE;

/**
 * App dependency injection module, that provide graph of main dependencies in app
 */
public class MainModule extends NanostreamModule {

    public MainModule(Builder builder) {
        super(builder);
    }


    public static class Builder extends NanostreamModule.Builder {

        @Override
        public MainModule build() {
            return new MainModule(this);
        }
    }

    @Provides
    @Singleton
    public NanostreamHttpService provideNanostreamHttpService(HttpHelper httpHelper) {
        return new NanostreamHttpService(httpHelper, servicesUrl);
    }

    @Provides
    public MakeAlignmentViaHttpFn provideMakeAlignmentViaHttpFn(NanostreamHttpService service) {
        return new MakeAlignmentViaHttpFn(service, bwaDB, bwaEndpoint);
    }

    @Provides
    public ProceedKAlignmentFn provideProceedKAlignmentFn(NanostreamHttpService service) {
        return new ProceedKAlignmentFn(service, kAlignEndpoint);
    }

    @Provides
    public WriteSequencesStatisticToFirestoreDbFn provideWriteDataToFirestoreDbFnStatistic() {
        return new WriteSequencesStatisticToFirestoreDbFn(
                EntityNamer.addPrefixWithProcessingMode(Constants.SEQUENCES_STATISTIC_COLLECTION_NAME_BASE,
                        processingMode,
                        outputFirestoreCollectionNamePrefix),
                projectId);
    }

    @Provides
    public WriteSequencesBodiesToFirestoreDbFn provideWriteSequencesBodiesToFirestoreDbFn() {
        return new WriteSequencesBodiesToFirestoreDbFn(
                EntityNamer.addPrefixWithProcessingMode(Constants.SEQUENCES_BODIES_COLLECTION_NAME_BASE,
                        processingMode,
                        outputFirestoreCollectionNamePrefix),
                projectId);
    }

    @Provides
    public GetSpeciesTaxonomyDataFn provideGetTaxonomyDataFn() {
        return new GetSpeciesTaxonomyDataFn(EntityNamer.addPrefix(Constants.GENE_CACHE_COLLECTION_NAME_BASE,
                processingMode.label), projectId);
    }

    @Provides
    public LoadGeneInfoTransform provideLoadGeneInfoTransform() {
        return new LoadGeneInfoTransform(resistanceGenesFastaDB, resistanceGenesList);
    }

    @Provides
    public PrepareSequencesStatisticToOutputDbFn providePrepareSequencesStatisticToOutputDbFn(EntityNamer entityNamer) {
        String documentName = outputFirestoreStatiscticDocumentName != null
                ? outputFirestoreStatiscticDocumentName
                : entityNamer.generateTimestampedName(SEQUENCES_STATISTIC_DOCUMENT_NAME_BASE);
        return new PrepareSequencesStatisticToOutputDbFn(documentName, entityNamer.getInitialTimestamp());
    }
}
