package pl.edu.agh.toik.human.body;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.toik.human.body.agent.AgentBehavior;
import pl.edu.agh.toik.human.body.agent.Coordinates;
import pl.edu.agh.toik.human.body.configuration.AgentDescriptor;
import pl.edu.agh.toik.human.body.configuration.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.util.Objects.requireNonNull;
import static pl.edu.agh.toik.human.body.util.TimeMeasurement.measureTime;

/**
 * Created by Kuba on 17.05.15.
 */
public class HumanBodyPlatform implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(HumanBodyPlatform.class);

    private final Configuration configuration;

    private final List<AgentBehavior> agents;

    private final List<AgentBehavior> humanTissueAgents = new ArrayList<>();

    private final Random random = new Random();

    public HumanBodyPlatform(final Configuration configuration) {
        this.configuration = requireNonNull(configuration);
        agents = measureTime(() -> instantiateAgents(configuration.agents()), "Agents created in: ");

    }

    private List<AgentBehavior> instantiateAgents(List<AgentDescriptor> agents) {
        log.debug("Instantiating {} workplaces.", agents.size());
        final ImmutableList.Builder<AgentBehavior> builder = ImmutableList.builder();
        for (int i = 0; i < agents.size(); i++) {
            final AgentDescriptor desc = agents.get(i);
            Coordinates position = new Coordinates(random.nextDouble() * 20, random.nextDouble() * 20);
            if (desc.agentClass().equals(AgentDescriptor.AgentClass.BLOODSTREAM_AGENT)) {
                final AgentBehavior agent = new BloodstreamAgent(position);
                builder.add(agent);

            } else if (desc.agentClass().equals(AgentDescriptor.AgentClass.HUMAN_TISSUE_AGENT)) {
                final AgentBehavior agent = new HumanTissueAgent(position);
                humanTissueAgents.add(agent);
                builder.add(agent);
            }
        }
        log.debug("Finished workplaces instantiation.");
        return builder.build();
    }

    @Override
    public void run() {

    }
}
