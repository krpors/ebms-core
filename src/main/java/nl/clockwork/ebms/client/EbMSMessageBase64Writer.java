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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;

import javax.xml.transform.TransformerException;

import nl.clockwork.ebms.Constants;
import nl.clockwork.ebms.common.util.DOMUtils;
import nl.clockwork.ebms.model.EbMSAttachment;
import nl.clockwork.ebms.model.EbMSDocument;

import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.codec.Base64OutputStream;
import org.springframework.util.StringUtils;

public class EbMSMessageBase64Writer extends EbMSMessageWriter
{
	public EbMSMessageBase64Writer(HttpURLConnection connection)
	{
		super(connection);
	}

	@Override
	protected void writeMimeMessage(EbMSDocument document) throws IOException, TransformerException
	{
		String boundary = createBoundary();
		String contentType = createContentType(boundary,document.getContentId());

		connection.setRequestProperty("MIME-Version","1.0");
		connection.setRequestProperty("Content-Type",contentType);
		connection.setRequestProperty("SOAPAction",Constants.EBMS_SOAP_ACTION);

		OutputStream outputStream = connection.getOutputStream();

		try (OutputStreamWriter writer = new OutputStreamWriter(outputStream,"UTF-8"))
		{
			writer.write("--");
			writer.write(boundary);
			writer.write("\r\n");

			writer.write("Content-Type: text/xml; charset=UTF-8");
			writer.write("\r\n");
			writer.write("Content-ID: <" + document.getContentId() + ">");
			writer.write("\r\n");
			writer.write("\r\n");
			DOMUtils.write(document.getMessage(),writer,"UTF-8");
			writer.write("\r\n");
			writer.write("--");
			writer.write(boundary);

			for (EbMSAttachment attachment : document.getAttachments())
				if (attachment.getContentType().matches("^(text/.*|.*/xml)$"))
					writeTextAttachment(boundary,outputStream,writer,attachment);
				else
					writeBinaryAttachment(boundary,outputStream,writer,attachment);

			writer.write("--");
		}
	}

	private void writeTextAttachment(String boundary, OutputStream outputStream, OutputStreamWriter writer, EbMSAttachment attachment) throws IOException
	{
		writer.write("\r\n");
		writer.write("Content-Type: " + attachment.getContentType());
		writer.write("\r\n");
		if (!StringUtils.isEmpty(attachment.getName()))
		{
			writer.write("Content-Disposition: attachment; filename=" + attachment.getName() + ";");
			writer.write("\r\n");
		}
		writer.write("Content-ID: <" + attachment.getContentId() + ">");
		writer.write("\r\n");
		writer.write("\r\n");
		writer.flush();
		IOUtils.copy(attachment.getInputStream(),outputStream);
		writer.write("\r\n");
		writer.write("--");
		writer.write(boundary);
	}

	private void writeBinaryAttachment(String boundary, OutputStream outputStream, OutputStreamWriter writer, EbMSAttachment attachment) throws IOException
	{
		writer.write("\r\n");
		writer.write("Content-Type: " + attachment.getContentType());
		writer.write("\r\n");
		if (!StringUtils.isEmpty(attachment.getName()))
		{
			writer.write("Content-Disposition: attachment; filename=" + attachment.getName() + ";");
			writer.write("\r\n");
		}
		writer.write("Content-Transfer-Encoding: base64");
		writer.write("\r\n");
		writer.write("Content-ID: <" + attachment.getContentId() + ">");
		writer.write("\r\n");
		writer.write("\r\n");
		writer.flush();
		IOUtils.copy(attachment.getInputStream(),new Base64OutputStream(outputStream));
		writer.write("\r\n");
		writer.write("--");
		writer.write(boundary);
	}

}
