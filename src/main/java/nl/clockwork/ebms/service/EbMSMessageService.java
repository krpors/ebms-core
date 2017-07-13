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
package nl.clockwork.ebms.service;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;

import nl.clockwork.ebms.Constants.EbMSMessageEventType;
import nl.clockwork.ebms.model.EbMSMessageContent;
import nl.clockwork.ebms.model.EbMSMessageContext;
import nl.clockwork.ebms.model.EbMSMessageEvent;
import nl.clockwork.ebms.model.MessageStatus;
import nl.clockwork.ebms.model.Party;

@WebService(targetNamespace="http://www.ordina.nl/ebms/2.20")
public interface EbMSMessageService
{
	@WebMethod(operationName="Ping")
	void ping(@WebParam(name="CPAId") @XmlElement(required=true) String cpaId, @WebParam(name="FromParty") @XmlElement(required=true) Party fromParty, @WebParam(name="ToParty") @XmlElement(required=true) Party toParty) throws EbMSMessageServiceException;
	
	@WebResult(name="EbMSMessageId")
	@WebMethod(operationName="SendMessage")
	long sendMessage(@WebParam(name="Message") @XmlElement(required=true) EbMSMessageContent messageContent) throws EbMSMessageServiceException;

	@WebResult(name="EbMSMessageId")
	@WebMethod(operationName="ResendMessage")
	long resendMessage(@WebParam(name="ebMSMessageId") @XmlElement(required=true) long ebMSMessageId) throws EbMSMessageServiceException;

	@WebResult(name="EbMSMessageIds")
	@WebMethod(operationName="GetEbMSMessageIds")
	List<Long> getEbMSMessageIds(@WebParam(name="MessageContext") @XmlElement(required=true) EbMSMessageContext messageContext, @WebParam(name="MaxNr") Integer maxNr) throws EbMSMessageServiceException;

	@WebResult(name="Message")
	@WebMethod(operationName="GetMessage")
	EbMSMessageContent getMessage(@WebParam(name="EbMSMessageId") @XmlElement(required=true) long ebMSMessageId, @WebParam(name="Process") Boolean process) throws EbMSMessageServiceException;

	@WebMethod(operationName="ProcessMessage")
	void processMessage(@WebParam(name="EbMSMessageId") @XmlElement(required=true) long ebMSMessageId) throws EbMSMessageServiceException;

	@WebMethod(operationName="ProcessMessages")
	void processMessages(@WebParam(name="EbMSMessageId") @XmlElement(required=true) List<Long> ebMSMessageIds) throws EbMSMessageServiceException;

	@WebResult(name="MessageStatus")
	@WebMethod(operationName="GetMessageStatus")
	MessageStatus getMessageStatus(@WebParam(name="EbMSMessageId") @XmlElement(required=true) long ebMSMessageId) throws EbMSMessageServiceException;

	@WebResult(name="MessageEvents")
	@WebMethod(operationName="GetMessageEvents")
	List<EbMSMessageEvent> getMessageEvents(@WebParam(name="MessageContext") @XmlElement(required=true) EbMSMessageContext messageContext, @WebParam(name="EventType") @XmlElement(required=true) EbMSMessageEventType[] eventTypes, @WebParam(name="MaxNr") Integer maxNr) throws EbMSMessageServiceException;

	@WebMethod(operationName="ProcessMessageEvent")
	void processMessageEvent(@WebParam(name="EbMSMessageId") @XmlElement(required=true) long ebMSMessageId) throws EbMSMessageServiceException;

	@WebMethod(operationName="ProcessMessageEvents")
	void processMessageEvents(@WebParam(name="EbMSMessageId") @XmlElement(required=true) List<Long> ebMSMessageIds) throws EbMSMessageServiceException;
}
