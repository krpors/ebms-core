package nl.clockwork.ebms.model;

import java.util.ArrayList;
import java.util.List;

import org.oasis_open.committees.ebxml_msg.schema.msg_header_2_0.AckRequested;
import org.oasis_open.committees.ebxml_msg.schema.msg_header_2_0.Acknowledgment;
import org.oasis_open.committees.ebxml_msg.schema.msg_header_2_0.ErrorList;
import org.oasis_open.committees.ebxml_msg.schema.msg_header_2_0.Manifest;
import org.oasis_open.committees.ebxml_msg.schema.msg_header_2_0.MessageHeader;
import org.oasis_open.committees.ebxml_msg.schema.msg_header_2_0.MessageOrder;
import org.oasis_open.committees.ebxml_msg.schema.msg_header_2_0.StatusRequest;
import org.oasis_open.committees.ebxml_msg.schema.msg_header_2_0.StatusResponse;
import org.oasis_open.committees.ebxml_msg.schema.msg_header_2_0.SyncReply;
import org.w3._2000._09.xmldsig.SignatureType;

import lombok.NonNull;
import nl.clockwork.ebms.Constants;
import nl.clockwork.ebms.EbMSAction;
import nl.clockwork.ebms.processor.EbMSProcessingException;

public class EbMSMessageBuilder
{
	private MessageHeader messageHeader;
	private SyncReply syncReply;
	private MessageOrder messageOrder;
	private AckRequested ackRequested;
	private ErrorList errorList;
	private Acknowledgment acknowledgment;
	private Manifest manifest;
	private StatusRequest statusRequest;
	private StatusResponse statusResponse;
	private SignatureType signature;
	private boolean attachments$set;
	private List<EbMSAttachment> attachments$value;

	public EbMSMessageBuilder()
	{
	}

	public EbMSMessageBuilder messageHeader(@NonNull final MessageHeader messageHeader)
	{
		if (messageHeader == null)
			throw new java.lang.NullPointerException("messageHeader is marked non-null but is null");
		this.messageHeader = messageHeader;
		return this;
	}

	public EbMSMessageBuilder syncReply(final SyncReply syncReply)
	{
		this.syncReply = syncReply;
		return this;
	}

	public EbMSMessageBuilder messageOrder(final MessageOrder messageOrder)
	{
		this.messageOrder = messageOrder;
		return this;
	}

	public EbMSMessageBuilder ackRequested(final AckRequested ackRequested)
	{
		this.ackRequested = ackRequested;
		return this;
	}

	public EbMSMessageBuilder errorList(final ErrorList errorList)
	{
		this.errorList = errorList;
		return this;
	}

	public EbMSMessageBuilder acknowledgment(final Acknowledgment acknowledgment)
	{
		this.acknowledgment = acknowledgment;
		return this;
	}

	public EbMSMessageBuilder manifest(final Manifest manifest)
	{
		this.manifest = manifest;
		return this;
	}

	public EbMSMessageBuilder statusRequest(final StatusRequest statusRequest)
	{
		this.statusRequest = statusRequest;
		return this;
	}

	public EbMSMessageBuilder statusResponse(final StatusResponse statusResponse)
	{
		this.statusResponse = statusResponse;
		return this;
	}

	public EbMSMessageBuilder signature(final SignatureType signature)
	{
		this.signature = signature;
		return this;
	}

	public EbMSMessageBuilder attachments(@NonNull final List<EbMSAttachment> attachments)
	{
		if (attachments == null)
			throw new java.lang.NullPointerException("attachments is marked non-null but is null");
		this.attachments$value = attachments;
		attachments$set = true;
		return this;
	}

	public EbMSBaseMessage build()
	{
		try
		{
			List<EbMSAttachment> attachments$value = this.attachments$value;
			if (!this.attachments$set) attachments$value = new ArrayList<EbMSAttachment>();
			if (!Constants.EBMS_SERVICE_URI.equals(messageHeader.getService().getValue()))
				return EbMSMessage.builder()
						.messageHeader(messageHeader)
						.signature(signature)
						.syncReply(syncReply)
						.messageOrder(messageOrder)
						.ackRequested(ackRequested)
						.manifest(manifest)
						.attachments(attachments$value)
						.build();
			else if (EbMSAction.MESSAGE_ERROR.getAction().equals(messageHeader.getAction()))
				return new EbMSMessageError(messageHeader,signature,errorList);
			else if (EbMSAction.ACKNOWLEDGMENT.getAction().equals(messageHeader.getAction()))
				return new EbMSAcknowledgment(messageHeader,signature,acknowledgment);
			else if (EbMSAction.STATUS_REQUEST.getAction().equals(messageHeader.getAction()))
				return new EbMSStatusRequest(messageHeader,signature,syncReply,statusRequest);
			else if (EbMSAction.STATUS_RESPONSE.getAction().equals(messageHeader.getAction()))
				return new EbMSStatusResponse(messageHeader,signature,statusResponse);
			else if (EbMSAction.PING.getAction().equals(messageHeader.getAction()))
				return new EbMSPing(messageHeader,signature,syncReply);
			else if (EbMSAction.PONG.getAction().equals(messageHeader.getAction()))
				return new EbMSPong(messageHeader,signature);
			else
				throw new EbMSProcessingException("Unable to create message!");
		}
		catch (Exception e)
		{
			throw new EbMSProcessingException(e);
		}
	}

	@java.lang.Override
	public java.lang.String toString()
	{
		return "EbMSMessage.EbMSMessageBuilder(messageHeader=" + this.messageHeader + ", syncReply=" + this.syncReply + ", messageOrder=" + this.messageOrder + ", ackRequested=" + this.ackRequested + ", errorList=" + this.errorList + ", acknowledgment=" + this.acknowledgment + ", manifest=" + this.manifest + ", statusRequest=" + this.statusRequest + ", statusResponse=" + this.statusResponse + ", signature=" + this.signature + ", attachments$value=" + this.attachments$value + ")";
	}
}