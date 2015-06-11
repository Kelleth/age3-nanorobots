package pl.edu.agh.toik.human.body;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.edu.agh.toik.human.body.agent.Coordinates;

/**
 * Class used to persist data collected in human body by agents
 */
public class Buffer {
	private static final Logger log = LoggerFactory.getLogger(Buffer.class);

	/** buffer current position */
	private Coordinates position;

	/** buffer data **/
	private Double data;

	public Buffer(Coordinates bufforPosition) {
		this.position = bufforPosition;
		data = new Double(0);
	}

	public Coordinates getPosition() {
		return position;
	}

	public void setPosition(Coordinates position) {
		this.position = position;
	}

	/**
	 * Gets data from buffer ( called by BloodstreamAgent/DataSummaryAgent ) and clears its value
	 * @return current buffer data
	 */
	public synchronized Double getAndClearData() {
		log.debug("Buffer get data: {}.", this.data);
		Double dataForAgent = new Double(data);
		data = new Double(0);
		return dataForAgent;
	}

	/**
	 * Adds new data to buffer
	 * @param data - data to add
	 */
	public synchronized void addData(double data) {
		this.data += data;
		log.debug("Buffer added data: {}.", this.data);
	}

	public synchronized Double getData() {
		return data;
	}
}
