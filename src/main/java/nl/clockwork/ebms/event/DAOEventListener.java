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

import nl.clockwork.ebms.Constants.EbMSMessageEventType;
import nl.clockwork.ebms.dao.EbMSDAO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DAOEventListener implements EventListener
{
	protected transient Log logger = LogFactory.getLog(getClass());
	private EbMSDAO ebMSDAO;

	public DAOEventListener()
	{
	}

	public DAOEventListener(EbMSDAO ebMSDAO)
	{
		this.ebMSDAO = ebMSDAO;
	}

	@Override
	public void onMessageReceived(long ebMSMessageId) throws EventException
	{
		logger.info("Message " + ebMSMessageId + " received");
		ebMSDAO.insertEbMSMessageEvent(ebMSMessageId,EbMSMessageEventType.RECEIVED);
	}

	@Override
	public void onMessageAcknowledged(long ebMSMessageId) throws EventException
	{
		logger.info("Message " + ebMSMessageId + " acknowledged");
		ebMSDAO.insertEbMSMessageEvent(ebMSMessageId,EbMSMessageEventType.ACKNOWLEDGED);
	}
	
	@Override
	public void onMessageFailed(long ebMSMessageId) throws EventException
	{
		logger.info("Message " + ebMSMessageId + " failed");
		ebMSDAO.insertEbMSMessageEvent(ebMSMessageId,EbMSMessageEventType.FAILED);
	}

	@Override
	public void onMessageExpired(long ebMSMessageId) throws EventException
	{
		logger.info("Message " + ebMSMessageId + " expired");
		ebMSDAO.insertEbMSMessageEvent(ebMSMessageId,EbMSMessageEventType.EXPIRED);
	}

	public void setEbMSDAO(EbMSDAO ebMSDAO)
	{
		this.ebMSDAO = ebMSDAO;
	}
}
