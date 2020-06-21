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
package nl.clockwork.ebms.event.processor;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.querydsl.sql.SQLQueryFactory;

import lombok.AccessLevel;
import lombok.val;
import lombok.experimental.FieldDefaults;
import nl.clockwork.ebms.cpa.CPAManager;
import nl.clockwork.ebms.dao.EbMSDAO;
import nl.clockwork.ebms.event.processor.EventProcessorConfig.EventProcessorType;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventManagerConfig
{
	@Value("${eventProcessor.type}")
	EventProcessorType eventProcessorType;
	@Autowired
	CPAManager cpaManager;
	@Value("${ebms.serverId}")
	String serverId;
	@Autowired()
	JmsTemplate jmsTemplate;
	@Value("${ebmsMessage.nrAutoRetries}")
	int nrAutoRetries;
	@Value("${ebmsMessage.autoRetryInterval}")
	int autoRetryInterval;
	@Autowired
	EbMSDAO ebMSDAO;
	@Autowired
	@Qualifier("dataSourceTransactionManager")
	PlatformTransactionManager dataSourceTransactionManager;
	@Autowired
	DataSource dataSource;
	@Autowired
	SQLQueryFactory queryFactory;

	@Bean()
	public EventManager eventManager() throws Exception
	{
		switch(eventProcessorType)
		{
			case NONE:
				return null;
			case JMS:
				return new JMSEventManager(jmsTemplate,ebMSDAO,ebMSEventDAO(),cpaManager,nrAutoRetries,autoRetryInterval);
			default:
				return new EbMSEventManager(ebMSDAO,ebMSEventDAO(),cpaManager,serverId,nrAutoRetries,autoRetryInterval);
		}
	}

	@Bean
	public EbMSEventDAO ebMSEventDAO() throws Exception
	{
		val transactionTemplate = new TransactionTemplate(dataSourceTransactionManager);
		val jdbcTemplate = new JdbcTemplate(dataSource);
		return new EbMSEventDAOImpl(transactionTemplate,jdbcTemplate,queryFactory);
	}
}