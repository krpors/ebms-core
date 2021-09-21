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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.w3c.dom.Document;

import io.vavr.Tuple2;
import lombok.val;
import nl.clockwork.ebms.EbMSAction;
import nl.clockwork.ebms.EbMSMessageStatus;
import nl.clockwork.ebms.model.EbMSAttachment;
import nl.clockwork.ebms.model.EbMSBaseMessage;
import nl.clockwork.ebms.model.EbMSDocument;
import nl.clockwork.ebms.model.EbMSMessageProperties;
import nl.clockwork.ebms.service.model.MTOMMessage;
import nl.clockwork.ebms.service.model.Message;
import nl.clockwork.ebms.service.model.MessageFilter;

public interface EbMSDAO
{
	void executeTransaction(Runnable runnable);

	boolean existsMessage(String messageId);
	boolean existsIdenticalMessage(EbMSBaseMessage message);

	Optional<Message> getMessage(String messageId);
	Optional<MTOMMessage> getMTOMMessage(String messageId);
	Optional<EbMSMessageProperties> getEbMSMessageProperties(String messageId);
	Optional<EbMSMessageProperties> getEbMSMessagePropertiesByRefToMessageId(String cpaId, String refToMessageId, EbMSAction...actions);
	Optional<Document> getDocument(String messageId);
	Optional<EbMSDocument> getEbMSDocumentIfUnsent(String messageId);
	Optional<EbMSDocument> getEbMSDocumentByRefToMessageId(String cpaId, String refToMessageId, EbMSAction...actions);
	Optional<Instant> getPersistTime(String messageId);
	Optional<EbMSAction> getMessageAction(String messageId);

	List<String> getMessageIds(MessageFilter messageFilter, EbMSMessageStatus status);
	List<String> getMessageIds(MessageFilter messageFilter, EbMSMessageStatus status, int maxNr);

	String insertMessage(Instant timestamp, Instant persistTime, Document document, EbMSBaseMessage message, List<EbMSAttachment> attachments, EbMSMessageStatus status);
	Tuple2<String,Integer> insertDuplicateMessage(Instant timestamp, Document document, EbMSBaseMessage message, List<EbMSAttachment> attachments);

	int updateMessage(String messageId, EbMSMessageStatus oldStatus, EbMSMessageStatus newStatus);

	int deleteAttachments(String messageId);

	public static String getMessageFilter(MessageFilter messageFilter, List<Object> parameters)
	{
		val result = new StringBuilder();
		if (messageFilter != null)
		{
			if (messageFilter.getCpaId() != null)
				add(result, "ebms_message.cpa_id", parameters, messageFilter.getCpaId());
			if (messageFilter.getFromParty() != null)
			{
				add(result, "ebms_message.from_party_id", parameters, messageFilter.getFromParty().getPartyId());
				if (messageFilter.getFromParty().getRole() != null)
					add(result, "ebms_message.from_role", parameters, messageFilter.getFromParty().getRole());
			}
			if (messageFilter.getToParty() != null)
			{
				add(result, "ebms_message.to_party_id", parameters, messageFilter.getToParty().getPartyId());
				if (messageFilter.getToParty().getRole() != null)
					add(result, "ebms_message.to_role", parameters, messageFilter.getToParty().getRole());
			}
			if (messageFilter.getService() != null)
				add(result, "ebms_message.service", parameters, messageFilter.getService());
			if (messageFilter.getAction() != null)
				add(result, "ebms_message.action", parameters, messageFilter.getAction());
			if (messageFilter.getConversationId() != null)
				add(result, "ebms_message.conversation_id", parameters, messageFilter.getConversationId());
			if (messageFilter.getMessageId() != null)
				add(result, "ebms_message.message_id", parameters, messageFilter.getMessageId());
			if (messageFilter.getRefToMessageId() != null)
				add(result, "ebms_message.ref_to_message_id", parameters, messageFilter.getRefToMessageId());
		}
		return result.toString();
	}

	static void add(final StringBuilder result, String fieldName, List<Object> parameters, String value)
	{
		result.append(" and " + fieldName + " = ?");
		parameters.add(value);
	}
}
