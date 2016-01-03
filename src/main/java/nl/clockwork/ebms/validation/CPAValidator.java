/**
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
 */
package nl.clockwork.ebms.validation;

import java.util.Date;

import nl.clockwork.ebms.Constants;
import nl.clockwork.ebms.model.EbMSMessage;
import nl.clockwork.ebms.util.CPAUtils;
import nl.clockwork.ebms.util.EbMSMessageUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.committees.ebxml_cppa.schema.cpp_cpa_2_0.ActorType;
import org.oasis_open.committees.ebxml_cppa.schema.cpp_cpa_2_0.CanReceive;
import org.oasis_open.committees.ebxml_cppa.schema.cpp_cpa_2_0.CanSend;
import org.oasis_open.committees.ebxml_cppa.schema.cpp_cpa_2_0.CollaborationProtocolAgreement;
import org.oasis_open.committees.ebxml_cppa.schema.cpp_cpa_2_0.CollaborationRole;
import org.oasis_open.committees.ebxml_cppa.schema.cpp_cpa_2_0.DeliveryChannel;
import org.oasis_open.committees.ebxml_cppa.schema.cpp_cpa_2_0.DocExchange;
import org.oasis_open.committees.ebxml_cppa.schema.cpp_cpa_2_0.MessageOrderSemanticsType;
import org.oasis_open.committees.ebxml_cppa.schema.cpp_cpa_2_0.PartyInfo;
import org.oasis_open.committees.ebxml_cppa.schema.cpp_cpa_2_0.PerMessageCharacteristicsType;
import org.oasis_open.committees.ebxml_cppa.schema.cpp_cpa_2_0.PersistenceLevelType;
import org.oasis_open.committees.ebxml_cppa.schema.cpp_cpa_2_0.SyncReplyModeType;
import org.oasis_open.committees.ebxml_cppa.schema.cpp_cpa_2_0.Transport;

public class CPAValidator
{
  protected transient Log logger = LogFactory.getLog(getClass());

	public void cpaExists(CollaborationProtocolAgreement cpa, EbMSMessage message) throws EbMSValidationException
	{
		if (cpa == null || !cpa.getCpaid().equals(message.getMessageHeader().getCPAId()))
			throw new EbMSValidationException(EbMSMessageUtils.createError("//Header/MessageHeader[@cpaid]",Constants.EbMSErrorCode.VALUE_NOT_RECOGNIZED.errorCode(),"CPA not found."));
	}
	
	public void validate(CollaborationProtocolAgreement cpa, EbMSMessage message) throws EbMSValidationException
	{
		cpaExists(cpa,message);
		if (!CPAUtils.isValid(cpa,message))
			throw new EbMSValidationException(EbMSMessageUtils.createError("//Header/MessageHeader[@cpaid]",Constants.EbMSErrorCode.INCONSISTENT.errorCode(),"Invalid CPA."));
	}

	public void validate(CollaborationProtocolAgreement cpa) throws ValidatorException
	{
		if (!"2_0b".equals(cpa.getVersion()))
			logger.warn("CPA version " + cpa.getVersion() + " detected! CPA version 2_0b expected.");
		if ("proposed".equals(cpa.getStatus()))
			throw new ValidationException("CPA Status is proposed!");
		if (!cpa.getStart().before(cpa.getEnd()))
			throw new ValidationException("CPA Start date not before End date!");
		if (new Date().before(cpa.getEnd()))
			throw new ValidationException("CPA expired on " + cpa.getEnd());
		if (cpa.getConversationConstraints() != null)
			logger.warn("CPA Conversation Constraints not implemented!");
		if (cpa.getSignature() != null)
			logger.warn("CPA Signature not implemented!");
		if (cpa.getPackaging() != null && cpa.getPackaging().size() > 0)
			logger.warn("Packaging not implemented!");
		validateActions(cpa);
		validateChannels(cpa);
		validateTransports(cpa);
	}

	private void validateActions(CollaborationProtocolAgreement cpa)
	{
		for (PartyInfo partyInfo : cpa.getPartyInfo())
			for (CollaborationRole role : partyInfo.getCollaborationRole())
			{
				for (CanSend canSend : role.getServiceBinding().getCanSend())
				{
					if (canSend.getCanReceive() != null && canSend.getCanReceive().size() > 0)
						logger.warn("Nesting of actions under CanSend in Service " + CPAUtils.toString(role.getServiceBinding().getService()) +  " not supported!");
					if (canSend.getThisPartyActionBinding().getChannelId().size() > 0)
						logger.warn("Multiple channels per action as defined in Action " + canSend.getThisPartyActionBinding().getAction() + " of Service " + CPAUtils.toString(role.getServiceBinding().getService()) + " not supported! Using first channel.");
					if (PersistenceLevelType.PERSISTENT.equals(canSend.getThisPartyActionBinding().getBusinessTransactionCharacteristics().getIsConfidential()) || PersistenceLevelType.TRANSIENT_AND_PERSISTENT.equals(canSend.getThisPartyActionBinding().getBusinessTransactionCharacteristics().getIsConfidential()))
						logger.warn("Encryption defined in Action " + canSend.getThisPartyActionBinding().getAction() + " of Service " + CPAUtils.toString(role.getServiceBinding().getService()) + " not implemented!");
					if (canSend.getThisPartyActionBinding().getBusinessTransactionCharacteristics().isIsNonRepudiationReceiptRequired() || canSend.getThisPartyActionBinding().getBusinessTransactionCharacteristics().isIsIntelligibleCheckRequired() || canSend.getThisPartyActionBinding().getBusinessTransactionCharacteristics().getTimeToAcknowledgeReceipt() != null || canSend.getThisPartyActionBinding().getBusinessTransactionCharacteristics().getTimeToAcknowledgeAcceptance() != null || canSend.getThisPartyActionBinding().getBusinessTransactionCharacteristics().getRetryCount() != null)
						logger.warn("Business signals defined in Action " + canSend.getThisPartyActionBinding().getAction() + " of Service " + CPAUtils.toString(role.getServiceBinding().getService()) + " not supported!");
					//if (canSend.getThisPartyActionBinding().getBusinessTransactionCharacteristics().isIsAuthorizationRequired())
						//logger.warn("Authorization Required defined in Action " + canSend.getThisPartyActionBinding().getAction() + " of Service " + CPAUtils.toString(role.getServiceBinding().getService()) + " ignored!");
				}
				for (CanReceive canReceive : role.getServiceBinding().getCanReceive())
				{
					if (canReceive.getCanSend() != null && canReceive.getCanSend().size() > 0)
						logger.warn("Nesting of actions under CanReceive in Service " + CPAUtils.toString(role.getServiceBinding().getService()) +  " not supported!");
					if (canReceive.getThisPartyActionBinding().getChannelId().size() > 0)
						logger.warn("Multiple channels per action as defined in Action " + canReceive.getThisPartyActionBinding().getAction() + " of Service " + CPAUtils.toString(role.getServiceBinding().getService()) + " not supported! Using first channel.");
					if (PersistenceLevelType.PERSISTENT.equals(canReceive.getThisPartyActionBinding().getBusinessTransactionCharacteristics().getIsConfidential()) || PersistenceLevelType.TRANSIENT_AND_PERSISTENT.equals(canReceive.getThisPartyActionBinding().getBusinessTransactionCharacteristics().getIsConfidential()))
						logger.warn("Encryption defined in Action " + canReceive.getThisPartyActionBinding().getAction() + " of Service " + CPAUtils.toString(role.getServiceBinding().getService()) + " not implemented!");
					if (canReceive.getThisPartyActionBinding().getBusinessTransactionCharacteristics().isIsNonRepudiationReceiptRequired() || canReceive.getThisPartyActionBinding().getBusinessTransactionCharacteristics().isIsIntelligibleCheckRequired() || canReceive.getThisPartyActionBinding().getBusinessTransactionCharacteristics().getTimeToAcknowledgeReceipt() != null || canReceive.getThisPartyActionBinding().getBusinessTransactionCharacteristics().getTimeToAcknowledgeAcceptance() != null || canReceive.getThisPartyActionBinding().getBusinessTransactionCharacteristics().getRetryCount() != null)
						logger.warn("Business signals defined in Action " + canReceive.getThisPartyActionBinding().getAction() + " of Service " + CPAUtils.toString(role.getServiceBinding().getService()) + " not supported!");
					//if (canReceive.getThisPartyActionBinding().getBusinessTransactionCharacteristics().isIsAuthorizationRequired())
						//logger.warn("Authorization Required defined in Action " + canReceive.getThisPartyActionBinding().getAction() + " of Service " + CPAUtils.toString(role.getServiceBinding().getService()) + " ignored!");
				}
			}
	}

	private void validateChannels(CollaborationProtocolAgreement cpa)
	{
		for (PartyInfo partyInfo : cpa.getPartyInfo())
			for (DeliveryChannel deliveryChannel : partyInfo.getDeliveryChannel())
			{
				if (((DocExchange)deliveryChannel.getDocExchangeId()).getEbXMLSenderBinding().getReliableMessaging() != null && MessageOrderSemanticsType.GUARANTEED.equals(((DocExchange)deliveryChannel.getDocExchangeId()).getEbXMLSenderBinding().getReliableMessaging().getMessageOrderSemantics()))
					logger.warn("Message Order as defined in DocExchange " + ((DocExchange)deliveryChannel.getDocExchangeId()).getDocExchangeId() + " not implemented!");
				if (SyncReplyModeType.SIGNALS_ONLY.equals(deliveryChannel.getMessagingCharacteristics().getSyncReplyMode()) || SyncReplyModeType.SIGNALS_AND_RESPONSE.equals(deliveryChannel.getMessagingCharacteristics().getSyncReplyMode()))
					logger.warn("Business signals defined in Channel " + deliveryChannel.getChannelId() + " not supported!");
				if (PerMessageCharacteristicsType.NEVER.equals(deliveryChannel.getMessagingCharacteristics().getDuplicateElimination()))
					logger.warn("Duplicate Elimination always enabled!");
				if (ActorType.URN_OASIS_NAMES_TC_EBXML_MSG_ACTOR_NEXT_MSH.equals(deliveryChannel.getMessagingCharacteristics().getActor()))
					logger.warn("Actor NextMSH not supported!");
			}
	}

	private void validateTransports(CollaborationProtocolAgreement cpa)
	{
		for (PartyInfo partyInfo : cpa.getPartyInfo())
			for (Transport transport : partyInfo.getTransport())
			{
				if (!"HTTP".equals(transport.getTransportSender().getTransportProtocol().getValue()))
					logger.warn("Transport protocol " + transport.getTransportSender().getTransportProtocol().getValue() + " defined in TransportSender of Transport " + transport.getTransportId() + " not implemented!");
				if (!"HTTP".equals(transport.getTransportReceiver().getTransportProtocol().getValue()))
					logger.warn("Transport protocol " + transport.getTransportReceiver().getTransportProtocol().getValue() + " defined in TransportReceiver of Transport " + transport.getTransportId() + " not implemented!");
				if (transport.getTransportReceiver().getEndpoint().size() > 1)
					logger.warn("Multiple endpoints defined in TransportReceiver of Transport " + transport.getTransportId() + "not supported! Only allPurpose endpoint supported. Using first endpoint.");
					return;
			}
	}
	
}
