package demo;

import com.orientechnologies.orient.core.storage.ORecordDuplicatedException;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientEdge;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ops4j.orient.spring.tx.OrientBlueprintsGraphFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@ContextConfiguration(classes = OrientalspringApplication.class)
@TestExecutionListeners({TransactionalTestExecutionListener.class})
@Transactional
public class OrientDBGeneralTests extends AbstractJUnit4SpringContextTests {

    private static final Logger LOG = LoggerFactory.getLogger(OrientDBGeneralTests.class);

    @Autowired
    OrientBlueprintsGraphFactory orientGraphFactory;

    @Before
    public void setup() {
        //
    }

    @After
    public void cleanup() {
        //
    }

    @Test
    public void testBasicGraphUsage() throws Exception {
        assertNotNull(orientGraphFactory);

        OrientGraph graph = orientGraphFactory.graph();
        assertNotNull(graph);

        Map<String, Object> values = new HashMap<>();
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
    }

    @Test(expected = ORecordDuplicatedException.class)
    public void testUniqueIndex() throws Exception {
        assertNotNull(orientGraphFactory);

        OrientGraph graph = orientGraphFactory.graph();
        assertNotNull(graph);

        Map<String, Object> values = new HashMap<>();
        values.put("name", "Luca");
        values.put("age", 22);
        graph.addVertex("class:" + ModelClasses.PERSON, values);

        values.clear();
        values.put("name", "Luca");
        values.put("age", 69);
        graph.addVertex("class:" + ModelClasses.PERSON, values);

        graph.commit();
        fail();
    }

    @Test
    @Ignore("Disabled to prevent travis timeouts")
    public void testIndexAndStress() throws Exception {
        assertNotNull(orientGraphFactory);

        OrientGraph graph = orientGraphFactory.graph();
        assertNotNull(graph);

        //TODO add departments
        final long numberOfPersons = 500000L;
        long idx = 0;
        Map<String, Object> values = new HashMap<>();
        StopWatch watch = new StopWatch();
        watch.start();
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

    }
}
