

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.jetel.data.DataRecord;
import org.jetel.data.parser.SQLDataParser;
import org.jetel.database.DBConnection;
import org.jetel.exception.BadDataFormatException;
import org.jetel.exception.ComponentNotReadyException;
import org.jetel.metadata.DataRecordMetadata;
import org.jetel.metadata.DataRecordMetadataXMLReaderWriter;

import junit.framework.TestCase;

/**
 * @author maciorowski
 *
 */
public class SQLDataParserTest  extends TestCase {
private SQLDataParser aParser2 = null;
private DataRecord record;
	
protected void setUp() { 
	DataRecordMetadata metadata = null;
	DataRecordMetadataXMLReaderWriter xmlReader = new DataRecordMetadataXMLReaderWriter();
	DBConnection aDBConnection = null;
				
	try {
		metadata = xmlReader.read(new FileInputStream("config\\test\\rec_def\\db_null_def_rec.xml"));
		aDBConnection = new DBConnection("", "config\\test\\msaccess.clover_test.txt");
	} catch(FileNotFoundException e){
		e.printStackTrace();
	}
	
	record = new DataRecord(metadata);
	record.init();

	aParser2 = new SQLDataParser("connection","SELECT * FROM bad");
	try {
		aParser2.open(aDBConnection,metadata);
		aParser2.initSQLDataMap(record);
	} catch (ComponentNotReadyException e1) {
		e1.printStackTrace();
	}
}
	
   protected void tearDown() {
	   aParser2.close();
	   aParser2 = null;
	   record  = null;
   }

public void test_parsing() {
// the content of the test file
//	  N/AStone          01/11/93
//	-15.5          112  11/03/02
//	 -0.7Bone Broo    99        
	int recCount = 0;
	try{
		while((record=aParser2.getNext(record))!=null){
			if(recCount==0) {
				assertEquals(record.getField(0).toString(),"1.0");
				assertTrue(record.getField(1).isNull());
				assertEquals(record.getField(2).toString(),"101");
				assertEquals(record.getField(3).toString(),"1993-01-11 00:00:00");
			} else if(recCount==1) {
				assertEquals(record.getField(0).toString(),"-15.5");
				assertTrue(record.getField(3).isNull());
				assertEquals(record.getField(2).toString(),"112");
				assertEquals(record.getField(1).toString(),"Brook");
			} else if(recCount==2) {
				assertTrue(record.getField(0).isNull());
				assertEquals(record.getField(1).toString(),"Bone Broo");
				assertTrue(record.getField(2).isNull());
				assertEquals(record.getField(3).toString(),"2003-01-01 00:00:00");
			}
			recCount++;
		}
	} catch (BadDataFormatException e){	
		fail("Should not raise an BadDataFormatException");
		e.printStackTrace();
	} catch (Exception ee){
		fail("Should not throw Exception");
		ee.printStackTrace();
	}
   assertEquals(3,recCount);
}
	

}
