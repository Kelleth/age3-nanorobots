package pl.edu.agh.toik.human.body;

import static java.util.Objects.requireNonNull;
import static pl.edu.agh.toik.human.body.util.TimeMeasurement.measureTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.edu.agh.toik.human.body.agent.AgentBehavior;
import pl.edu.agh.toik.human.body.agent.Coordinates;
import pl.edu.agh.toik.human.body.configuration.AgentDescriptor;
import pl.edu.agh.toik.human.body.configuration.Configuration;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.ImmutableList;

/**
 * Created by Kuba on 17.05.15.
 */
public class HumanBodyPlatform implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(HumanBodyPlatform.class);

	private final Configuration configuration;

	private final List<AgentBehavior> agents;

	private final List<AgentBehavior> humanTissueAgents = new ArrayList<>();
	private final WorkplaceBehavior behavior = new WorkplaceBehavior();

	private final Random random = new Random();
	private final Buffer buffer;

	public HumanBodyPlatform(final Configuration configuration) {
		Coordinates bufforPosition = new Coordinates(random.nextDouble() * 20, random.nextDouble() * 20);
		buffer = new Buffer(bufforPosition);
		this.configuration = requireNonNull(configuration);
		agents = measureTime(() -> instantiateAgents(configuration.agents()), "Agents created in: ");

	}

	public class WorkplaceBehavior extends AgentBehavior {
		@Override
		public void doStep(final int stepNumber) {
			log.debug("Workplace step on agents {}.", stepNumber);

			for (final AgentBehavior agent : agents) {
				agent.doStep(stepNumber);
			}

		}
	}

	private List<AgentBehavior> instantiateAgents(List<AgentDescriptor> agents) {
		log.debug("Instantiating {} workplaces.", agents.size());
		final ImmutableList.Builder<AgentBehavior> builder = ImmutableList.builder();
		for (int i = 0; i < agents.size(); i++) {
			final AgentDescriptor desc = agents.get(i);
			Coordinates position = new Coordinates(random.nextDouble() * 20, random.nextDouble() * 20);

			if (desc.agentClass().equals(BloodstreamAgent.class)) {
				final AgentBehavior agent = new BloodstreamAgent(position);

				((BloodstreamAgent) agent).setBuffers(Lists.newArrayList(buffer));
				builder.add(agent);

			} else if (desc.agentClass().equals(HumanTissueAgent.class)) {
				final AgentBehavior agent = new HumanTissueAgent(position);
				humanTissueAgents.add(agent);
				((HumanTissueAgent) agent).setBuffer(buffer);
				builder.add(agent);
			}
		}
		log.debug("Finished workplaces instantiation.");
		return builder.build();
	}

	@Override
	public void run() {
		int step = 1;
		while (!Thread.currentThread().isInterrupted()) {
			behavior.doStep(step++);
		}
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public List<AgentBehavior> getAgents() {
		return agents;
	}
}
