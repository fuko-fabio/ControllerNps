/**
 * 
 */
package com.nps.usb.packet;

/**
 * @author Norbert Pabian
 * www.npsoftware.pl
 */
public class MicrocontrollerException extends Exception {

	private static final long serialVersionUID = -8485014422033067650L;

	public MicrocontrollerException() {
		super("PacketTransferException: An unknown error occurred!");
	}

	public MicrocontrollerException(String error) {
		super(error);
	}
}
