/*
 *  jETeL/Clover - Java based ETL application framework.
 *  Created on Apr 17, 2003
 *  Copyright (C) 2003, 2002  David Pavlis, Wes Maciorowski
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.jetel.exception;

import org.jetel.data.DataRecord;
import org.jetel.metadata.DataRecordMetadata;

/**
 * @author maciorowski
 *
 */
public class LenientBadDataFormatExceptionHandler
	extends BadDataFormatExceptionHandler {

		/**
		 * It implements the behavior of the handles.
		 */
		public void handleException() {
		}

		/**
		 * Since BadDataFormatExceptionHandler is handling 'strict' data policy,
		 * there is no need to implement this method as any BadDataFormatException
		 * aborts further procesing.
		 */
		public void reset() {}
	
		/**
		 * @param record
		 * @param fieldCounter
		 * @param string
		 */
		public void populateFieldFailure(DataRecord record, int fieldCounter, String string) {
			DataRecordMetadata metadata = null;
			try {
				metadata = record.getMetadata();
				record.getField(fieldCounter).fromString( metadata.getField(fieldCounter).getDefaultValue() );
			} catch (Exception ex) {
				// here, the only reason to fail is bad DefaultValue
				throw new RuntimeException(metadata.getField(fieldCounter).getName() + " has incorrect default value("+metadata.getField(fieldCounter).getDefaultValue()+")!  You are using lenient data policy which requires default values.");
			}
		}
}
