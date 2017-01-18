package gov.uspto.patent.doc.cpc.masterfile;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;

import gov.uspto.common.filter.SuffixFilter;
import gov.uspto.patent.bulk.BulkArchive;
import gov.uspto.patent.bulk.DumpFile;
import gov.uspto.patent.model.classification.ClassificationPredicate;
import gov.uspto.patent.model.classification.CpcClassification;
import gov.uspto.patent.model.classification.PatentClassification;
import gov.uspto.patent.serialize.DocumentBuilder;
import gov.uspto.patent.thread.DumpFileProcessThread;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * 
 * Build CSV File from CPC Master
 * 
 * <p>
 * CSV fields: grantIdFull,grantCC,grantId,grantKind,appIdFull,appCC,appId,appKind,cpcLevel,cpcClass
 * </p>
 * 
 * <pre>
 * Time to process US_Grant_CPC_MCF 
 *    (95 xml files, about 98,000 to 100,150 records per file)
 *    on a laptop with 8 threads, at 97% cpu, takes approximately 10 minutes.
 * </pre>
 * 
 * <p>
 * --input="..\download\US_Grant_CPC_MCF_XML_2016-12-31.zip" --cpc="H04N,H04H,G06F"
 * <p>
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class CpcMasterParser extends BulkArchive {
    private static final Logger LOGGER = LoggerFactory.getLogger(CpcMasterParser.class);

    //private static final int CPU_PROCESSORS = Runtime.getRuntime().availableProcessors();

    private static FileFilter fileFilter = new SuffixFilter("xml");

    private DocumentBuilder<MasterClassificationRecord> docBuilder;
    private Path outputDir;
    private Predicate<PatentClassification> classPredicate;

    private TransferQueue<Runnable> recordQueue;

    public CpcMasterParser(File file, DocumentBuilder<MasterClassificationRecord> docBuilder, Path outputDir) {
        super(file, fileFilter);
        this.outputDir = outputDir;
        this.docBuilder = docBuilder;
    }

    public void setClassificationPredicate(Predicate<PatentClassification> predicate) {
        this.classPredicate = predicate;
    }

    public void skipMasterDoc(int skip) {
        skip(skip);
    }

    public void process(int maxThreads) {
        recordQueue = new LinkedTransferQueue<Runnable>();

        ThreadPoolExecutor executor = new ThreadPoolExecutor(maxThreads, maxThreads, 1, TimeUnit.MINUTES, recordQueue,
                new ThreadPoolExecutor.CallerRunsPolicy());
        executor.prestartAllCoreThreads();

        CpcMasterReader reader = new CpcMasterReader();

        if (classPredicate != null) {
            reader.setClassificationPredicate(classPredicate);
        }

        while (hasNext()) {
            DumpFile dumpFile = next();
            File outputFile = outputDir.resolve("cpc_master_" + dumpFile.getFile().getName() + ".csv").toFile();

            DumpFileProcessThread workThread = new DumpFileProcessThread(dumpFile, reader, docBuilder, outputFile); // @TODO add predicate.

            if (recordQueue.size() < maxThreads * 3) {
                recordQueue.add(workThread);
            } else {
                try {
                    recordQueue.transfer(workThread);
                } catch (InterruptedException e) {
                    LOGGER.error("LinkedTransferQueue Interrupted", e);
                }
            }
        }

        executor.shutdown();

        while (!executor.isTerminated()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                LOGGER.error("ThreadPoolExecutor Interrupted", e);
            }
        }
    }

    public static void main(String[] args) throws IOException {

        LOGGER.info("--- START ---");

        OptionParser parser = new OptionParser() {
            {
                accepts("input").withRequiredArg().ofType(String.class).describedAs("Input Master CPC Zip File")
                        .required();
                accepts("skip").withOptionalArg().ofType(Integer.class).describedAs("skip number of master cpc files")
                        .defaultsTo(0);
                accepts("limit").withOptionalArg().ofType(Integer.class).describedAs("limit master cpc files to parse")
                        .defaultsTo(0);
                accepts("threads").withOptionalArg().ofType(Integer.class).describedAs("Threads to spawn")
                        .defaultsTo(5);
                accepts("outdir").withOptionalArg().ofType(String.class).describedAs("directory").defaultsTo("output");
                accepts("cpc").withOptionalArg().ofType(String.class)
                        .describedAs("comma separated list of wanted CPC Classifications");
            }
        };

        OptionSet options = parser.parse(args);
        if (!options.hasOptions()) {
            parser.printHelpOn(System.out);
            System.exit(1);
        }

        String inputZipFile = (String) options.valueOf("input");
        Path zipFilePath = Paths.get(inputZipFile);
        File zipFile = zipFilePath.toFile();
        if (!zipFile.canRead()) {
            LOGGER.error("Failed to read: '{}'", zipFile.getAbsolutePath());
            System.exit(1);
        }

        int skip = (Integer) options.valueOf("skip");
        int limit = (Integer) options.valueOf("limit"); // limit is not currently used.
        int threads = (Integer) options.valueOf("threads");
        String outDir = (String) options.valueOf("outdir");
        Path outputPath = Paths.get(outDir);

        /*
         * Cpc Predicates
         */
        String cpcInput = (String) options.valueOf("cpc");
        Predicate<PatentClassification> classPredicate = null;
        if (cpcInput != null) {
            Iterable<String> wantedCpcClasses = Splitter.on(',').trimResults().omitEmptyStrings().split(cpcInput);
            LOGGER.info("Classifications wanted: {}", wantedCpcClasses);
            classPredicate = ClassificationPredicate.isContained(wantedCpcClasses, CpcClassification.class);
            LOGGER.info("Classification predicate built");
        }

        if (!outputPath.toFile().isDirectory()) {
            outputPath.toFile().mkdir();
        }

        MasterCpcCsvBuilder docBuilder = new MasterCpcCsvBuilder();

        CpcMasterParser cpcMaster = new CpcMasterParser(zipFile, docBuilder, outputPath);

        if (classPredicate != null) {
            cpcMaster.setClassificationPredicate(classPredicate);
        }

        cpcMaster.open();

        if (skip > 0) {
            cpcMaster.skipMasterDoc(skip);
        }

        cpcMaster.process(threads);

        cpcMaster.close();

        LOGGER.info("--- DONE ---");
    }

}
