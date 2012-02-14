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
package org.jetel.data.tree.formatter.designmodel;

import org.jetel.data.tree.formatter.util.MappingVisitor;


/**
 * Class representing xml attribute
 * 
 * @author lkrejci (info@cloveretl.com) (c) Javlin, a.s. (www.cloveretl.com)
 * 
 * @created 14 Dec 2010
 */
public class Attribute extends AbstractNode {
	
	private static final MappingProperty[] AVAILABLE_PROPERTIES = {MappingProperty.NAME, 
		MappingProperty.VALUE};
	
	public static final boolean WRITE_NULL_DEFAULT = false;

	public Attribute(ContainerNode parent) {
		super(parent, false);
	}

	@Override
	public void accept(MappingVisitor visitor) throws Exception {
		visitor.visit(this);
	}
	
	@Override
	public MappingProperty[] getAvailableProperties() {
		return AVAILABLE_PROPERTIES;
	}

	@Override
	public String getSimpleContent() {
		return properties.get(MappingProperty.VALUE);
	}

	@Override
	public String getDisplayName() {
		return properties.get(MappingProperty.NAME);
	}

	@Override
	public String getPath() {
		return parent.getPath() + "[@" + getDisplayName() + "]";
	}

	@Override
	public short getType() {
		return AbstractNode.ATTRIBUTE;
	}

	@Override
	public String getDescription() {
		return "An XML attribute. Example: <element0 attribute=\"value\">";
	}
}