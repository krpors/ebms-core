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
package nl.clockwork.ebms.event;

import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import nl.clockwork.ebms.Constants.EbMSMessageEventType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

public class SimpleJMSEventListener implements EventListener
{
	public class EventMessageCreator implements MessageCreator
	{
		private long ebMSMessageId;

		public EventMessageCreator(long ebMSMessageId)
		{
			this.ebMSMessageId = ebMSMessageId;
		}

		@Override
		public Message createMessage(Session session) throws JMSException
		{
			Message result = session.createMessage();
			result.setLongProperty("ebMSMessageId",ebMSMessageId);
			return result;
		}
	}

	protected transient Log logger = LogFactory.getLog(getClass());
	private JmsTemplate jmsTemplate;
	private Map<String,Destination> destinations;

	public SimpleJMSEventListener()
	{
	}

	public SimpleJMSEventListener(JmsTemplate jmsTemplate, Map<String,Destination> destinations)
	{
		this.jmsTemplate = jmsTemplate;
		this.destinations = destinations;
	}

	@Override
	public void onMessageReceived(long ebMSMessageId) throws EventException
	{
		try
		{
			logger.info("Message " + ebMSMessageId + " received");
			jmsTemplate.send(destinations.get(EbMSMessageEventType.RECEIVED.name()),new EventMessageCreator(ebMSMessageId));
		}
		catch (JmsException e)
		{
			throw new EventException(e);
		}
	}

	@Override
	public void onMessageAcknowledged(long ebMSMessageId) throws EventException
	{
		try
		{
			logger.info("Message " + ebMSMessageId + " acknowledged");
			jmsTemplate.send(destinations.get(EbMSMessageEventType.ACKNOWLEDGED.name()),new EventMessageCreator(ebMSMessageId));
		}
		catch (JmsException e)
		{
			throw new EventException(e);
		}
	}
	
	@Override
	public void onMessageFailed(long ebMSMessageId) throws EventException
	{
		try
		{
			logger.info("Message " + ebMSMessageId + " failed");
			jmsTemplate.send(destinations.get(EbMSMessageEventType.FAILED.name()),new EventMessageCreator(ebMSMessageId));
		}
		catch (JmsException e)
		{
			throw new EventException(e);
		}
	}

	@Override
	public void onMessageExpired(long ebMSMessageId) throws EventException
	{
		try
		{
			logger.info("Message " + ebMSMessageId + " expired");
			jmsTemplate.send(destinations.get(EbMSMessageEventType.EXPIRED.name()),new EventMessageCreator(ebMSMessageId));
		}
		catch (JmsException e)
		{
			throw new EventException(e);
		}
	}

	public void setJmsTemplate(JmsTemplate jmsTemplate)
	{
		this.jmsTemplate = jmsTemplate;
	}
	
	public void setDestinations(Map<String,Destination> destinations)
	{
		this.destinations = destinations;
	}
}
