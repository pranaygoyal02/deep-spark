package com.stratio.deep.mongodb.extractor;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.google.common.io.Resources;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongoImportExecutable;
import de.flapdoodle.embed.mongo.MongoImportStarter;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.ArtifactStoreBuilder;
import de.flapdoodle.embed.mongo.config.DownloadConfigBuilder;
import de.flapdoodle.embed.mongo.config.IMongoImportConfig;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongoCmdOptionsBuilder;
import de.flapdoodle.embed.mongo.config.MongoImportConfigBuilder;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.config.Storage;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.io.file.Files;
import de.flapdoodle.embed.process.runtime.Network;

/**
 * Created by rcrespo on 16/07/14.
 */
@Test(suiteName = "mongoRddTests", groups = { "MongoJavaRDDTest" })
public class MongoJavaRDDTest {

    public static MongodExecutable mongodExecutable = null;
    public static MongoClient mongo = null;

    public static DBCollection col = null;

    public static final String MESSAGE_TEST = "new message test";

    public static final Integer PORT = 27890;

    public static final String HOST = "localhost";

    public static final String DATABASE = "test";

    public static final String COLLECTION_INPUT = "input";

    public static final String COLLECTION_OUTPUT = "output";

    public static final String COLLECTION_OUTPUT_CELL = "outputcell";

    public static final String DATA_SET_NAME = "divineComedy.json";

    public final static String DB_FOLDER_NAME = System.getProperty("user.home") +
            File.separator + "mongoEntityRDDTest";

    public static final Long WORD_COUNT_SPECTED = 3833L;

    public static final String DATA_SET_URL = "http://docs.openstratio.org/resources/datasets/divineComedy.json";

    private static MongodProcess mongod;

    @BeforeSuite
    public static void init() throws IOException {
        Command command = Command.MongoD;

        try {
            Files.forceDelete(new File(DB_FOLDER_NAME));
        } catch (Exception e) {

        }

        new File(DB_FOLDER_NAME).mkdirs();

        IMongodConfig mongodConfig = new MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .configServer(false)
                .replication(new Storage(DB_FOLDER_NAME, null, 0))
                .net(new Net(PORT, Network.localhostIsIPv6()))
                .cmdOptions(new MongoCmdOptionsBuilder()
                        .syncDelay(10)
                        .useNoPrealloc(true)
                        .useSmallFiles(true)
                        .useNoJournal(true)
                        .build())
                .build();

        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
                .defaults(command)
                .artifactStore(new ArtifactStoreBuilder()
                        .defaults(command)
                        .download(new DownloadConfigBuilder()
                                .defaultsForCommand(command)
                                .downloadPath("https://s3-eu-west-1.amazonaws.com/stratio-mongodb-distribution/")))
                .build();

        MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);

        mongodExecutable = null;

        mongodExecutable = runtime.prepare(mongodConfig);

        mongod = mongodExecutable.start();

        mongo = new MongoClient(HOST, PORT);
        DB db = mongo.getDB(DATABASE);
        col = db.getCollection(COLLECTION_INPUT);
        Map<String, String> map = new HashMap<>();
        map.put("id", "messageTest");
        map.put("message", MESSAGE_TEST);
        col.save(new BasicDBObject(map));

        dataSetImport();

    }

    @Test
    public void testRDD() {
        assertEquals(true, true);
    }

    /**
     * Imports dataset
     *
     * @throws java.io.IOException
     */
    private static void dataSetImport() throws IOException {
        String dbName = "book";
        String collection = "input";
        URL url = Resources.getResource(DATA_SET_NAME);
        IMongoImportConfig mongoImportConfig = new MongoImportConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(PORT, Network.localhostIsIPv6()))
                .db(dbName)
                .collection(collection)
                .upsert(false)
                .dropCollection(true)
                .jsonArray(true)
                .importFile(url.getFile())
                .build();
        MongoImportExecutable mongoImportExecutable = MongoImportStarter.getDefaultInstance()
                .prepare(mongoImportConfig);
        mongoImportExecutable.start();

    }

    @AfterSuite
    public static void cleanup() {
        try {
            if (mongodExecutable != null) {
                mongodExecutable.stop();
            }
        } finally {
            Files.forceDelete(new File(DB_FOLDER_NAME));
        }

    }
}
