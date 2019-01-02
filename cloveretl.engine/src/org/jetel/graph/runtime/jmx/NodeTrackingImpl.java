/*
 * jETeL/CloverETL - Java based ETL application framework.
 * Copyright (c) Javlin, a.s. (info@cloveretl.com)
 *  
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jetel.graph.runtime.jmx;

import org.jetel.graph.Result;
import org.jetel.graph.runtime.InputPortTrackingDetail;
import org.jetel.graph.runtime.NodeTrackingDetail;
import org.jetel.graph.runtime.OutputPortTrackingDetail;

/**
 * Simple DTO holding tracking information about a node.
 * 
 * @author Filip Reichman
 *         (c) Javlin Consulting (www.javlinconsulting.cz)
 *
 * @created Jan 2, 2019
 */
public class NodeTrackingImpl implements NodeTracking {

	private static final long serialVersionUID = 3570320889692545386L;
	
	private final PhaseTracking parentPhaseTracking;
	
	protected Result result;

	protected String nodeId;
	protected String nodeName;
    protected long totalCPUTime;
    protected long totalUserTime;
    protected float usageCPU;
    protected float peakUsageCPU;
    protected float usageUser;
    protected float peakUsageUser;
	
	private InputPortTracking[] inputPortsDetails;
	private OutputPortTracking[] outputPortsDetails;

	public NodeTrackingImpl(PhaseTracking parentPhaseTracking) {
		this.parentPhaseTracking = parentPhaseTracking;
		inputPortsDetails = new InputPortTracking[0];
		outputPortsDetails = new OutputPortTracking[0]; 
	}

	public NodeTrackingImpl(PhaseTracking parentPhaseTracking, NodeTrackingDetail nodeTracking) {
		this.parentPhaseTracking = parentPhaseTracking;
		this.nodeId = nodeTracking.getNodeID();
		this.nodeName = nodeTracking.getNodeName();
		this.result = nodeTracking.getResult();

		this.totalCPUTime = nodeTracking.getTotalCPUTime();
		this.totalUserTime = nodeTracking.getTotalUserTime();
		this.usageCPU = nodeTracking.getUsageCPU();
		this.peakUsageCPU = nodeTracking.getPeakUsageCPU();
		this.usageUser = nodeTracking.getUsageUser();
		this.peakUsageUser = nodeTracking.getPeakUsageUser();
		
		
		this.inputPortsDetails = new InputPortTracking[nodeTracking.getInputPortTracking().length];
		int i = 0;
		for (InputPortTrackingDetail inputPort : nodeTracking.getInputPortTracking()) {
			inputPortsDetails[i] = new InputPortTrackingImpl(this, inputPort);
			i++;
		}
		
		this.outputPortsDetails = new OutputPortTracking[nodeTracking.getOutputPortTracking().length];
		i = 0;		
		for (OutputPortTrackingDetail outputPortDetail : nodeTracking.getOutputPortTracking()) {
			outputPortsDetails[i] = new OutputPortTrackingImpl(this, outputPortDetail);
			i++;
		}
	}
	
	@Override
	public InputPortTracking[] getInputPortTracking() {
		return inputPortsDetails;
	}

	public void setInputPortsDetails(InputPortTracking[] inputPortsDetails) {
		this.inputPortsDetails = inputPortsDetails;
	}

	@Override
	public OutputPortTracking[] getOutputPortTracking() {
		return outputPortsDetails;
	}
	
	public void setOutputPortsDetails(OutputPortTracking[] outputPortsDetails) {
		this.outputPortsDetails = outputPortsDetails;
	}
	
	@Override
	public PhaseTracking getParentPhaseTracking() {
		return parentPhaseTracking;
	}

	/* (non-Javadoc)
	 * @see org.jetel.graph.runtime.jmx.NodeTracking#getResult()
	 */
	@Override
	public Result getResult() {
		return result;
	}

	/* (non-Javadoc)
	 * @see org.jetel.graph.runtime.jmx.NodeTracking#getTotalCPUTime()
	 */
	@Override
	public long getTotalCPUTime() {
		return totalCPUTime;
	}

	/* (non-Javadoc)
	 * @see org.jetel.graph.runtime.jmx.NodeTracking#getTotalUserTime()
	 */
	@Override
	public long getTotalUserTime() {
		return totalUserTime;
	}

	/* (non-Javadoc)
	 * @see org.jetel.graph.runtime.jmx.NodeTracking#getUsageCPU()
	 */
	@Override
	public float getUsageCPU() {
		return usageCPU;
	}

	/* (non-Javadoc)
	 * @see org.jetel.graph.runtime.jmx.NodeTracking#getPeakUsageCPU()
	 */
	@Override
	public float getPeakUsageCPU() {
		return peakUsageCPU;
	}

	/* (non-Javadoc)
	 * @see org.jetel.graph.runtime.jmx.NodeTracking#getPeakUsageUser()
	 */
	@Override
	public float getPeakUsageUser() {
		return peakUsageUser;
	}

	/* (non-Javadoc)
	 * @see org.jetel.graph.runtime.jmx.NodeTracking#getUsageUser()
	 */
	@Override
	public float getUsageUser() {
		return usageUser;
	}

	/* (non-Javadoc)
	 * @see org.jetel.graph.runtime.jmx.NodeTracking#getNodeID()
	 */
	@Override
	public String getNodeID() {
		return nodeId;
	}
	
	/* (non-Javadoc)
	 * @see org.jetel.graph.runtime.jmx.NodeTracking#getNodeName()
	 */
	@Override
	public String getNodeName() {
		return nodeName;
	}
	
	@Override
	public boolean hasPorts() {
		return getInputPortTracking().length > 0 || getOutputPortTracking().length > 0;
	}
	
	@Override
	public InputPortTracking getInputPortTracking(int portNumber) {
		for (InputPortTracking inputPortDetail : getInputPortTracking()) {
			if (inputPortDetail.getIndex() == portNumber) {
				return inputPortDetail;
			}
		}
		return null;
	}

	@Override
	public OutputPortTracking getOutputPortTracking(int portNumber) {
		for (OutputPortTracking outputPortDetail : getOutputPortTracking()) {
			if (outputPortDetail.getIndex() == portNumber) {
				return outputPortDetail;
			}
		}
		return null;
	}
	
	//******************* SETTERS *******************/
	
	@Override
	public void setResult(Result result) {
		this.result = result;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public void setTotalCPUTime(long totalCPUTime) {
		this.totalCPUTime = totalCPUTime;
	}

	public void setTotalUserTime(long totalUserTime) {
		this.totalUserTime = totalUserTime;
	}

	public void setUsageCPU(float usageCPU) {
		this.usageCPU = usageCPU;
	}

	public void setPeakUsageCPU(float peakUsageCPU) {
		this.peakUsageCPU = peakUsageCPU;
	}

	public void setUsageUser(float usageUser) {
		this.usageUser = usageUser;
	}

	public void setPeakUsageUser(float peakUsageUser) {
		this.peakUsageUser = peakUsageUser;
	}
}
