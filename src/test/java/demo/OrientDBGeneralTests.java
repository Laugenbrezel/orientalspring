package demo;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.storage.ORecordDuplicatedException;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.*;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.ui.Model;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import static com.orientechnologies.orient.core.metadata.schema.OType.INTEGER;
import static com.orientechnologies.orient.core.metadata.schema.OType.STRING;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = OrientalspringApplication.class)
public class OrientDBGeneralTests {

    private static final Logger LOG = LoggerFactory.getLogger(OrientDBGeneralTests.class);

    @Autowired
    OrientGraphFactory orientGraphFactory;

    @Before
    public void setup() {
        OrientGraphNoTx graphNoTx = orientGraphFactory.getNoTx();
        try {
            OrientVertexType personType = graphNoTx.createVertexType(ModelClasses.PERSON);
            personType.createProperty("name", STRING);
            personType.createProperty("age", INTEGER);
            personType.createIndex("Person.name", OClass.INDEX_TYPE.UNIQUE, "name");

            OrientVertexType depType = graphNoTx.createVertexType(ModelClasses.DEPARTMENT);
            depType.createProperty("name", STRING);
            depType.createIndex("Department.name", OClass.INDEX_TYPE.UNIQUE, "name");

            OrientEdgeType worksAtEdgeType = graphNoTx.createEdgeType(EdgeTypes.WORKS_AT);
            worksAtEdgeType.createProperty("since", OType.DATETIME);
        } finally {
            graphNoTx.shutdown();
        }
    }

    @After
    public void cleanup() {
        OrientGraphNoTx graphNoTx = orientGraphFactory.getNoTx();
        try {
            graphNoTx.dropVertexType(ModelClasses.PERSON);
            graphNoTx.dropVertexType(ModelClasses.DEPARTMENT);
            graphNoTx.dropEdgeType(EdgeTypes.WORKS_AT);
        } finally {
            graphNoTx.shutdown();
        }
    }

    @Test
    public void testBasicGraphUsage() throws Exception {
        assertNotNull(orientGraphFactory);

        OrientGraph graph = orientGraphFactory.getTx();
        assertNotNull(graph);

        Map<String, Object> values = new HashMap<>();
        try {
            values.put("name", "Luca");
            values.put("age", 22);
            Vertex luca = graph.addVertex("class:" + ModelClasses.PERSON, values);

            values.clear();
            values.put("name", "Marko");
            values.put("age", 69);
            Vertex marko = graph.addVertex("class:" + ModelClasses.PERSON, values);

            values.clear();
            values.put("name", "OBI");
            Vertex obi = graph.addVertex("class:" + ModelClasses.DEPARTMENT, values);

            OrientEdge newEdge = graph.addEdge("class:" + EdgeTypes.WORKS_AT, luca, obi, EdgeTypes.WORKS_AT);
            newEdge.setProperty("since", Date.from(LocalDate.of(1970, 12, 24).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));

            newEdge = graph.addEdge("class:" + EdgeTypes.WORKS_AT, marko, obi, EdgeTypes.WORKS_AT);
            newEdge.setProperty("since", Date.from(LocalDate.of(2001, 1, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));

            graph.commit();

            Vertex person = graph.getVertexByKey(ModelClasses.PERSON + ".name", "Marko");
            assertNotNull(person);
            assertEquals("Marko", person.getProperty("name"));
            Iterable<Edge> edges = person.getEdges(Direction.OUT, EdgeTypes.WORKS_AT);
            assertNotNull(edges);
            edges.forEach(worksAt -> {
                assertNotNull(worksAt.getProperty("since"));
                Vertex department = worksAt.getVertex(Direction.IN);
                assertNotNull(department);
                assertEquals("OBI", department.getProperty("name"));
            });
        } finally {
            graph.shutdown();
        }
    }

    @Test(expected = ORecordDuplicatedException.class)
    public void testUniqueIndex() throws Exception {
        assertNotNull(orientGraphFactory);

        OrientGraph graph = orientGraphFactory.getTx();
        assertNotNull(graph);

        Map<String, Object> values = new HashMap<>();
        try {
            values.put("name", "Luca");
            values.put("age", 22);
            graph.addVertex("class:" + ModelClasses.PERSON, values);

            values.clear();
            values.put("name", "Luca");
            values.put("age", 69);
            graph.addVertex("class:" + ModelClasses.PERSON, values);

            graph.commit();
            fail();
        } finally {
            graph.shutdown();
        }
    }

    @Test
    @Ignore
    public void testIndexAndStress() throws Exception {
        assertNotNull(orientGraphFactory);

        OrientGraph graph = orientGraphFactory.getTx();
        assertNotNull(graph);

        //TODO add departments
        final long numberOfPersons = 500000L;
        long idx = 0;
        Map<String, Object> values = new HashMap<>();
        StopWatch watch = new StopWatch();
        watch.start();
        try {
            while (idx < numberOfPersons) {
                if (idx == 200007) {
                    values.put("name", "Captain Future");
                } else {
                    values.put("name", RandomStringUtils.randomAlphabetic(14));
                }
                values.put("age", 77);
                graph.addVertex("class:" + ModelClasses.PERSON, values);
                idx++;
                values.clear();

                if (idx % 5000 == 0) {
                    graph.commit();
                    LOG.debug("## done " + idx + " / " + numberOfPersons + " ( " + ((double) idx / numberOfPersons) * 100 + "% )");
                }
            }
            graph.commit();
            watch.stop();
            LOG.info("Time to save {} new Person vertices: {}", numberOfPersons, watch.toString());

            watch.reset();
            watch.start();
            Vertex captain = graph.getVertexByKey(ModelClasses.PERSON + ".name", "Captain Future");
            assertNotNull(captain);
            watch.stop();
            LOG.info("Time to find the Captain: {}", watch.toString());

        } finally {
            graph.shutdown();
        }
    }

    private class ModelClasses {
        public static final String PERSON = "Person";
        public static final String DEPARTMENT = "Department";
    }

    private class EdgeTypes {
        public static final String WORKS_AT = "worksAt";
    }
}
