package pl.edu.agh.toik.human.body;

import static com.google.common.base.MoreObjects.toStringHelper;

import java.util.List;

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
	private List<Buffer> buffers;

	public BloodstreamAgent(Coordinates position) {
		this.setPosition(position);
	}

	@Override
	public void doStep(int stepNumber) {
		log.debug("Step {}.", stepNumber);
		log.debug("Collected calcium {}.", this.toString());
		getDataFromBuffer();
		log.debug("Collected calcium after clearing buffer{}.", this.toString());
	}

	private void getDataFromBuffer() {
		for (Buffer b : buffers)
			// FIXME: add if condition (if buffor is closer than....)
			collectedCalcium += b.getAndClearData();

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

	public List<Buffer> getBuffers() {
		return buffers;
	}

	public void setBuffers(List<Buffer> buffers) {
		this.buffers = buffers;
	}

	public Coordinates getPosition() {
		return position;
	}

	public void setPosition(Coordinates position) {
		this.position = position;
	}
}
