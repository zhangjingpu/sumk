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
package org.yx.http.handler;

import java.lang.reflect.Method;

import org.yx.annotation.ErrorHandler;
import org.yx.asm.ArgPojo;
import org.yx.common.CalleeNode;
import org.yx.http.HttpSettings;
import org.yx.http.Upload;
import org.yx.http.Web;
import org.yx.validate.Param;

public final class HttpNode extends CalleeNode {

	public final Web action;
	public final Upload upload;
	private final String[] types;

	public final ErrorHandler errorHandler;

	public boolean acceptType(String type) {
		if (type == null) {
			type = "";
		}
		if (types == null) {
			return type.isEmpty();
		}
		for (String t : types) {
			if (type.equals(t)) {
				return true;
			}
		}
		return false;
	}

	public HttpNode(Object obj, Method proxyMethod, Class<? extends ArgPojo> argClz, String[] argNames,
			Class<?>[] argTypes, Param[] params, Method m) {
		super(obj, proxyMethod, argClz, argNames, argTypes, params);
		this.action = m.getAnnotation(Web.class);
		if (HttpSettings.isUploadEnable()) {
			this.upload = m.getAnnotation(Upload.class);
		} else {
			this.upload = null;
		}
		this.errorHandler = m.getAnnotation(ErrorHandler.class);

		String[] _types = action != null ? action.type() : upload.type();
		this.types = _types.length == 0 || (_types.length == 1 && _types[0].isEmpty()) ? null : _types;
	}

}
