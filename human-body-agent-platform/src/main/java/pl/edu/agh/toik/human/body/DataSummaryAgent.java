package pl.edu.agh.toik.human.body;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.toik.human.body.agent.AgentBehavior;
import pl.edu.agh.toik.human.body.agent.Coordinates;

/**
 * Agent which collect data from all Bloodstream agents in one place
 * Created by Ewelina on 2015-06-07.
 */
public class DataSummaryAgent extends AgentBehavior {

    private static final Logger log = LoggerFactory.getLogger(DataSummaryAgent.class);

    /** total collected calcium value */
    private double totalCalcium = 0;

    /** current position of agent */
    private Coordinates position;

    /** data buffer - agent reads data from it */
    private Buffer buffer;

    public DataSummaryAgent(Coordinates position, Buffer buffer) {
        this.position = position;
        this.buffer = buffer;
    }

    @Override
    public void doStep(int stepNumber) {
        log.debug("Step {}.", stepNumber);;
        getDataFromBuffer();
        log.debug("Total calcium collected from buffer: {}.", totalCalcium);
    }

    /**
     * Appends data from buffer to total calcium and clears it
     */
    public void getDataFromBuffer() {
        totalCalcium += buffer.getAndClearData();
    }

    public Coordinates getPosition() {
        return position;
    }

    public Buffer getBuffer() {
        return buffer;
    }

}
