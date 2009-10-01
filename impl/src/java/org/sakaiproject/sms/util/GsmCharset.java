package org.sakaiproject.sms.util;


/*
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
//package com.kunilkuda; //< Replace with your package

import java.util.ArrayList;

/**
 * \brief Provides services to translate ASCII (ISO-8859-1 / Latin 1) charset
 *        into GSM 03.38 character set
 */
public class GsmCharset {
  /**
   * \brief Escape byte for the extended ISO
   */
  private final Short ESC_BYTE = Short.valueOf((short) 27);

  /**
   * \brief ISO-8859-1 - GSM 03.38 character map
   *
   * Taken from http://www.dreamfabric.com/sms/default_alphabet.html
   */
  private final short[] isoGsmMap = {
    // Index = GSM, { ISO }
      64, 163,  36, 165, 232, 233, 249, 236, 242, 199,  10, 216,
     248,  13, 197, 229,   0,  95,   0,   0,   0,   0,   0,   0,
       0,   0,   0,   0, 198, 230, 223, 201,  32,  33,  34,  35,
     164,  37,  38,  39,  40,  41,  42,  43,  44,  45,  46,  47,
      48,  49,  50,  51,  52,  53,  54,  55,  56,  57,  58,  59,
      60,  61,  62,  63, 161,  65,  66,  67,  68,  69,  70,  71,
      72,  73,  74,  75,  76,  77,  78,  79,  80,  81,  82,  83,
      84,  85,  86,  87,  88,  89,  90, 196, 214, 209, 220, 167,
     191,  97,  98,  99, 100, 101, 102, 103, 104, 105, 106, 107,
     108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119,
     120, 121, 122, 228, 246, 241, 252, 224
  };

  /**
   * \brief Extended ISO-8859-1 - GSM 03.38 character map
   *
   * Taken from http://www.dreamfabric.com/sms/default_alphabet.html
   */
  private final short[][] extIsoGsmMap = {
    //{ {Ext GSM,ISO} }
    { 10, 12}, { 20, 94}, { 40,123}, { 41,125}, { 47, 92},
    { 60, 91}, { 61,126}, { 62, 93}, { 64,124}, {101,164}
  };

  /**
   * \brief Translate ISO-8859-1 character set into GSM 03.38 character set
   * \param dataIso Data in ISO-8859-1 charset
   * \return GSM03.38 character set in byte array
   */
  public byte[] translateToGsm0338(String dataIso) {
    byte[] dataIsoBytes = dataIso.getBytes();
    ArrayList<Short> dataGsm = new ArrayList<Short>();

    for (int dataIndex = 0; dataIndex < dataIsoBytes.length; dataIndex++) {
      byte currentDataIso = dataIsoBytes[dataIndex];

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
   * \brief Translate GSM 03.38 character set set into ISO-8859-1 character
   * \param dataGsm Data in GSM 03.38 charset
   * \return ISO-8859-1 string
   */
  public String translateToIso(byte[] dataGsm) {
    ArrayList<Short> dataIso = new ArrayList<Short>();

    boolean isEscape = false;
    for (int dataIndex = 0; dataIndex < dataGsm.length; dataIndex++) {
      // Convert to short to avoid negative values
      short currentDataGsm = (short) dataGsm[dataIndex];
      short currentDataIso = -1;

      if (currentDataGsm == ESC_BYTE.shortValue()) {
        isEscape = true;
      }
      else if (!isEscape) {
        currentDataIso = findIsoChar(currentDataGsm);
        dataIso.add(Short.valueOf(currentDataIso));
      }
      else {
        currentDataIso = findExtIsoChar(currentDataGsm);
        dataIso.add(Short.valueOf(currentDataIso));
        isEscape = false;
      }
    }

    Short[] dataIsoShortArray = (Short[]) dataIso.toArray(new Short[0]);
    byte[] dataIsoByteArray = translateShortToByteArray(dataIsoShortArray);
    return new String(dataIsoByteArray);
  }

  /**
   * \brief Find GSM 03.38 character for the ISO-8859-1 character
   * \param isoChar ISO-8859-1 character
   * \result GSM 03.38 character or -1 if no match
   */
  private short findGsmChar(byte isoChar) {
    short gsmChar = -1;

    for (short mapIndex = 0; mapIndex < isoGsmMap.length; mapIndex++) {
      if (isoGsmMap[mapIndex] == (short) isoChar) {
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
  private short findExtGsmChar(byte isoChar) {
    short gsmChar = -1;

    for (short mapIndex = 0; mapIndex < extIsoGsmMap.length; mapIndex++) {
      if (extIsoGsmMap[mapIndex][1] == isoChar) {
        gsmChar = extIsoGsmMap[mapIndex][0];
        break;
      }
    }

    return gsmChar;
  }

  /**
   * \brief Find ISO-8859-1 character for the GSM 03.38 character
   * \param gsmChar GSM 03.38 character
   * \result ISO-8859-1 character or -1 if no match
   */
  private short findIsoChar(short gsmChar) {
    short isoChar = -1;

    if (gsmChar < isoGsmMap.length) {
      isoChar = isoGsmMap[gsmChar];
    }

    return isoChar;
  }

  /**
   * \brief Find ISO-8859-1 character for the extended GSM 03.38 character
   * \param gsmChar Extended GSM 03.38 character
   * \result ISO-8859-1 character or 0xFFFF (-1) if no match
   */
  private short findExtIsoChar(short gsmChar) {
    short isoChar = -1;

    for (short mapIndex = 0; mapIndex < extIsoGsmMap.length; mapIndex++) {
      if (extIsoGsmMap[mapIndex][0] == gsmChar) {
        isoChar = extIsoGsmMap[mapIndex][1];
        break;
      }
    }

    return isoChar;
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
   * This main function is used for the class test purpose
   */
  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("Usage: java com.kunilkuda.GsmCharset <string>");
      System.exit(1);
    }

    byte[] data = new GsmCharset().translateToGsm0338(args[0]);
    for(int i = 0; i < data.length; i++) {
      System.out.print(data[i] + " ");
    }
    System.out.println();

    System.out.println(new GsmCharset().translateToIso(data));
  }
}

