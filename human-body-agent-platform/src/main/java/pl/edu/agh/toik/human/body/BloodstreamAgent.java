package pl.edu.agh.toik.human.body;

import static com.google.common.base.MoreObjects.toStringHelper;

import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.edu.agh.toik.human.body.agent.AgentBehavior;
import pl.edu.agh.toik.human.body.agent.Coordinates;

/**
 * Created by Ewelina on 2015-05-09.
 */
public class BloodstreamAgent extends AgentBehavior {

	private double collectedCalcium = 0;
	private static final Logger log = LoggerFactory.getLogger(BloodstreamAgent.class);
	private Coordinates position;
	private final Random random = new Random();
//	private List<Buffer> buffers;

	public BloodstreamAgent(Coordinates position) {
		this.setPosition(position);
	}

	@Override
	public void doStep(int stepNumber) {
		log.debug("Step {}.", stepNumber);
		log.debug("Collected calcium {}.", this.toString());
		getDataFromBuffer();
		move();
		log.debug("Collected calcium after clearing buffer{}.", this.toString());
	}

	// FIXME : agent currently changes only y coordinate (x coordinate is set to zero)
	private void move() {
		final boolean moveForward = random.nextBoolean();
		final double currentYCoordinate = this.position.getyCoordinate();
		final int moveDistance = random.nextInt(3);

		if ( moveForward ) {
			this.position.setyCoordinate(currentYCoordinate + moveDistance);
		} else {
			this.position.setyCoordinate(currentYCoordinate - moveDistance);
		}

		log.debug("Bloodstream agent moves to position [{}, {}]", position.getxCoordinate(), position.getyCoordinate());
	}

	private void getDataFromBuffer() {
		final Buffer buffer = HumanBodyPlatform.getCloseDataBufferIfExists(position);
		// currently it means that agent is located at the same position as buffer is
		if( buffer != null ) {
			collectedCalcium += buffer.getAndClearData();
		}
	}

	public double getCollectedCalcium() {
		return collectedCalcium;
	}

	public void addCalcium(double calcium) {
		this.collectedCalcium += calcium;
	}

	@Override
	public String toString() {
		return toStringHelper(this).add("collectedCalcium", collectedCalcium).toString();
	}

//	public List<Buffer> getBuffers() {
//		return buffers;
//	}
//
//	public void setBuffers(List<Buffer> buffers) {
//		this.buffers = buffers;
//	}

	public Coordinates getPosition() {
		return position;
	}

	public void setPosition(Coordinates position) {
		this.position = position;
	}
}
