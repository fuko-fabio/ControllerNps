package com.nps.usb.packet;

import com.nps.usb.UsbGate;
import com.nps.usb.UsbGateException;

/**
 * @author Norbert Pabian
 * www.npsoftware.pl
 */
public class Microcontroller {

	private Mode mode = Mode.COMMAND;
	private PacketSize packetSize = new PacketSize();
	private UsbGate usbGate;
	private byte[] receivedBuffer;

	public Microcontroller(UsbGate usbGate) {
		this.usbGate = usbGate;
	}

	public void getStreamParameters() throws MicrocontrollerException, UsbGateException {
		if(mode == Mode.STREAM) {
			throw new MicrocontrollerException("Cannot read stream parameters. Current communication mode is 'STREAM'");
		}
		Packet packet = new PacketBuilder(
				packetSize.getDefaultStreamOutSize())
				.withCommand(Command.GET_STREAM_PARAMETERS)
				.withLastByte((byte) 'a').build();
		receivedBuffer = new byte[packetSize.getDefaultStreamInSize()];
		try {
			this.usbGate.send(packet);
			this.usbGate.receive(receivedBuffer);
			updatePacketSize(receivedBuffer);
		} catch (IllegalAccessException e) {
			throw new MicrocontrollerException("Cannot read stream parameters cause: " + e.getMessage());
		}
	}

	public void setStreamParameters(short streamOutSize, short streamInSize) throws MicrocontrollerException, UsbGateException {
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
		receivedBuffer = new byte[packetSize.getDefaultStreamInSize()];
		try {
			this.usbGate.send(packet);
			this.usbGate.receive(receivedBuffer);
			updatePacketSize(receivedBuffer);
		} catch (IllegalAccessException e) {
			throw new MicrocontrollerException(
					"Cannot set stream parameters cause: " + e.getMessage());
        }
	}

	public long sendStreamPacket() throws MicrocontrollerException, UsbGateException {
		if(mode == Mode.COMMAND) {
			throw new MicrocontrollerException("Cannot seant stream packet. Current communication mode is 'COMMAND'");
		}
		Packet packet = new PacketBuilder(packetSize.getStreamOutSize())
				.withCommand(Command.SEND_STREAM_PACKET)
				.withLastByte((byte) 'a').build();
		try {
			return this.usbGate.send(packet);
		} catch (IllegalAccessException e) {
			throw new MicrocontrollerException(
					"Cannot send stream packet cause: " + e.getMessage());
		}
	}
	
	public long receiveStreamPacket() throws MicrocontrollerException, UsbGateException {
		if(mode == Mode.COMMAND) {
			throw new MicrocontrollerException("Cannot read stream packet. Current communication mode is 'COMMAND'");
		}
		receivedBuffer = new byte[packetSize.getStreamInSize()];
		try {
			return this.usbGate.receive(receivedBuffer);
		} catch (IllegalAccessException e) {
			throw new MicrocontrollerException(
					"Cannot read stream packet cause: " + e.getMessage());
		}
	}

	/**
	 * Switch communication to command mode
	 * 
	 * @throws MicrocontrollerException
	 * @throws UsbGateException 
	 */
	public void switchToCommandMode() throws MicrocontrollerException, UsbGateException {
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
		receivedBuffer = new byte[packetSize.getDefaultStreamInSize()];
		try {
			this.usbGate.send(packet);
			this.usbGate.receive(receivedBuffer);
			updatePacketSize(receivedBuffer);
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
	 * @throws UsbGateException 
	 */
	public void switchToStreamMode() throws MicrocontrollerException, UsbGateException {
		if(mode == Mode.STREAM) {
			return;
		}
		Packet packet = new PacketBuilder(
				packetSize.getDefaultStreamOutSize())
				.withCommand(Command.SWITCH_TO_STREAM)
				.withLastByte((byte) 'a').build();
		receivedBuffer = new byte[packetSize.getStreamInSize()];
		try {
			this.usbGate.send(packet);
			this.usbGate.receive(receivedBuffer);
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

    public PacketSize getCurrentPacketSize() {
        return packetSize;
    }

    public byte[] getLastReceivedData() {
        return receivedBuffer;
    }

	private void updatePacketSize(byte[] inputBuffer) {
		packetSize.setStreamOutSize(
				Packet.shortFromBytes(inputBuffer[16], inputBuffer[17]));
		packetSize.setStreamInSize(
				Packet.shortFromBytes(inputBuffer[18], inputBuffer[19]));
	}

    public String getDeviceDescription() {
        // TODO Auto-generated method stub
        return null;
    }
}
