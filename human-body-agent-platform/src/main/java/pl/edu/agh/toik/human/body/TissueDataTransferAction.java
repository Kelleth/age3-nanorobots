package pl.edu.agh.toik.human.body;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.toik.human.body.agent.Agent;

import java.util.Collection;

/**
 * Created by Ewelina on 2015-05-10.
 */
public class TissueDataTransferAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(TissueDataTransferAction.class);

    @Override
    public void execute(Collection<Agent<?>> agents) {
        @SuppressWarnings("unchecked")
        final Collection<Agent> castedAgents = (Collection)agents;

        logger.info("Agents number : {}", castedAgents.size());

        for(final Agent agent : castedAgents) {
            if (agent.behaviorClass().getSimpleName().equals("BloodstreamAgent")) {
                final Agent<BloodstreamAgent> bloodstreamAgent = agent;
                for(final Agent otherAgent : castedAgents) {
                    if( !otherAgent.behaviorClass().getSimpleName().equals("BloodstreamAgent") ) {
                        final Agent<HumanTissueAgent> humanTissueAgent = otherAgent;
                        bloodstreamAgent.behavior().addCalcium(humanTissueAgent.behavior().getCalcium());
                        logger.info("Agent: {} have now: {} collected calcium", bloodstreamAgent.name(), bloodstreamAgent.behavior().getCollectedCalcium());
                    }
                }
            }
        }

    }
}