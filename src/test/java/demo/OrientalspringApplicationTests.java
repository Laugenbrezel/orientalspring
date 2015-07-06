package demo;

import com.orientechnologies.common.util.OCallable;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = OrientalspringApplication.class)
@WebAppConfiguration
public class OrientalspringApplicationTests {

	@Autowired
	OrientGraphFactory orientGraphFactory;

	@Test
	public void contextLoads() {
	}

	@Test
	public void testFactoryInitialized() throws Exception {
		assertNotNull(orientGraphFactory);

        OrientGraph graph = orientGraphFactory.getTx();
        assertNotNull(graph);

        Map<String, Object> values = new HashMap<>();
        try{
            values.put("name", "Luca");
            Vertex luca = graph.addVertex("class:Person", values);
            values.put("name", "Marko");
            Vertex marko = graph.addVertex("class:Person", values);

            graph.commit();

            graph.addEdge(null, luca, marko, "knows");
        } catch( Exception e ) {
            graph.rollback();
            throw new RuntimeException(e);
        }

        Iterable<Vertex> vertices = graph.getVertices();
        assertNotNull(vertices);
    }
}
