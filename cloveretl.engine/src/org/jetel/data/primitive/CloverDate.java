/**
*    jETeL/Clover - Java based ETL application framework.
*    Copyright (C) 2002-09  David Pavlis, Javlin a.s. <david.pavlis@javlin.eu>
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
*/
package org.jetel.data.primitive;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.sql.Date;
import java.util.Calendar;


/**
 * @author dpavlis
 *
 */
public class CloverDate implements Comparable<CloverDate>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1235233894807618937L;
	
	private long datetime;
	
	public  CloverDate(){
		datetime=-1;
	}
	
	public  CloverDate(long time){
		datetime=time;
	}
	
	public  CloverDate(java.util.Date date){
		datetime=date.getTime();
	}
	
	public CloverDate(CloverDate date){
		datetime=date.datetime;
	}
	
	public long getTime(){
		return datetime;
	}
	
	public Date getDate(){
		return new Date(datetime);
	}
	
	public void setTime(long time){
		datetime=time;
	}
	
	public void setDate(Date date){
		datetime=date.getTime();
	}
	
	
	public void truncate_time_portion(){
		Calendar cal= Calendar.getInstance(); 
		cal.setTimeInMillis(datetime);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE , 0);
        cal.set(Calendar.SECOND , 0);
        cal.set(Calendar.MILLISECOND , 0);
        datetime=cal.getTimeInMillis();
	}
	
	public void truncate_date_portion(){
		Calendar cal= Calendar.getInstance(); 
		cal.setTimeInMillis(datetime);
		cal.set(Calendar.YEAR,0);
        cal.set(Calendar.MONTH,0);
        cal.set(Calendar.DAY_OF_MONTH,1);
        datetime=cal.getTimeInMillis();
	}
	
	public void setNow(){
		datetime=Calendar.getInstance().getTimeInMillis();
	}
	
	public CloverDate duplicate(){
		return new CloverDate(datetime);
	}
	
	public int compareTo(CloverDate o) {
		if (datetime!=o.datetime){
			return datetime>o.datetime ? 1 : -1;
		}
		return 0;
	}

	public int compareTo(Date date){
		long otherdate=date.getTime();
		if (datetime!=otherdate){
			return datetime>otherdate ? 1 : -1;
		}
		return 0;
	}
	
	@Override
	public boolean equals(Object anObject){
		if (this==anObject) return true;
		if (anObject instanceof CloverDate){
			return datetime==((CloverDate)anObject).datetime;
		}else if (anObject instanceof Date){
			return datetime==((Date)anObject).getTime();
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		 return (int)(datetime^datetime>>32);
	}
	
	public String toString(){
		return new Date(datetime).toString();
	}
	
	public void serialize(ByteBuffer buffer){
		buffer.putLong(datetime);
	}
	
	public void deserialize(ByteBuffer buffer){
		datetime=buffer.getLong();
	}
	
}