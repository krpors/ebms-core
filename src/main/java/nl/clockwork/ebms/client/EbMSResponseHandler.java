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
package nl.clockwork.ebms.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;
import org.xml.sax.SAXException;

import nl.clockwork.ebms.Constants;
import nl.clockwork.ebms.common.util.HTTPUtils;
import nl.clockwork.ebms.model.EbMSDocument;
import nl.clockwork.ebms.processor.EbMSProcessingException;
import nl.clockwork.ebms.processor.EbMSProcessorException;
import nl.clockwork.ebms.server.EbMSMessageReader;

public class EbMSResponseHandler
{
	protected transient Log messageLogger = LogFactory.getLog(Constants.MESSAGE_LOG);
	private HttpURLConnection connection;
	private List<Integer> recoverableHttpErrors;
	private List<Integer> unrecoverableHttpErrors;
	
	public EbMSResponseHandler(HttpURLConnection connection, List<Integer> recoverableHttpErrors, List<Integer> unrecoverableHttpErrors)
	{
		this.connection = connection;
		this.recoverableHttpErrors = recoverableHttpErrors;
		this.unrecoverableHttpErrors = unrecoverableHttpErrors;
	}

	public EbMSDocument read() throws EbMSProcessorException
	{
		try
		{
			if (connection.getResponseCode() / 100 == 2)
			{
				if (connection.getResponseCode() == Constants.SC_NOCONTENT || connection.getContentLength() == 0)
				{
					logResponse(connection);
					return null;
				}
				else
					return readSuccesResponse(connection);
			}
			else if (connection.getResponseCode() / 100 == 1 || connection.getResponseCode() / 100 == 3 || connection.getResponseCode() / 100 == 4)
				throw createRecoverableErrorException(connection);
			else if (connection.getResponseCode() / 100 == 5)
				throw createUnrecoverableErrorException(connection);
			else
			{
				logResponse(connection);
				throw new EbMSUnrecoverableResponseException(connection.getResponseCode(),connection.getHeaderFields());
			}
		}
		catch (IOException e)
		{
			try
			{
				throw new EbMSResponseException(connection.getResponseCode(),connection.getHeaderFields(),e);
			}
			catch (IOException e1)
			{
				throw new EbMSProcessingException(e);
			}
		}
	}

	private EbMSDocument readSuccesResponse(HttpURLConnection connection) throws IOException
	{
		try (InputStream input = connection.getInputStream())
		{
			EbMSMessageReader messageReader = new EbMSMessageReader(getHeaderField("Content-ID"),getHeaderField("Content-Type"));
			String response = IOUtils.toString(input,getEncoding());
			logResponse(connection,response);
			try
			{
				return messageReader.readResponse(response);
			}
			catch (ParserConfigurationException e)
			{
				throw new EbMSProcessorException(e);
			}
			catch (SAXException e)
			{
				throw new EbMSResponseException(connection.getResponseCode(),connection.getHeaderFields(),response);
			}
		}
	}

	private EbMSResponseException createRecoverableErrorException(HttpURLConnection connection) throws IOException
	{
		try (InputStream input = connection.getErrorStream())
		{
			String response = readResponse(connection,input);
			if (recoverableHttpErrors.contains(connection.getResponseCode()))
				return new EbMSResponseException(connection.getResponseCode(),connection.getHeaderFields(),response);
			else
				return new EbMSUnrecoverableResponseException(connection.getResponseCode(),connection.getHeaderFields(),response);
		}
	}

	private EbMSResponseException createUnrecoverableErrorException(HttpURLConnection connection) throws IOException
	{
		try (InputStream input = connection.getErrorStream())
		{
			String response = readResponse(connection,input);
			if (unrecoverableHttpErrors.contains(connection.getResponseCode()))
				return new EbMSUnrecoverableResponseException(connection.getResponseCode(),connection.getHeaderFields(),response);
			else
				return new EbMSResponseException(connection.getResponseCode(),connection.getHeaderFields(),response);
		}
	}

	private String readResponse(HttpURLConnection connection, InputStream input) throws IOException
	{
		String response = null;
		if (input != null)
		{
			response = IOUtils.toString(input,Charset.defaultCharset());
			logResponse(connection,response);
		}
		return response;
	}

	private String getEncoding() throws EbMSProcessingException
	{
		String contentType = getHeaderField("Content-Type");
		if (!StringUtils.isEmpty(contentType))
			return HTTPUtils.getCharSet(contentType);
		else
			throw new EbMSProcessingException("HTTP header Content-Type is not set!");
	}
	
	private String getHeaderField(String name)
	{
		return connection.getHeaderField(name);
	}

	private void logResponse(HttpURLConnection connection) throws IOException
	{
		logResponse(connection,null);
	}

	private void logResponse(HttpURLConnection connection, String response) throws IOException
	{
		String headers = connection.getResponseCode() + (messageLogger.isDebugEnabled() ? "\n" + HTTPUtils.toString(connection.getHeaderFields()) : "");
		response = response != null ? "\n" + response : "";
		messageLogger.info("<<<<\nstatusCode: " + headers + response);
	}

}
