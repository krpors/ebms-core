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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LoggingEventListener implements EventListener
{
	protected transient Log logger = LogFactory.getLog(getClass());

	@Override
	public void onMessageReceived(long ebMSMessageId) throws EventException
	{
		logger.info("Message " + ebMSMessageId + " received");
	}

	@Override
	public void onMessageAcknowledged(long ebMSMessageId) throws EventException
	{
		logger.info("Message " + ebMSMessageId + " acknowledged");
	}
	
	@Override
	public void onMessageFailed(long ebMSMessageId) throws EventException
	{
		logger.info("Message " + ebMSMessageId + " failed");
	}

	@Override
	public void onMessageExpired(long ebMSMessageId) throws EventException
	{
		logger.info("Message " + ebMSMessageId + " expired");
	}
}
