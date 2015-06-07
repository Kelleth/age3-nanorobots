/*
 * Copyright (C) 2014-2015 Intelligent Information Systems Group.
 *
 * This file is part of AgE.
 *
 * AgE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AgE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AgE.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.edu.agh.toik.human.body;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.edu.agh.toik.human.body.agent.Coordinates;

public class Buffer {
	private static final Logger log = LoggerFactory.getLogger(Buffer.class);
	private Coordinates position;
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

	public synchronized Double getAndClearData() {
		log.debug("Buffer get data: {}.", this.data);
		Double dataForAgent = new Double(data);
		data = new Double(0);
		return dataForAgent;
	}

	public synchronized void addData(double data) {
		this.data += data;
		log.debug("Buffer added data: {}.", this.data);
	}

	public synchronized Double getData() {
		return data;
	}
}
