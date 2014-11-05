package edu.rose_hulman.rosierovercommsapp.rosierovercomms;

import java.util.UUID;

/*
This class was taken from the example in Dr. Fisher's book (Getting Started
with Bluetooth Low Energy, it has a bird on it). See the listed website in Ch 8 on Android development.
I recommend you don't mess with it.
 */

public class BleDefinedUUIDs {
	
	public static class Service {
		final static public UUID HEART_RATE               = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");
	};
	
	public static class Characteristic {
		final static public UUID HEART_RATE_MEASUREMENT   = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
		final static public UUID MANUFACTURER_STRING      = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
		final static public UUID MODEL_NUMBER_STRING      = UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb");
		final static public UUID FIRMWARE_REVISION_STRING = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
		final static public UUID APPEARANCE               = UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb");
		final static public UUID BODY_SENSOR_LOCATION     = UUID.fromString("00002a38-0000-1000-8000-00805f9b34fb");
		final static public UUID BATTERY_LEVEL            = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

        //this is what I added, but I don't think it is used - brandon
        public static final String SerialPortUUIDString = "0000dfb1-0000-1000-8000-00805f9b34fb";
        public static final String CommandUUIDString = "0000dfb2-0000-1000-8000-00805f9b34fb";
        public static final String ModelNumberStringUUIDString = "00002a24-0000-1000-8000-00805f9b34fb";
        public static final UUID SerialPortUUID = UUID.fromString("0000dfb1-0000-1000-8000-00805f9b34fb");
        public static final UUID CommandUUID = UUID.fromString("0000dfb2-0000-1000-8000-00805f9b34fb");
        public static final UUID ModelNumberStringUUID = UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb");
	}
	
	public static class Descriptor {
		final static public UUID CHAR_CLIENT_CONFIG       = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
	}
	
}
