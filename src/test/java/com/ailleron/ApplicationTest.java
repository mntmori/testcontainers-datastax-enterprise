package com.ailleron;

import com.datastax.driver.core.Row;
import com.datastax.driver.dse.DseCluster;
import com.datastax.driver.dse.DseSession;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.MountableFile;

import java.util.List;

public class ApplicationTest {

    Logger logger = LoggerFactory.getLogger(ApplicationTest.class);

    @ClassRule
    public static GenericContainer dseServer =
            new FixedHostPortGenericContainer("datastax/dse-server:6.7.2")
                    .withCommand("-s")
                    .withEnv("DS_LICENSE", "accept")
                    .withExposedPorts(9042, 8983);


    @Before
    public void setUp() throws Exception {

        logger.info("!!!!!!!! Ports: 9042: {}, 8983: {} !!!!!!!!",
                dseServer.getMappedPort(9042), dseServer.getMappedPort(8983));

        dseServer.execInContainer("mkdir", "/opt/dse/resources/solr/lib/custom");

        /**
         * DSE Customization plugin
         */
        dseServer.copyFileToContainer(MountableFile.forClasspathResource("dse-fit.jar"), "/opt/dse/resources/solr/lib/dse-fit.jar");

        /**
         * CORE files
         */
        dseServer.copyFileToContainer(MountableFile.forClasspathResource("schema.xml"), "/opt/dse/schema.xml");
        dseServer.copyFileToContainer(MountableFile.forClasspathResource("solrconfig.xml"), "/opt/dse/solrconfig.xml");

        /**
         * CQL SCHEMA
         */
        dseServer.copyFileToContainer(MountableFile.forClasspathResource("schema.cql"), "/opt/dse/schema.cql");
        setupSearchCore();

    }

    private void setupSearchCore() throws java.io.IOException, InterruptedException {
        // CREATE KEYSPACE
        dseServer.execInContainer("cqlsh", "-f", "/opt/dse/schema.cql");

        // CREATE CORE
        dseServer.execInContainer("dsetool", "create_core", "sort.sort_table", "schema=/opt/dse/schema.xml", "solrconfig=/opt/dse/solrconfig.xml");
    }


    @Test
    public void testDatastax() {
        try (DseCluster dseCluster = DseCluster.builder()
                .addContactPoint("127.0.0.1")
                .withPort(dseServer.getMappedPort(9042))
                .build()) {

            DseSession dseSession = dseCluster.connect();
            List<Row> rows = dseSession.execute("select * from sort.sort_table where solr_query = '{\"q\": \"*:*\"}';").all();
            Assert.assertFalse(rows.isEmpty());
        }
    }

    @Test
    public void testSort() {
        try (DseCluster dseCluster = DseCluster.builder()
                .addContactPoint("127.0.0.1")
                .withPort(dseServer.getMappedPort(9042))
                .build()) {

            DseSession dseSession = dseCluster.connect();
            List<Row> rows = dseSession
                    .execute("select * from sort.sort_table where solr_query = '{\"q\": \"*:*\", \"sort\": \"shortname_ci asc\"}';").all();
            Assert.assertFalse(rows.isEmpty());
        }
    }

    @Test
    public void testFilter() {
        try (DseCluster dseCluster = DseCluster.builder()
                .addContactPoint("127.0.0.1")
                .withPort(dseServer.getMappedPort(9042))
                .build()) {

            DseSession dseSession = dseCluster.connect();
            List<Row> rows = dseSession
                    .execute("select * from sort.sort_table where solr_query = '{\"q\": \"*:*\", \"fq\": \"shortname_ci:*d*\" \"sort\": \"shortname_ci asc\"}';").all();
            Assert.assertFalse(rows.isEmpty());
        }
    }

    @Test
    public void testAsciiFiltering() {
        try (DseCluster dseCluster = DseCluster.builder()
                .addContactPoint("127.0.0.1")
                .withPort(dseServer.getMappedPort(9042))
                .build()) {

            DseSession dseSession = dseCluster.connect();
            List<Row> rows = dseSession
                    .execute("select * from sort.sort_table where solr_query = '{\"q\": \"*:*\", \"fq\": \"shortname_ci:*s*\" \"sort\": \"shortname_ci asc\"}';").all();
            Assert.assertFalse(rows.isEmpty());
            Assert.assertEquals(3, rows.size());
        }
    }
}