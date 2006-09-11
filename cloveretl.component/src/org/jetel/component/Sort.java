/*
*    jETeL/Clover - Java based ETL application framework.
*    Copyright (C) 2002-04  David Pavlis <david_pavlis@hotmail.com>
*    
*    This library is free software; you can redistribute it and/or
*    modify it under the terms of the GNU Lesser General Public
*    License as published by the Free Software Foundation; either
*    version 2.1 of the License, or (at your option) any later version.
*    
*    This library is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU    
*    Lesser General Public License for more details.
*    
*    You should have received a copy of the GNU Lesser General Public
*    License along with this library; if not, write to the Free Software
*    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*
*/
package org.jetel.component;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jetel.data.DataRecord;
import org.jetel.data.Defaults;
import org.jetel.data.SortDataRecordInternal;
import org.jetel.exception.ComponentNotReadyException;
import org.jetel.exception.XMLConfigurationException;
import org.jetel.graph.InputPort;
import org.jetel.graph.Node;
import org.jetel.graph.TransformationGraph;
import org.jetel.util.ComponentXMLAttributes;
import org.w3c.dom.Element;
/**
 *  <h3>Sort Component</h3>
 *
 * <!-- Sorts the incoming records based on specified key -->
 *
 * <table border="1">
 *  <th>Component:</th>
 * <tr><td><h4><i>Name:</i></h4></td>
 * <td>Sort</td></tr>
 * <tr><td><h4><i>Category:</i></h4></td>
 * <td></td></tr>
 * <tr><td><h4><i>Description:</i></h4></td>
 * <td>Sorts the incoming records based on specified key.<br>
 *  The key is name (or combination of names) of field(s) from input record.
 *  The sort order is either Ascending (default) or Descending.</td></tr>
 * <tr><td><h4><i>Inputs:</i></h4></td>
 * <td>[0]- input records</td></tr>
 * <tr><td><h4><i>Outputs:</i></h4></td>
 * <td>At least one connected output port.</td></tr>
 * <tr><td><h4><i>Comment:</i></h4></td>
 * <td></td></tr>
 * </table>
 *  <br>
 *  <table border="1">
 *  <th>XML attributes:</th>
 *  <tr><td><b>type</b></td><td>"SORT"</td></tr>
 *  <tr><td><b>id</b></td><td>component identification</td>
 *  <tr><td><b>sortKey</b></td><td>field names separated by :;|  {colon, semicolon, pipe}</td>
 *  <tr><td><b>sortOrder</b><br><i>optional</i></td><td>one of "Ascending|Descending" {the fist letter is sufficient, if not defined, then Ascending}</td>
 *  </tr>
 *  </table>
 *
 *  <h4>Example:</h4>
 *  <pre>&lt;Node id="SORT_CUSTOMER" type="SORT" sortKey="Name:Address" sortOrder="A"/&gt;</pre>
 *
 * @author      dpavlis
 * @since       April 4, 2002
 * @revision    $Revision$
 */
public class Sort extends Node {

	private static final String XML_SORTORDER_ATTRIBUTE = "sortOrder";
	private static final String XML_SORTKEY_ATTRIBUTE = "sortKey";
	/**  Description of the Field */
	public final static String COMPONENT_TYPE = "SORT";

	private final static int WRITE_TO_PORT = 0;
	private final static int READ_FROM_PORT = 0;

	private SortDataRecordInternal newSorter;
	private boolean sortOrderAscending;
	private String[] sortKeys;
	private ByteBuffer recordBuffer;

	private final static boolean DEFAULT_ASCENDING_SORT_ORDER = true; 

	/**
	 *Constructor for the Sort object
	 *
	 * @param  id         Description of the Parameter
	 * @param  sortKeys   Description of the Parameter
	 * @param  sortOrder  Description of the Parameter
	 */
	public Sort(String id, String[] sortKeys, boolean sortOrder) {
		super(id);
		this.sortOrderAscending = sortOrder;
		this.sortKeys = sortKeys;
	}


	/**
	 *Constructor for the Sort object
	 *
	 * @param  id        Description of the Parameter
	 * @param  sortKeys  Description of the Parameter
	 */
	public Sort(String id, String[] sortKeys) {
		this(id,sortKeys,DEFAULT_ASCENDING_SORT_ORDER);
	}


	/**
	 *  Main processing method for the SimpleCopy object
	 *
	 * @since    April 4, 2002
	 */
	public void run() {
		InputPort inPort = getInputPort(READ_FROM_PORT);
		DataRecord inRecord = new DataRecord(inPort.getMetadata());
		inRecord.init();
		//InputPortDirect inPort = (InputPortDirect) getInputPort(READ_FROM_PORT);
		// --- store input records into internal buffer
		while (inRecord!=null && runIt) {
			try {
				inRecord = inPort.readRecord(inRecord);// readRecord(READ_FROM_PORT,inRecord);
				if (inRecord!=null) {
						if(!newSorter.put(inRecord)){
						    System.err.println("Sorter "+getId()+" has no more capacity to sort additional records." +
						    		"The output will be incomplete !");
						    break; // no more capacity
						}
				}
			} catch (IOException ex) {
				resultMsg = ex.getMessage();
				resultCode = Node.RESULT_ERROR;
				closeAllOutputPorts();
				return;
			} catch (Exception ex) {
				resultMsg = ex.getMessage();
				resultCode = Node.RESULT_FATAL_ERROR;
				//closeAllOutputPorts();
				return;
			}
		}
		// --- sort the records now
		try {
				newSorter.sort();
		} catch (Exception ex) {
			resultMsg = "Error when sorting: " + ex.getMessage();
			resultCode = Node.RESULT_FATAL_ERROR;
			//closeAllOutputPorts();
			return;
		}
		// --- read sorted records
		while (newSorter.get(recordBuffer) && runIt) {
		    try {
		        writeRecordBroadcastDirect(recordBuffer);
		        recordBuffer.clear();
		    } catch (IOException ex) {
		        resultMsg = ex.getMessage();
		        resultCode = Node.RESULT_ERROR;
		        closeAllOutputPorts();
		        return;
		    } catch (Exception ex) {
		        resultMsg = ex.getMessage();
		        resultCode = Node.RESULT_FATAL_ERROR;
		        //closeAllOutputPorts();
		        return;
		    }
		}
		broadcastEOF();
		if (runIt) {
			resultMsg = "OK";
		} else {
			resultMsg = "STOPPED";
		}
		resultCode = Node.RESULT_OK;
	}


	/**
	 *  Sets the sortOrderAscending attribute of the Sort object
	 *
	 * @param  ascending  The new sortOrderAscending value
	 */
	public void setSortOrderAscending(boolean ascending) {
		sortOrderAscending = ascending;
	}


	/**
	 *  Description of the Method
	 *
	 * @exception  ComponentNotReadyException  Description of the Exception
	 * @since                                  April 4, 2002
	 */
	public void init() throws ComponentNotReadyException {
		// test that we have at least one input port and one output
		if (inPorts.size() < 1) {
			throw new ComponentNotReadyException("At least one input port has to be defined!");
		} else if (outPorts.size() < 1) {
			throw new ComponentNotReadyException("At least one output port has to be defined!");
		}
		recordBuffer = ByteBuffer.allocateDirect(Defaults.Record.MAX_RECORD_SIZE);
		if (recordBuffer == null) {
			throw new ComponentNotReadyException("Can NOT allocate internal record buffer ! Required size:" +
					Defaults.Record.MAX_RECORD_SIZE);
		}
		// create sorter
		newSorter = new SortDataRecordInternal(
		        getInputPort(READ_FROM_PORT).getMetadata(), sortKeys, sortOrderAscending);
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Returned Value
	 * @since     May 21, 2002
	 */
	public void toXML(Element xmlElement) {
		super.toXML(xmlElement);
		if (sortKeys != null) {
			StringBuffer buf = new StringBuffer(sortKeys[0]);
			for (int i=1; i< sortKeys.length; i++) {
				buf.append(Defaults.Component.KEY_FIELDS_DELIMITER + sortKeys[i]); 
			}
			xmlElement.setAttribute(XML_SORTKEY_ATTRIBUTE,buf.toString());
		}
		if (this.sortOrderAscending == false) {
			xmlElement.setAttribute(XML_SORTORDER_ATTRIBUTE, "Descending");
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  nodeXML  Description of Parameter
	 * @return          Description of the Returned Value
	 * @since           May 21, 2002
	 */
	   public static Node fromXML(TransformationGraph graph, Element xmlElement) throws XMLConfigurationException {
		ComponentXMLAttributes xattribs = new ComponentXMLAttributes(xmlElement, graph);
		Sort sort;
		try {
			sort = new Sort(xattribs.getString(XML_ID_ATTRIBUTE),
					xattribs.getString(XML_SORTKEY_ATTRIBUTE).split(Defaults.Component.KEY_FIELDS_DELIMITER_REGEX));
			if (xattribs.exists(XML_SORTORDER_ATTRIBUTE)) {
				sort.setSortOrderAscending(xattribs.getString(XML_SORTORDER_ATTRIBUTE).matches("^[Aa].*"));
			}
		} catch (Exception ex) {
	           throw new XMLConfigurationException(COMPONENT_TYPE + ":" + xattribs.getString(XML_ID_ATTRIBUTE," unknown ID ") + ":" + ex.getMessage(),ex);
		}
		return sort;
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	public boolean checkConfig() {
		return true;
	}
	
	public String getType(){
		return COMPONENT_TYPE;
	}
}

