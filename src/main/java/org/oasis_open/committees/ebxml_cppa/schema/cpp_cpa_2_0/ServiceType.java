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
package org.oasis_open.committees.ebxml_cppa.schema.cpp_cpa_2_0;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import java.io.Serializable;

/**
 * <p>
 * Java class for service.type complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="service.type">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.oasis-open.org/committees/ebxml-cppa/schema/cpp-cpa-2_0.xsd>non-empty-string">
 *       &lt;attribute name="type" type="{http://www.oasis-open.org/committees/ebxml-cppa/schema/cpp-cpa-2_0.xsd}non-empty-string" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "service.type", propOrder = {"value"})
public class ServiceType implements Serializable
{

	private final static long serialVersionUID = 1L;
	@XmlValue
	protected String value;
	@XmlAttribute(name = "type", namespace = "http://www.oasis-open.org/committees/ebxml-cppa/schema/cpp-cpa-2_0.xsd")
	protected String type;

	/**
	 * Gets the value of the value property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 * Sets the value of the value property.
	 * 
	 * @param value allowed object is {@link String }
	 */
	public void setValue(String value)
	{
		this.value = value;
	}

	/**
	 * Gets the value of the type property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * Sets the value of the type property.
	 * 
	 * @param value allowed object is {@link String }
	 */
	public void setType(String value)
	{
		this.type = value;
	}

}
