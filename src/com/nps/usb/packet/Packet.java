package com.nps.usb.packet;

/**
 * @author Norbert Pabian
 * www.npsoftware.pl
 */
public class Packet {

	PacketCommand command;
	int size;
	int timeout;
	byte[] data;
	
	public Packet(PacketCommand command, int size, byte[] data, int timeout) {
		super();
		this.command = command;
		this.size = size;
		this.data = data;
		this.timeout = timeout;
	}
	
	public PacketCommand getCommand() {
		return command;
	}

	public void setCommand(PacketCommand command) {
		this.command = command;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
	
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public byte[] toByteArray(){
		byte[] packet = data;
		packet[0] = command.value();
		packet[4] = (byte) size; // TODO FIXME
		return packet;
	}
}
