Log:
- Oct 8: Researching the data that is received from the sensor. A data (Byte) file that needs to be process to get all necessaryry information.
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
- Oct 10: Create code to read the data in frame. Roughly get all the frame and TLV data. Further processing is necessary to get all the data from each TLV and stores each TLV into the corresponding frame.