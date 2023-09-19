/*
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
package nl.clockwork.ebms.jaxb;

import java.security.cert.X509Certificate;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class X509CertificateAdapter extends XmlAdapter<byte[], X509Certificate>
{
	@Override
	public X509Certificate unmarshal(byte[] certificate) throws Exception
	{
		return X509CertificateConverter.parseCertificate(certificate);
	}

	@Override
	public byte[] marshal(X509Certificate certificate) throws Exception
	{
		return X509CertificateConverter.printCertificate(certificate);
	}
}
