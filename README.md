Log:
- Sep 16: Discuss Design Specificatio with sponsor.
- Sep 19 and earlier: Researched about the hardware.
- Sep 24: Finalized the sensor to use with sponsor.
- Sep 25-28: Look at the SDK provided by the Sensor company to understand the application and the output of the application in sensor.
- Sep 29: Created the android application and create the service class
- Sep 28: Added communication between the service class and the main thread.
- Oct 1: Added cited website, update the service class to use UDP.
- Oct 2: Updated the design speciication
- OCt 7: Fixed some errors
- Oct 8: Researched the data that is received from the sensor. A data (Byte) file that needs to be process to get all necessaryry information.
	+ Spend more than 2 hours
- Oct 9: Read in byte by byte of the data file. Finish processing the frame header. Currently looking at TLV (type-length-value) in the data packet.
	+ Look at the first TLV: Object Detected TLV.
		+ First 2 bytes are number of object
		+ Second 2 bytes are xyzQFormat
		+ then read OdsDemo_detectedObj for each detected object.
			typedef volatile struct OdsDemo_detectedObj_t
			{
			    uint16_t   rangeIdx;     /*!< @brief Range index */
			    uint16_t   dopplerIdx;   /*!< @brief Dopler index */
			    uint16_t  peakVal;      /*!< @brief Peak value */
			    int16_t  x;             /*!< @brief x - coordinate in meters. Q format depends on the range resolution */
			    int16_t  y;             /*!< @brief y - coordinate in meters. Q format depends on the range resolution */
			    int16_t  z;             /*!< @brief z - coordinate in meters. Q format depends on the range resolution */
			} OdsDemo_detectedObj;
		+ look at how to convert range to meters
- Oct 10: Create code to read the data in frame. Roughly get all the frame and TLV data. Further processing is necessary to get all the data from each TLV and stores each TLV into the corresponding frame. Tested out the code in eclipse with a sample data file.
- Oct 15: Added the code to process the sensor data to the android application.
- Oct 17: Continue to process the sensor data. Convert the dopplerIdx to correct velocity result. Found that the three objects detected are in the same position. Tried to figure why.
- Oct 18: Send the data to the Mobile App to test the processing on the mobile app.
- Oct 19: Found that a lot of packets are lost when send through UDP. Needs to figure out is it because of the code or the hardware (The AP specifically).
- Oct 22: Fix a bug in the processing and still try to fix the packet losing problem. Move the data processing from the listener service to the main thread.
- Oct 23: Finish processing the date from the sensor and send the data to the main UI thread.
- Oct 24: Display the detected object data on a graph.
- Oct 25: Play sounds.

What I will do next:
 - Play sounds when detected an object and when the objects leave the detection zone.
 - Have a more interesting view.
 - Polish the UI
 - Test the speed of the sensor.
 - Test the accuracy of the sensor.
 - Add more data that sensors provides.