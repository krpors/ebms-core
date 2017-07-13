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
package nl.clockwork.ebms.dao;

import java.util.Date;
import java.util.List;

import nl.clockwork.ebms.Constants.EbMSEventStatus;
import nl.clockwork.ebms.Constants.EbMSMessageEventType;
import nl.clockwork.ebms.Constants.EbMSMessageStatus;
import nl.clockwork.ebms.model.EbMSDocument;
import nl.clockwork.ebms.model.EbMSEvent;
import nl.clockwork.ebms.model.EbMSMessage;
import nl.clockwork.ebms.model.EbMSMessageContent;
import nl.clockwork.ebms.model.EbMSMessageContext;
import nl.clockwork.ebms.model.EbMSMessageEvent;
import nl.clockwork.ebms.model.URLMapping;

import org.oasis_open.committees.ebxml_cppa.schema.cpp_cpa_2_0.CollaborationProtocolAgreement;
import org.oasis_open.committees.ebxml_msg.schema.msg_header_2_0.Service;
import org.w3c.dom.Document;

public interface EbMSDAO
{
	void executeTransaction(DAOTransactionCallback callback) throws DAOException;

	boolean existsCPA(String cpaId) throws DAOException;
	CollaborationProtocolAgreement getCPA(String cpaId) throws DAOException;
	List<String> getCPAIds() throws DAOException;
	void insertCPA(CollaborationProtocolAgreement cpa) throws DAOException;
	int updateCPA(CollaborationProtocolAgreement cpa) throws DAOException;
	int deleteCPA(String cpaId) throws DAOException;
	
	boolean existsURLMapping(String source) throws DAOException;
	String getURLMapping(String source) throws DAOException;
	List<URLMapping> getURLMappings() throws DAOException;
	void insertURLMapping(URLMapping urlMapping) throws DAOException;
	int updateURLMapping(URLMapping urlMapping) throws DAOException;
	int deleteURLMapping(String source) throws DAOException;

	boolean existsMessage(String messageId) throws DAOException;
	boolean existsIdenticalMessage(EbMSMessage message) throws DAOException;
	EbMSMessageContent getMessageContent(long ebMSMessageId) throws DAOException;
	EbMSMessageContext getMessageContext(long ebMSMessageId) throws DAOException;
	EbMSMessageContext getMessageContextByRefToMessageId(String cpaId, String refToMessageId, Service service, String...actions) throws DAOException;
	Long getEbMSMessageId(String messageId, EbMSMessageStatus...ebMSMessageStatus) throws DAOException;
	Long getEbMSMessageIdByRefToMessageId(String cpaId, String refToMessageId, Service service, String...actions) throws DAOException;
	Document getDocument(long ebMSMessageId) throws DAOException;
	EbMSDocument getEbMSDocument(long ebMSMessageId) throws DAOException;
	EbMSDocument getEbMSDocumentIfUnsent(long ebMSMessageId) throws DAOException;
	EbMSMessageStatus getMessageStatus(String messageId) throws DAOException;

	List<Long> getMessageIds(EbMSMessageContext messageContext, EbMSMessageStatus status) throws DAOException;
	List<Long> getEbMSMessageIds(EbMSMessageContext messageContext, EbMSMessageStatus status, int maxNr) throws DAOException;

	long insertMessage(Date timestamp, Date persistTime, EbMSMessage message, EbMSMessageStatus status) throws DAOException;
	long insertDuplicateMessage(Date timestamp, EbMSMessage message) throws DAOException;
	int updateMessage(long id, EbMSMessageStatus oldStatus, EbMSMessageStatus newStatus) throws DAOException;
	void updateMessages(List<Long> ids, EbMSMessageStatus oldStatus, EbMSMessageStatus newStatus) throws DAOException;

	void deleteAttachments(long ebMSMessageId);
	void deleteAttachments(List<Long> ebMSMessageIds);

	List<EbMSEvent> getEventsBefore(Date timestamp) throws DAOException;
	void insertEvent(EbMSEvent event) throws DAOException;
	void updateEvent(EbMSEvent event) throws DAOException;
	void deleteEvent(long ebMSMessageId) throws DAOException;
	void insertEventLog(long ebMSMessageId, Date timestamp, String uri, EbMSEventStatus status, String errorMessage) throws DAOException;

	List<EbMSMessageEvent> getEbMSMessageEvents(EbMSMessageContext messageContext, EbMSMessageEventType[] types) throws DAOException;
	List<EbMSMessageEvent> getEbMSMessageEvents(EbMSMessageContext messageContext, EbMSMessageEventType[] types, int maxNr) throws DAOException;
	void insertEbMSMessageEvent(long ebMSMessageId, EbMSMessageEventType eventType) throws DAOException;
	int processEbMSMessageEvent(long ebMSMessageId) throws DAOException;
	void processEbMSMessageEvents(List<Long> ebMSMessageIds) throws DAOException;
	
	Date getPersistTime(String messageId);

}
