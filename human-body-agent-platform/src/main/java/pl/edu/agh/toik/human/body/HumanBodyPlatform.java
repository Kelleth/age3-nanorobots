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
	private static List<Buffer> buffers;



	/**
	 * Returns close data buffer or null if not exists
	 * @param agentCoordinates - current coordinates of agent
	 * @return - data buffer or null
	 */
	// FIXME: return buffer with close enough coordinates, not only equal to agent coordinates
	static Buffer getCloseDataBufferIfExists(Coordinates agentCoordinates) {
		for( Buffer buffer : buffers ) {
			if(buffer.getPosition().equals(agentCoordinates)) {
				log.debug("Bloodstream agent gets close buffer with coordinates [{}, {}]", agentCoordinates.getxCoordinate(), agentCoordinates.getyCoordinate());
				return buffer;
			}
		}

		return null;
	}

	public HumanBodyPlatform(final Configuration configuration) {
		buffers = new ArrayList<>();
		// assumption: buffers are situated on [0, x: int] coordinates, agents (bloodstream) are moving along the path : [0, x: int]
		// random number of randomly located buffers (min 3)
		for(int i=0; i<random.nextInt(10) + 3; i++) {
			final Coordinates coordinates = new Coordinates(0.0, (double) random.nextInt(20));
			log.debug("Created buffer with coordinates [{}, {}]", coordinates.getxCoordinate(), coordinates.getyCoordinate());
			buffers.add(new Buffer(coordinates));
		}

		//Coordinates bufforPosition = new Coordinates(random.nextDouble() * 20, random.nextDouble() * 20);
		//buffers = Collections.singletonList(new Buffer(bufforPosition));
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
			//Coordinates position = new Coordinates(random.nextDouble() * 20, random.nextDouble() * 20);
			final Coordinates position = new Coordinates(0.0, (double) random.nextInt(20));

			if (desc.agentClass().equals(BloodstreamAgent.class)) {
				final AgentBehavior agent = new BloodstreamAgent(position);

//				((BloodstreamAgent) agent).setBuffers(buffers);
				builder.add(agent);

			} else if (desc.agentClass().equals(HumanTissueAgent.class)) {
				final AgentBehavior agent = new HumanTissueAgent(position);
				humanTissueAgents.add(agent);
				((HumanTissueAgent) agent).setBuffer(getClosestDataBuffer(position));
				builder.add(agent);
			}
		}
		log.debug("Finished workplaces instantiation.");
		return builder.build();
	}

	// finds closest buffer for each human tissue agent
	private Buffer getClosestDataBuffer(Coordinates position) {
		int chosenBufferNo = 0;
		Buffer closestBuffer = buffers.get(0);
		double closestDistance = closestBuffer.getPosition().calculateDistanceTo(position);
		if( buffers. size() > 1 ) {
			for(int i=1; i<buffers.size(); i++) {
				final double distance = buffers.get(i).getPosition().calculateDistanceTo(position);
				if( distance < closestDistance ) {
					closestDistance = distance;
					closestBuffer = buffers.get(i);
					chosenBufferNo = i;
				}
			}
		}
		log.debug("HumanTissueAgent has buffer no: {} with coordinates [{}, {}].", chosenBufferNo, closestBuffer.getPosition().getxCoordinate(), closestBuffer.getPosition().getyCoordinate());
		return closestBuffer;
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
