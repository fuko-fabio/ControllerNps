package com.nps.usb.packet;

import com.nps.usb.UsbGate;

public class PacketTransfer {

	private CommunicationMode communicationMode = CommunicationMode.COMMAND;
	private PacketSize packetSize = new PacketSize();
	private UsbGate usbGate;

	public PacketTransfer(UsbGate usbGate) {
		this.usbGate = usbGate;
	}

	public void getStreamParameters() throws PacketTransferException {
		Packet packet = new PacketBuilder(
				packetSize.getStreamOutSize())
				.withCommand(Command.GET_STREAM_PARAMETERS)
				.withLastByte((byte) 'a').build();
		byte[] inputBuffer = getInputBuffer();
		try {
			this.usbGate.send(packet);
			this.usbGate.receive(inputBuffer);
			updateTransferParameters(inputBuffer);
		} catch (IllegalAccessException e) {
			throw new PacketTransferException("Cannot read stream parameters cause: " + e.getMessage());
		}
	}

	public void setStreamParameters(short streamOutSize, short streamInSize) throws PacketTransferException {
		byte[] streamOutSizeBytes = Packet.bytesFromShort(streamOutSize);
		byte[] streamInSizeBytes = Packet.bytesFromShort(streamInSize);
		Packet packet = new PacketBuilder(
				packetSize.getStreamOutSize())
				.withCommand(Command.SET_STREAM_PARAMETERS)
				.withByte(8, streamOutSizeBytes[0])
				.withByte(9, streamOutSizeBytes[1])
				.withByte(10, streamInSizeBytes[0])
				.withByte(11, streamInSizeBytes[1]).build();
		byte[] inputBuffer = getInputBuffer();
		try {
			this.usbGate.send(packet);
			this.usbGate.receive(inputBuffer);
			updateTransferParameters(inputBuffer);
		} catch (IllegalAccessException e) {
			throw new PacketTransferException(
					"Cannot set stream parameters cause: " + e.getMessage());
		}
	}

	public void sendStreamPacket() throws PacketTransferException {
		Packet packet = new PacketBuilder(packetSize.getStreamOutSize())
				.withCommand(Command.SEND_STREAM_PACKET)
				.withLastByte((byte) 'a').build();
		try {
			this.usbGate.send(packet);
		} catch (IllegalAccessException e) {
			throw new PacketTransferException(
					"Cannot send stream packet cause: " + e.getMessage());
		}
	}
	
	public byte[] receiveStreamPacket() throws PacketTransferException {
		byte[] inputBuffer = getInputBuffer();
		try {
			this.usbGate.receive(inputBuffer);
		} catch (IllegalAccessException e) {
			throw new PacketTransferException(
					"Cannot read stream packet cause: " + e.getMessage());
		}
		return inputBuffer;
	}

	/**
	 * Switch communication to command mode
	 * 
	 * @throws PacketTransferException
	 */
	public void switchToCommandMode() throws PacketTransferException {
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
			updateTransferParameters(inputBuffer);
		} catch (IllegalAccessException e) {
			throw new PacketTransferException(
					"Cannot switch to command mode cause: " + e.getMessage());
		}
		communicationMode = CommunicationMode.COMMAND;
	}
	
	/**
	 * Switch communication to stream mode
	 * 
	 * @throws PacketTransferException
	 */
	public void switchToStreamMode() throws PacketTransferException {
		Packet packet = new PacketBuilder(
				packetSize.getStreamOutSize())
				.withCommand(Command.SWITCH_TO_STREAM)
				.withLastByte((byte) 'a').build();
		byte[] inputBuffer = getInputBuffer();
		try {
			this.usbGate.send(packet);
			this.usbGate.receive(inputBuffer, this.packetSize.getStreamInSize());
		} catch (IllegalAccessException e) {
			throw new PacketTransferException("Cannot switch to stream mode: " + e.getMessage());
		}
		communicationMode = CommunicationMode.STREAM;
	}

	/**
	 * Current communication mode
	 * 
	 * @return communication mode
	 */
	public CommunicationMode getCommunicationMode() {
		return communicationMode;
	}

	private byte[] getInputBuffer() {
		return new byte[packetSize.getStreamInSize()];
	}

	private void updateTransferParameters(byte[] inputBuffer) {
		packetSize.setStreamOutSize(
				Packet.shortFromBytes(inputBuffer[16], inputBuffer[17]));
		packetSize.setStreamInSize(
				Packet.shortFromBytes(inputBuffer[18], inputBuffer[19]));
	}
}
