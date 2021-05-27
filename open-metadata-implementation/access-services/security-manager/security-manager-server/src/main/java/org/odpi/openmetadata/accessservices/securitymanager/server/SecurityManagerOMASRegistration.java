/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.accessservices.securitymanager.server;

import org.odpi.openmetadata.accessservices.securitymanager.admin.SecurityManagerAdmin;
import org.odpi.openmetadata.adminservices.configuration.OMAGAccessServiceRegistration;
import org.odpi.openmetadata.adminservices.configuration.registration.AccessServiceDescription;
import org.odpi.openmetadata.adminservices.configuration.registration.AccessServiceRegistration;
import org.odpi.openmetadata.adminservices.configuration.registration.ServiceOperationalStatus;


/**
 * SecurityManagerOMASRegistration registers the access service with the OMAG Server administration services.
 * This registration must be driven once at server start up.  The OMAG Server administration services
 * then use this registration information as confirmation that there is an implementation of this
 * access service in the server and it can be configured and used.
 */
public class SecurityManagerOMASRegistration
{
    /**
     * Pass information about this access service to the OMAG Server administration services.
     */
    static void registerAccessService()
    {
        AccessServiceDescription myDescription = AccessServiceDescription.SECURITY_MANAGER_OMAS;
        AccessServiceRegistration myRegistration = new AccessServiceRegistration(myDescription,
                                                                                 ServiceOperationalStatus.ENABLED,
                                                                                 SecurityManagerAdmin.class.getName());

        OMAGAccessServiceRegistration.registerAccessService(myRegistration);
    }
}


