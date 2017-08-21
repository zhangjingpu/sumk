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
package org.yx.db.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.yx.db.visit.SumkDbVisitor;
import org.yx.exception.SumkException;
import org.yx.util.CollectionUtil;
import org.yx.util.StringUtil;

public class SelectBuilder extends AbstractSqlBuilder<List<Map<String, Object>>> {

	public SelectBuilder(SumkDbVisitor<List<Map<String, Object>>> visitor) {
		super(visitor);
		this.fromCache = OrmSettings.FROM_CACHE;
		this.toCache = OrmSettings.TO_CACHE;
		this.withnull = false;
	}

	protected static final String[] COMPARES = { ">", ">=", "<", "<=" };

	List<String> selectColumns;

	protected Map<String, Object>[] _compare;

	List<Order> orderby;

	int offset;

	int limit;

	boolean fromCache;
	boolean toCache;

	protected boolean allowEmptyWhere;

	@Override
	public MapedSql toMapedSql() throws Exception {
		List<Object> paramters = new ArrayList<>(10);
		this.pojoMeta = parsePojoMeta(true);
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ").append(this.buildField()).append(" FROM ").append(this.pojoMeta.getTableName());
		CharSequence where = this.buildWhere(paramters);
		if (StringUtil.isEmpty(where) && !this.allowEmptyWhere) {
			SumkException.throwException(63254325, "empty where");
		}
		if (StringUtil.isNotEmpty(where)) {
			sql.append(" WHERE ").append(where);
		}
		CharSequence order = buildOrder();
		if (StringUtil.isNotEmpty(order)) {
			sql.append(" ORDER BY ").append(order);
		}
		if (this.offset >= 0 && this.limit > 0) {
			sql.append(" LIMIT ").append(this.offset).append(',').append(this.limit);
		}
		return new MapedSql(sql.toString(), paramters);
	}

	protected CharSequence buildOrder() {
		if (CollectionUtil.isEmpty(this.orderby)) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (Order order : this.orderby) {
			if (sb.length() > 0) {
				sb.append(',');
			}
			sb.append(order.toString(this.pojoMeta));
		}
		return sb;
	}

	protected CharSequence buildField() {
		if (this.selectColumns != null && this.selectColumns.size() > 0) {
			return String.join(",", this.selectColumns.toArray(new String[this.selectColumns.size()]));
		}
		StringJoiner sj = new StringJoiner(",");
		for (ColumnMeta cm : this.pojoMeta.fieldMetas) {
			sj.add(cm.dbColumn);
		}
		return sj.toString();
	}

	protected CharSequence buildWhere(List<Object> paramters) {
		ItemJoiner joiner = new ItemJoiner(" AND ", "", "");
		joiner.addNotEmptyItem(buildValid(paramters)).addNotEmptyItem(buildIn(paramters))
				.addNotEmptyItem(buildCompare(paramters));
		return joiner.toCharSequence();
	}

	private CharSequence buildValid(List<Object> paramters) {
		SoftDeleteMeta sm = this.pojoMeta.softDelete;
		if (sm == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(sm.columnName).append(" =? ");
		paramters.add(sm.validValue);
		return sb;
	}

	private CharSequence buildCompare(List<Object> paramters) {
		if (this._compare == null) {
			return null;
		}
		ItemJoiner joiner = ItemJoiner.create();
		for (int i = 0; i < COMPARES.length && i < this._compare.length; i++) {
			Map<String, Object> map = this._compare[i];
			CharSequence sub = this.parseMap(map, COMPARES[i], paramters);
			if (sub == null) {
				continue;
			}
			joiner.item().append(sub);
		}
		return joiner.toCharSequence();
	}

	private CharSequence buildIn(List<Object> paramters) {
		if (this.in == null || this.in.isEmpty()) {
			return null;
		}
		ItemJoiner joiner = ItemJoiner.create(" OR ");
		List<Map<String, Object>> list = this.in;
		for (Map<String, Object> map : list) {
			CharSequence sub = this.parseEqual(map, paramters);
			if (sub == null) {
				continue;
			}
			joiner.item().append(sub);
		}
		return joiner.toCharSequence();
	}

	private CharSequence parseMap(Map<String, Object> src, String compare, List<Object> paramters) {
		if (CollectionUtil.isEmpty(src)) {
			return null;
		}

		ItemJoiner joiner = ItemJoiner.create();
		src.forEach((filedName, v) -> {
			ColumnMeta cm = pojoMeta.getByFieldName(filedName);
			if (v == null) {
				return;
			}
			if (cm == null) {
				if (this.failIfPropertyNotMapped) {
					SumkException.throwException(234, filedName + " has no mapper");
				}
				return;
			}
			joiner.item().append(cm.dbColumn).append(compare).append(" ? ");
			paramters.add(v);
		});

		return joiner.toCharSequence();
	}

	private CharSequence parseEqual(Map<String, Object> src, List<Object> paramters) {
		if (CollectionUtil.isEmpty(src)) {
			return null;
		}
		ItemJoiner joiner = ItemJoiner.create();
		src.forEach((filedName, v) -> {

			if (v == null && !this.withnull) {
				return;
			}
			ColumnMeta cm = pojoMeta.getByFieldName(filedName);
			if (cm == null) {
				if (this.failIfPropertyNotMapped) {
					SumkException.throwException(234, filedName + " has no mapper");
				}
				return;
			}
			if (v == null) {
				joiner.item().append(cm.dbColumn).append(" IS NULL ");
				return;
			}
			joiner.item().append(cm.dbColumn).append("=? ");
			paramters.add(v);
		});

		return joiner.toCharSequence();
	}

	protected static class Order {

		String name;

		boolean desc;

		public String toString(PojoMeta pm) {
			ColumnMeta cm = pm.getByFieldName(name);
			String dbName = cm == null ? name : cm.dbColumn;
			if (desc) {
				return dbName + " desc";
			}
			return dbName;
		}

	}

}
