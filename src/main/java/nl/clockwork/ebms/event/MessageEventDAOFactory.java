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
package nl.clockwork.ebms.event;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import nl.clockwork.ebms.dao.AbstractDAOFactory;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class MessageEventDAOFactory extends AbstractDAOFactory<MessageEventDAO>
{
	private static class DB2EbMSMessageEventDAO extends MessageEventDAOImpl
	{
		public DB2EbMSMessageEventDAO(@NonNull JdbcTemplate jdbcTemplate)
		{
			super(jdbcTemplate);
		}
	}
	private static class H2MessageEventDAO extends MessageEventDAOImpl
	{
		public H2MessageEventDAO(@NonNull JdbcTemplate jdbcTemplate)
		{
			super(jdbcTemplate);
		}
	}
	private static class HSQLDBMessageEventDAO extends MessageEventDAOImpl
	{
		public HSQLDBMessageEventDAO(@NonNull JdbcTemplate jdbcTemplate)
		{
			super(jdbcTemplate);
		}
	}
	private static class MSSQLMessageEventDAO extends MessageEventDAOImpl
	{
		public MSSQLMessageEventDAO(@NonNull JdbcTemplate jdbcTemplate)
		{
			super(jdbcTemplate);
		}
	}
	private static class MySQLMessageEventDAO extends MessageEventDAOImpl
	{
		public MySQLMessageEventDAO(@NonNull JdbcTemplate jdbcTemplate)
		{
			super(jdbcTemplate);
		}
	}
	private static class OracleMessageEventDAO extends MessageEventDAOImpl
	{
		public OracleMessageEventDAO(@NonNull JdbcTemplate jdbcTemplate)
		{
			super(jdbcTemplate);
		}
	}
	private static class PostgreSQLMessageEventDAO extends MessageEventDAOImpl
	{
		public PostgreSQLMessageEventDAO(@NonNull JdbcTemplate jdbcTemplate)
		{
			super(jdbcTemplate);
		}
	}
	@NonNull
	JdbcTemplate jdbcTemplate;

	public MessageEventDAOFactory(DataSource dataSource,	@NonNull JdbcTemplate jdbcTemplate)
	{
		super(dataSource);
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Class<MessageEventDAO> getObjectType()
	{
		return MessageEventDAO.class;
	}

	@Override
	public MessageEventDAO createDB2DAO()
	{
		return new DB2EbMSMessageEventDAO(jdbcTemplate);
	}

	@Override
	public MessageEventDAO createH2DAO()
	{
		return new H2MessageEventDAO(jdbcTemplate);
	}

	@Override
	public MessageEventDAO createHSQLDBDAO()
	{
		return new HSQLDBMessageEventDAO(jdbcTemplate);
	}

	@Override
	public MessageEventDAO createMSSQLDAO()
	{
		return new MSSQLMessageEventDAO(jdbcTemplate);
	}

	@Override
	public MessageEventDAO createMySQLDAO()
	{
		return new MySQLMessageEventDAO(jdbcTemplate);
	}

	@Override
	public MessageEventDAO createOracleDAO()
	{
		return new OracleMessageEventDAO(jdbcTemplate);
	}

	@Override
	public MessageEventDAO createPostgreSQLDAO()
	{
		return new PostgreSQLMessageEventDAO(jdbcTemplate);
	}
}
