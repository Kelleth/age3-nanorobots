package pl.edu.agh.toik.human.body;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.toik.human.body.agent.AgentBehavior;
import pl.edu.agh.toik.human.body.agent.Coordinates;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Created by Ewelina on 2015-05-09.
 */
public class BloodstreamAgent extends AgentBehavior {

    private double collectedCalcium = 0;
    private static final Logger log = LoggerFactory.getLogger(BloodstreamAgent.class);
    private Coordinates position;

    public BloodstreamAgent(Coordinates position) {
        this.position = position;
    }

    @Override
    public void doStep(int stepNumber) {
        log.debug("Step {}.", stepNumber);
        log.debug("Collected calcium {}.", this.toString());
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
}