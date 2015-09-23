/*
 * Copyright 2015 Avanza Bank AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.avanza.carbon.java.relay.pickle;

import java.util.List;

import com.avanza.carbon.java.relay.MetricTuple;

public class Pickler {
	/**
	 * Minimally necessary pickle opcodes.
	 */
	private static final char MARK = '(', STOP = '.', LONG = 'L', STRING = 'S', APPEND = 'a', LIST = 'l', TUPLE = 't';

	public static String convertMetricsToGraphitePickleMetricsFormat(List<MetricTuple> metrics) {

		StringBuilder pickled = new StringBuilder();
		pickled.append(MARK);
		pickled.append(LIST);

		for (MetricTuple tuple : metrics) {
			// start the outer tuple
			pickled.append(MARK);

			// the metric name is a string.
			pickled.append(STRING);
			// the single quotes are to match python's repr("abcd")
			pickled.append('\'');
			pickled.append(tuple.getName());
			pickled.append('\'');
			pickled.append('\n');

			// start the inner tuple
			pickled.append(MARK);

			// timestamp is a long
			pickled.append(LONG);
			pickled.append(tuple.getTimestamp());
			// the trailing L is to match python's repr(long(1234))
			pickled.append('L');
			pickled.append('\n');

			pickled.append(STRING);
			pickled.append('\'');
			pickled.append(tuple.getValue().toString());
			pickled.append('\'');
			pickled.append('\n');

			pickled.append(TUPLE); // inner close
			pickled.append(TUPLE); // outer close

			pickled.append(APPEND);
		}

		// every pickle ends with STOP
		pickled.append(STOP);
		return pickled.toString();
	}

}
