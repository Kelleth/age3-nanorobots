package pl.edu.agh.toik.human.body.agents;


import jline.internal.Log;
import org.age.compute.mas.agent.AgentBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Created by Ewelina on 2015-05-09.
 */
public class HumanTissueAgent extends AgentBehavior {

    private double calcium = new Random().nextDouble();

    private static final Logger log = LoggerFactory.getLogger(HumanTissueAgent.class);

    @Override
    public void doStep(int stepNumber) {
        log.debug("Step {}.", stepNumber);
        computeCalcium();
    }

    private void computeCalcium() {
        this.calcium = new Random().nextDouble();
    }

    public double getCalcium() {
        return calcium;
    }

    public void setCalcium(double calcium) {
        this.calcium = calcium;
    }

    @Override
    public String toString() {
        return toStringHelper(this).add("calcium", calcium).toString();
    }
}
