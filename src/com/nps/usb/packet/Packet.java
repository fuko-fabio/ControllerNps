package com.nps.usb.packet;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * @author Norbert Pabian
 * www.npsoftware.pl
 */
public class Packet {

	Command command;
	short size;
	int timeout;
	byte[] data;
	
	public Packet(Command command, short size, byte[] data, int timeout) {
		super();
		this.command = command;
		this.size = size;
		this.data = data;
		this.timeout = timeout;
	}
	
	public Command getCommand() {
		return command;
	}

	public void setCommand(Command command) {
		this.command = command;
	}

	public short getSize() {
		return size;
	}

	public void setSize(short size) {
		this.size = size;
	}
	
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public byte[] toByteArray(){
		byte[] sizeBytes = bytesFromShort(this.size);
		byte[] packet = this.data;
		packet[0] = this.command.value();
		packet[4] = sizeBytes[0];
		packet[5] = sizeBytes[1];
		return packet;
	}

	public static short shortFromBytes(byte firstByte, byte secondByte) {
		byte[] byteArray = new byte[2];

		byteArray[0] = firstByte;
		byteArray[1] = secondByte;
		ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
		return shortBuffer.get();
	}

	public static byte[] bytesFromShort(short value) {
		ByteBuffer buffer = ByteBuffer.allocate(2);
		buffer.putShort(value);
		return buffer.array();
	}
}
