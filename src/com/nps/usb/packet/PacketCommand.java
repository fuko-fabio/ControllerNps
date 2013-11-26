package com.nps.usb.packet;

public enum PacketCommand {
	
	UNKNOWN((byte)0),
	SEND_STREAM_PACKET((byte)1),
	RESET_PACKETS((byte)2),
	GET_STREAM_PARAMETERS((byte)3),
	SWITCH_TO_STREAM((byte)4),
	SET_STREAM_PARAMETERS((byte)5);
	
	private byte value = 0;

	PacketCommand(byte value) {
		this.value = value;
	}

	public byte value() {
		return this.value;
	}

	public static PacketCommand getValue(int id) {
		PacketCommand[] PacketCommands = PacketCommand.values();
		for (int i = 0; i < PacketCommands.length; i++) {
			if (PacketCommands[i].value() == id)
				return PacketCommands[i];
		}
		return PacketCommand.UNKNOWN;
	}

}
