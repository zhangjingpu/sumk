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
package org.yx.db.conn;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.yx.db.DBType;
import org.yx.exception.SumkException;
import org.yx.log.Log;
import org.yx.util.Assert;

public class ConnectionFactory {

	private static Map<String, ConnectionFactory> factoryMap = new ConcurrentHashMap<>();

	private WeightedDataSourceRoute read;
	private WeightedDataSourceRoute write;

	private String db;

	private ConnectionFactory(String dbName) {
		this.db = dbName;
	}

	public Map<String, Map<String, Integer>> status() {
		Set<DataSource> set = new HashSet<>();
		set.addAll(this.read.allDataSource());
		set.addAll(this.write.allDataSource());
		Map<String, Map<String, Integer>> statusMap = new HashMap<>();
		for (DataSource datasource : set) {
			if (!BasicDataSource.class.isInstance(datasource)) {
				Log.get(this.getClass(), 25345).info("ds.class({}) is not instance form BasicDataSource",
						datasource.getClass().getName());
				continue;
			}
			@SuppressWarnings("resource")
			BasicDataSource ds = (BasicDataSource) datasource;
			Map<String, Integer> map = new HashMap<>();
			map.put("active", ds.getNumActive());
			map.put("idle", ds.getNumIdle());
			map.put("minIdle", ds.getMinIdle());
			map.put("maxIdle", ds.getMaxIdle());
			map.put("maxTotal", ds.getMaxTotal());
			statusMap.put(ds.toString(), map);
		}
		return statusMap;
	}

	public static ConnectionFactory get(String dbName) {
		try {
			Assert.hasText(dbName, "db name can not be empty");
			dbName = dbName.trim();
			ConnectionFactory factory = factoryMap.get(dbName);
			if (factory != null) {
				return factory;
			}
			synchronized (ConnectionFactory.class) {
				factory = factoryMap.get(dbName);
				if (factory != null) {
					return factory;
				}
				factory = new ConnectionFactory(dbName);
				factory.parseDatasource();
				factoryMap.put(dbName, factory);
			}
			Log.get("sumk.db").debug("{}", factory);
			return factory;
		} catch (Exception e) {
			Log.printStack(e);
			SumkException.throwException(100234325, "create factory failed");
			return null;
		}
	}

	void destroy() {

	}

	public static void reload(String dbName) throws Exception {
		Assert.hasText(dbName, "db name can not be empty");
		dbName = dbName.trim();
		ConnectionFactory factory = factoryMap.get(dbName);
		if (factory == null) {
			return;
		}
		factory = new ConnectionFactory(dbName);
		ConnectionFactory old = factoryMap.put(dbName, factory);
		old.destroy();
	}

	private void parseDatasource() throws Exception {
		if (this.write != null || this.read != null) {
			Log.get(this.getClass(), 34534543).info("{} has init datasource", this.db);
			return;
		}
		Map<DBType, WeightedDataSourceRoute> map = DataSourceFactory.create(this.db);
		this.write = map.get(DBType.WRITE);
		this.read = map.get(DBType.READ);
	}

	public Connection getConnection(DBType type, Connection writeConn) {
		if (!type.isWritable()) {
			try {
				DataSource ds = this.read.datasource();
				if (writeConn != null && DataSourceWraper.class.isInstance(ds)) {
					@SuppressWarnings("resource")
					DataSourceWraper wrapper = (DataSourceWraper) ds;

					wrapper.readConnection(writeConn);
				}
				return ds.getConnection();
			} catch (SQLException e) {
				SumkException.throwException(100001, "获取" + db + "读连接失败", e);
			}
		}
		try {
			return this.write.datasource().getConnection();
		} catch (SQLException e) {
			SumkException.throwException(100001, "获取" + db + "写连接失败", e);
		}
		return null;
	}

	@Override
	public String toString() {
		return "datasource[" + db + "] write=" + write + " ,read=" + read;
	}

}
