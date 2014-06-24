package com.prudential.wcm.renderer;

import java.util.logging.Logger;

import java.util.Map;

public class MapRenderResults implements WCMRenderResults {
	private static Logger log = Logger.getLogger(MapRenderResults.class.getName());
	Map<String, Object> segments;

	public MapRenderResults(Map<String, Object> segments) {
		super();
		this.segments = segments;
	}

	public String[] getSegmentNames() {
		return segments.keySet().toArray(new String[segments.size()]);
	}

	public Object getResultSegment(String name) {
		return segments.get(name);
	}

	public void close() {
		// do nothing
	}
}
