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
package org.yx.conf;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.yx.db.conn.DataSourceWraper;
import org.yx.log.Log;
import org.yx.util.Assert;
import org.yx.util.SimpleBeanUtil;
import org.yx.util.secury.AESEncry;
import org.yx.util.secury.Base64Util;

public class DBConfig {

	String type = "";
	int weight = 0;
	int read_weight = 0;
	Map<String, String> properties;

	public DBConfig() {
		properties = new HashMap<>();
		properties.put("driverClassName", "com.mysql.jdbc.Driver");

		properties.put("maxTotal", "30");
		properties.put("minIdle", "2");
		properties.put("maxIdle", "10");
		properties.put("maxWaitMillis", "10000");
		properties.put("testOnBorrow", "false");
		properties.put("testOnReturn", "false");
		properties.put("testWhileIdle", "true");
		properties.put("removeAbandonedOnBorrow", "false");

		properties.put("removeAbandonedOnMaintenance", "true");

		properties.put("removeAbandonedTimeout", "30");
		properties.put("logAbandoned", "true");
		properties.put("timeBetweenEvictionRunsMillis", "30000");
		properties.put("softMinEvictableIdleTimeMillis", "60000");

		properties.put("logExpiredConnections", "false");
		properties.put("poolPreparedStatements", "false");
		properties.put("defaultAutoCommit", "false");

	}

	public String getProperty(String name) {
		return this.properties.get(name);
	}

	public void setProperties(Map<String, String> p) throws Exception {
		Set<String> set = p.keySet();
		for (String key : set) {
			String v = p.get(key);
			if (v == null) {
				Log.get("sumk.db.config").debug("{} key的值是null，被忽略掉", key);
				continue;
			}
			switch (key) {
			case "type":
				this.type = v.toLowerCase();
				break;
			case "weight":
				this.weight = Integer.parseInt(v);
				break;
			case "password":

				if (AppInfo.getBoolean("sumk.db.password.encry", false)) {
					byte[] bs = Base64Util.decode(v.getBytes());
					v = new String(new AESEncry().decrypt(bs, new byte[] { 121, 111, 117, 116, 111, 110, 103, 108, 117,
							97, 110, 64, 115, 117, 109, 107 }));
				}
				properties.put(key, v);
				break;
			case "read_weight":
				this.read_weight = Integer.parseInt(v);
				break;
			default:
				properties.put(key, v);
				break;
			}

		}
	}

	public DataSourceWraper createDS(String name) throws Exception {
		Assert.isTrue(this.valid(), "url,username,password,type should not be null");
		Assert.isTrue(type.matches("^(wr|read|write)$"), "db type should be one of(wr,read,write)");
		DataSourceWraper ds = new DataSourceWraper(name, type);
		SimpleBeanUtil.copyProperties(ds, this.properties);
		return ds;
	}

	public boolean valid() {
		return this.type != null && properties.get("url") != null && properties.get("username") != null
				&& properties.get("password") != null;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public int getRead_weight() {
		return read_weight;
	}

	public void setRead_weight(int read_weight) {
		this.read_weight = read_weight;
	}

	@Override
	public String toString() {
		return "DBConfig [type=" + type + ", weight=" + weight + ", read_weight=" + read_weight + ", properties="
				+ properties + "]";
	}

}
