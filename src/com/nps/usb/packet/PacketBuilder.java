package com.nps.usb.packet;

import com.nps.usb.microcontroller.Command;

/**
 * @author Norbert Pabian
 * www.npsoftware.pl
 */
public class PacketBuilder {

	Command command;
	short size;
	byte[] packetData;
	
	public PacketBuilder (short packetSize){
		super();
		this.size= packetSize;
		this.packetData = new byte[this.size];
	}

	public PacketBuilder withCommand(Command command) {
		this.command = command;
		return this;
	}

	public PacketBuilder withByte(int position, byte data) {
		this.packetData[position] = data;
		return this;
	}

	public PacketBuilder withLastByte(byte data) {
		this.packetData[size - 1] = data;
		return this;
	}

	public Packet build() {
		return new Packet(command, size, packetData);
	}

}
