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
package nl.clockwork.ebms.service;

import java.security.cert.X509Certificate;
import java.util.List;

import org.oasis_open.committees.ebxml_cppa.schema.cpp_cpa_2_0.CollaborationProtocolAgreement;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.val;
import lombok.experimental.FieldDefaults;
import lombok.extern.apachecommons.CommonsLog;
import nl.clockwork.ebms.cpa.CPAManager;
import nl.clockwork.ebms.cpa.CertificateMapper;
import nl.clockwork.ebms.cpa.URLMapper;
import nl.clockwork.ebms.jaxb.JAXBParser;
import nl.clockwork.ebms.service.model.CertificateMapping;
import nl.clockwork.ebms.service.model.URLMapping;
import nl.clockwork.ebms.validation.CPAValidator;
import nl.clockwork.ebms.validation.XSDValidator;

@CommonsLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class CPAServiceImpl implements CPAService
{
  @NonNull
	CPAManager cpaManager;
  @NonNull
	URLMapper urlMapper;
  @NonNull
	CertificateMapper certificateMapper;
  @NonNull
	CPAValidator cpaValidator;
	XSDValidator xsdValidator = new XSDValidator("/nl/clockwork/ebms/xsd/cpp-cpa-2_0.xsd");
	Object cpaMonitor = new Object();

	@Override
	public void validateCPA(/*CollaborationProtocolAgreement*/String cpa) throws CPAServiceException
	{
		try
		{
			log.debug("ValidateCPA");
			xsdValidator.validate(cpa);
			val cpa_ = JAXBParser.getInstance(CollaborationProtocolAgreement.class).handle(cpa);
			log.info("Validating CPA " + cpa_.getCpaid());
			cpaValidator.validate(cpa_);
		}
		catch (Exception e)
		{
			log.error("ValidateCPA\n" + cpa,e);
			throw new CPAServiceException(e);
		}
	}
	
	@Override
	public String insertCPA(/*CollaborationProtocolAgreement*/String cpa, Boolean overwrite) throws CPAServiceException
	{
		try
		{
			log.debug("InsertCPA");
			xsdValidator.validate(cpa);
			val cpa_ = JAXBParser.getInstance(CollaborationProtocolAgreement.class).handle(cpa);
			val currentValidator = new CPAValidator(cpaManager);
			currentValidator.validate(cpa_);
			synchronized (cpaMonitor)
			{
				if (cpaManager.existsCPA(cpa_.getCpaid()))
				{
					if (overwrite != null && overwrite)
					{
						if (cpaManager.updateCPA(cpa_) == 0)
							throw new IllegalArgumentException("Could not update CPA " + cpa_.getCpaid() + "! CPA does not exists.");
					}
					else
						throw new IllegalArgumentException("Did not insert CPA " + cpa_.getCpaid() + "! CPA already exists.");
				}
				else
					cpaManager.insertCPA(cpa_);
			}
			log.debug("InsertCPA done");
			return cpa_.getCpaid();
		}
		catch (Exception e)
		{
			log.error("InsertCPA\n" + cpa,e);
			throw new CPAServiceException(e);
		}
	}

	@Override
	public void deleteCPA(String cpaId) throws CPAServiceException
	{
		try
		{
			log.debug("DeleteCPA " + cpaId);
			synchronized(cpaMonitor)
			{
				if (cpaManager.deleteCPA(cpaId) == 0)
					throw new IllegalArgumentException("Could not delete CPA " + cpaId + "! CPA does not exists.");
			}
		}
		catch (Exception e)
		{
			log.error("DeleteCPA " + cpaId,e);
			throw new CPAServiceException(e);
		}
	}

	@Override
	public List<String> getCPAIds() throws CPAServiceException
	{
		try
		{
			log.debug("GetCPAIds");
			return cpaManager.getCPAIds();
		}
		catch (Exception e)
		{
			log.error("GetCPAIds",e);
			throw new CPAServiceException(e);
		}
	}

	@Override
	public /*CollaborationProtocolAgreement*/String getCPA(String cpaId) throws CPAServiceException
	{
		try
		{
			log.debug("GetCPAId " + cpaId);
			return JAXBParser.getInstance(CollaborationProtocolAgreement.class).handle(cpaManager.getCPA(cpaId).orElse(null));
		}
		catch (Exception e)
		{
			log.error("GetCPAId " + cpaId,e);
			throw new CPAServiceException(e);
		}
	}

	@Override
	public void setURLMapping(URLMapping urlMapping) throws CPAServiceException
	{
		try
		{
			if (log.isDebugEnabled())
				log.debug("SetURLMapping " + urlMapping);
			urlMapper.setURLMapping(urlMapping);
		}
		catch (Exception e)
		{
			log.error("SetURLMapping " + urlMapping,e);
			throw new CPAServiceException(e);
		}
	}

	@Override
	public void deleteURLMapping(String source) throws CPAServiceException
	{
		try
		{
			log.debug("DeleteURLMapping " + source);
			urlMapper.deleteURLMapping(source);
		}
		catch (Exception e)
		{
			log.error("DeleteURLMapping " + source,e);
			throw new CPAServiceException(e);
		}
	}

	@Override
	public List<URLMapping> getURLMappings() throws CPAServiceException
	{
		try
		{
			log.debug("GetURLMappings");
			return urlMapper.getURLs();
		}
		catch (Exception e)
		{
			log.error("GetURLMappings",e);
			throw new CPAServiceException(e);
		}
	}

	@Override
	public void setCertificateMapping(CertificateMapping certificateMapping) throws CPAServiceException
	{
		try
		{
			if (log.isDebugEnabled())
				log.debug("SetCertificateMapping" + certificateMapping);
			certificateMapper.setCertificateMapping(certificateMapping);
		}
		catch (Exception e)
		{
			log.error("SetCertificateMapping " + certificateMapping,e);
			throw new CPAServiceException(e);
		}
	}

	@Override
	public void deleteCertificateMapping(X509Certificate source) throws CPAServiceException
	{
		try
		{
			if (log.isDebugEnabled())
				log.debug("SetCertificateMapping" + source);
			certificateMapper.deleteCertificateMapping(source);
		}
		catch (Exception e)
		{
			log.error("SetCertificateMapping" + source,e);
			throw new CPAServiceException(e);
		}
	}

	@Override
	public List<CertificateMapping> getCertificateMappings() throws CPAServiceException
	{
		try
		{
			log.debug("SetCertificateMapping");
			return certificateMapper.getCertificates();
		}
		catch (Exception e)
		{
			log.error("SetCertificateMapping",e);
			throw new CPAServiceException(e);
		}
	}
}
