package com.synapticloop.panl.exception;

public class PanlServerException extends Exception {
	public PanlServerException(String message) {
		super(message);
	}

	public PanlServerException(String message, Throwable cause) {
		super(message, cause);
	}
}
