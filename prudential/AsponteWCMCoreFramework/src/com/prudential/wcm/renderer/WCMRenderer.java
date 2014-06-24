package com.prudential.wcm.renderer;

import java.util.logging.Logger;

import com.ibm.workplace.wcm.api.Document;

public interface WCMRenderer {
	public WCMRenderResults render(Document doc) throws RenderException;
}
