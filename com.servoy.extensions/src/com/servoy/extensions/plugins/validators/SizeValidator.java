/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2010 Servoy BV

 This program is free software; you can redistribute it and/or modify it under
 the terms of the GNU Affero General Public License as published by the Free
 Software Foundation; either version 3 of the License, or (at your option) any
 later version.

 This program is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License along
 with this program; if not, see http://www.gnu.org/licenses or write to the Free
 Software Foundation,Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
*/
package com.servoy.extensions.plugins.validators;

import java.util.HashMap;
import java.util.Map;
import com.servoy.j2db.dataprocessing.IColumnValidator;
import com.servoy.j2db.persistence.IColumnTypes;
import com.servoy.j2db.util.Utils;

public class SizeValidator implements IColumnValidator
{
	private static final String LENGTH_PROPERTY = "length";

	public Map getDefaultProperties()
	{
		Map props = new HashMap();
		props.put(LENGTH_PROPERTY, "");
		return props;
	}

	public String getName()
	{
		return "servoy.SizeValidator";
	}

	public int[] getSupportedColumnTypes()
	{
		return new int[]{IColumnTypes.TEXT,IColumnTypes.MEDIA};
	}

	public void validate(Map props, Object arg) throws IllegalArgumentException
	{
		int length = Utils.getAsInteger(props.get(LENGTH_PROPERTY));
		if (arg instanceof byte[] && ((byte[])arg).length > length)
		{
			throw new IllegalArgumentException();
		}
		else if (arg instanceof String && ((String)arg).length() > length)
		{
			throw new IllegalArgumentException();
		}
	}
}