/***********************************************************************************
 * SakaiPersonManagerStub.java
 * Copyright (c) 2008 Sakai Project/Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.sms.logic.stubs;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.api.common.type.Type;

public class SakaiPersonManagerStub implements SakaiPersonManager {

	public SakaiPerson create(String arg0, Type arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public void delete(SakaiPerson arg0) {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("unchecked")
	public List findAllFerpaEnabled() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	public List findSakaiPerson(SakaiPerson arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@SuppressWarnings("unchecked")
	public List findSakaiPerson(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	public List findSakaiPersonByUid(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public SakaiPerson getPrototype() {
		// TODO Auto-generated method stub
		return null;
	}

	public SakaiPerson getSakaiPerson(Type arg0) {
		SakaiPersonStub sakaipersion = new SakaiPersonStub();

		return sakaipersion;
	}

	public SakaiPerson getSakaiPerson(String arg0, Type arg1) {
		if (arg0 == null) {
			return null;
		}
		return new SakaiPersonStub();
	}

	public Map<String, SakaiPerson> getSakaiPersons(Set<String> arg0, Type arg1) {
		Iterator<String> it = arg0.iterator();
        Map<String, SakaiPerson> ret = new HashMap<String, SakaiPerson>();
		while (it.hasNext()) {
			String val = it.next();
			ret.put(val, getSakaiPerson(val, arg1));
		}
		
		return ret;
	}

	public Type getSystemMutableType() {
		// TODO Auto-generated method stub
		return null;
	}

	public Type getUserMutableType() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	public List isFerpaEnabled(Collection arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void save(SakaiPerson arg0) {
		// TODO Auto-generated method stub

	}
}