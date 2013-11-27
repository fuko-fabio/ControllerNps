package com.nps.usb.packet;

import com.nps.usb.UsbGate;

/**
 * @author Norbert Pabian
 * www.npsoftware.pl
 */
public class Microcontroller {

	private Mode mode = Mode.COMMAND;
	private PacketSize packetSize = new PacketSize();
	private UsbGate usbGate;

	public Microcontroller(UsbGate usbGate) {
		this.usbGate = usbGate;
	}

	public void getStreamParameters() throws MicrocontrollerException {
		if(mode == Mode.STREAM) {
			throw new MicrocontrollerException("Cannot read stream parameters. Current communication mode is 'STREAM'");
		}
		Packet packet = new PacketBuilder(
				packetSize.getDefaultStreamOutSize())
				.withCommand(Command.GET_STREAM_PARAMETERS)
				.withLastByte((byte) 'a').build();
		byte[] inputBuffer = new byte[packetSize.getDefaultStreamInSize()];
		try {
			this.usbGate.send(packet);
			this.usbGate.receive(inputBuffer);
			updatePacketSize(inputBuffer);
		} catch (IllegalAccessException e) {
			throw new MicrocontrollerException("Cannot read stream parameters cause: " + e.getMessage());
		}
	}

	public void setStreamParameters(short streamOutSize, short streamInSize) throws MicrocontrollerException {
		if(mode == Mode.STREAM) {
			throw new MicrocontrollerException("Cannot set stream parameters. Current communication mode is 'STREAM'");
		}
		byte[] streamOutSizeBytes = Packet.bytesFromShort(streamOutSize);
		byte[] streamInSizeBytes = Packet.bytesFromShort(streamInSize);
		Packet packet = new PacketBuilder(
				packetSize.getDefaultStreamOutSize())
				.withCommand(Command.SET_STREAM_PARAMETERS)
				.withByte(8, streamOutSizeBytes[0])
				.withByte(9, streamOutSizeBytes[1])
				.withByte(10, streamInSizeBytes[0])
				.withByte(11, streamInSizeBytes[1]).build();
		byte[] inputBuffer = new byte[packetSize.getDefaultStreamInSize()];
		try {
			this.usbGate.send(packet);
			this.usbGate.receive(inputBuffer);
			updatePacketSize(inputBuffer);
		} catch (IllegalAccessException e) {
			throw new MicrocontrollerException(
					"Cannot set stream parameters cause: " + e.getMessage());
		}
	}

	public void sendStreamPacket() throws MicrocontrollerException {
		if(mode == Mode.COMMAND) {
			throw new MicrocontrollerException("Cannot seant stream packet. Current communication mode is 'COMMAND'");
		}
		Packet packet = new PacketBuilder(packetSize.getStreamOutSize())
				.withCommand(Command.SEND_STREAM_PACKET)
				.withLastByte((byte) 'a').build();
		try {
			this.usbGate.send(packet);
		} catch (IllegalAccessException e) {
			throw new MicrocontrollerException(
					"Cannot send stream packet cause: " + e.getMessage());
		}
	}
	
	public byte[] receiveStreamPacket() throws MicrocontrollerException {
		if(mode == Mode.COMMAND) {
			throw new MicrocontrollerException("Cannot read stream packet. Current communication mode is 'COMMAND'");
		}
		byte[] inputBuffer = new byte[packetSize.getStreamInSize()];
		try {
			this.usbGate.receive(inputBuffer);
		} catch (IllegalAccessException e) {
			throw new MicrocontrollerException(
					"Cannot read stream packet cause: " + e.getMessage());
		}
		return inputBuffer;
	}

	/**
	 * Switch communication to command mode
	 * 
	 * @throws MicrocontrollerException
	 */
	public void switchToCommandMode() throws MicrocontrollerException {
		if(mode == Mode.COMMAND) {
			return;
		}
		byte[] streamOutSizeBytes = Packet.bytesFromShort(packetSize.getDefaultStreamOutSize());
		byte[] streamInSizeBytes = Packet.bytesFromShort(packetSize.getDefaultStreamInSize());
		Packet packet = new PacketBuilder(
				packetSize.getStreamOutSize())
		 		.withCommand(Command.RESET_PACKETS)
				.withByte(8, streamOutSizeBytes[0])
				.withByte(9, streamOutSizeBytes[1])
				.withByte(10, streamInSizeBytes[0])
				.withByte(11, streamInSizeBytes[1])
				.withLastByte((byte) 'a').build();
		byte[] inputBuffer = new byte[packetSize.getDefaultStreamInSize()];
		try {
			this.usbGate.send(packet);
			this.usbGate.receive(inputBuffer);
			updatePacketSize(inputBuffer);
		} catch (IllegalAccessException e) {
			throw new MicrocontrollerException(
					"Cannot switch to command mode cause: " + e.getMessage());
		}
		mode = Mode.COMMAND;
	}
	
	/**
	 * Switch communication to stream mode
	 * 
	 * @throws MicrocontrollerException
	 */
	public void switchToStreamMode() throws MicrocontrollerException {
		if(mode == Mode.STREAM) {
			return;
		}
		Packet packet = new PacketBuilder(
				packetSize.getDefaultStreamOutSize())
				.withCommand(Command.SWITCH_TO_STREAM)
				.withLastByte((byte) 'a').build();
		byte[] inputBuffer = new byte[packetSize.getStreamInSize()];
		try {
			this.usbGate.send(packet);
			this.usbGate.receive(inputBuffer);
		} catch (IllegalAccessException e) {
			throw new MicrocontrollerException("Cannot switch to stream mode: " + e.getMessage());
		}
		mode = Mode.STREAM;
	}

	/**
	 * Current communication mode
	 * 
	 * @return communication mode
	 */
	public Mode getMode() {
		return mode;
	}

	private void updatePacketSize(byte[] inputBuffer) {
		packetSize.setStreamOutSize(
				Packet.shortFromBytes(inputBuffer[16], inputBuffer[17]));
		packetSize.setStreamInSize(
				Packet.shortFromBytes(inputBuffer[18], inputBuffer[19]));
	}
}
