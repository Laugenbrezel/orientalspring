package demo;

import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Lutz Zimmermann, SVA GmbH.
 */
@Configuration
public class OrientDbConfiguration {

    @Bean
    public OrientGraphFactory orientGraphFactory() {
        return new OrientGraphFactory("memory:demo").setupPool(1,10);
    }
}
