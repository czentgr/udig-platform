/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2012, Refractions Research Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Refractions BSD
 * License v1.0 (http://udig.refractions.net/files/bsd3-v10.html).
 */
package net.refractions.udig.aoi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import net.refractions.udig.aoi.IAOIService;
import net.refractions.udig.internal.aoi.AOIServiceFactory;

import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("nls")
public class AOIServiceTest {

	private AOIServiceFactory aoiFactory = null;
	private IAOIService aOIService = null;
	
	@Before
	public void testService() {
	    aoiFactory = new AOIServiceFactory();
	    assertNotNull(aoiFactory);
	    aOIService = aoiFactory.create(IAOIService.class, null, null);
		assertNotNull(aOIService);
		
		// used to list the existing strategies
		/*for (BoundaryProxy proxy : boundaryService.getProxyList()) {
		    System.out.println(proxy.getId());
		}*/
	}

    @Test
	public void testAllStrategy() {
        aOIService.setProxy(aOIService.findProxy("net.refractions.udig.ui.aoiAll"));
        String id = aOIService.getProxy().getId();
        assertEquals("net.refractions.udig.ui.aoiAll", id);
        
        assertNull(aOIService.getExtent());
        assertNull(aOIService.getGeometry());
        assertNull(aOIService.getCrs());
	}
	
}
