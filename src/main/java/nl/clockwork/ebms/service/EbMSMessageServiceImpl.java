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

import java.util.Date;
import java.util.List;

import javax.xml.transform.TransformerFactoryConfigurationError;

import nl.clockwork.ebms.Constants;
import nl.clockwork.ebms.Constants.EbMSAction;
import nl.clockwork.ebms.Constants.EbMSMessageEventType;
import nl.clockwork.ebms.Constants.EbMSMessageStatus;
import nl.clockwork.ebms.client.DeliveryManager;
import nl.clockwork.ebms.common.CPAManager;
import nl.clockwork.ebms.common.EbMSMessageFactory;
import nl.clockwork.ebms.dao.DAOException;
import nl.clockwork.ebms.dao.DAOTransactionCallback;
import nl.clockwork.ebms.dao.EbMSDAO;
import nl.clockwork.ebms.job.EventManager;
import nl.clockwork.ebms.model.CacheablePartyId;
import nl.clockwork.ebms.model.EbMSMessage;
import nl.clockwork.ebms.model.EbMSMessageContent;
import nl.clockwork.ebms.model.EbMSMessageContext;
import nl.clockwork.ebms.model.EbMSMessageEvent;
import nl.clockwork.ebms.model.IdHolder;
import nl.clockwork.ebms.model.MessageStatus;
import nl.clockwork.ebms.model.Party;
import nl.clockwork.ebms.processor.EbMSProcessorException;
import nl.clockwork.ebms.signing.EbMSSignatureGenerator;
import nl.clockwork.ebms.util.CPAUtils;
import nl.clockwork.ebms.validation.EbMSMessageContextValidator;
import nl.clockwork.ebms.validation.ValidationException;
import nl.clockwork.ebms.validation.ValidatorException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.committees.ebxml_cppa.schema.cpp_cpa_2_0.DeliveryChannel;
import org.springframework.beans.factory.InitializingBean;

public class EbMSMessageServiceImpl implements InitializingBean, EbMSMessageService
{
  protected transient Log logger = LogFactory.getLog(getClass());
	private DeliveryManager deliveryManager;
	private EbMSDAO ebMSDAO;
	private CPAManager cpaManager;
	private EbMSMessageFactory ebMSMessageFactory;
	private EventManager eventManager;
	private EbMSMessageContextValidator ebMSMessageContextValidator;
	private EbMSSignatureGenerator signatureGenerator;
	protected boolean deleteEbMSAttachmentsOnMessageProcessed;

	@Override
	public void afterPropertiesSet() throws Exception
	{
		ebMSMessageContextValidator = new EbMSMessageContextValidator(cpaManager);
	}
  
	@Override
	public void ping(String cpaId, Party fromParty, Party toParty) throws EbMSMessageServiceException
	{
		try
		{
			ebMSMessageContextValidator.validate(cpaId,fromParty,toParty);
			EbMSMessage request = ebMSMessageFactory.createEbMSPing(cpaId,fromParty,toParty);
			EbMSMessage response = deliveryManager.sendMessage(cpaManager.getUri(cpaId,new CacheablePartyId(request.getMessageHeader().getTo().getPartyId()),request.getMessageHeader().getTo().getRole(),CPAUtils.toString(request.getMessageHeader().getService()),request.getMessageHeader().getAction()),request);
			if (response != null)
			{
				if (!EbMSAction.PONG.action().equals(response.getMessageHeader().getAction()))
					throw new EbMSMessageServiceException("No valid response received!");
			}
			else
				throw new EbMSMessageServiceException("No response received!");
		}
		catch (ValidationException | EbMSProcessorException e)
		{
			throw new EbMSMessageServiceException(e);
		}
	}
	
	@Override
	public long sendMessage(EbMSMessageContent messageContent) throws EbMSMessageServiceException
	{
		try
		{
			ebMSMessageContextValidator.validate(messageContent.getContext());
			return sendMessage_(messageContent);
		}
		catch (ValidatorException | DAOException | TransformerFactoryConfigurationError | EbMSProcessorException e)
		{
			throw new EbMSMessageServiceException(e);
		}
	}

	@Override
	public long resendMessage(long ebMSMessageId) throws EbMSMessageServiceException
	{
		try
		{
			EbMSMessageContent messageContent = ebMSDAO.getMessageContent(ebMSMessageId);
			if (messageContent != null)
			{
				resetMessage(messageContent.getContext());
				return sendMessage_(messageContent);
			}
			else
				throw new EbMSMessageServiceException("Message not found!");
		}
		catch (DAOException | EbMSProcessorException e)
		{
			throw new EbMSMessageServiceException(e);
		}
	}

	@Override
	public List<Long> getEbMSMessageIds(EbMSMessageContext messageContext, Integer maxNr) throws EbMSMessageServiceException
	{
		try
		{
			if (maxNr == null || maxNr == 0)
				return ebMSDAO.getMessageIds(messageContext,EbMSMessageStatus.RECEIVED);
			else
				return ebMSDAO.getEbMSMessageIds(messageContext,EbMSMessageStatus.RECEIVED,maxNr);
		}
		catch (DAOException e)
		{
			throw new EbMSMessageServiceException(e);
		}
	}

	@Override
	public EbMSMessageContent getMessage(final long ebMSMessageId, Boolean process) throws EbMSMessageServiceException
	{
		try
		{
			if (process != null && process)
				ebMSDAO.executeTransaction(new DAOTransactionCallback()
				{
					@Override
					public void doInTransaction() throws DAOException
					{
						ebMSDAO.updateMessage(ebMSMessageId,EbMSMessageStatus.RECEIVED,EbMSMessageStatus.PROCESSED);
						if (deleteEbMSAttachmentsOnMessageProcessed)
							ebMSDAO.deleteAttachments(ebMSMessageId);
					}
				});
			return ebMSDAO.getMessageContent(ebMSMessageId);
		}
		catch (DAOException e)
		{
			throw new EbMSMessageServiceException(e);
		}
	}

	@Override
	public void processMessage(final long ebMSMessageId) throws EbMSMessageServiceException
	{
		try
		{
			ebMSDAO.executeTransaction(new DAOTransactionCallback()
			{
				@Override
				public void doInTransaction() throws DAOException
				{
					ebMSDAO.updateMessage(ebMSMessageId,EbMSMessageStatus.RECEIVED,EbMSMessageStatus.PROCESSED);
					if (deleteEbMSAttachmentsOnMessageProcessed)
						ebMSDAO.deleteAttachments(ebMSMessageId);
				}
			});
		}
		catch (DAOException e)
		{
			throw new EbMSMessageServiceException(e);
		}
	}

	@Override
	public void processMessages(final List<Long> ebMSMessageIds) throws EbMSMessageServiceException
	{
		try
		{
			ebMSDAO.executeTransaction(new DAOTransactionCallback()
			{
				@Override
				public void doInTransaction() throws DAOException
				{
					ebMSDAO.updateMessages(ebMSMessageIds,EbMSMessageStatus.RECEIVED,EbMSMessageStatus.PROCESSED);
					if (deleteEbMSAttachmentsOnMessageProcessed)
						ebMSDAO.deleteAttachments(ebMSMessageIds);
				}
			});
		}
		catch (DAOException e)
		{
			throw new EbMSMessageServiceException(e);
		}
	}
	
	@Override
	public MessageStatus getMessageStatus(long ebMSMessageId) throws EbMSMessageServiceException
	{
		try
		{
			EbMSMessageContext context = ebMSDAO.getMessageContext(ebMSMessageId);
			if (context == null)
				throw new EbMSMessageServiceException("No message found with messageId " + ebMSMessageId + "!");
			else if (Constants.EBMS_SERVICE_URI.equals(context.getService()))
				throw new EbMSMessageServiceException("Message with messageId " + ebMSMessageId + " is an EbMS service message!");
			else
			{
				EbMSMessage request = ebMSMessageFactory.createEbMSStatusRequest(context.getCpaId(),cpaManager.getFromParty(context.getCpaId(),context.getFromRole(),context.getService(),context.getAction()),cpaManager.getToParty(context.getCpaId(),context.getToRole(),context.getService(),context.getAction()),context.getMessageId());
				EbMSMessage response = deliveryManager.sendMessage(cpaManager.getUri(context.getCpaId(),new CacheablePartyId(request.getMessageHeader().getTo().getPartyId()),request.getMessageHeader().getTo().getRole(),CPAUtils.toString(request.getMessageHeader().getService()),request.getMessageHeader().getAction()),request);
				if (response != null)
				{
					if (EbMSAction.STATUS_RESPONSE.action().equals(response.getMessageHeader().getAction()) && response.getStatusResponse() != null)
						return new MessageStatus(response.getStatusResponse().getTimestamp() == null ? null : response.getStatusResponse().getTimestamp(),EbMSMessageStatus.get(response.getStatusResponse().getMessageStatus()));
					else
						throw new EbMSMessageServiceException("No valid response received!");
				}
				else
					throw new EbMSMessageServiceException("No response received!");
			}
		}
		catch (DAOException | EbMSProcessorException e)
		{
			throw new EbMSMessageServiceException(e);
		}
	}

	@Override
	public List<EbMSMessageEvent> getMessageEvents(EbMSMessageContext messageContext, EbMSMessageEventType[] eventTypes, Integer maxNr) throws EbMSMessageServiceException
	{
		try
		{
			if (maxNr == null || maxNr == 0)
				return ebMSDAO.getEbMSMessageEvents(messageContext,eventTypes);
			else
				return ebMSDAO.getEbMSMessageEvents(messageContext,eventTypes,maxNr);
		}
		catch (DAOException e)
		{
			throw new EbMSMessageServiceException(e);
		}
	}

	@Override
	public void processMessageEvent(final long ebMSMessageId) throws EbMSMessageServiceException
	{
		try
		{
			ebMSDAO.executeTransaction(new DAOTransactionCallback()
			{
				@Override
				public void doInTransaction() throws DAOException
				{
					ebMSDAO.processEbMSMessageEvent(ebMSMessageId);
					processMessage(ebMSMessageId);
				}
			});
		}
		catch (DAOException e)
		{
			throw new EbMSMessageServiceException(e);
		}
	}

	@Override
	public void processMessageEvents(final List<Long> ebMSMessageIds) throws EbMSMessageServiceException
	{
		try
		{
			ebMSDAO.executeTransaction(new DAOTransactionCallback()
			{
				@Override
				public void doInTransaction() throws DAOException
				{
					ebMSDAO.processEbMSMessageEvents(ebMSMessageIds);
					processMessages(ebMSMessageIds);
				}
			});
		}
		catch (DAOException e)
		{
			throw new EbMSMessageServiceException(e);
		}
	}

	private void resetMessage(EbMSMessageContext context)
	{
		//context.setConversationId(null);
		context.setMessageId(null);
		context.setTimestamp(null);
	}

	private long sendMessage_(EbMSMessageContent messageContent) throws EbMSProcessorException
	{
		final IdHolder result = new IdHolder();
		final EbMSMessage message = ebMSMessageFactory.createEbMSMessage(messageContent.getContext().getCpaId(),messageContent);
		signatureGenerator.generate(message);
		ebMSDAO.executeTransaction(
			new DAOTransactionCallback()
			{
				@Override
				public void doInTransaction()
				{
					Date timestamp = new Date();
					DeliveryChannel deliveryChannel = cpaManager.getReceiveDeliveryChannel(message.getMessageHeader().getCPAId(),new CacheablePartyId(message.getMessageHeader().getTo().getPartyId()),message.getMessageHeader().getTo().getRole(),CPAUtils.toString(message.getMessageHeader().getService()),message.getMessageHeader().getAction());
					result.id = ebMSDAO.insertMessage(timestamp,CPAUtils.getPersistTime(timestamp,deliveryChannel),message,EbMSMessageStatus.SENDING);
					eventManager.createEvent(message.getMessageHeader().getCPAId(),deliveryChannel,result.id,message.getMessageHeader().getMessageData().getTimeToLive(),message.getMessageHeader().getMessageData().getTimestamp(),cpaManager.isConfidential(message.getMessageHeader().getCPAId(),new CacheablePartyId(message.getMessageHeader().getFrom().getPartyId()),message.getMessageHeader().getFrom().getRole(),CPAUtils.toString(message.getMessageHeader().getService()),message.getMessageHeader().getAction()));
				}
			}
		);
		return result.id;
	}

	public void setDeliveryManager(DeliveryManager deliveryManager)
	{
		this.deliveryManager = deliveryManager;
	}

	public void setEbMSDAO(EbMSDAO ebMSDAO)
	{
		this.ebMSDAO = ebMSDAO;
	}

	public void setCpaManager(CPAManager cpaManager)
	{
		this.cpaManager = cpaManager;
	}

	public void setEbMSMessageFactory(EbMSMessageFactory ebMSMessageFactory)
	{
		this.ebMSMessageFactory = ebMSMessageFactory;
	}

	public void setEventManager(EventManager eventManager)
	{
		this.eventManager = eventManager;
	}

	public void setSignatureGenerator(EbMSSignatureGenerator signatureGenerator)
	{
		this.signatureGenerator = signatureGenerator;
	}

	public void setDeleteEbMSAttachmentsOnMessageProcessed(boolean deleteEbMSAttachmentsOnMessageProcessed)
	{
		this.deleteEbMSAttachmentsOnMessageProcessed = deleteEbMSAttachmentsOnMessageProcessed;
	}
}
