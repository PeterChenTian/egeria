/* SPDX-License-Identifier: Apache 2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.commonservices.generichandlers;


import org.odpi.openmetadata.commonservices.ffdc.InvalidParameterHandler;
import org.odpi.openmetadata.commonservices.repositoryhandler.RepositoryHandler;
import org.odpi.openmetadata.frameworks.auditlog.AuditLog;
import org.odpi.openmetadata.frameworks.connectors.ffdc.InvalidParameterException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.PropertyServerException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.UserNotAuthorizedException;
import org.odpi.openmetadata.metadatasecurity.server.OpenMetadataServerSecurityVerifier;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.EntityDetail;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.repositoryconnector.OMRSRepositoryHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ActorProfileHandler provides the exchange of metadata about glossaries between the repository and the OMAS.
 * Note glossaries are governance metadata and are always defined with LOCAL-COHORT provenance.
 *
 * @param <B> class that represents the profile
 */
public class ActorProfileHandler<B> extends ReferenceableHandler<B>
{
    /**
     * Construct the handler with information needed to work with B objects.
     *
     * @param converter specific converter for this bean class
     * @param beanClass name of bean class that is represented by the generic class B
     * @param serviceName name of this service
     * @param serverName name of the local server
     * @param invalidParameterHandler handler for managing parameter errors
     * @param repositoryHandler manages calls to the repository services
     * @param repositoryHelper provides utilities for manipulating the repository services objects
     * @param localServerUserId userId for this server
     * @param securityVerifier open metadata security services verifier
     * @param supportedZones list of zones that the access service is allowed to serve B instances from
     * @param defaultZones list of zones that the access service should set in all new B instances
     * @param publishZones list of zones that the access service sets up in published B instances
     * @param auditLog destination for audit log events
     */
    public ActorProfileHandler(OpenMetadataAPIGenericConverter<B> converter,
                               Class<B>                           beanClass,
                               String                             serviceName,
                               String                             serverName,
                               InvalidParameterHandler            invalidParameterHandler,
                               RepositoryHandler                  repositoryHandler,
                               OMRSRepositoryHelper               repositoryHelper,
                               String                             localServerUserId,
                               OpenMetadataServerSecurityVerifier securityVerifier,
                               List<String>                       supportedZones,
                               List<String>                       defaultZones,
                               List<String>                       publishZones,
                               AuditLog                           auditLog)
    {
        super(converter,
              beanClass,
              serviceName,
              serverName,
              invalidParameterHandler,
              repositoryHandler,
              repositoryHelper,
              localServerUserId,
              securityVerifier,
              supportedZones,
              defaultZones,
              publishZones,
              auditLog);
    }


    /**
     * Create the anchor object that all elements in a profile (terms and categories) are linked to.
     *
     * @param userId calling user
     * @param externalSourceGUID     unique identifier of software server capability representing the caller
     * @param externalSourceName     unique name of software server capability representing the caller
     * @param qualifiedName unique name for the profile - used in other configuration
     * @param name short display name for the profile
     * @param description description of the governance profile
     * @param additionalProperties additional properties for a profile
     * @param suppliedTypeName type name from the caller (enables creation of subtypes)
     * @param extendedProperties  properties for a governance profile subtype
     * @param methodName calling method
     *
     * @return unique identifier of the new profile object
     * @throws InvalidParameterException qualifiedName or userId is null
     * @throws PropertyServerException problem accessing property server
     * @throws UserNotAuthorizedException security access problem
     */
    public String createActorProfile(String              userId,
                                     String              externalSourceGUID,
                                     String              externalSourceName,
                                     String              qualifiedName,
                                     String              name,
                                     String              description,
                                     Map<String, String> additionalProperties,
                                     String              suppliedTypeName,
                                     Map<String, Object> extendedProperties,
                                     String              methodName) throws InvalidParameterException,
                                                                            UserNotAuthorizedException,
                                                                            PropertyServerException
    {
        final String qualifiedNameParameterName = "qualifiedName";

        invalidParameterHandler.validateUserId(userId, methodName);
        invalidParameterHandler.validateName(qualifiedName, qualifiedNameParameterName, methodName);

        String typeName = OpenMetadataAPIMapper.ACTOR_PROFILE_TYPE_NAME;

        if (suppliedTypeName != null)
        {
            typeName = suppliedTypeName;
        }

        String typeGUID = invalidParameterHandler.validateTypeName(typeName,
                                                                   OpenMetadataAPIMapper.ACTOR_PROFILE_TYPE_NAME,
                                                                   serviceName,
                                                                   methodName,
                                                                   repositoryHelper);

        ActorProfileBuilder builder = new ActorProfileBuilder(qualifiedName,
                                                              name,
                                                              description,
                                                              additionalProperties,
                                                              typeGUID,
                                                              typeName,
                                                              extendedProperties,
                                                              repositoryHelper,
                                                              serviceName,
                                                              serverName);

        return this.createBeanInRepository(userId,
                                           externalSourceGUID,
                                           externalSourceName,
                                           typeGUID,
                                           typeName,
                                           qualifiedName,
                                           OpenMetadataAPIMapper.QUALIFIED_NAME_PROPERTY_NAME,
                                           builder,
                                           methodName);
    }


    /**
     * Create a new metadata element to represent a profile using an existing metadata element as a template.
     * The template defines additional classifications and relationships that should be added to the new profile.
     *
     * All categories and terms are linked to a single profile.  They are owned by this profile and if the
     * profile is deleted, any linked terms and categories are deleted as well.
     *
     * @param userId calling user
     * @param externalSourceGUID     unique identifier of software server capability representing the caller
     * @param externalSourceName     unique name of software server capability representing the caller
     * @param templateGUID unique identifier of the metadata element to copy
     * @param qualifiedName unique name for the profile - used in other configuration
     * @param name short display name for the profile
     * @param description description of the governance profile
     * @param methodName calling method
     *
     * @return unique identifier of the new metadata element
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public String createActorProfileFromTemplate(String userId,
                                                 String externalSourceGUID,
                                                 String externalSourceName,
                                                 String templateGUID,
                                                 String qualifiedName,
                                                 String name,
                                                 String description,
                                                 String methodName) throws InvalidParameterException,
                                                                           UserNotAuthorizedException,
                                                                           PropertyServerException
    {
        final String templateGUIDParameterName   = "templateGUID";
        final String qualifiedNameParameterName  = "qualifiedName";

        invalidParameterHandler.validateUserId(userId, methodName);
        invalidParameterHandler.validateGUID(templateGUID, templateGUIDParameterName, methodName);
        invalidParameterHandler.validateName(qualifiedName, qualifiedNameParameterName, methodName);

        ActorProfileBuilder builder = new ActorProfileBuilder(qualifiedName,
                                                              name,
                                                              description,
                                                              repositoryHelper,
                                                              serviceName,
                                                              serverName);

        return this.createBeanFromTemplate(userId,
                                           externalSourceGUID,
                                           externalSourceName,
                                           templateGUID,
                                           templateGUIDParameterName,
                                           OpenMetadataAPIMapper.ACTOR_PROFILE_TYPE_GUID,
                                           OpenMetadataAPIMapper.ACTOR_PROFILE_TYPE_NAME,
                                           qualifiedName,
                                           OpenMetadataAPIMapper.QUALIFIED_NAME_PROPERTY_NAME,
                                           builder,
                                           methodName);
    }


    /**
     * Update the anchor object that all elements in a profile (terms and categories) are linked to.
     *
     * @param userId calling user
     * @param externalSourceGUID     unique identifier of software server capability representing the caller
     * @param externalSourceName     unique name of software server capability representing the caller
     * @param profileGUID unique identifier of the profile to update
     * @param profileGUIDParameterName parameter passing the profileGUID
     * @param qualifiedName unique name for the profile - used in other configuration
     * @param name short display name for the profile
     * @param description description of the governance profile
     * @param additionalProperties additional properties for a governance profile
     * @param typeName type of profile
     * @param extendedProperties  properties for a governance profile subtype
     * @param isMergeUpdate should the supplied properties be merged with existing properties (true) only replacing the properties with
     *                      matching names, or should the entire properties of the instance be replaced?
     * @param methodName calling method
     *
     * @throws InvalidParameterException qualifiedName or userId is null
     * @throws PropertyServerException problem accessing property server
     * @throws UserNotAuthorizedException security access problem
     */
    public void   updateActorProfile(String              userId,
                                     String              externalSourceGUID,
                                     String              externalSourceName,
                                     String              profileGUID,
                                     String              profileGUIDParameterName,
                                     String              qualifiedName,
                                     String              name,
                                     String              description,
                                     Map<String, String> additionalProperties,
                                     String              typeName,
                                     Map<String, Object> extendedProperties,
                                     boolean             isMergeUpdate,
                                     String              methodName) throws InvalidParameterException,
                                                                            UserNotAuthorizedException,
                                                                            PropertyServerException
    {
        final String qualifiedNameParameterName = "qualifiedName";

        invalidParameterHandler.validateUserId(userId, methodName);
        invalidParameterHandler.validateGUID(profileGUID, profileGUIDParameterName, methodName);
        invalidParameterHandler.validateName(qualifiedName, qualifiedNameParameterName, methodName);

        String typeGUID = invalidParameterHandler.validateTypeName(typeName,
                                                                   OpenMetadataAPIMapper.ACTOR_PROFILE_TYPE_NAME,
                                                                   serviceName,
                                                                   methodName,
                                                                   repositoryHelper);

        ActorProfileBuilder builder = new ActorProfileBuilder(qualifiedName,
                                                              name,
                                                              description,
                                                              additionalProperties,
                                                              typeGUID,
                                                              typeName,
                                                              extendedProperties,
                                                              repositoryHelper,
                                                              serviceName,
                                                              serverName);

        this.updateBeanInRepository(userId,
                                    externalSourceGUID,
                                    externalSourceName,
                                    profileGUID,
                                    profileGUIDParameterName,
                                    typeGUID,
                                    typeName,
                                    builder.getInstanceProperties(methodName),
                                    isMergeUpdate,
                                    methodName);
    }


    /**
     * Link two person profiles as peers.
     *
     * @param userId calling user
     * @param externalSourceGUID     unique identifier of software server capability representing the caller
     * @param externalSourceName     unique name of software server capability representing the caller
     * @param profile1GUID unique identifier of person profile
     * @param profile1GUIDParameterName parameter name supplying profile1GUID
     * @param profile2GUID  unique identifier of the other person profile
     * @param profile2GUIDParameterName parameter name supplying profile2GUID
     * @param methodName calling method
     *
     * @throws InvalidParameterException entity not known, null userId or guid
     * @throws PropertyServerException problem accessing property server
     * @throws UserNotAuthorizedException security access problem
     */
    public void  linkPeerPersonProfiles(String userId,
                                        String externalSourceGUID,
                                        String externalSourceName,
                                        String profile1GUID,
                                        String profile1GUIDParameterName,
                                        String profile2GUID,
                                        String profile2GUIDParameterName,
                                        String methodName) throws InvalidParameterException,
                                                                  UserNotAuthorizedException,
                                                                  PropertyServerException
    {
        this.linkElementToElement(userId,
                                  externalSourceGUID,
                                  externalSourceName,
                                  profile1GUID,
                                  profile1GUIDParameterName,
                                  OpenMetadataAPIMapper.PERSON_TYPE_NAME,
                                  profile2GUID,
                                  profile2GUIDParameterName,
                                  OpenMetadataAPIMapper.PERSON_TYPE_NAME,
                                  supportedZones,
                                  OpenMetadataAPIMapper.PEER_RELATIONSHIP_TYPE_GUID,
                                  OpenMetadataAPIMapper.PEER_RELATIONSHIP_TYPE_NAME,
                                  null,
                                  methodName);
    }



    /**
     * Unlink two person profiles as peers.
     *
     * @param userId calling user
     * @param externalSourceGUID     unique identifier of software server capability representing the caller
     * @param externalSourceName     unique name of software server capability representing the caller
     * @param profile1GUID unique identifier of person profile
     * @param profile1GUIDParameterName parameter name supplying profile1GUID
     * @param profile2GUID  unique identifier of the other person profile
     * @param profile2GUIDParameterName parameter name supplying profile2GUID
     * @param methodName calling method
     *
     * @throws InvalidParameterException entity not known, null userId or guid
     * @throws PropertyServerException problem accessing property server
     * @throws UserNotAuthorizedException security access problem
     */
    public void unlinkPeerPersonProfiles(String userId,
                                         String externalSourceGUID,
                                         String externalSourceName,
                                         String profile1GUID,
                                         String profile1GUIDParameterName,
                                         String profile2GUID,
                                         String profile2GUIDParameterName,
                                         String methodName) throws InvalidParameterException,
                                                                   UserNotAuthorizedException,
                                                                   PropertyServerException
    {
        this.unlinkElementFromElement(userId,
                                      false,
                                      externalSourceGUID,
                                      externalSourceName,
                                      profile1GUID,
                                      profile1GUIDParameterName,
                                      OpenMetadataAPIMapper.PERSON_TYPE_NAME,
                                      profile2GUID,
                                      profile2GUIDParameterName,
                                      OpenMetadataAPIMapper.PERSON_TYPE_GUID,
                                      OpenMetadataAPIMapper.PERSON_TYPE_NAME,
                                      supportedZones,
                                      OpenMetadataAPIMapper.PEER_RELATIONSHIP_TYPE_GUID,
                                      OpenMetadataAPIMapper.PEER_RELATIONSHIP_TYPE_NAME,
                                      methodName);
    }


    /**
     * Link two team profiles as part of a hierarchy.
     *
     * @param userId calling user
     * @param externalSourceGUID     unique identifier of software server capability representing the caller
     * @param externalSourceName     unique name of software server capability representing the caller
     * @param superTeamGUID unique identifier of team profile
     * @param superTeamGUIDParameterName parameter name supplying superTeamGUID
     * @param subTeamGUID  unique identifier of the other team profile
     * @param subTeamGUIDParameterName parameter name supplying subTeamGUID
     * @param delegationEscalationAuthority can workflows delegate/escalate through this link?
     * @param methodName calling method
     *
     * @throws InvalidParameterException entity not known, null userId or guid
     * @throws PropertyServerException problem accessing property server
     * @throws UserNotAuthorizedException security access problem
     */
    public void  linkTeamHierarchy(String  userId,
                                   String  externalSourceGUID,
                                   String  externalSourceName,
                                   String  superTeamGUID,
                                   String  superTeamGUIDParameterName,
                                   String  subTeamGUID,
                                   String  subTeamGUIDParameterName,
                                   boolean delegationEscalationAuthority,
                                   String  methodName) throws InvalidParameterException,
                                                              UserNotAuthorizedException,
                                                              PropertyServerException
    {
        ActorProfileBuilder builder = new ActorProfileBuilder(repositoryHelper, serviceName, serverName);

        this.linkElementToElement(userId,
                                  externalSourceGUID,
                                  externalSourceName,
                                  superTeamGUID,
                                  superTeamGUIDParameterName,
                                  OpenMetadataAPIMapper.TEAM_TYPE_NAME,
                                  subTeamGUID,
                                  subTeamGUIDParameterName,
                                  OpenMetadataAPIMapper.TEAM_TYPE_NAME,
                                  supportedZones,
                                  OpenMetadataAPIMapper.TEAM_STRUCTURE_RELATIONSHIP_TYPE_GUID,
                                  OpenMetadataAPIMapper.TEAM_STRUCTURE_RELATIONSHIP_TYPE_NAME,
                                  builder.getTeamStructureProperties(delegationEscalationAuthority, methodName),
                                  methodName);
    }


    /**
     * Unlink two team profiles from the hierarchy.
     *
     * @param userId calling user
     * @param externalSourceGUID     unique identifier of software server capability representing the caller
     * @param externalSourceName     unique name of software server capability representing the caller
     * @param superTeamGUID unique identifier of team profile
     * @param superTeamGUIDParameterName parameter name supplying superTeamGUID
     * @param subTeamGUID  unique identifier of the other team profile
     * @param subTeamGUIDParameterName parameter name supplying subTeamGUID
     * @param methodName calling method
     *
     * @throws InvalidParameterException entity not known, null userId or guid
     * @throws PropertyServerException problem accessing property server
     * @throws UserNotAuthorizedException security access problem
     */
    public void unlinkTeamHierarchy(String userId,
                                    String externalSourceGUID,
                                    String externalSourceName,
                                    String superTeamGUID,
                                    String superTeamGUIDParameterName,
                                    String subTeamGUID,
                                    String subTeamGUIDParameterName,
                                    String methodName) throws InvalidParameterException,
                                                              UserNotAuthorizedException,
                                                              PropertyServerException
    {
        this.unlinkElementFromElement(userId,
                                      false,
                                      externalSourceGUID,
                                      externalSourceName,
                                      superTeamGUID,
                                      superTeamGUIDParameterName,
                                      OpenMetadataAPIMapper.TEAM_TYPE_NAME,
                                      subTeamGUID,
                                      subTeamGUIDParameterName,
                                      OpenMetadataAPIMapper.TEAM_TYPE_GUID,
                                      OpenMetadataAPIMapper.TEAM_TYPE_NAME,
                                      supportedZones,
                                      OpenMetadataAPIMapper.TEAM_STRUCTURE_RELATIONSHIP_TYPE_GUID,
                                      OpenMetadataAPIMapper.TEAM_STRUCTURE_RELATIONSHIP_TYPE_NAME,
                                      methodName);
    }


    /**
     * Remove the metadata element representing a profile.  This will delete the profile and all categories and terms because
     * the Anchors classifications are set up in these elements.
     *
     * @param userId calling user
     * @param externalSourceGUID     unique identifier of software server capability representing the caller
     * @param externalSourceName     unique name of software server capability representing the caller
     * @param profileGUID unique identifier of the metadata element to remove
     * @param profileGUIDParameterName parameter supplying the profileGUID
     * @param methodName calling method
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public void removeActorProfile(String userId,
                                   String externalSourceGUID,
                                   String externalSourceName,
                                   String profileGUID,
                                   String profileGUIDParameterName,
                                   String methodName) throws InvalidParameterException,
                                                             UserNotAuthorizedException,
                                                             PropertyServerException
    {
        this.deleteBeanInRepository(userId,
                                    externalSourceGUID,
                                    externalSourceName,
                                    profileGUID,
                                    profileGUIDParameterName,
                                    OpenMetadataAPIMapper.ACTOR_PROFILE_TYPE_GUID,
                                    OpenMetadataAPIMapper.ACTOR_PROFILE_TYPE_NAME,
                                    null,
                                    null,
                                    methodName);
    }


    /**
     * Retrieve the list of profile metadata elements that contain the search string.
     * The search string is treated as a regular expression.
     *
     * @param userId calling user
     * @param searchString string to find in the properties
     * @param searchStringParameterName name of parameter supplying the search string
     * @param startFrom paging start point
     * @param pageSize maximum results that can be returned
     * @param methodName calling method
     *
     * @return list of matching metadata elements
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public List<B> findActorProfiles(String userId,
                                     String searchString,
                                     String searchStringParameterName,
                                     int    startFrom,
                                     int    pageSize,
                                     String methodName) throws InvalidParameterException,
                                                               UserNotAuthorizedException,
                                                               PropertyServerException
    {
        return this.findBeans(userId,
                              searchString,
                              searchStringParameterName,
                              OpenMetadataAPIMapper.ACTOR_PROFILE_TYPE_GUID,
                              OpenMetadataAPIMapper.ACTOR_PROFILE_TYPE_NAME,
                              null,
                              startFrom,
                              pageSize,
                              methodName);
    }


    /**
     * Retrieve the list of profile metadata elements with a matching qualified or display name.
     * There are no wildcards supported on this request.
     *
     * @param userId calling user
     * @param name name to search for
     * @param nameParameterName parameter supplying name
     * @param startFrom paging start point
     * @param pageSize maximum results that can be returned
     * @param methodName calling method
     *
     * @return list of matching metadata elements
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public List<B> getActorProfileByName(String userId,
                                         String name,
                                         String nameParameterName,
                                         int    startFrom,
                                         int    pageSize,
                                         String methodName) throws InvalidParameterException,
                                                                   UserNotAuthorizedException,
                                                                   PropertyServerException
    {
        List<String> specificMatchPropertyNames = new ArrayList<>();
        specificMatchPropertyNames.add(OpenMetadataAPIMapper.QUALIFIED_NAME_PROPERTY_NAME);
        specificMatchPropertyNames.add(OpenMetadataAPIMapper.NAME_PROPERTY_NAME);

        return this.getBeansByValue(userId,
                                    name,
                                    nameParameterName,
                                    OpenMetadataAPIMapper.ACTOR_PROFILE_TYPE_GUID,
                                    OpenMetadataAPIMapper.ACTOR_PROFILE_TYPE_NAME,
                                    specificMatchPropertyNames,
                                    true,
                                    null,
                                    null,
                                    false,
                                    supportedZones,
                                    null,
                                    startFrom,
                                    pageSize,
                                    methodName);
    }


    /**
     * Retrieve the profile metadata element with the supplied unique identifier.
     *
     * @param userId calling user
     * @param guid unique identifier of the requested metadata element
     * @param guidParameterName parameter name of guid
     * @param methodName calling method
     *
     * @return matching metadata element
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public B getActorProfileByGUID(String userId,
                                   String guid,
                                   String guidParameterName,
                                   String methodName) throws InvalidParameterException,
                                                             UserNotAuthorizedException,
                                                             PropertyServerException
    {
        return this.getBeanFromRepository(userId,
                                          guid,
                                          guidParameterName,
                                          OpenMetadataAPIMapper.ACTOR_PROFILE_TYPE_NAME,
                                          methodName);

    }


    /**
     * Retrieve the profile metadata element with the supplied unique identifier.
     *
     * @param userId calling user
     * @param profileUserId unique name of the linked user id
     * @param profileUserIdParameterName parameter name of profileUserId
     * @param methodName calling method
     *
     * @return matching metadata element
     *
     * @throws InvalidParameterException  one of the parameters is invalid
     * @throws UserNotAuthorizedException the user is not authorized to issue this request
     * @throws PropertyServerException    there is a problem reported in the open metadata server(s)
     */
    public B getActorProfileForUser(String userId,
                                    String profileUserId,
                                    String profileUserIdParameterName,
                                    String methodName) throws InvalidParameterException,
                                                              UserNotAuthorizedException,
                                                              PropertyServerException
    {
        final String userGUIDParameterName = "userIdentity.getGUID";
        EntityDetail userIdentity = this.getEntityByUniqueQualifiedName(userId,
                                                                        OpenMetadataAPIMapper.ACTOR_PROFILE_TYPE_GUID,
                                                                        OpenMetadataAPIMapper.ACTOR_PROFILE_TYPE_NAME,
                                                                        profileUserId,
                                                                        profileUserIdParameterName,
                                                                        methodName);

        if (userIdentity != null)
        {
            return this.getAttachedElement(userId,
                                           userIdentity.getGUID(),
                                           userGUIDParameterName,
                                           OpenMetadataAPIMapper.USER_IDENTITY_TYPE_NAME,
                                           OpenMetadataAPIMapper.PROFILE_IDENTITY_RELATIONSHIP_TYPE_GUID,
                                           OpenMetadataAPIMapper.PROFILE_IDENTITY_RELATIONSHIP_TYPE_NAME,
                                           OpenMetadataAPIMapper.ACTOR_PROFILE_TYPE_NAME,
                                           methodName);
        }

        return null;
    }
}
