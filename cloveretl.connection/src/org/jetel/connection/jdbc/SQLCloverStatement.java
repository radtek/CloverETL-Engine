/*
*    jETeL/Clover - Java based ETL application framework.
*    Copyright (C) 2005-06  Javlin Consulting <info@javlinconsulting.cz>
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

package org.jetel.connection.jdbc;

import java.sql.BatchUpdateException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.jetel.connection.jdbc.specific.DBConnectionInstance;
import org.jetel.connection.jdbc.specific.JdbcSpecific.AutoGeneratedKeysType;
import org.jetel.data.DataRecord;
import org.jetel.data.Defaults;
import org.jetel.exception.ComponentNotReadyException;
import org.jetel.exception.ConfigurationProblem;
import org.jetel.exception.ConfigurationStatus;
import org.jetel.exception.ConfigurationStatus.Priority;
import org.jetel.exception.ConfigurationStatus.Severity;
import org.jetel.metadata.DataRecordMetadata;
import org.jetel.util.string.StringUtils;

/**
 * This class represents prepared statement with CopySQLData[] object prepared to work with concrete record.
 * It can work with queries in internal clover format or with queries with place holders [?]:
 * <ul><li><i>insert into mytable [(f1,f2,...,fn)] values (val1, $field2, ...,$fieldm ) returning $key := dbfield1, $field := dbfield2</i> 
 * or <i>insert into mytable [(f1,f2,...,fn)] values (val1, $field2, ...,$fieldm ) returning $key := auto_generated, $field := infield</i> 
 * <ul><li>$field2, ...,$fieldm are interpreted as input record's fields</li>
 * <li>$key, $field are interpreted as output record's fields</li>
 * <li>fields on assignments right sides in <i>where</i> clause are interpreted as database or clover fields depending on database type 
 * ({@link org.jetel.component.DBOutputTable} and {@link org.jetel.connection.jdbc.AutoKeyGenerator})</li></ul></li>
 * <li><i>update mytable set dbField1 = $f1 where dbField2=$f2 returning $update:=update_count, $field:=infield</i> or
 * <i>delete from mytable where dbField = $f1 returning $update:=update_count, $field:=infield</i>
 *  <ul><li>$f1, $f2 , infield are interpreted as input record's fields</li>
 * <li>$update, $field are interpreted as output record's fields</li>
 * <li> field on which is mapped <i>update_count</i> is filled by number of records updated in database by current query</li></ul></li>
 * <li><i>insert into mytable [(f1,f2,...,fn)] values (val1, ?, ...,? )</i> or <i>update mytable set dbField1 = ? where dbField2=?</i>
 * <ul><li>needs to set <i>cloverFields</i> and <i>dbFields</i>, eg. <i>cloverFields={field2, ...,fieldm}</i> or 
 * <i>cloverFields={f1, f2}</i> and <i>dbFields={f1,f2,...,fn}</i> or <i>dbFields={dbField1, dbField2}</i> respectively</li>
 * <li>can be set <i>autoGeneratedColumns</i>, eg. <i>{dbField1, .., dbFieldN}</i></li></ul></li></ul>
 * 
 * @author avackova (agata.vackova@javlinconsulting.cz) ; 
 * (c) JavlinConsulting s.r.o.
 *  www.javlinconsulting.cz
 *
 * @since Nov 2, 2007
 *
 */
public class SQLCloverStatement {
	
	public final static String UPDATE_NUMBER_FIELD_NAME = "UPDATE_COUNT";
	public final static String RETURNING_KEY_WORD = "returning";
	
	private final static Pattern CLOVER_OUTPUT_FIELD = Pattern.compile("\\$(\\w+)\\s*" + Defaults.ASSIGN_SIGN);
	private final static Pattern CLOVER_INPUT_FIELD = Pattern.compile("(?<!\\$)\\$(\\w++)(?!:)");
	
    final static Pattern PREPARED_STMT_PATTERN = Pattern.compile("\\?");

	
	private String query;//query as set in constructor
	private String sqlQuery;//query as send to database
	private DBConnectionInstance connection;
	private Statement statement;
	private CopySQLData[] transMap = new CopySQLData[0];
	private String[] cloverInputFields;
	private String[] cloverOutputFields;
	private int[] cloverOutputFieldsIndex;
	private String[] autoGeneratedColumn;
	private int[] autoGeneratedColumnNumber;
	private DataRecord record;
	private List<DataRecord> outRecords = new ArrayList<DataRecord>();
	DataRecord tmpRecord;
	
	private AutoKeyGenerator autoKeyGenerator;
	private ResultSet generatedKeys;
	
	private Integer updatedNumberCloverFieldNumber = -1;
	
	private String tableName;
	private Log logger;
	private boolean returnResult;
	private QueryType queryType;
	
	private SQLIncremental incremental;
	
	private boolean isInitialized = false;
	
	private final static Pattern TABLE_NAME_PATTERN = Pattern.compile(
			"(?:from|into|update)\\s+" + SQLUtil.DB_FIELD_PATTERN, Pattern.CASE_INSENSITIVE);
	
	public enum QueryType {

		INSERT,
		UPDATE,
		DELETE,
		SELECT,
		UNKNOWN;

		public static QueryType fromSqlQuery(String sqlQuery) {
            if (sqlQuery == null) {
                throw new NullPointerException("sqlQuery");
            }

            String normalizedSqlQuery = sqlQuery.trim().toUpperCase();

            for (QueryType queryType : values()) {
				if (normalizedSqlQuery.startsWith(queryType.name())) {
					return queryType;
				}
			}

			return UNKNOWN;
		}

	}
	
	/**
	 * Constructor for sql query containing db - clover mapping
	 * 
	 * @param connection connection to db
	 * @param query sql query
	 * @param record record from which will be set parameters in prepared statement
	 */
	public SQLCloverStatement(DBConnectionInstance connection, String query, DataRecord inRecord){
		this.connection = connection;
		this.query = query;
		this.record = inRecord;
		returnResult = query.toLowerCase().contains(RETURNING_KEY_WORD);
		queryType = QueryType.fromSqlQuery(query);
	}
	
	/**
	 * Constructor for sql query containing question marks
	 * 
	 * @param connection connection to db
	 * @param query sql query
	 * @param record record from which will be set parameters in prepared statement
	 * @param cloverFields clover fields to populate 
	 * 
	 * @see org.jetel.component.DBOutputTable
	 */
	public SQLCloverStatement(DBConnectionInstance connection, String query, DataRecord inRecord, 
			String[] cloverFields){
		this(connection, query, inRecord);
		this.cloverInputFields = cloverFields;
	}

	/**
	 * Constructor for sql query containing question marks
	 * 
	 * @param connection connection to db
	 * @param query sql query
	 * @param record record from which will be set parameters in prepared statement
	 * @param cloverFields clover fields to populate 
	 * @param autoGeneratedColumns auto generated columns to be returned
	 */
	public SQLCloverStatement(DBConnectionInstance connection, String query, DataRecord record, 
			String[] cloverFields, String[] autoGeneratedColumns){
		this(connection, query, record, cloverFields);
		this.autoGeneratedColumn = autoGeneratedColumns;
		returnResult = returnResult || autoGeneratedColumns != null && autoGeneratedColumns.length > 0;
	}
	
	/**
	 * Prepares trans map between input record and prepared statement
	 * 
	 * @throws ComponentNotReadyException
	 */
	private void initTransMap() throws ComponentNotReadyException{
		List<Integer> dbFieldTypes;
		if (cloverInputFields != null) {
			try {
				dbFieldTypes= SQLUtil.getFieldTypes(record.getMetadata(), cloverInputFields, connection.getJdbcSpecific());
				transMap = CopySQLData.jetel2sqlTransMap(dbFieldTypes, record, cloverInputFields);
			} catch (Exception e) {
				throw new ComponentNotReadyException(e);
			}
		} else{
			dbFieldTypes= SQLUtil.getFieldTypes(record.getMetadata(), connection.getJdbcSpecific());
			transMap = CopySQLData.jetel2sqlTransMap(dbFieldTypes, record);
		}		
	}
	
	/**
	 * This method prepares statement from query (which can contain db <--> clover fields mapping)
	 * and checks input metadata.
	 * 
	 * @throws ComponentNotReadyException
	 * @throws SQLException
	 */
	public void init() throws ComponentNotReadyException, SQLException{
		if (isInitialized) return;
		int ri = query.toLowerCase().indexOf(RETURNING_KEY_WORD);
		if (ri != -1 && autoGeneratedColumn != null) {
			logger.warn("Autogenerated columns defined in both \"sqlQuery\" and "
					+ "\"autoGeneratedColumns\" attributes --> getting value from "
					+  "\"sqlQuery\" attribute (" + query + ").");
			autoGeneratedColumn = null;
		}
		sqlQuery = ri != -1 ? query.substring(0, ri) : query;
		String returningClause = ri != -1 ?	query.substring(ri) : "";
		//remove input clover fields from query
		Matcher inputFieldsMatecher = CLOVER_INPUT_FIELD.matcher(sqlQuery);
		ArrayList<String> inputFields = new ArrayList<String>();
		StringBuffer sb = new StringBuffer();
		while (inputFieldsMatecher.find()) {
			inputFields.add(inputFieldsMatecher.group(1));
			inputFieldsMatecher.appendReplacement(sb, "?");
		}
		inputFieldsMatecher.appendTail(sb);
		if (inputFields.size() > 0) {
			if (cloverInputFields != null) {
				if (logger != null) {
					logger.warn("Clover input fields defined in both \"sqlQuery\" and "
							+ "\"cloverFields\" attributes --> getting value from " + "\"sqlQuery\" attribute ("
							+ query + ").");
				}
			}
			cloverInputFields = inputFields.toArray(new String[inputFields.size()]);
		}
		if (record != null) {
			initTransMap();
		}
		
		//remove clover output fields from query
		Matcher outputFieldsMatcher = CLOVER_OUTPUT_FIELD.matcher(sb.append(returningClause).toString());
		ArrayList<String> outputFields = new ArrayList<String>();
		sb = new StringBuffer();
		while (outputFieldsMatcher.find()) {
			outputFields.add(outputFieldsMatcher.group(1));
			outputFieldsMatcher.appendReplacement(sb, "");
		}
		outputFieldsMatcher.appendTail(sb);
		if (outputFields.size() > 0) {
			cloverOutputFields = outputFields.toArray(new String[outputFields.size()]);
		}
		
		sqlQuery = sb.toString().replace("$$", "$");
		
		//autogenerated columns
		if (returnResult && autoGeneratedColumn == null) {
			autoGeneratedColumn = sqlQuery.substring(sqlQuery.toLowerCase().indexOf(
					RETURNING_KEY_WORD) + RETURNING_KEY_WORD.length()).trim().split("\\s*,\\s*");
		}
	
		if (returnResult) {
			sqlQuery = sqlQuery.substring(0, sqlQuery.toString().toLowerCase().indexOf(RETURNING_KEY_WORD));
			if (queryType == QueryType.INSERT) {
				autoKeyGenerator = new AutoKeyGenerator(connection, sqlQuery, autoGeneratedColumn);
				autoKeyGenerator.setLogger(logger);
				autoKeyGenerator.setFillFields(cloverOutputFields);
				statement = autoKeyGenerator.prepareStatement();
			}else{
				if (autoGeneratedColumn != null) {//update or delete
					List<String> autoFields = new ArrayList<String>();
					for (int i = 0; i < autoGeneratedColumn.length; i++) {
						if (!autoGeneratedColumn[i].equalsIgnoreCase(UPDATE_NUMBER_FIELD_NAME)) {
							autoFields.add(autoGeneratedColumn[i].startsWith(Defaults.CLOVER_FIELD_INDICATOR) ?
									autoGeneratedColumn[i].substring(Defaults.CLOVER_FIELD_INDICATOR.length()) :
									autoGeneratedColumn[i]);
						} 
					}
					autoGeneratedColumnNumber = new int[autoFields.size()];
					for (int i = 0; i < autoGeneratedColumnNumber.length; i++) {
						autoGeneratedColumnNumber[i] = record.getMetadata().getFieldPosition(autoFields.get(i));
						if (autoGeneratedColumnNumber[i] == -1) {
							throw new ComponentNotReadyException("Field " + StringUtils.quote(autoFields.get(i)) + " dos not exist in input record!!!");
						}
					}
				}
				statement = connection.getSqlConnection().prepareStatement(sqlQuery);
			}
		}else{
			if (incremental == null) {
				//we can create Statement (not PreparedStatement) only in case input record is not null
				statement = record != null ? connection.getSqlConnection().prepareStatement(sqlQuery) :	connection.getSqlConnection().createStatement();
			}else{//we have incremental
				statement = incremental.updateQuery(connection);
			}
		}
		isInitialized = true;
	}
	
	/**
	 * Validates this object for output metadata
	 * 
	 * @param outMetadata metadata of the record, that will be pass in {@link #executeUpdate(DataRecord)}
	 * or {@link #addBatch(DataRecord)} method, can be null
	 * @return status 
	 * @throws SQLException if validation is impossible
	 */
	public ConfigurationStatus checkConfig(ConfigurationStatus status, DataRecordMetadata outMetadata) throws SQLException{
		try{
			init();
		}catch (Exception e) {
			status.add(new ConfigurationProblem(e.getMessage(), Severity.ERROR, null, Priority.NORMAL));
			return status;
		}
		if (record != null) {
			String validateMap = CopySQLData.validateJetel2sqlMap(transMap, (PreparedStatement) statement, 
					record.getMetadata(), connection.getJdbcSpecific());
			if (validateMap != null) {
				status.add(new ConfigurationProblem(validateMap, Severity.WARNING, null, Priority.NORMAL));
			}
		}
		if (outMetadata == null) return status;
		DataRecord outRecord = new DataRecord(outMetadata);
		outRecord.init();
		try {
			prepareMapping(outRecord);
		} catch (Exception e) {
			status.add(new ConfigurationProblem(e.getMessage(), Severity.ERROR, null, Priority.NORMAL));
		}finally{//"reset" mapping
			cloverOutputFieldsIndex = null;
		}
		CopySQLData[] tm;
		if (!(statement instanceof PreparedStatement)) return status;
		ResultSetMetaData resultMetadata = ((PreparedStatement) statement).getMetaData();
		if (resultMetadata == null) {
			return status;
		}
		if (cloverOutputFields != null){
			List<Integer> fieldTypes = SQLUtil.getFieldTypes(resultMetadata);
			if (fieldTypes != null && cloverOutputFields != null && fieldTypes.size() != cloverOutputFields.length) {
				ConfigurationProblem problem = new ConfigurationProblem(
						"Can't validate SQL statement "+query, ConfigurationStatus.Severity.WARNING, null,
						ConfigurationStatus.Priority.NORMAL);
				status.add(problem);
				return status;
			}
			tm = CopySQLData.sql2JetelTransMap(fieldTypes, outMetadata, outRecord, cloverOutputFields);
		}else{
			tm = CopySQLData.sql2JetelTransMap(SQLUtil.getFieldTypes(resultMetadata), outMetadata, outRecord);
		}
		String validateMap = CopySQLData.validateSql2JetelMap(tm, resultMetadata, outRecord.getMetadata(), 
				connection.getJdbcSpecific());
		if (validateMap != null) {
			status.add(new ConfigurationProblem(validateMap, Severity.WARNING, null, Priority.NORMAL));
		}
		return status;
	}
	
	/**
	 * Resets this object. For complete reset method setInRecord must be called too.
	 * 
	 * @throws SQLException
	 */
	public void reset() throws SQLException{
		if (autoKeyGenerator != null) {
			statement = autoKeyGenerator.reset();
		}else{
			statement.close();
			statement = connection.getSqlConnection().prepareStatement(sqlQuery);
		}
	}

	public void setConnection(DBConnectionInstance connection) {
		this.connection = connection;
	}

	/**
	 * Sets input record.
	 * 
	 * @param inRecord
	 * @throws ComponentNotReadyException
	 */
	public void setInRecord(DataRecord inRecord) throws ComponentNotReadyException {
		this.record = inRecord;
		if (statement != null && !(statement instanceof PreparedStatement)) {//we haven't counted on input record, so statement have been created, not PreparedStatement
			try {
				try {
					close();
				} catch (SQLException e) {
					// do nothing
				}
				isInitialized = false;
				init();
			} catch (SQLException e) {
				throw new ComponentNotReadyException(e);
			}
			return;
		}
		if (transMap != null) {
			CopySQLData.resetDataRecord(transMap, record);
		}else{
			initTransMap();
		}
	}
	
	/**
	 * Fills prepared statements with data obtained from input record and executes update on database.
	 * If argument is not null sets on requested field number of updated records and  fills its requested fields from input record
	 * 
	 * @return number of updated records in database
	 * @throws SQLException
	 */
	public int executeUpdate(DataRecord outRecord) throws SQLException{
		//fill trans map
		for (int i = 0; i < transMap.length; i++) {
			transMap[i].jetel2sql((PreparedStatement) statement);
		}
		//execute query
		int updatedRecords = ((PreparedStatement)statement).executeUpdate();
		//fill output record
		switch (queryType) {
		case SELECT:
		case INSERT:
		case UNKNOWN:
			fillKeyRecord(outRecord);
			break;
		case UPDATE:
		case DELETE:
			if (outRecord != null) {
				if (cloverOutputFieldsIndex == null) {//first call 
					prepareMapping(outRecord);
				}
				if (updatedNumberCloverFieldNumber != -1) {
					outRecord.getField(updatedNumberCloverFieldNumber).setValue(updatedRecords);
				}
				for (int i = 0; i < autoGeneratedColumnNumber.length; i++) {
					outRecord.getField(cloverOutputFieldsIndex[i]).setValue(record.getField(autoGeneratedColumnNumber[i]));
				}
			}
			break;
		}
		return updatedRecords;
	}
	
	/**
	 * Fills prepared statements with data obtained from input record and executes query on database.
	 * 
	 * @return result set got from database
	 * @throws SQLException
	 */
	public ResultSet executeQuery() throws SQLException{
		if (statement instanceof PreparedStatement) {
			//fill trans map
			for (int i = 0; i < transMap.length; i++) {
				transMap[i].jetel2sql((PreparedStatement) statement);
			}
			return ((PreparedStatement)statement).executeQuery();
		}
		return statement.executeQuery(getQuery());
	}
	
	/**
	 * Modifies the query so that it won't do anything for selects, updates and deletes,
	 * only report sql and other possible problems.
	 * 
	 * At the moment, insert queries can not be validated (how one does that)
	 * 
	 * It would be best to use a parser to parse the query and add where 0=1 appropriately
	 * At the moment selects are wrapped inside outer select and update/delete statements
	 * are simply searched for where clause or added if not found
	 * 
	 * WARNING ====== READ BEFORE USE =====
	 * This method will break your query and will not fall back. You have to destroy this instance
	 * and recreate it if you want to use it normally. You have been warned!
	 * 
	 * @author pnajvar
	 * @since Mar 2009
	 * @throws SQLException
	 * @throws ComponentNotReadyException 
	 */
	
	public void executeValidate() throws SQLException, ComponentNotReadyException {

        String q = connection.getJdbcSpecific().getValidateQuery(getQuery(), getQueryType());
        
        if (q != null) {
        	this.isInitialized = false;
        	this.query = q;
        	init();
        	if (getQueryType().equals(QueryType.SELECT)) {
        		
        		PreparedStatement ps = null;
        		// for prepared statements use null values and construct simple prepared statement
    			int wherePos = query.toLowerCase().indexOf("where");
        		if (wherePos > -1) {
        			Matcher m = PREPARED_STMT_PATTERN.matcher(query); 
        			int paramCntr = 1;
        			if (m.find(wherePos)) {
        				ps = connection.getSqlConnection().prepareStatement(q);
        				
        				setObjectValue(paramCntr, ps);
        				while(m.find()) {
        					setObjectValue(++paramCntr, ps);
        				}
        			}
        		}
        		if (ps != null) {
        			ps.execute();
        		} else {
        			// not a prepared statement
        			executeQuery();
        		}
        		
        	} else {
        		if (statement instanceof PreparedStatement) {
        			executeUpdate(record);
        		} else {
        			statement.executeUpdate(q);
        		}
        	}
        }
        
	}
	
	void setObjectValue(int paramIndex, PreparedStatement ps) {
		try {
			ps.setNull(paramIndex, ps.getParameterMetaData().getParameterType(paramIndex));
		} catch (SQLException e) {
			try {
				ps.setObject(paramIndex, null);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	/**
	 * Initializes fields numbers arrays due to names
	 * 
	 * @param outRecord
	 */
	private void prepareMapping(DataRecord outRecord){
		//if we don't want to get anything back create empty arrays to not get NPE
		if (autoGeneratedColumn == null) {
			autoGeneratedColumn = new String[0];
			return;
		}
		if (cloverOutputFields == null){
			cloverOutputFields = new String[0];
		}
		ArrayList<String> outFields = new ArrayList<String>();
		for (int i = 0; i < autoGeneratedColumn.length; i++) {
			if (autoGeneratedColumn[i].equalsIgnoreCase(UPDATE_NUMBER_FIELD_NAME)) {
				updatedNumberCloverFieldNumber = outRecord.getMetadata().getFieldPosition(cloverOutputFields[i]);
			}else{
				outFields.add(cloverOutputFields[i]);
			}
		}
		//prepare numbers from names
		cloverOutputFieldsIndex = new int[outFields.size()];
		for (int i = 0; i < outFields.size(); i++) {
			cloverOutputFieldsIndex[i] = outRecord.getMetadata().getFieldPosition(outFields.get(i));
			if (cloverOutputFieldsIndex[i] == -1) {
				throw new RuntimeException("Field " + StringUtils.quote(outFields.get(i)) + " dos not exist in output record!!!");
			}
		}
	}
	
	/**
	 * Adds set of parameters to batch and prepares output record from input record
	 * 
	 * @throws SQLException
	 */
	public void addBatch(DataRecord outRecord) throws SQLException{
		//fill trans map
		for (int i = 0; i < transMap.length; i++) {
			transMap[i].jetel2sql((PreparedStatement) statement);
		}
		//add statement to batch
		((PreparedStatement)statement).addBatch();
		//prepare output record from input record
		if (outRecord != null) {
			if (cloverOutputFieldsIndex == null) {//first call 
				prepareMapping(outRecord);
			}
			tmpRecord = outRecord.duplicate();
			for (int i = 0; i < cloverOutputFieldsIndex.length; i++) {
				tmpRecord.getField(cloverOutputFieldsIndex[i]).setValue(record.getField(autoGeneratedColumnNumber[i]));
			}
			outRecords.add(tmpRecord);
		}
	}
	
	/**
	 * Submits a batch of commands to the database for execution and if all commands execute successfully, returns an 
	 * array of update counts. Fills stored records with number of updated records in database.
	 * 
	 * @return an array of update counts containing one element for each command in the batch. The elements of the 
	 * array are ordered according to the order in which commands were added to the batch.
	 * @throws SQLException
	 */
	public int[] executeBatch() throws SQLException{
		int[] updatedRecords = null;
		BatchUpdateException ex = null;
		try {
			updatedRecords = statement.executeBatch();
		} catch (BatchUpdateException e) {
			updatedRecords = e.getUpdateCounts();
			ex = e;
		}
		for (int i = 0; i < outRecords.size(); i++) {
			outRecords.get(i).getField(updatedNumberCloverFieldNumber).setValue(updatedRecords[i]);
		}
		if (ex != null) throw ex;
		return updatedRecords;
	}
	
	/**
	 * @return updated records stored by addBatch(DataRecord) method 
	 */
	public DataRecord[] getBatchResult(){
		return outRecords.toArray(new DataRecord[outRecords.size()]);
	}
	/**
	 * Empties this Statement object's current list of SQL commands. Removes all stored records.
	 * 
	 * @throws SQLException
	 */
	public void clearBatch() throws SQLException{
		statement.clearBatch();
		outRecords.clear();
	}
	 
	/**
	 * Fills record by data received from database by calling getGeneratedKeys() method
	 * 
	 * @param keyRecord record to fill 
	 * @throws SQLException
	 */
	private void fillKeyRecord(DataRecord keyRecord) throws SQLException{
		if (autoKeyGenerator == null) {
			return;
		}
		if (autoKeyGenerator.getAutoKeyType() == AutoGeneratedKeysType.NONE) {
			return;
		}
		
		generatedKeys = statement.getGeneratedKeys();
		if (generatedKeys.next()) {
			autoKeyGenerator.fillKeyRecord(record, keyRecord, generatedKeys);
		}					
	}
	
	/**
	 * @param batchUpdate
	 */
	public void setBatchUpdate(boolean batchUpdate){
        CopySQLData.setBatchUpdate(transMap,batchUpdate);
	}
	
	/**
	 * Releases this Statement object's database and JDBC resources immediately instead of waiting for this to happen 
	 * when it is automatically closed.
	 * 
	 * @throws SQLException
	 */
	public void close() throws SQLException{
		if (statement != null) {
			statement.close();
		}
	}
	
	/**
	 * @return underlying PreparedStatement object
	 */
	public Statement getStatement(){
		return statement;
	}

	/**
	 * @return sql query as is send to database. For original query use toString() method
	 */
	public String getQuery(){
		return incremental != null ? incremental.getPreparedQuery() : sqlQuery;
	}
	
	/**
	 * @return logger
	 */
	public Log getLogger() {
		return logger;
	}

	/**
	 * Sets logger
	 * 
	 * @param logger
	 */
	public void setLogger(Log logger) {
		this.logger = logger;
	}

	/**
	 * @return table name from query
	 */
	public String getTableName() {
		if (tableName != null) {
			return tableName;
		}else{
			Matcher matcher = TABLE_NAME_PATTERN.matcher(query);
			if (matcher.find()) {
				tableName = matcher.group(1);
			}
		}
		return tableName;
	}

	/**
	 * @return <b>true<b> if and only if query is to return some values (<i>returning</i> clause or exist some autogenereted columns)
	 */
	public boolean returnResult() {
		return returnResult;
	}

	public QueryType getQueryType() {
		return queryType;
	}

	@Override
	public String toString() {
		return query;
	}

	/**
	 * @return names of clover fields, which will be used for filling prepared statement
	 */
	public String[] getCloverInputFields() {
		return cloverInputFields;
	}

	/**
	 * @return names of clover fields, which will be fulfilled from database 
	 */
	public String[] getCloverOutputFields() {
		return cloverOutputFields;
	}

	/**
	 * @return names of fields for getting from database or from input record
	 */
	public String[] getAutoGeneratedColumn() {
		return autoGeneratedColumn;
	}

	public SQLIncremental getIncremental() {
		return incremental;
	}

	public void setIncremental(SQLIncremental incremental) {
		this.incremental = incremental;
	}
}


