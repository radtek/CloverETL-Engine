/*
 * Copyright 2006-2009 Opensys TM by Javlin, a.s. All rights reserved.
 * Opensys TM by Javlin PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * Opensys TM by Javlin a.s.; Kremencova 18; Prague; Czech Republic
 * www.cloveretl.com; info@cloveretl.com
 *
 */

package org.jetel.util;

import junit.framework.TestCase;

/**
 * @author reichman (info@cloveretl.com)
 *         (c) Opensys TM by Javlin, a.s. (www.cloveretl.com)
 *
 * @created Jul 27, 2015
 */
public class UtilsTest extends TestCase {

	public void testParseHumanReadableSize() {
		assertEquals(69256347648L, DirectMemoryUtils.parseHumanReadableSize("64.5G"));
		assertEquals(268435456, DirectMemoryUtils.parseHumanReadableSize("256M"));
		assertEquals(361999892L, DirectMemoryUtils.parseHumanReadableSize("345.23 MB"));
		assertEquals(186604L, DirectMemoryUtils.parseHumanReadableSize("182.23KB"));
		assertEquals(4159L, DirectMemoryUtils.parseHumanReadableSize("4159"));
		assertEquals(-1L, DirectMemoryUtils.parseHumanReadableSize(""));
		assertEquals(-1L, DirectMemoryUtils.parseHumanReadableSize(null));
		
		try {
			/** Invalid values **/
			DirectMemoryUtils.parseHumanReadableSize("GK");
			DirectMemoryUtils.parseHumanReadableSize("4159D");
			DirectMemoryUtils.parseHumanReadableSize("4159.5");
			fail();
		} catch (IllegalArgumentException e) {
		}
	}
}
