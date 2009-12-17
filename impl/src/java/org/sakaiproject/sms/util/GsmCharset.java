/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.sms.util;

/*
 * Incorporates some code from 
 * http://embeddedfreak.wordpress.com/2008/10/08/java-gsm-0338-sms-character-set-translator/
 * 
 * Copyright (c) 2008, Daniel Widyanto <kunilkuda at gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY Daniel Widyanto ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL Daniel Widyanto BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.util.ArrayList;

/**
 * \brief Provides services to translate ASCII (ISO-8859-1 / Latin 1) charset
 *        into GSM 03.38 character set
 */
public class GsmCharset {
  /**
   * \brief Escape byte for the extended ISO
   */
  private static final Short ESC_BYTE = Short.valueOf((short) 27);

  /**
   * Mappings from GSM 03.38 to Unicode as per
   * http://unicode.org/Public/MAPPINGS/ETSI/GSM0338.TXT
   */
  private static final char[] gsmUtfMap = { 0x0040, 0x00A3, 0x0024, 0x00A5,
			0x00E8, 0x00E9, 0x00F9, 0x00EC, 0x00F2, 0x00E7, 0x000A, 0x00D8,
			0x00F8, 0x000D, 0x00C5, 0x00E5, 0x0394, 0x005F, 0x03A6, 0x0393,
			0x039B, 0x03A9, 0x03A0, 0x03A8, 0x03A3, 0x0398, 0x039E, 0x00A0,
			0x00C6, 0x00E6, 0x00DF, 0x00C9, 0x0020, 0x0021, 0x0022, 0x0023,
			0x00A4, 0x0025, 0x0026, 0x0027, 0x0028, 0x0029, 0x002A, 0x002B,
			0x002C, 0x002D, 0x002E, 0x002F, 0x0030, 0x0031, 0x0032, 0x0033,
			0x0034, 0x0035, 0x0036, 0x0037, 0x0038, 0x0039, 0x003A, 0x003B,
			0x003C, 0x003D, 0x003E, 0x003F, 0x00A1, 0x0041, 0x0042, 0x0043,
			0x0044, 0x0045, 0x0046, 0x0047, 0x0048, 0x0049, 0x004A, 0x004B,
			0x004C, 0x004D, 0x004E, 0x004F, 0x0050, 0x0051, 0x0052, 0x0053,
			0x0054, 0x0055, 0x0056, 0x0057, 0x0058, 0x0059, 0x005A, 0x00C4,
			0x00D6, 0x00D1, 0x00DC, 0x00A7, 0x00BF, 0x0061, 0x0062, 0x0063,
			0x0064, 0x0065, 0x0066, 0x0067, 0x0068, 0x0069, 0x006A, 0x006B,
			0x006C, 0x006D, 0x006E, 0x006F, 0x0070, 0x0071, 0x0072, 0x0073,
			0x0074, 0x0075, 0x0076, 0x0077, 0x0078, 0x0079, 0x007A, 0x00E4,
			0x00F6, 0x00F1, 0x00FC, 0x00E0 };

  /**
   * Extended GSM 03.38 to Unicode characters as per
   * http://unicode.org/Public/MAPPINGS/ETSI/GSM0338.TXT
   */
  private static final char[][] extGsmUtfMap = {
			// { {Ext GSM,UTF} }
			{ 0x0A, 0x000C }, { 0x14, 0x005E }, { 0x28, 0x007B },
			{ 0x29, 0x007D }, { 0x2F, 0x005C }, { 0x3C, 0x005B },
			{ 0x3D, 0x007E }, { 0x3E, 0x005D }, { 0x40, 0x007C },
			{ 0x65, 0x20AC } };
  
  
  public byte[] utfToGsm(String dataUtf) {
	    char[] dataUtfBytes = dataUtf.toCharArray();
	    ArrayList<Short> dataGsm = new ArrayList<Short>();

	    for (int dataIndex = 0; dataIndex < dataUtfBytes.length; dataIndex++) {
	      char currentDataIso = dataUtfBytes[dataIndex];

	      // Search currentDataGsm in the isoGsmMap
	      short currentDataGsm = findGsmChar(currentDataIso);

	      // If the data is not available in the isoGsmMap, search in the extended
	      // ISO-GSM map (extIsoGsmMap)
	      if (currentDataGsm == -1) {
	        currentDataGsm = findExtGsmChar(currentDataIso);

	        // If the character is found inside the extended map, add escape byte in
	        // the return byte[]
	        if (currentDataGsm != -1) {
	          dataGsm.add(ESC_BYTE);
	        }
	      }

	      dataGsm.add(Short.valueOf(currentDataGsm));
	    }

	    Short[] dataGsmShortArray = (Short[]) dataGsm.toArray(new Short[0]);
	    return translateShortToByteArray(dataGsmShortArray);
	  }

  
  /**
   * Translate GSM 03.38 character set into UTF String
   * @param dataGsm Data in GSM 03.38 charset
   * @return UTF String
   */
  public String gsmToUtf(byte[] dataGsm) {

		StringBuilder sb = new StringBuilder();

		boolean isEscape = false;
		for (int dataIndex = 0; dataIndex < dataGsm.length; dataIndex++) {
			// Convert to short to avoid negative values
			short currentDataGsm = (short) dataGsm[dataIndex];

			if (currentDataGsm == ESC_BYTE.shortValue()) {
				isEscape = true;
			} else if (!isEscape) {
				if (currentDataGsm >=0 && currentDataGsm < gsmUtfMap.length) {
					sb.append(gsmUtfMap[currentDataGsm]);
				}
			} else {
				char extchar = findExtUtfChar(currentDataGsm);
				if (extchar != 0xffff) {
					sb.append(extchar);
				}
				isEscape = false;
			}
		}

		return sb.toString();
	}
  
  /**
   * \brief Find GSM 03.38 character for the UTF character
   * \param utfChar UTF-16 character
   * \result GSM 03.38 character or -1 if no match
   */
  private short findGsmChar(char utfChar) {
    short gsmChar = -1;

    for (short mapIndex = 0; mapIndex < gsmUtfMap.length; mapIndex++) {
      if (gsmUtfMap[mapIndex] == utfChar) {
        gsmChar = mapIndex;
        break;
      }
    }

    return gsmChar;
  }

  /**
   * \brief Find extended GSM 03.38 character for the ISO-8859-1 character
   * \param isoChar ISO-8859-1 character
   * \result Extended GSM 03.38 character or 0xFFFF (-1) if no match
   */
  private short findExtGsmChar(char utfChar) {
    short gsmChar = -1;

    for (short mapIndex = 0; mapIndex < extGsmUtfMap.length; mapIndex++) {
      if (extGsmUtfMap[mapIndex][1] == utfChar) {
        gsmChar = (short) extGsmUtfMap[mapIndex][0];
        break;
      }
    }

    return gsmChar;
  }

   /**
   * \brief Find UTF character for the extended GSM 03.38 character
   * \param gsmChar Extended GSM 03.38 character
   * \result UTF character or 0xFFFF (-1) if no match
   */
  private char findExtUtfChar(short gsmChar) {
    char utfChar = 0xffff;

    for (short mapIndex = 0; mapIndex < extGsmUtfMap.length; mapIndex++) {
      if (extGsmUtfMap[mapIndex][0] == gsmChar) {
        utfChar = extGsmUtfMap[mapIndex][1];
        break;
      }
    }

    return utfChar;
  }

  
  /**
   * \brief Translate Short[] array to byte[]. Needed since there's no direct
   *        Java method/class to do this.
   * \param shortArray Short[] array
   * \return byte[] array
   */
  private byte[] translateShortToByteArray(Short[] shortArray) {
    byte[] byteArrayResult = new byte[shortArray.length];

    for (int i = 0; i < shortArray.length; i++) {
      byteArrayResult[i] = (byte) shortArray[i].shortValue();
    }

    return byteArrayResult;
  }

  /**
   * Is this string encodeable in the GSM 03.38 alphabet ?
   * 
   * Adapted from http://code.google.com/p/jsmpp/source/browse/trunk/src/java/examples/org/jsmpp/examples/SubmitMultipartMultilangualExample.java
   * Based on http://www.smsitaly.com/Download/ETSI_GSM_03.38.pdf
   */
   public boolean isEncodeableInGsm0338(String utfString) {

		char[] utfChars = utfString.toCharArray();

		outer: for (int i = 0; i < utfChars.length; i++) {
			for (int j = 0; j < gsmUtfMap.length; j++) {
				if (gsmUtfMap[j] == utfChars[i]) {
					continue outer;
				}
			}
			for (int j = 0; j < extGsmUtfMap.length; j++) {
				if (extGsmUtfMap[j][1] == utfChars[i]) {
					continue outer;
				}
			}
			return false;
		}
		return true;
	}

}

