package com.prudential.wcm.renderer;

import java.util.logging.Logger;

public class RenderException extends Exception {
	private static Logger log = Logger.getLogger(RenderException.class.getName());
	private static final long serialVersionUID = -5699554663442156180L;

	public RenderException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public RenderException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public RenderException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public RenderException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}
	
}
