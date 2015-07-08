package demo;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.tinkerpop.blueprints.impls.orient.OrientEdgeType;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertexType;
import org.ops4j.orient.spring.tx.OrientBlueprintsGraphFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.orientechnologies.orient.core.metadata.schema.OType.INTEGER;
import static com.orientechnologies.orient.core.metadata.schema.OType.STRING;

/**
 * @author Lutz Zimmermann, SVA GmbH.
 */
@Service
public class TestGraphInitializer implements GraphInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(TestGraphInitializer.class);

    @Autowired
    private OrientBlueprintsGraphFactory orientGraphFactory;

    @Override
    @Transactional(propagation = Propagation.NEVER)
    public void initializeGraph() {
        try (ODatabaseDocumentTx db = orientGraphFactory.openDatabase()) {
            OrientGraphNoTx graph = new OrientGraphNoTx(db);

            OrientVertexType personType = graph.createVertexType(ModelClasses.PERSON);
            personType.createProperty("name", STRING);
            personType.createProperty("age", INTEGER);
            personType.createIndex("Person.name", OClass.INDEX_TYPE.UNIQUE, "name");

            OrientVertexType depType = graph.createVertexType(ModelClasses.DEPARTMENT);
            depType.createProperty("name", STRING);
            depType.createIndex("Department.name", OClass.INDEX_TYPE.UNIQUE, "name");

            OrientEdgeType worksAtEdgeType = graph.createEdgeType(EdgeTypes.WORKS_AT);
            worksAtEdgeType.createProperty("since", OType.DATETIME);

            graph.shutdown();
        }
    }

    @Override
    public void cleanup() {
        //TODO
    }
}
