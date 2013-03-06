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
package nl.clockwork.ebms.signing;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;

import nl.clockwork.ebms.Constants;
import nl.clockwork.ebms.common.util.DOMUtils;
import nl.clockwork.ebms.common.util.SecurityUtils;
import nl.clockwork.ebms.model.EbMSAttachment;
import nl.clockwork.ebms.processor.EbMSProcessorException;
import nl.clockwork.ebms.xml.dsig.EbMSAttachmentResolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.transforms.Transforms;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLSecSignatureGenerator implements SignatureGenerator
{
	protected transient Log logger = LogFactory.getLog(getClass());
  private String canonicalizationMethodAlgorithm = Transforms.TRANSFORM_C14N_OMIT_COMMENTS;
  private String signatureMethodAlgorithm = XMLSignature.ALGO_ID_SIGNATURE_DSA;
  private String transformAlgorithm = Transforms.TRANSFORM_C14N_OMIT_COMMENTS;
	private String keyStorePath;
	private String keyStorePassword;
	private String keyAlias;
	private String keyPassword;
	private KeyStore keyStore;
	private KeyPair keyPair;

	public void init() throws GeneralSecurityException, IOException
	{
		keyStore = SecurityUtils.loadKeyStore(keyStorePath,keyStorePassword);
		keyPair = SecurityUtils.getKeyPair(keyStore,keyAlias,keyPassword);
	}
	
	@Override
	public boolean generate(Document document, List<EbMSAttachment> attachments) throws EbMSProcessorException
	{
		try
		{
			sign(keyStore,keyPair,keyAlias,document,attachments);
			return true;
		}
		catch (XMLSignatureException e)
		{
			return false;
		}
		catch (Exception e)
		{
			throw new EbMSProcessorException(e);
		}
	}

	private void sign(KeyStore keyStore, KeyPair keyPair, String alias, Document document, List<EbMSAttachment> attachments) throws XMLSecurityException, KeyStoreException
	{
		XMLSignature signature = new XMLSignature(document,org.apache.xml.security.utils.Constants.SignatureSpecNS,signatureMethodAlgorithm,canonicalizationMethodAlgorithm);

		Element soapHeader = DOMUtils.getFirstChildElement(document.getDocumentElement());
		soapHeader.appendChild(signature.getElement());
		
		EbMSAttachmentResolver resolver = new EbMSAttachmentResolver(attachments);
		signature.getSignedInfo().addResourceResolver(resolver);
			
		Transforms transforms = new Transforms(document);
		transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
		Element xpath = document.createElementNS(org.apache.xml.security.utils.Constants.SignatureSpecNS,org.apache.xml.security.utils.Constants._TAG_XPATH);
		xpath.setAttributeNS(org.apache.xml.security.utils.Constants.NamespaceSpecNS,"xmlns:" + Constants.NAMESPACE_PREFIX_SOAP_ENVELOPE,Constants.NAMESPACE_URI_SOAP_ENVELOPE);
		xpath.appendChild(document.createTextNode(Constants.TRANSFORM_XPATH));
		xpath.setPrefix(Constants.NAMESPACE_PREFIX_DS);
		transforms.addTransform(Transforms.TRANSFORM_XPATH,xpath);
		transforms.addTransform(transformAlgorithm);
		
		signature.addDocument("",transforms,org.apache.xml.security.utils.Constants.ALGO_ID_DIGEST_SHA1);
		
		for (EbMSAttachment attachment : attachments)
			signature.addDocument(Constants.CID + attachment.getContentId());
		
		signature.addKeyInfo(keyPair.getPublic());
		
		Certificate[] certificates = keyStore.getCertificateChain(alias);
    //for (Certificate certificate : certificates)
    //	signature.addKeyInfo((X509Certificate)certificate);
		signature.addKeyInfo((X509Certificate)certificates[0]);

		signature.sign(keyPair.getPrivate());
	}
	
	public void setCanonicalizationMethodAlgorithm(String canonicalizationMethodAlgorithm)
	{
		this.canonicalizationMethodAlgorithm = canonicalizationMethodAlgorithm;
	}
	
	public void setSignatureMethodAlgorithm(String signatureMethodAlgorithm)
	{
		this.signatureMethodAlgorithm = signatureMethodAlgorithm;
	}
	
	public void setTransformAlgorithm(String transformAlgorithm)
	{
		this.transformAlgorithm = transformAlgorithm;
	}

	public void setKeyStorePath(String keyStorePath)
	{
		this.keyStorePath = keyStorePath;
	}
	
	public void setKeyStorePassword(String keyStorePassword)
	{
		this.keyStorePassword = keyStorePassword;
	}
	
	public void setKeyAlias(String keyAlias)
	{
		this.keyAlias = keyAlias;
	}
	
	public void setKeyPassword(String keyPassword)
	{
		this.keyPassword = keyPassword;
	}
}