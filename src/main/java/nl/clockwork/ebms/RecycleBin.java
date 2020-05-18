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
package nl.clockwork.ebms;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.cxf.io.CachedOutputStream;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
public class RecycleBin
{
	private static class Cleaner implements Runnable
	{
		@Override
		public void run()
		{
			while (true)
				try
				{
					CachedOutputStream stream = streams.poll(Long.MAX_VALUE,TimeUnit.DAYS);
					if (stream != null)
						stream.close();
				}
				catch (InterruptedException e)
				{
					// do nothing
				}
				catch (IOException e)
				{
					log.debug(e);
				}
		}
	}
	
	private static BlockingQueue<CachedOutputStream> streams;

	{
		Thread thread = new Thread(new Cleaner());
		thread.setDaemon(true);
		thread.start();
	}

	public static void markForDeletion(CachedOutputStream stream)
	{
		streams.add(stream);
	}

}
