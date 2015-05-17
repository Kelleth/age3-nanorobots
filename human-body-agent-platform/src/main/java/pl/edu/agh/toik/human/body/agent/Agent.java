package pl.edu.agh.toik.human.body.agent;

/**
 * Created by Kuba on 17.05.15.
 */
public interface Agent<A extends AgentBehavior> {
    String name();

    A behavior();

    Class<A> behaviorClass();
}
