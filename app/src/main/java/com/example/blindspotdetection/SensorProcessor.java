package com.example.blindspotdetection;

import android.util.Log;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SensorProcessor {
    /** A variable that store all the frame header's header name*/
    private final String frameName[] = {"version", "totalPacketLen", "platform", "frameNumber", "timeCpuCycles", "numDetectedObj", "numTLVs", "subFrameNumber"};

    /** A variable that store all the TLV header's header name*/
    private final String TLVheaderName[] = {"type", "length"};

    /** The size of Detected Object data structure that is used in the data packet format*/
    private final int DETECTED_OBJ_STRUCT_SIZE = 12;

    /** An array that stores the detected objects in the current frame. */
    private DetectedObject[] detectedObjects;

    /** A byte array that stores all the byte in a Datagram packet that is passed to this class. */
    private byte[] data;

    /** A TAG for logging. */
    private static final String TAG = "SensorProcessor";

    // Channel Config
    private final int txChannelEn = 3;
    private final int numTxAzimAnt = 2;
    private final int numTxElevAnt = 0;
    private final int numTxAnt = numTxElevAnt + numTxAzimAnt;

    // Profile Configuration
    private final int startFreq = 77;
    private final int idleTime = 7;
    private final int rampEndTime = 58;
    private final int freqSlopeConst = 68;
    private final int numAdcSamples = 256;
    private final int digitalSampleRate = 5500; //ksps

    // Frame Config.
    private final int chirpStartIdx = 0;
    private final int chirpEndIdx = 1;
    private final int numLoops = 32;

    // Data to convert from rangeIdx to meters
    private final int numRangeBins = pow2roundup(numAdcSamples);
    private final int rangeBias = 0; // Depend on calibration
    private final double rangeIdxToMeters = 3e8 * digitalSampleRate * 1e3 /(2 * freqSlopeConst * 1e12 * numRangeBins) - rangeBias;

    // Convert doppler to m/s
    private final int numChirpsPerFrame = (chirpEndIdx - chirpStartIdx + 1) * numLoops;
    private final double numDopplerBins = numChirpsPerFrame / numTxAnt;
    private final double dopplerResolutionMps = 3e8 / (2 * startFreq * 1e9 * (idleTime + rampEndTime) * 1e-6 * numChirpsPerFrame);

    public SensorProcessor(){

    }

    public SensorProcessor(DatagramPacket packet){
        data = packet.getData();
    }

    public void loadPacket(byte[] buffer){
        data = buffer;
        Log.i(TAG, String.format("Length of data: %d", data.length));
    }
    public void loadPacket(DatagramPacket packet){
        data = packet.getData();
        Log.i(TAG, String.format("Length of data: %d", data.length));
    }
    public boolean processData(){
        int len;
        int index = 0;
        byte[] slice;
        byte lastSevenMagicWord[] = new byte[7];
        lastSevenMagicWord[0] = 1;
        lastSevenMagicWord[1] = 4;
        lastSevenMagicWord[2] = 3;
        lastSevenMagicWord[3] = 6;
        lastSevenMagicWord[4] = 5;
        lastSevenMagicWord[5] = 8;
        lastSevenMagicWord[6] = 7;

        byte firstMagicWorld[] = new byte[1];
        firstMagicWorld[0] = 2;

        boolean start = false;
        do {
            slice = Arrays.copyOfRange(data, index,index+1);
            index++;
            Log.i(TAG,  String.format("%02X ", slice[0]));
            Log.i(TAG, String.format("Length of data: %d", data.length));
            Log.i(TAG, String.format("index: %d", index));
            if (Arrays.equals(slice, firstMagicWorld) && (index + 7) <= data.length) {
                Log.i(TAG, "Found first magic Word");
                slice = Arrays.copyOfRange(data, index,index+7);
                index += 7;
                Log.i(TAG,  String.format("%02X %02X %02X %02X %02X %02X %02X ", slice[0], slice[0],slice[0],slice[0],slice[0],slice[0],slice[0]));
                if (Arrays.equals(slice, lastSevenMagicWord)) {
                    start = true;
                    Log.i(TAG, "Found the rest of magic Word");
                }
                else {
                    Log.i(TAG, "Not Found the rest of magic Word");
                }
            }
            else {
                Log.i(TAG, "NOT found first magic Word");
            }
        } while (index <= data.length && !start);

        // If no frame header is found, return False.
        if (!start) {
            Log.i(TAG, "NOT FOUND THE START OF THE FRAME");
            return false;
        }
        // A variable to store the frame header data
        Map<String, Long> frameHeader = new HashMap<String, Long>();

        // Reading the frame
        index = readFrameHeader(frameHeader, data, index);
//        for (int i = 0; i < frameName.length; i++) {
//            Log.i(TAG, frameName[i] + ": " +frameHeader.get(frameName[i]));
//        }
//        Log.i(TAG, String.format("Index: %d", index));

        Map<String, Long> TLVheader;

        // Reading in the all TLV for one frame
        for (int i = 0; i < frameHeader.get("numTLVs"); i ++) {
            TLVheader = new HashMap<String, Long>();

            // Reading TLV header
            index = readTLVHeader(TLVheader, data, index);
//            for (String aTLVheaderName : TLVheaderName) {
//                Log.i(TAG, aTLVheaderName + ": " + TLVheader.get(aTLVheaderName));
//            }

            // Reading the TLV data
            switch(TLVheader.get("type").intValue()) {
                case 1:
                    if (TLVheader.get("length").intValue() > 0) {
                        // Get detected object descriptor
                        //slice = Arrays.copyOfRange(data, index,index+2);
                        int numObj = data[index] + data[index+1] * 256; //(int)Integer.toUnsignedLong(ByteBuffer.wrap(slice).order(ByteOrder.LITTLE_ENDIAN).getShort());
                        detectedObjects = new DetectedObject[numObj];
                        index += 2;
//                        slice = Arrays.copyOfRange(data, index,index+2);
                        int xqzQFormat = (int) Math.pow(2, data[index] + data[index+1] * 256);//(int)Integer.toUnsignedLong(ByteBuffer.wrap(slice).order(ByteOrder.LITTLE_ENDIAN).getShort()));
                        index += 2;

                        System.out.println("TLV Names: List of Detected Objects");
                        System.out.println("Number of Obj: " + numObj);
                        System.out.println("xyzQ Format: " + xqzQFormat);

                        slice = Arrays.copyOfRange(data, index,index+ (numObj * DETECTED_OBJ_STRUCT_SIZE));
                        index += numObj * DETECTED_OBJ_STRUCT_SIZE;

                        byte[][] matrix = reshape(slice, DETECTED_OBJ_STRUCT_SIZE, numObj);
//                        for (int row = 0; row < matrix.length; row ++) {
//                            for (int col = 0; col < matrix[0].length; col ++) {
//                                System.out.print(String.format("%02X ", matrix[row][col]) + "\t");
//                            }
//                            System.out.println();
//                        }

                        int count = 0; // counting the objects added to array.
                        // Process each object
                        for (byte[] row : matrix) {
                            // Getting the range
                            int rangeIdx = row[0] + row[1] * 256;
                            double range = rangeIdx * rangeIdxToMeters;
//                            System.out.println("Range Index: " + rangeIdx);
//                            System.out.println("Range: " + range);

                            // Getting doppler
                            // Asuming that doppler is signed int
//                            slice = Arrays.copyOfRange(row, 2, 4);  // Slice is used for unsigned number
                            double dopplerIdx = row[2] + row[3] * 256; //ByteBuffer.wrap(slice).order(ByteOrder.LITTLE_ENDIAN).getShort();
                            if (dopplerIdx > numDopplerBins/2 -1)
                                dopplerIdx -= numDopplerBins;
                            double doppler = dopplerIdx * dopplerResolutionMps;
//                            System.out.println("Doppler Index: " + dopplerIdx);
//                            System.out.println("Doppler: " + doppler + " m/s");

                            // Getting peakVal
                            int peakVal = row[4] + row[5] * 256;

                            // Getting x, y, z
                            double x = row[6] + row[7] * 256;
                            double y = row[8] + row[9] * 256;
                            double z = row[10] + row[11] * 256;

                            if (x > 32767) x -= 65536;
                            if (y > 32767) y -= 65536;
                            if (z > 32767) z -= 65536;

                            x = x /(double) xqzQFormat;
                            y = y / (double)xqzQFormat;
                            z = z / (double)xqzQFormat;


                            // Create a detected object
                            DetectedObject object = new DetectedObject(range, doppler, peakVal, x, y, z);
                            detectedObjects[count] = object;
                            count ++;
                            // Question: Is doppler, peak Val and x,y,z signed or unsigned

                        }
                    }
                    break;
                case 2:
                    slice = Arrays.copyOfRange(data, index,index+TLVheader.get("length").intValue());
                    index+= TLVheader.get("length").intValue();
                    break;
                case 3:
                    slice = Arrays.copyOfRange(data, index,index+TLVheader.get("length").intValue());
                    index+= TLVheader.get("length").intValue();
                    break;
                case 4:
                    break;
                case 5:
                    break;
                case 6:
                    slice = Arrays.copyOfRange(data, index,index+24);
                    index += 24;
                    index+= 2;
                    break;
                default:
                    System.out.println("Failed to detect the whole frame");
                    return false;
            }
        }
        // Return True if works successfully
        return true;
    }

    private int pow2roundup(int x) {
        int y = 1;
        while (x > y) {
            y = y * 2;
        }
        return y;
    }

    /**
     * A function read the frame header without the magic world.
     *
     * @param frameHeader   A Map data file that stores the TLV header data.
     * @param data          A byte array that contains the packet data.
     * @param index         The current index in the byte array that we are looking at.
     * @return              The index after processing the data.
     */
    private int readFrameHeader(Map<String, Long> frameHeader, byte[] data, int index){
        //byte slice[] = new byte [4];
        for (String aFrameName : frameName) {
            byte[] slice = Arrays.copyOfRange(data, index, index + 4);
            index += 4;
            frameHeader.put(aFrameName, Integer.toUnsignedLong(ByteBuffer.wrap(slice).order(ByteOrder.LITTLE_ENDIAN).getInt()));
        }

        return index;
    }

    /**
     * A function read the frame header without the magic world.
     *
     * @param TLVheader A Map data file that stores the TLV header data.
     * @param data      A byte array that contains the packet data.
     * @param index     The current index in the byte array that we are looking at.
     * @return          The index after processing the data.
     */
    private int readTLVHeader(Map<String, Long> TLVheader, byte[] data, int index){
        for (String aTLVheaderName : TLVheaderName) {
            byte[] slice = Arrays.copyOfRange(data, index, index + 4);
            index += 4;
            TLVheader.put(aTLVheaderName, Integer.toUnsignedLong(ByteBuffer.wrap(slice).order(ByteOrder.LITTLE_ENDIAN).getInt()));
        }
        return index;
    }

    /**
     * A function that takes in an array of byte and reshape it into a 2D byte array.
     * @param data  one dimension byte array
     * @param m     the number of column for the result 2D array
     * @param n     the number of row for the result 2D array
     * @return      the 2D array
     */
    private byte[][] reshape(byte[] data, int m, int n) {
        if(data.length != m*n){
            throw new IllegalArgumentException("New matrix must be of same area as the orignial byte array");
        }
        byte[][] matrix = new byte[n][m];

        int index = 0;
        for(int i = 0;i<n;i++){
            for(int j = 0;j<m;j++){
                matrix[i][j] = data[index++];
            }
        }
        return matrix;
    }

    public DetectedObject[] getDetectedObjects() {
        return detectedObjects;
    }

    public void sortDetectedObjects(){
        int n = detectedObjects.length;
        for (int i=1; i<n; ++i)
        {
            DetectedObject key = detectedObjects[i];
            int j = i-1;

            /* Move elements of arr[0..i-1], that are
               greater than key, to one position ahead
               of their current position */
            while (j>=0 && detectedObjects[j].compareTo(key) > 0)
            {
                detectedObjects[j+1] = detectedObjects[j];
                j = j-1;
            }
            detectedObjects[j+1] = key;
        }
    }
}