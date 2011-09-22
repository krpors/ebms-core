/*******************************************************************************
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
 ******************************************************************************/
package nl.clockwork.mule.ebms.stub.ebf.transformer;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.datatype.DatatypeFactory;

import nl.clockwork.common.util.XMLMessageBuilder;
import nl.clockwork.mule.ebms.model.EbMSMessageContent;
import nl.clockwork.mule.ebms.stub.ebf.model.afleveren.bericht.AfleverBericht;
import nl.clockwork.mule.ebms.stub.ebf.model.afleveren.bevestiging.BevestigAfleverBericht;
import nl.clockwork.mule.ebms.stub.ebf.model.afleveren.bevestiging.FoutType;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageAwareTransformer;

public class AfleverBerichtToAfleverBevestiging extends AbstractMessageAwareTransformer
{
	public AfleverBerichtToAfleverBevestiging()
	{
		registerSourceType(EbMSMessageContent.class);
	}
	
	@Override
	public Object transform(MuleMessage message, String outputEncoding) throws TransformerException
	{
		try
		{
			EbMSMessageContent content = (EbMSMessageContent)message.getPayload();
			AfleverBericht afleverBericht = XMLMessageBuilder.getInstance(AfleverBericht.class).handle(content.getAttachments().iterator().next().getInputStream());
			BevestigAfleverBericht afleverBevestiging = new BevestigAfleverBericht();

			afleverBevestiging.setKenmerk(afleverBericht.getKenmerk());
			afleverBevestiging.setBerichtsoort(afleverBericht.getBerichtsoort());

			FoutType error = (FoutType)message.getProperty("AFLEVERBERICHT_ERROR");
			if (error == null)
				afleverBevestiging.setTijdstempelAfgeleverd(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
			else
				afleverBevestiging.setFout(error);

			ByteArrayDataSource ds = new ByteArrayDataSource(XMLMessageBuilder.getInstance(BevestigAfleverBericht.class).handle(afleverBevestiging),"application/xml");
			ds.setName(name);
			List<DataSource> attachments = new ArrayList<DataSource>();
			attachments.add(ds);

			content.getAttachments().clear();
			content.getAttachments().addAll(attachments);
			return content;
		}
		catch (Exception e)
		{
			throw new TransformerException(this,e);
		}
	}

}
