package nl.clockwork.ebms.model;

import java.util.Date;

import nl.clockwork.ebms.Constants.EbMSMessageStatus;

public class MessageStatus
{
	private Date timestamp;
	private EbMSMessageStatus status;
	
	public MessageStatus()
	{
	}
	
	public MessageStatus(Date timestamp, EbMSMessageStatus status)
	{
		this.timestamp = timestamp;
		this.status = status;
	}
	
	public Date getTimestamp()
	{
		return timestamp;
	}
	
	public void setTimestamp(Date timestamp)
	{
		this.timestamp = timestamp;
	}

	public EbMSMessageStatus getStatus()
	{
		return status;
	}
	
	public void setStatus(EbMSMessageStatus status)
	{
		this.status = status;
	}
}