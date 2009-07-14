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
package org.jetel.connection.jdbc.specific;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jetel.connection.jdbc.DBConnection;
import org.jetel.connection.jdbc.SQLCloverStatement.QueryType;
import org.jetel.exception.JetelException;
import org.jetel.metadata.DataFieldMetadata;

/**
 * This interface represents customization in behaviour of a JDBC connection.
 * The class parameter of jdbcSpecific extension point has to implement this interface.
 * 
 * @author Martin Zatopek (martin.zatopek@javlinconsulting.cz)
 *         (c) Javlin Consulting (www.javlinconsulting.cz)
 *
 * @created Jun 3, 2008
 */
public interface JdbcSpecific {

	/**
	 * List of all considered database operations.
	 */
	public enum OperationType {
		READ,
		WRITE,
		CALL,
		TRANSACTION,
		UNKNOWN
	}
	
	/**
	 * List of all supported retrieving types of auto-generated keys. 
	 */
	public enum AutoGeneratedKeysType {
		MULTI,
		SINGLE,
		NONE
	}

	/**
	 * Creates java.sql.Connection, which should follow 
	 * all specific behaviour with the given operation type.
	 * @param connection
	 * @param operationType
	 * @return
	 * @throws JetelException
	 */
	public Connection createSQLConnection(DBConnection connection, OperationType operationType) throws JetelException;

	/**
	 * @return type of supported auto-generated key retrieving
	 */
	public AutoGeneratedKeysType getAutoKeyType();
	
	/**
	 * Via this method, it could be a result set optimized with the given operation type.
	 * @param resultSet
	 * @param operationType
	 */
	public void optimizeResultSet(ResultSet resultSet, OperationType operationType);
	
	/**
	 * This method defines a conversion table from a sql type to a clover field type.
	 * @param sqlType
	 * @return
	 */
	public char sqlType2jetel(int sqlType);
	
	/**
	 * This method defines a conversion table from a clover field type to a sql type .
	 * @param field
	 * @return
	 */
	public int jetelType2sql(DataFieldMetadata field);
	
	/**
	 * Converts field Clover metadata into SQL DDL type...
	 * e.g. for a fixed length string Clover field it returns "CHAR(15)", etc.
	 * 
	 * Similar to sqlType2str but this one is more precise as it knows more about the particular clover field
	 * 
	 * @param field
	 * @return
	 */
	public String jetelType2sqlDDL(DataFieldMetadata field);
	
	/**
	 * @return class name where are constants with sql types
	 */
	public String getTypesClassName();
	
	/**
	 * @return constant for sql type, which will be regarded as "Result set"
	 */
	public String getResultSetParameterTypeField();

	/**
	 * This can be used to convert java sql types into real names of a data type instide the database
	 * @return Name of database specific data type corresponding to java.sql.Types type
	 */
	public String sqlType2str(int sqlType);

	/**
	 * Quotes (escapes) a given identifier according to the database specifics.
	 *
	 * @param identifier the identifier to be quoted
	 *
	 * @return the quoted identifier
	 */
	public String quoteIdentifier(String identifier);

	/**
	 * Transforms `query` into another query, which can be used to validate the original `query`
	 * Typically somehow adds some always failing where clause so that the query is never executed
	 * @param query Original query to be validated
	 * @param queryType Type of query
	 * @return A query that can be executed to validate original `query`
	 * @throws SQLException In can query cannot be generated or is otherwise invalid
	 */
	public String getValidateQuery(String query, QueryType queryType) throws SQLException;
	
	/**
	 * Returns whether given string is a literal in gived db engine
	 * Examples:
	 * 'string' - true
	 * fieldName - false
	 * 123 - true
	 * SELECT - false
	 * , - false
	 * `name` - false
	 * etc.
	 * @return
	 */
	public boolean isLiteral(String s);
	
}
