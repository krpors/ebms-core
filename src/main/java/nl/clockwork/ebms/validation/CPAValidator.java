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
package nl.clockwork.ebms.validation;

import java.util.GregorianCalendar;

import nl.clockwork.ebms.Constants;
import nl.clockwork.ebms.util.CPAUtils;
import nl.clockwork.ebms.util.EbMSMessageUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.committees.ebxml_cppa.schema.cpp_cpa_2_0.CollaborationProtocolAgreement;
import org.oasis_open.committees.ebxml_msg.schema.msg_header_2_0.ErrorList;
import org.oasis_open.committees.ebxml_msg.schema.msg_header_2_0.MessageHeader;
import org.oasis_open.committees.ebxml_msg.schema.msg_header_2_0.SeverityType;

public class CPAValidator
{
  protected transient Log logger = LogFactory.getLog(getClass());

	public boolean isValid(ErrorList errorList, CollaborationProtocolAgreement cpa, MessageHeader messageHeader, GregorianCalendar timestamp)
	{
		if (!cpaExists(cpa,messageHeader))
		{
			errorList.getError().add(EbMSMessageUtils.createError("//Header/MessageHeader[@cpaid]",Constants.EbMSErrorCode.VALUE_NOT_RECOGNIZED.errorCode(),"CPA not found."));
			errorList.setHighestSeverity(SeverityType.ERROR);
			return false;
		}
		if (!CPAUtils.isValid(cpa,timestamp))
		{
			errorList.getError().add(EbMSMessageUtils.createError("//Header/MessageHeader[@cpaid]",Constants.EbMSErrorCode.INCONSISTENT.errorCode(),"Invalid CPA."));
			errorList.setHighestSeverity(SeverityType.ERROR);
			return false;
		}
		return true;
	}

	public void validate(CollaborationProtocolAgreement cpa) throws ValidatorException
	{
		
	}
	
	private boolean cpaExists(CollaborationProtocolAgreement cpa, MessageHeader messageHeader)
	{
		return cpa != null && cpa.getCpaid().equals(messageHeader.getCPAId());
	}
	
}
