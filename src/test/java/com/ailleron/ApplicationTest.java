package com.ailleron;

import com.datastax.driver.core.Row;
import com.datastax.driver.dse.DseCluster;
import com.datastax.driver.dse.DseSession;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;

import java.util.UUID;

public class ApplicationTest {

    @ClassRule
    public static GenericContainer dseServer =
            new FixedHostPortGenericContainer("datastax/dse-server:6.7.2")
                    .withCommand("-s")
                    .withEnv("DS_LICENSE", "accept")
                    .withExposedPorts(9042);


    @Before
    public void setUp() throws Exception {
        setupSearchCore();

    }

    private void setupSearchCore() throws java.io.IOException, InterruptedException {
        // CREATE KEYSPACE
        dseServer.execInContainer("cqlsh", "--execute",
                "CREATE KEYSPACE test " +
                        "WITH replication = {'class':'SimpleStrategy', 'replication_factor' : 1}");

        // CREATE TABLE
        dseServer.execInContainer("cqlsh", "--execute",
                "CREATE TABLE test.test (id uuid, name text, PRIMARY KEY(id))"
        );

        // INSERT SOME DATA
        dseServer.execInContainer("cqlsh", "--execute",
                "INSERT INTO test.test (id, name) VALUES (uuid(), '12312')"
        );

        // CREATE CORE
        dseServer.execInContainer("dsetool", "create_core", "test.test", "generateResources=true");
    }


    @Test
    public void testDatastax() {
        try (DseCluster dseCluster = DseCluster.builder()
                .addContactPoint("127.0.0.1")
                .withPort(dseServer.getMappedPort(9042))
                .build()) {

            DseSession dseSession = dseCluster.connect();
            Row row = dseSession.execute("select * from test.test where solr_query = '{\"q\": \"*:*\"}';").one();
            UUID id = row.getUUID("id");
            Assert.assertNotNull(id);
        }
    }
}