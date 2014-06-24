package com.prudential.wcm.renderer;

import java.util.logging.Logger;

public interface WCMRenderResults {
	public String[] getSegmentNames();
	public Object getResultSegment(String name);
	public void close();
}
