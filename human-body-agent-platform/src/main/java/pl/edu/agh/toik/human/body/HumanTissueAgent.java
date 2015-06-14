package pl.edu.agh.toik.human.body;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.toik.human.body.agent.AgentBehavior;
import pl.edu.agh.toik.human.body.agent.Coordinates;

import java.util.Random;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Agent placed in human tissue, collects data from human body and puts it to data buffer
 * Created by Ewelina on 2015-05-09.
 */
public class HumanTissueAgent extends AgentBehavior {
    private static final Logger log = LoggerFactory.getLogger(HumanTissueAgent.class);

    /**
     * Calcium from human body
     */
    private double calcium = new Random().nextDouble();

    /**
     * Agent position
     */
    private Coordinates position;

    /**
     * Buffer for data connected with agent
     */
    private Buffer buffer;

    public HumanTissueAgent(Coordinates position) {
        this.setPosition(position);
    }

    @Override
    public void doStep(int stepNumber) {
        log.debug("Step {}.", stepNumber);
        computeCalcium();
        writeDataToBuffer();
        log.debug("Wrote to buffer: {}.", calcium);
    }

    /**
     * Writes data to buffer
     */
    private void writeDataToBuffer() {
        buffer.addData(calcium);
    }

    /**
     * Computes new calcium value ( currently is a randomly generated number )
     */
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

    public Coordinates getPosition() {
        return position;
    }

    public void setPosition(Coordinates position) {
        this.position = position;
    }

    public Buffer getBuffer() {
        return buffer;
    }

    public void setBuffer(Buffer buffer) {
        this.buffer = buffer;
    }
}
