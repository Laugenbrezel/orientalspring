package demo;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import org.junit.Test;
import org.ops4j.orient.spring.tx.OrientBlueprintsGraphFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

@ContextConfiguration(classes = OrientalspringApplication.class)
@TestExecutionListeners({TransactionalTestExecutionListener.class})
@Transactional
public class OrientalspringApplicationTests extends AbstractJUnit4SpringContextTests {

    @Autowired
    OrientBlueprintsGraphFactory orientGraphFactory;

    @Test
    public void contextLoads() {
    }

    @Test
    public void testFactoryInitialized() throws Exception {
        assertNotNull(orientGraphFactory);

        OrientGraph graph = orientGraphFactory.graph();
        assertNotNull(graph);

        Map<String, Object> values = new HashMap<>();
        try {
            values.put("name", "Luca");
            Vertex luca = graph.addVertex("class:Person", values);
            values.put("name", "Marko");
            Vertex marko = graph.addVertex("class:Person", values);

            graph.commit();

            graph.addEdge(null, luca, marko, "knows");
        } catch (Exception e) {
            graph.rollback();
            throw new RuntimeException(e);
        }

        Iterable<Vertex> vertices = graph.getVertices();
        assertNotNull(vertices);
    }
}
