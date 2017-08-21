/**
 * Copyright (C) 2016 - 2017 youtongluan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.yx.bean;

public final class InnerIOC {
	final static BeanPool pool = new BeanPool();

	public static <T> T putClass(String name, Class<T> clz) throws Exception {
		return pool.putClass(name, clz);
	}

	public static <T> T putClassByInterface(Class<?> intf, Class<T> clz) throws Exception {
		return pool.putClass(BeanPool.getBeanName(intf), clz);
	}

	public static <T> T getOrCreate(Class<T> clz) throws Exception {
		T obj = IOC.get(clz);
		if (obj != null) {
			return obj;
		}
		return putClass(null, clz);
	}

}
