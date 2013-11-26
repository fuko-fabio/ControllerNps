package com.nps.usb.packet;

/**
 * @author Norbert Pabian
 * www.npsoftware.pl
 */
public class PacketBuilder {

	PacketCommand command;
	int size;
	int timeout = 0;
	byte[] packetData;
	
	PacketBuilder (int packetSize){
		super();
		this.size= packetSize;
		this.packetData = new byte[this.size];
	}

	PacketBuilder withCommand(PacketCommand command) {
		this.command = command;
		return this;
	}

	PacketBuilder withByte(int position, byte data) {
		this.packetData[position] = data;
		return this;
	}
	
	PacketBuilder withTimeout(int timeout) {
		this.timeout = timeout;
		return this;
	}
	
	Packet build() {
		return new Packet(command, size, packetData, timeout);
	}
}
