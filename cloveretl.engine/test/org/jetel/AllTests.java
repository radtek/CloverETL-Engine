/*
 *  jETeL/Clover - Java based ETL application framework.
 *  Created on Mar 26, 2003
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

package test.org.jetel;

/**
 * @author maciorowski
 * @version 1.0
 *
 */
import test.org.jetel.data.DataTestSuite;
import test.org.jetel.metadata.MetadataTestSuite;
import test.org.jetel.util.UtilTestSuite;

import junit.framework.*;

/**
 * TestSuite that runs all the sample tests
 *
 */
public class AllTests {

	public static Test suite() {

		TestSuite suite = new TestSuite();

		suite.addTest(UtilTestSuite.suite());
		suite.addTest(MetadataTestSuite.suite());
		suite.addTest(DataTestSuite.suite());
		
		return suite;
	}

	public static void main(String args[]) {
		junit.textui.TestRunner.run(suite());
	}
}

