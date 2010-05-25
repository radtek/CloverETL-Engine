/*
 *    jETeL/Clover - Java based ETL application framework.
 *    Copyright (c) Opensys TM by Javlin, a.s. (www.opensys.com)
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 */
package org.jetel.ctl.extensions;

import org.jetel.ctl.data.TLType;

/**
 * @author jakub (info@cloveretl.com)
 *         (c) Opensys TM by Javlin, a.s. (www.cloveretl.com)
 *
 * @created May 19, 2010
 */
public class TLFunctionCallContext {
	
	private TLType[] params;
	private Object[] cache;
	private boolean[] isLiteral;
	private Object[] paramValues;
	private int index;
	private boolean hasInit;
	private String libClassName;
	private String initMethodName;
	
	/**
	 * The tl parameters.
	 * CAUTION! This method can be used in interpreter only, don't use it inside method marked @TLFunctionAnnotation,
	 * it contains no information in compiled mode (null). 
	 * 
	 * @return the params
	 */
	public TLType[] getParams() {
		return params;
	}
	/**
	 * @param params the params to set
	 */
	public void setParams(TLType[] params) {
		this.params = params;
	}
	/**
	 * @return the cache
	 */
	public Object[] getCache() {
		return cache;
	}
	/**
	 * @param cache the cache to set
	 */
	public void setCache(Object[] cache) {
		this.cache = cache;
	}
	
	public boolean isLiteral(int i) {
		return isLiteral[i];
	}
	
	public void setLiterals(boolean[] literals) {
		isLiteral = literals;
	}
	
	public int getLiteralsSize() {
		return isLiteral.length;
	}
	
	public void initVoidLterals(int count) {
		isLiteral = new boolean[count];
		paramValues = new Object[count];
		for (int i = 0; i < count; i++) {
			isLiteral[i] = false;
			paramValues[i] = null;
		}
	}
	               
	/**
	 * @param i
	 * @param b
	 */
	public void setLiteral(int i) {
		isLiteral[i] = true;
	}
	/**
	 * @param i
	 * @return
	 */
	public Object getParamValue(int i) {
		return paramValues[i];
	}
	/**
	 * @param paramValues the paramValues to set
	 */
	public void setParamValues(Object[] paramValues) {
		this.paramValues = paramValues;
	}
	
	public void setParamValue(int i, Object paramValue) {
		this.paramValues[i] = paramValue;
	}
	/**
	 * @param index the index to set
	 */
	public void setIndex(int index) {
		this.index = index;
	}
	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}
	/**
	 * @return
	 */
	public int literalsCount() {
		return isLiteral.length;
	}
	/**
	 * @param hasInit
	 */
	public void setHasInit(boolean hasInit) {
		this.hasInit = hasInit;
	}
	
	public boolean hasInit() {
		return hasInit;
	}
	/**
	 * @param libName the libName to set
	 */
	public void setLibClassName(String libName) {
		this.libClassName = libName;
	}
	/**
	 * @return the libName
	 */
	public String getLibClassName() {
		return libClassName;
	}
	/**
	 * @param initMethodName the initMethodName to set
	 */
	public void setInitMethodName(String initMethodName) {
		this.initMethodName = initMethodName;
	}
	/**
	 * @return the initMethodName
	 */
	public String getInitMethodName() {
		return initMethodName;
	}

}
