/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core.logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.pentaho.di.core.Const;


/**
 * This class keeps the last N lines in a buffer
 *
 * @author matt
 */
public class LoggingBuffer {
	private String name;

	private Map<String, List<BufferLine>> buffer;

	// private ReadWriteLock lock = new ReentrantReadWriteLock();

	private int bufferSize;

	private KettleLogLayout layout;

	private List<KettleLoggingEventListener> eventListeners;

	private LoggingRegistry loggingRegistry = LoggingRegistry.getInstance();

	public LoggingBuffer(int bufferSize) {
		this.bufferSize = bufferSize;
		// The buffer overflow protection allows it to be overflowed for 1 item
		// within a single thread.
		// Considering a possible high contention, let's set it's max overflow
		// size to be 10%.
		// Anyway, even an overflow goes higher than 10%, it wouldn't cost us
		// too much.
		buffer = new ConcurrentHashMap<String, List<BufferLine>>();
		layout = new KettleLogLayout(true);
		eventListeners = new CopyOnWriteArrayList<>();
	}

	/**
	 * @return the number (sequence, 1..N) of the last log line. If no records
	 *         are present in the buffer, 0 is returned.
	 */
	public int getLastBufferLineNr() {
		return 0;
	}

	/**
	 * @param channelId
	 *            channel IDs to grab
	 * @param includeGeneral
	 *            include general log lines
	 * @param from
	 * @param to
	 * @return
	 */
	public List<KettleLoggingEvent> getLogBufferFromTo(List<String> channelId,
			boolean includeGeneral, int from, int to) {
		List<KettleLoggingEvent> loggingEvents = new ArrayList<KettleLoggingEvent>();
		for (String chnl : channelId) {
			//[2022-04-27 liqiulin] fix ConncurrentModificationException
		    //[2022-06-28 liqiulin] fix NPE in ImmutableList.copy
		    List<BufferLine> logList = buffer.get(chnl);
		    if (logList != null) {
		        List<BufferLine> lineList = new ArrayList<BufferLine>();
		        lineList.addAll(logList);
		        if ( lineList.size() > 0 ) {
		            loggingEvents.addAll(lineList.stream().filter(bl -> bl != null)
						.map(BufferLine::getEvent).collect(Collectors.toList()));
		        }
		    }
		}
		return loggingEvents.stream().sorted(Comparator.comparing(KettleLoggingEvent::getTimeStamp))
				.collect(Collectors.toList());
	}

	/**
	 * @param parentLogChannelId
	 *            the parent log channel ID to grab
	 * @param includeGeneral
	 *            include general log lines
	 * @param from
	 * @param to
	 * @return
	 */
	public List<KettleLoggingEvent> getLogBufferFromTo(String parentLogChannelId,
			boolean includeGeneral, int from, int to) {

		// Typically, the log channel id is the one from the transformation or
		// job running currently.
		// However, we also want to see the details of the steps etc.
		// So we need to look at the parents all the way up if needed...
		//
		List<String> childIds = loggingRegistry.getLogChannelChildren(parentLogChannelId);

		return getLogBufferFromTo(childIds, includeGeneral, from, to);
	}

	public StringBuffer getBuffer(String parentLogChannelId, boolean includeGeneral,
			int startLineNr, int endLineNr) {
		StringBuilder eventBuffer = new StringBuilder(10000);

		List<KettleLoggingEvent> events = getLogBufferFromTo(parentLogChannelId, includeGeneral,
				startLineNr, endLineNr);
		for (KettleLoggingEvent event : events) {
			eventBuffer.append(layout.format(event)).append(Const.CR);
		}

		return new StringBuffer(eventBuffer);
	}

	public StringBuffer getBuffer(String parentLogChannelId, boolean includeGeneral) {
		return getBuffer(parentLogChannelId, includeGeneral, 0);
	}

	public StringBuffer getBuffer(String parentLogChannelId, boolean includeGeneral,
			int startLineNr) {
		return getBuffer(parentLogChannelId, includeGeneral, startLineNr, getLastBufferLineNr());
	}

	public StringBuffer getBuffer() {
		return getBuffer(null, true);
	}

	public void close() {
	}

	public void doAppend(KettleLoggingEvent event) {
		if (event.getMessage() instanceof LogMessage) {
			String chnl = getLogChId(event);
			if (buffer.containsKey(chnl)) {
				buffer.get(chnl).add(new BufferLine(event));
			} else {
				buffer.put(chnl, new ArrayList<BufferLine>(Arrays.asList(new BufferLine(event))));
			}
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setLayout(KettleLogLayout layout) {
		this.layout = layout;
	}

	public KettleLogLayout getLayout() {
		return layout;
	}

	public boolean requiresLayout() {
		return true;
	}

	public void clear() {
		buffer.clear();
	}

	/**
	 * @return the maximum number of lines that this buffer contains, 0 or lower
	 *         means: no limit
	 */
	public int getMaxNrLines() {
		return bufferSize;
	}

	/**
	 * @param maxNrLines
	 *            the maximum number of lines that this buffer should contain, 0
	 *            or lower means: no limit
	 */
	public void setMaxNrLines(int maxNrLines) {
		this.bufferSize = maxNrLines;
	}

	/**
	 * @return the nrLines
	 */
	public int getNrLines() {
		return buffer.size();
	}

	/**
	 * Removes all rows for the channel with the specified id
	 *
	 * @param id
	 *            the id of the logging channel to remove
	 */
	public void removeChannelFromBuffer(String id) {
		buffer.remove(id);
	}

	public void removeChannelFromBuffer(String id, boolean includeGeneral) {
		buffer.remove(id);
	}

	public int size() {
		return buffer.size();
	}
	
	public Map<String, List<BufferLine>> getBufferMap(){
		return buffer;
	}

	public void addLogggingEvent(KettleLoggingEvent loggingEvent) {
		doAppend(loggingEvent);
		eventListeners.forEach(event -> event.eventAdded(loggingEvent));
	}

	public void addLoggingEventListener(KettleLoggingEventListener listener) {
		eventListeners.add(listener);
	}

	public void removeLoggingEventListener(KettleLoggingEventListener listener) {
		eventListeners.remove(listener);
	}

	private static String getLogChId(KettleLoggingEvent event) {
		return ((LogMessage) event.getMessage()).getLogChannelId();
	}

}
