package demo;

import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import org.ops4j.orient.spring.tx.OrientBlueprintsGraphFactory;
import org.ops4j.orient.spring.tx.OrientObjectDatabaseFactory;
import org.ops4j.orient.spring.tx.OrientTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Lutz Zimmermann, SVA GmbH.
 */
@Configuration
@EnableTransactionManagement
public class OrientDbConfiguration {

    @Bean
    public OrientTransactionManager transactionManager() {
        OrientTransactionManager bean = new OrientTransactionManager();
        bean.setDatabaseManager(databaseFactory());
        return bean;
    }

    @Bean
    public OrientBlueprintsGraphFactory databaseFactory() {
        OrientBlueprintsGraphFactory manager = new OrientBlueprintsGraphFactory();
        manager.setUrl("memory:demo");
        return manager;
    }
}
