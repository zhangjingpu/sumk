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
package org.yx.db.sql.token;

import java.util.HashMap;
import java.util.Map;

/**
 * 大小写不敏感
 * 
 * @author 游夏
 *
 */
public class ReplaceTokenHandler implements StringTokenParser.TokenHandler {
	private Map<String, Object> variables;

	public ReplaceTokenHandler(Map<String, Object> map) {
		this.variables = new HashMap<>();
		if (map == null) {
			return;
		}
		map.forEach((k, v) -> {
			if (k == null) {
				return;
			}
			this.variables.put(k.toLowerCase(), v);
		});
	}

	@Override
	public String handleToken(String content) {
		content = content.toLowerCase();
		if (variables != null && variables.containsKey(content)) {
			Object v = variables.get(content.toLowerCase());
			if (v == null) {
				return null;
			}
			return v.toString();
		}
		return null;
	}
}