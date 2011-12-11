/*******************************************************************************
 * Copyright 2011 Clockwork
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package nl.clockwork.mule.ebms.util;

import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.Duration;

import nl.clockwork.mule.ebms.model.cpp.cpa.ActionBindingType;
import nl.clockwork.mule.ebms.model.cpp.cpa.CanReceive;
import nl.clockwork.mule.ebms.model.cpp.cpa.CanSend;
import nl.clockwork.mule.ebms.model.cpp.cpa.Certificate;
import nl.clockwork.mule.ebms.model.cpp.cpa.CollaborationProtocolAgreement;
import nl.clockwork.mule.ebms.model.cpp.cpa.CollaborationRole;
import nl.clockwork.mule.ebms.model.cpp.cpa.DeliveryChannel;
import nl.clockwork.mule.ebms.model.cpp.cpa.DocExchange;
import nl.clockwork.mule.ebms.model.cpp.cpa.PartyId;
import nl.clockwork.mule.ebms.model.cpp.cpa.PartyInfo;
import nl.clockwork.mule.ebms.model.cpp.cpa.ServiceBinding;
import nl.clockwork.mule.ebms.model.cpp.cpa.ServiceType;
import nl.clockwork.mule.ebms.model.ebxml.MessageHeader;
import nl.clockwork.mule.ebms.model.ebxml.Service;

//FIXME use JXPath
public class CPAUtils
{
	public static PartyInfo getPartyInfo(CollaborationProtocolAgreement cpa, List<nl.clockwork.mule.ebms.model.ebxml.PartyId> partyIds)
	{
		for (PartyInfo partyInfo : cpa.getPartyInfo())
		{
			boolean result = true;
			for (nl.clockwork.mule.ebms.model.ebxml.PartyId partyId : partyIds)
				for (PartyId cpaPartyId : partyInfo.getPartyId())
					result &= equals(partyId,cpaPartyId);
			if (result)
				return partyInfo;
		}
		return null;
	}
	
	private static boolean equals(nl.clockwork.mule.ebms.model.ebxml.PartyId partyId, PartyId cpaPartyId)
	{
		return partyId.getType().equals(cpaPartyId.getType())
			&& partyId.getValue().equals(cpaPartyId.getValue());
	}
	
	public static ServiceBinding getServiceBinding(PartyInfo partyInfo, String role, Service service)
	{
		for (CollaborationRole collaborationRole : partyInfo.getCollaborationRole())
			if (role.equals(collaborationRole.getRole().getName()) && equals(collaborationRole.getServiceBinding().getService(),service))
				return collaborationRole.getServiceBinding();
		return null;
	}

	private static boolean equals(ServiceType serviceType, Service service)
	{
		return serviceType.getType().equals(service.getType())
			&& serviceType.getValue().equals(service.getValue());
	}

	public static PartyInfo getSendingPartyInfo(CollaborationProtocolAgreement cpa, String actionId)
	{
		for (PartyInfo partyInfo : cpa.getPartyInfo())
			for (CollaborationRole role : partyInfo.getCollaborationRole())
				for (CanSend canSend : role.getServiceBinding().getCanSend())
					if (actionId.equals(canSend.getThisPartyActionBinding().getId()))
					{
						//return partyInfo;
						PartyInfo p = new PartyInfo();
						p.getPartyId().addAll(partyInfo.getPartyId());
						CollaborationRole cr = new CollaborationRole();
						cr.setRole(role.getRole());
						cr.setServiceBinding(new ServiceBinding());
						cr.getServiceBinding().setService(role.getServiceBinding().getService());
						cr.getServiceBinding().getCanSend().add(canSend);
						p.getCollaborationRole().add(cr);
						return p;
					}
		return null;
	}
	
	public static PartyInfo getReceivingPartyInfo(CollaborationProtocolAgreement cpa, String actionId)
	{
		for (PartyInfo partyInfo : cpa.getPartyInfo())
			for (CollaborationRole role : partyInfo.getCollaborationRole())
				for (CanReceive canReceive : role.getServiceBinding().getCanReceive())
					if (actionId.equals(canReceive.getThisPartyActionBinding().getId()))
					{
						//return partyInfo;
						PartyInfo p = new PartyInfo();
						p.getPartyId().addAll(partyInfo.getPartyId());
						CollaborationRole r = new CollaborationRole();
						r.setRole(role.getRole());
						r.setServiceBinding(new ServiceBinding());
						r.getServiceBinding().setService(role.getServiceBinding().getService());
						r.getServiceBinding().getCanReceive().add(canReceive);
						p.getCollaborationRole().add(r);
						return p;
					}
		return null;
	}
	
	public static PartyInfo getOtherReceivingPartyInfo(CollaborationProtocolAgreement cpa, String actionId)
	{
		for (PartyInfo partyInfo : cpa.getPartyInfo())
			for (CollaborationRole role : partyInfo.getCollaborationRole())
				for (CanSend canSend : role.getServiceBinding().getCanSend())
					if (actionId.equals(canSend.getThisPartyActionBinding().getId()))
						return getReceivingPartyInfo(cpa,((ActionBindingType)canSend.getOtherPartyActionBinding()).getId());
		return null;
	}
	
	public static CanSend getCanSend(PartyInfo partyInfo, String role, Service service, String action)
	{
		ServiceBinding serviceBinding = getServiceBinding(partyInfo, role, service);
		if (serviceBinding != null)
			for (CanSend canSend : serviceBinding.getCanSend())
				if (action.equals(canSend.getThisPartyActionBinding().getAction()))
					return canSend;
		return null;
	}

	public static CanReceive getCanReceive(PartyInfo partyInfo, String role, Service service, String action)
	{
		ServiceBinding serviceBinding = getServiceBinding(partyInfo, role, service);
		if (serviceBinding != null)
			for (CanReceive canReceive : serviceBinding.getCanReceive())
				if (action.equals(canReceive.getThisPartyActionBinding().getAction()))
					return canReceive;
		return null;
	}

	public static boolean canSend(PartyInfo partyInfo, String role, Service service, String action)
	{
		return getCanSend(partyInfo,role,service,action) != null;
	}

	public static boolean canReceive(PartyInfo partyInfo, String role, Service service, String action)
	{
		return getCanReceive(partyInfo,role,service,action) != null;
	}

	public static List<DeliveryChannel> getDeliveryChannels(PartyInfo partyInfo, String role, Service service, String action)
	{
		List<DeliveryChannel> result = new ArrayList<DeliveryChannel>();
		ServiceBinding serviceBinding = getServiceBinding(partyInfo, role, service);
		if (serviceBinding != null)
			for (CanSend canSend : serviceBinding.getCanSend())
				if (action.equals(canSend.getThisPartyActionBinding().getAction()))
					result.add((DeliveryChannel)canSend.getThisPartyActionBinding().getChannelId().get(0).getValue());
		return result;
	}

/*
	public static PartyInfo getOtherPartyInfo(CollaborationProtocolAgreement cpa, String actionId)
	{
		for (PartyInfo partyInfo : cpa.getPartyInfo())
			for (CollaborationRole role : partyInfo.getCollaborationRole())
				for (CanSend canSend : role.getServiceBinding().getCanSend())
					if (actionId.equals(canSend.getThisPartyActionBinding().getId()))
						return getPartyInfoReceive(cpa,((ActionBindingType)canSend.getOtherPartyActionBinding()).getId());
		return null;
	}
	
	public static PartyInfo getPartyInfo(CollaborationProtocolAgreement cpa, String partyIdType, String partyId)
	{
		for (PartyInfo partyInfo : cpa.getPartyInfo())
			for (PartyId pId : partyInfo.getPartyId())
				if (pId.getType().equals(partyIdType) && pId.getValue().equals(partyId))
					return partyInfo;
		return null;
	}
	
	public static List<CollaborationRole> getCollaborationRoles(CollaborationProtocolAgreement cpa, String partyIdType, String partyId, String roleName)
	{
		List<CollaborationRole> result = new ArrayList<CollaborationRole>();
		for (PartyInfo partyInfo : cpa.getPartyInfo())
			for (PartyId pId : partyInfo.getPartyId())
				if (pId.getType().equals(partyIdType) && pId.getValue().equals(partyId))
				{
					for (CollaborationRole role : partyInfo.getCollaborationRole())
						if ((roleName == null  && role.getRole() == null) || roleName.equals(role.getRole().getName()))
							result.add(role);
					break;
				}
		return result;
	}
	
	public static CanSend getCanSend(List<CollaborationRole> roles, String serviceType, String service, String action)
	{
		for (CollaborationRole role : roles)
			if ((serviceType == null && role.getServiceBinding().getService().getType() == null) || serviceType.equals(role.getServiceBinding().getService().getType()))
				for (CanSend canSend : role.getServiceBinding().getCanSend())
					if (canSend.getThisPartyActionBinding().getAction().equals(action))
						return canSend;
		return null;
	}
	
	public static CanReceive getCanReceive(List<CollaborationRole> roles, String serviceType, String service, String action)
	{
		for (CollaborationRole role : roles)
			if ((serviceType == null && role.getServiceBinding().getService().getType() == null) || serviceType.equals(role.getServiceBinding().getService().getType()))
				for (CanReceive canReceive : role.getServiceBinding().getCanReceive())
					if (canReceive.getThisPartyActionBinding().getAction().equals(action))
						return canReceive;
		return null;
	}
	
	public static DeliveryChannel getDeliveryChannel(ActionBindingType actionBinding)
	{
		return actionBinding.getChannelId().size() == 0 ? null : (DeliveryChannel)actionBinding.getChannelId().get(0).getValue();
	}

//	public static ServiceBinding getServiceBinding(PartyInfo partyInfo, /*String roleName, String serviceType, * /String service)
//	{
//		for (CollaborationRole collaborationRole : partyInfo.getCollaborationRole())
//			//if (collaborationRole.getRole().getName().equals(roleName))
//				if (/*collaborationRole.getServiceBinding().getService().getType().equals(serviceType) && * /collaborationRole.getServiceBinding().getService().getValue().equals(service))
//				return collaborationRole.getServiceBinding();
//		return null;
//	}
//	
//	public static ReliableMessaging getReliableMessaging(ServiceBinding serviceBinding, String action)
//	{
//		for (CanSend canSend : serviceBinding.getCanSend())
//			if (canSend.getThisPartyActionBinding().getAction().equals(action))
//				for (JAXBElement<Object> deliveryChannel : canSend.getThisPartyActionBinding().getChannelId())
//					return ((DocExchange)((DeliveryChannel)deliveryChannel.getValue()).getDocExchangeId()).getEbXMLSenderBinding().getReliableMessaging();
//		return null;
//	}
	
	public static EbXMLSenderBinding getEbXMLSenderBinding(PartyInfo partyInfo, String service, String action)
	{
		for (CollaborationRole collaborationRole : partyInfo.getCollaborationRole())
			if (collaborationRole.getServiceBinding().getService().getValue().equals(service))
				for (CanSend canSend : collaborationRole.getServiceBinding().getCanSend())
					if (canSend.getThisPartyActionBinding().getAction().equals(action))
						for (JAXBElement<Object> deliveryChannel : canSend.getThisPartyActionBinding().getChannelId())
							return ((DocExchange)((DeliveryChannel)deliveryChannel.getValue()).getDocExchangeId()).getEbXMLSenderBinding();
		return null;
	}
	
	public static ReliableMessaging getReliableMessaging(PartyInfo partyInfo, String service, String action)
	{
		return getEbXMLSenderBinding(partyInfo,service,action).getReliableMessaging();
	}
	
	public static ReliableMessaging getReliableMessaging(CollaborationProtocolAgreement cpa, MessageHeader messageHeader)
	{
		PartyInfo partyInfo = getPartyInfo(cpa,messageHeader.getFrom().getPartyId().get(0).getType(),messageHeader.getFrom().getPartyId().get(0).getValue());
		return getReliableMessaging(partyInfo,messageHeader.getService().getValue(),messageHeader.getAction());
	}

	public static SenderNonRepudiation getSenderNonRepudiation(PartyInfo partyInfo, String service, String action)
	{
		return getEbXMLSenderBinding(partyInfo,service,action).getSenderNonRepudiation();
	}
	
	public static SenderNonRepudiation getSenderNonRepudiation(CollaborationProtocolAgreement cpa, MessageHeader messageHeader)
	{
		PartyInfo partyInfo = getPartyInfo(cpa,messageHeader.getFrom().getPartyId().get(0).getType(),messageHeader.getFrom().getPartyId().get(0).getValue());
		return getSenderNonRepudiation(partyInfo,messageHeader.getService().getValue(),messageHeader.getAction());
	}

	public static int getNrRetries(CollaborationProtocolAgreement cpa, MessageHeader messageHeader)
	{
		try
		{
			PartyInfo partyInfo = getPartyInfo(cpa,messageHeader.getFrom().getPartyId().get(0).getType(),messageHeader.getFrom().getPartyId().get(0).getValue());
			return getReliableMessaging(partyInfo,messageHeader.getService().getValue(),messageHeader.getAction()).getRetries().intValue();
		}
		catch (Exception e)
		{
			return 0;
		}
	}
*/
	public static Duration getDuration(CollaborationProtocolAgreement cpa, MessageHeader messageHeader)
	{
		try
		{
			PartyInfo partyInfo = getPartyInfo(cpa,messageHeader.getFrom().getPartyId());
			List<DeliveryChannel> deliveryChannels = getDeliveryChannels(partyInfo,messageHeader.getFrom().getRole(),messageHeader.getService(),messageHeader.getAction());
			return ((DocExchange)deliveryChannels.get(0).getDocExchangeId()).getEbXMLSenderBinding().getReliableMessaging().getRetryInterval();
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public static Certificate getCertificateById(CollaborationProtocolAgreement cpa, MessageHeader messageHeader, String id)
	{
		List<Certificate> certificates = getCertificates(cpa, messageHeader);
		for (Certificate certificate : certificates)
			if (id.equals(certificate.getCertId()))
				return certificate;
		return null;
	}

	public static List<Certificate> getCertificates(CollaborationProtocolAgreement cpa, MessageHeader messageHeader)
	{
		try
		{
			PartyInfo partyInfo = getPartyInfo(cpa,messageHeader.getFrom().getPartyId());
			return partyInfo.getCertificate();
		}
		catch (Exception e)
		{
			return new ArrayList<Certificate>();
		}
	}

}
