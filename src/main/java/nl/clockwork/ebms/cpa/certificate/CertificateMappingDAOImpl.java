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

import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
class CertificateMappingDAOImpl implements CertificateMappingDAO
{
	@NonNull
	JdbcTemplate jdbcTemplate;

	@Override
	@Cacheable(cacheNames = "CertificateMapping", keyGenerator = "ebMSKeyGenerator")
	public boolean existsCertificateMapping(String id, String cpaId)
	{
//		cpaId = ESAPI.encoder().encodeForSQL(new OracleCodec(),cpaId);
		return jdbcTemplate.queryForObject(
				"select count(*) from certificate_mapping where id = ?" +
						(cpaId == null ? " and cpa_id is null" : " and cpa_id = '" + cpaId + "'"),
				Integer.class,
				id) > 0;
	}

	@Override
	@Cacheable(cacheNames = "CertificateMapping", keyGenerator = "ebMSKeyGenerator")
	public Optional<X509Certificate> getCertificateMapping(String id, String cpaId, boolean getSpecific)
	{
//		cpaId = ESAPI.encoder().encodeForSQL(new OracleCodec(),cpaId);
		try
		{
			val result = jdbcTemplate.query(
				"select destination, cpa_id from certificate_mapping where id = ?" +
						(cpaId == null ? " and cpa_id is null" : (getSpecific ? " and cpa_id = '" + cpaId + "'" : " and (cpa_id = '" + cpaId + "' or cpa_id is null)")),
				new RowMapper<Tuple2<X509Certificate,String>>()
				{
					@Override
					public Tuple2<X509Certificate,String> mapRow(ResultSet rs, int rowNum) throws SQLException
					{
						try
						{
							CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
							return Tuple.of((X509Certificate)certificateFactory.generateCertificate(rs.getBinaryStream("source")),rs.getString("cpa_id"));
						}
						catch (CertificateException e)
						{
							throw new SQLException(e);
						}
					}
				},
				id);
			if (result.size() == 0)
				return Optional.empty();
			else if (result.size() == 1)
				return Optional.of(result.get(0)._1);
			else
				return result.stream().filter(r -> r._2 != null).findFirst().map(r -> r._1);
		}
		catch(EmptyResultDataAccessException e)
		{
			return Optional.empty();
		}
	}

	@Override
	@Cacheable(cacheNames = "CertificateMapping", keyGenerator = "ebMSKeyGenerator")
	public List<CertificateMapping> getCertificateMappings()
	{
		return jdbcTemplate.query(
				"select source, destination, cpa_id from certificate_mapping",
				new RowMapper<CertificateMapping>()
				{
					@Override
					public CertificateMapping mapRow(ResultSet rs, int nr) throws SQLException
					{
						try
						{
							val certificateFactory = CertificateFactory.getInstance("X509");
							val source = (X509Certificate)certificateFactory.generateCertificate(rs.getBinaryStream("source"));
							val destination = (X509Certificate)certificateFactory.generateCertificate(rs.getBinaryStream("destination"));
							val cpaId = rs.getString("cpa_id");
							return new CertificateMapping(source,destination,cpaId);
						}
						catch (CertificateException e)
						{
							throw new SQLException(e);
						}
					}
				});
	}

	@Override
	@CacheEvict(cacheNames = "CertificateMapping", allEntries = true)
	public void insertCertificateMapping(CertificateMapping mapping)
	{
		try
		{
			jdbcTemplate.update(
				"insert into certificate_mapping (id,source,destination,cpa_id) values (?,?,?,?)",
				mapping.getId(),
				mapping.getSource().getEncoded(),
				mapping.getDestination().getEncoded(),
				mapping.getCpaId());
		}
		catch (CertificateEncodingException e)
		{
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	@CacheEvict(cacheNames = "CertificateMapping", allEntries = true)
	public int updateCertificateMapping(CertificateMapping mapping)
	{
		val cpaId = mapping.getCpaId();//ESAPI.encoder().encodeForSQL(new OracleCodec(),mapping.getCpaId());
		try
		{
			return jdbcTemplate.update(
				"update certificate_mapping set destination = ? where id = ?" +
						(cpaId == null ? " and cpa_id is null" : " and cpa_id = '" + cpaId + "'"),
				mapping.getDestination().getEncoded(),
				mapping.getId()
			);
		}
		catch (CertificateEncodingException e)
		{
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	@CacheEvict(cacheNames = "CertificateMapping", allEntries = true)
	public int deleteCertificateMapping(String id, String cpaId)
	{
//		cpaId = ESAPI.encoder().encodeForSQL(new OracleCodec(),cpaId);
		return jdbcTemplate.update(
			"delete from certificate_mapping where id = ?" +
					(cpaId == null ? " and cpa_id is null" : " and cpa_id = '" + cpaId + "'"),
			id);
	}
}
