package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * @author Lutz Zimmermann, SVA GmbH.
 */
@Service
public class GraphInitializerService implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private Collection<GraphInitializer> graphInitializers;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        for (GraphInitializer graphInitializer : graphInitializers) {
            graphInitializer.initializeGraph();
        }

    }
}
