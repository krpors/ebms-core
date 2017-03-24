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

import nl.clockwork.ebms.Constants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

public class SimpleJMSEventListener implements EventListener
{
	public class EventMessageCreator implements MessageCreator
	{
		private String messageId;

		public EventMessageCreator(String messageId)
		{
			this.messageId = messageId;
		}

		@Override
		public Message createMessage(Session session) throws JMSException
		{
			Message result = session.createMessage();
			result.setStringProperty("messageId",messageId);
			return result;
		}
	}

	protected transient Log logger = LogFactory.getLog(getClass());
	private JmsTemplate jmsTemplate;
	private Map<String,Destination> destinations;

	@Override
	public void onMessageReceived(String messageId) throws EventException
	{
		try
		{
			logger.info("Message " + messageId + " received");
			jmsTemplate.send(destinations.get(Constants.EVENT_RECEIVED),new EventMessageCreator(messageId));
		}
		catch (JmsException e)
		{
			throw new EventException(e);
		}
	}

	@Override
	public void onMessageAcknowledged(String messageId) throws EventException
	{
		try
		{
			logger.info("Message " + messageId + " acknowledged");
			jmsTemplate.send(destinations.get(Constants.EVENT_ACKNOWLEDGED),new EventMessageCreator(messageId));
		}
		catch (JmsException e)
		{
			throw new EventException(e);
		}
	}
	
	@Override
	public void onMessageFailed(String messageId) throws EventException
	{
		try
		{
			logger.info("Message " + messageId + " failed");
			jmsTemplate.send(destinations.get(Constants.EVENT_FAILED),new EventMessageCreator(messageId));
		}
		catch (JmsException e)
		{
			throw new EventException(e);
		}
	}

	@Override
	public void onMessageExpired(String messageId) throws EventException
	{
		try
		{
			logger.info("Message " + messageId + " expired");
			jmsTemplate.send(destinations.get(Constants.EVENT_EXPIRED),new EventMessageCreator(messageId));
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