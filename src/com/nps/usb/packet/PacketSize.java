package com.nps.usb.packet;

/**
 * @author Norbert Pabian
 * www.npsoftware.pl
 */
public class PacketSize {
	private static final short defaultStreamInSize = 48;
	private static final short defaultStreamOutSize = 16;
	private short customStreamInSize;
	private short customStreamOutSize;

	/**
	 * Default constructor
	 * stream in = default input stream size 16
	 * stream out = default output stream size 48
	 */
	public PacketSize() {
		customStreamInSize = defaultStreamInSize;
		customStreamOutSize = defaultStreamOutSize;
	}
	
	/**
	 * @param streamInSize size of input stream
	 * @param streamOutSize size of output stream
	 */
	public PacketSize(short streamInSize, short streamOutSize) {
		customStreamInSize = streamInSize;
		customStreamOutSize = streamOutSize;
	}

	/**
	 * @return size of input stream
	 */
	public short getStreamInSize(){
		return customStreamInSize;
	}
	
	/**
	 * @param size size of input stream
	 */
	public void setStreamInSize(short size){
		this.customStreamInSize = size;
	}
	
	/**
	 * @return size of output stream
	 */
	public short getStreamOutSize(){
		return customStreamOutSize;
	}
	
	/**
	 * @param size size of output stream
	 */
	public void setStreamOutSize(short size){
		this.customStreamOutSize = size;
	}
	
	/**
	 * @return default size of input stream
	 */
	public short getDefaultStreamInSize(){
		return defaultStreamInSize;
	}
	
	/**
	 * @return default size of output stream
	 */
	public short getDefaultStreamOutSize(){
		return defaultStreamOutSize;
	}
}
