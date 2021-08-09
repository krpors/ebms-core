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
package nl.clockwork.ebms.cpa.certificate;

import java.security.cert.X509Certificate;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import nl.clockwork.ebms.jaxrs.WithService;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@Path("certificateMapping")
@Produces(MediaType.APPLICATION_JSON)
public class CertificateMappingServiceImpl implements CertificateMappingService, WithService
{
  @NonNull
	CertificateMapper certificateMapper;

	@POST
	@Override
	public void setCertificateMapping(CertificateMapping certificateMapping) throws CertificateMappingServiceException
	{
		try
		{
			if (log.isDebugEnabled())
				log.debug("SetCertificateMapping " + certificateMapping);
			certificateMapper.setCertificateMapping(certificateMapping);
		}
		catch (Exception e)
		{
			log.error("SetCertificateMapping " + certificateMapping,e);
			throwServiceException(new CertificateMappingServiceException(e));
		}
	}

	@DELETE
	@Path("{id}/{cpaId}")
	@Override
	public void deleteCertificateMapping(@PathParam("id") X509Certificate source, @PathParam("cpaId") String cpaId) throws CertificateMappingServiceException
	{
		try
		{
			if (log.isDebugEnabled())
				log.debug("SetCertificateMapping" + source);
			certificateMapper.deleteCertificateMapping(source,cpaId);
		}
		catch (Exception e)
		{
			log.error("SetCertificateMapping" + source,e);
			throwServiceException(new CertificateMappingServiceException(e));
		}
	}

	@GET
	@Override
	public List<CertificateMapping> getCertificateMappings() throws CertificateMappingServiceException
	{
		try
		{
			log.debug("SetCertificateMapping");
			return certificateMapper.getCertificates();
		}
		catch (Exception e)
		{
			log.error("SetCertificateMapping",e);
			throwServiceException(new CertificateMappingServiceException(e));
			return null;
		}
	}
}
