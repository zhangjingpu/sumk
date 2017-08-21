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
package org.yx.redis;

import java.io.InputStream;
import java.util.function.Consumer;

import org.yx.conf.MultiResourceLoader;
import org.yx.conf.SingleResourceLoader;
import org.yx.log.Log;

public class RedisPropertiesLoader implements SingleResourceLoader {

	@Override
	public InputStream openInput(String fileName) throws Exception {
		InputStream in = RedisLoader.class.getClassLoader().getResourceAsStream(fileName);
		if (in != null) {
			return in;
		}
		Log.get("sumk.redis").info("can not found redis property file:{}", fileName);
		return null;
	}

	@Override
	public boolean startListen(Consumer<MultiResourceLoader> consumer) {
		return false;
	}

}
