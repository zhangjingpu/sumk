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
package org.yx.db.kit;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

import org.yx.exception.SumkException;
import org.yx.util.date.SumkDate;

public class TimeUtil {

	public static boolean isGenericDate(Class<?> type) {
		return type == Date.class || type == java.sql.Date.class || type == Time.class || type == Timestamp.class
				|| type == LocalDate.class || type == LocalTime.class || type == LocalDateTime.class;
	}

	@SuppressWarnings("unchecked")
	public static <T> T toType(Object v, Class<?> type, boolean failIfNotSupport) {
		if (v.getClass() == type) {
			return (T) v;
		}
		if (Date.class.isInstance(v)) {
			return toType((Date) v, type, failIfNotSupport);
		}
		Class<?> sourceClz = v.getClass();
		if (LocalDateTime.class == sourceClz) {
			return toType((LocalDateTime) v, type, failIfNotSupport);
		}

		if (LocalDate.class == sourceClz) {
			return toType((LocalDate) v, type, failIfNotSupport);
		}

		if (LocalTime.class == sourceClz) {
			return toType((LocalTime) v, type, failIfNotSupport);
		}

		if (failIfNotSupport || !isGenericDate(type)) {
			throw new SumkException(1234345, type.getClass().getName() + " cannot convert to " + type.getName());
		}
		return (T) v;
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	private static <T> T toType(Date v, Class<?> type, boolean failIfNotSupport) {
		long time = v.getTime();
		if (Date.class == type) {
			return (T) new Date(time);
		}
		if (java.sql.Date.class == type) {
			if (Time.class == v.getClass()) {
				SumkException.throwException(868927175, "Time cannot convert to java.sql.Date");
			}
			return (T) new java.sql.Date(v.getYear(), v.getMonth(), v.getDate());
		}

		if (Timestamp.class == type) {
			return (T) new Timestamp(time);
		}

		if (Time.class == type) {
			if (java.sql.Date.class == v.getClass()) {
				SumkException.throwException(868927175, "java.sql.Date cannot convert to Time");
			}
			return (T) new Time(v.getHours(), v.getMinutes(), v.getSeconds());
		}
		SumkDate sumk = new SumkDate(v);

		if (LocalDate.class == type) {
			if (Time.class == v.getClass()) {
				SumkException.throwException(868927175, "Time cannot convert to LocalDate");
			}
			return (T) sumk.toLocalDate();
		}

		if (LocalDateTime.class == type) {
			return (T) sumk.toLocalDateTime();
		}

		if (LocalTime.class == type) {
			if (java.sql.Date.class == v.getClass()) {
				SumkException.throwException(868927175, "java.sql.Date cannot convert to LocalTime");
			}
			return (T) sumk.toLocalTime();
		}
		if (failIfNotSupport || !isGenericDate(type)) {
			throw new SumkException(63414353, type.getClass().getName() + "is not supported Date type");
		}
		return (T) v;
	}

	@SuppressWarnings("unchecked")
	private static <T> T toType(LocalDateTime v, Class<?> type, boolean failIfNotSupport) {
		if (Date.class == type) {
			return (T) Date.from(v.atZone(ZoneId.systemDefault()).toInstant());
		}
		if (java.sql.Date.class == type) {
			return (T) java.sql.Date.valueOf(v.toLocalDate());
		}

		if (Timestamp.class == type) {
			return (T) Timestamp.valueOf(v);
		}

		if (Time.class == type) {
			return (T) Time.valueOf(v.toLocalTime());
		}

		if (LocalDate.class == type) {
			return (T) v.toLocalDate();
		}

		if (LocalTime.class == type) {
			return (T) v.toLocalTime();
		}

		if (failIfNotSupport || !isGenericDate(type)) {
			throw new SumkException(868927175, type.getClass().getName() + "is not a supported Date type");
		}
		return (T) v;
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	private static <T> T toType(LocalDate v, Class<?> type, boolean failIfNotSupport) {
		if (Date.class == type) {
			return (T) new Date(v.getYear() - 1900, v.getMonthValue() - 1, v.getDayOfMonth());
		}
		if (java.sql.Date.class == type) {
			return (T) java.sql.Date.valueOf(v);
		}

		if (Timestamp.class == type) {
			LocalDateTime dt = LocalDateTime.of(v, LocalTime.of(0, 0));
			return (T) Timestamp.valueOf(dt);
		}

		if (Time.class == type) {
			SumkException.throwException(868927175, "LocalDate cannot convert to Time");
		}

		if (LocalDateTime.class == type) {
			return (T) LocalDateTime.of(v, LocalTime.of(0, 0));
		}

		if (LocalTime.class == type) {
			SumkException.throwException(868927175, "LocalDate cannot convert to LocalTime");
		}

		if (failIfNotSupport || !isGenericDate(type)) {
			throw new SumkException(868927175, type.getClass().getName() + "is not a supported Date type");
		}
		return (T) v;
	}

	@SuppressWarnings("unchecked")
	private static <T> T toType(LocalTime v, Class<?> type, boolean failIfNotSupport) {
		if (Date.class == type) {
			return (T) new Date(Time.valueOf(v).getTime());
		}
		if (java.sql.Date.class == type) {
			SumkException.throwException(868927175, "LocalTime cannot convert to java.sql.Date");
		}

		if (Timestamp.class == type) {
			return (T) new Timestamp(Time.valueOf(v).getTime());
		}

		if (Time.class == type) {
			return (T) Time.valueOf(v);
		}

		if (LocalDateTime.class == type) {
			return (T) LocalDateTime.of(LocalDate.ofEpochDay(0), v);
		}

		if (LocalDate.class == type) {
			SumkException.throwException(868927175, "LocalTime cannot convert to LocalDate");
		}

		if (failIfNotSupport || !isGenericDate(type)) {
			throw new SumkException(868927175, type.getClass().getName() + "is not a supported Date type");
		}
		return (T) v;
	}
}
