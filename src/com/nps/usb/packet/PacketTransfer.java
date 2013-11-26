package com.nps.usb.packet;

import com.nps.usb.UsbGate;

public class PacketTransfer {

	private static final int DAFAULT_PACKET_OUT_SIZE = 16;
	private static final int DAFAULT_PACKET_IN_SIZE = 48;
	private CommunicationMode communicationMode = CommunicationMode.COMMAND;
	private TransferParameters transferParameters = new TransferParameters();
	private UsbGate usbGate;

	public PacketTransfer(UsbGate usbGate) {
		this.usbGate = usbGate;
	}

	public void getStreamParameters() throws PacketTransferException {
		Packet packet = new PacketBuilder(DAFAULT_PACKET_OUT_SIZE)
				.withCommand(PacketCommand.GET_STREAM_PARAMETERS).build();
		byte[] readBuffer = new byte[DAFAULT_PACKET_IN_SIZE];
		try {
			this.usbGate.send(packet);
			this.usbGate.receive(readBuffer, 48);
		} catch (IllegalAccessException e) {
			throw new PacketTransferException("Cannot read stream parameters cause: " + e.getMessage());
		}
	}

	/**
	 * Switch communication to command mode
	 * 
	 * @throws PacketTransferException
	 */
	public void setToCommandMode() throws PacketTransferException {
		Packet packet = new PacketBuilder(DAFAULT_PACKET_OUT_SIZE).withCommand(
				PacketCommand.RESET_PACKETS).build();
		try {
			this.usbGate.send(packet);
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
	public void setToStreamMode() throws PacketTransferException {
		byte[] readBuffer = new byte[DAFAULT_PACKET_IN_SIZE];
		Packet packet = new PacketBuilder(DAFAULT_PACKET_OUT_SIZE).withCommand(
				PacketCommand.SWITCH_TO_STREAM).build();
		try {
			if (this.usbGate.send(packet)) {

				if (this.usbGate.receive(readBuffer, this.transferParameters.getStreamInSize())) {					
					parseReceivedData(readBuffer);
				} else {
					throw new PacketTransferException("Cannot read data from device");
				}
			} else {
				throw new PacketTransferException("Cannot send data to device");
			}
		} catch (Exception e) {
			throw new PacketTransferException("Cannot switch to stream mode: " + e.getMessage());
		}
	}

	private void parseReceivedData(byte[] readBuffer) {
//		PacketData packedData = readPacket((byte) 0, readBuffer);
//		if (packedData.getCommand() != PacketCommand.SWITCH_TO_STREAM) {
//			throw new PacketTransferException(
//					"Received unknown command. Buffer: " + readBuffer);
//		} else {
//			this.communicationMode = CommunicationMode.STREAM;
//		}
	}

	/**
	 * Current communication mode
	 * 
	 * @return communication mode
	 */
	public CommunicationMode getCommunicationMode() {
		return communicationMode;
	}
}
