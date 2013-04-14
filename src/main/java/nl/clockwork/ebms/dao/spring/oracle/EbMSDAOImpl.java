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
package nl.clockwork.ebms.dao.spring.oracle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import nl.clockwork.ebms.Constants.EbMSMessageStatus;
import nl.clockwork.ebms.common.util.XMLMessageBuilder;
import nl.clockwork.ebms.dao.DAOException;
import nl.clockwork.ebms.dao.spring.AbstractEbMSDAO;
import nl.clockwork.ebms.model.EbMSAttachment;
import nl.clockwork.ebms.model.EbMSMessage;
import nl.clockwork.ebms.model.ebxml.AckRequested;
import nl.clockwork.ebms.model.ebxml.MessageHeader;
import nl.clockwork.ebms.model.ebxml.MessageOrder;
import nl.clockwork.ebms.model.ebxml.SyncReply;
import nl.clockwork.ebms.model.xml.dsig.SignatureType;

import org.apache.commons.io.IOUtils;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public class EbMSDAOImpl extends AbstractEbMSDAO
{
	public EbMSDAOImpl(PlatformTransactionManager transactionManager, javax.sql.DataSource dataSource)
	{
		super(transactionManager,dataSource);
	}

	public EbMSDAOImpl(TransactionTemplate transactionTemplate, javax.sql.DataSource dataSource)
	{
		super(transactionTemplate,dataSource);
	}

//	@Override
//	public String getDateFormat()
//	{
//		return "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL";
//	}

	@Override
	public String getTimestampFunction()
	{
		return "SYSDATE";
	}

	@Override
	public String getMessageIdsQuery(String messageContextFilter, EbMSMessageStatus status, int maxNr)
	{
		return "select * from (" +
		"select message_id" +
		" from ebms_message" +
		" where status = " + status.id() +
		messageContextFilter +
		" order by time_stamp asc)" +
		" where ROWNUM <= " + maxNr;
	}

	@Override
	public long insertMessage(final Date timestamp, final EbMSMessage message, final EbMSMessageStatus status) throws DAOException
	{
		try
		{
			return transactionTemplate.execute(
				new TransactionCallback<Long>()
				{
					@Override
					public Long doInTransaction(TransactionStatus arg0)
					{
						try
						{
							KeyHolder keyHolder = new GeneratedKeyHolder();
							jdbcTemplate.update(
								new PreparedStatementCreator()
								{
									
									@Override
									public PreparedStatement createPreparedStatement(Connection connection) throws SQLException
									{
										try
										{
											PreparedStatement ps = connection.prepareStatement
											(
												"insert into ebms_message (" +
													"id," +
													//"creation_time," +
													"cpa_id," +
													"conversation_id," +
													"sequence_nr," +
													"message_id," +
													"time_stamp," +
													"ref_to_message_id," +
													"time_to_live," +
													"from_role," +
													"to_role," +
													"service_type," +
													"service," +
													"action," +
													"signature," +
													"message_header," +
													"sync_reply," +
													"message_order," +
													"ack_requested," +
													"content," +
													"status," +
													"status_time" +
												") values (seq_ebms_message_id.nextval,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," + (status == null ? "null" : getTimestampFunction()) + ")",
												//new String[]{"id"}
												new int[]{1}
											);
											//ps.setDate(1,new java.sql.Date(timestamp.getTime()));
											//ps.setString(1,String.format(getDateFormat(),timestamp));
											//ps.setTimestamp(1,new Timestamp(timestamp.getTime()));
											//ps.setObject(1,timestamp,Types.TIMESTAMP);
											//ps.setObject(1,timestamp);
											MessageHeader messageHeader = message.getMessageHeader();
											ps.setString(1,messageHeader.getCPAId());
											ps.setString(2,messageHeader.getConversationId());
											if (message.getMessageOrder() == null || message.getMessageOrder().getSequenceNumber() == null)
												ps.setNull(3,java.sql.Types.BIGINT);
											else
												ps.setLong(3,message.getMessageOrder().getSequenceNumber().getValue().longValue());
											ps.setString(4,messageHeader.getMessageData().getMessageId());
											ps.setTimestamp(5,new Timestamp(messageHeader.getMessageData().getTimestamp().toGregorianCalendar().getTimeInMillis()));
											ps.setString(6,messageHeader.getMessageData().getRefToMessageId());
											ps.setTimestamp(7,messageHeader.getMessageData().getTimeToLive() == null ? null : new Timestamp(messageHeader.getMessageData().getTimeToLive().toGregorianCalendar().getTimeInMillis()));
											ps.setString(8,messageHeader.getFrom().getRole());
											ps.setString(9,messageHeader.getTo().getRole());
											ps.setString(10,messageHeader.getService().getType());
											ps.setString(11,messageHeader.getService().getValue());
											ps.setString(12,messageHeader.getAction());
											ps.setString(13,XMLMessageBuilder.getInstance(SignatureType.class).handle(new JAXBElement<SignatureType>(new QName("http://www.w3.org/2000/09/xmldsig#","Signature"),SignatureType.class,message.getSignature())));
											ps.setString(14,XMLMessageBuilder.getInstance(MessageHeader.class).handle(messageHeader));
											ps.setString(15,XMLMessageBuilder.getInstance(SyncReply.class).handle(message.getSyncReply()));
											ps.setString(16,XMLMessageBuilder.getInstance(MessageOrder.class).handle(message.getMessageOrder()));
											ps.setString(17,XMLMessageBuilder.getInstance(AckRequested.class).handle(message.getAckRequested()));
											ps.setString(18,getContent(message));
											if (status == null)
												ps.setNull(19,java.sql.Types.INTEGER);
											else
												ps.setInt(19,status.id());
											//ps.setString(20,status == null ? null : String.format(getDateFormat(),timestamp));
											//ps.setTimestamp(20,status == null ? null : new Timestamp(timestamp.getTime()));
											//ps.setObject(20,status == null ? null : timestamp,Types.TIMESTAMP);
											//ps.setObject(20,status == null ? null : timestamp);
											return ps;
										}
										catch (JAXBException e)
										{
											throw new SQLException(e);
										}
									}
								},
								keyHolder
							);
					
							for (EbMSAttachment attachment : message.getAttachments())
							{
								jdbcTemplate.update
								(
									"insert into ebms_attachment (" +
										"ebms_message_id," +
										"name," +
										"content_id," +
										"content_type," +
										"content" +
									") values (?,?,?,?,?)",
									keyHolder.getKey().longValue(),
									attachment.getName(),
									attachment.getContentId(),
									attachment.getContentType().split(";")[0].trim(),
									IOUtils.toByteArray(attachment.getInputStream())
								);
							}
							
							return keyHolder.getKey().longValue();
						}
						catch (Exception e)
						{
							throw new DAOException(e);
						}
					}
				}
			);
		}
		catch (TransactionException e)
		{
			throw new DAOException(e);
		}
	}

}
