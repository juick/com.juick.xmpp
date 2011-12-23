// Copyright (C) 2001 Stefan Haustein, Oberhausen (Rhld.), Germany
//
// Contributors: Stefan Haustein
//
// License: LGPL
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation; either version 2.1 of
// the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
// USA
package com.juick.xmpp;

public class Base64 {

    static final char[] charTab = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

    /**
     * Create a base64 encoded string. 
     * @param data
     * @return encoded
     */
    public static String encode(byte[] data) {
        return encode(data, 0, data.length, null).toString();
    }

    /**
     * Encodes the part of the given byte array denoted by start and
     * len to the Base64 format.  The encoded data is appended to the
     * given StringBuffer. If no StringBuffer is given, a new one is
     * created automatically. The StringBuffer is the return value of
     * this method.
     * @param data
     * @param start
     * @param len
     * @param buf
     * @return Encoded string
     */
    public static StringBuffer encode(byte[] data, int start, int len, StringBuffer buf) {

        if (buf == null) {
            buf = new StringBuffer(data.length * 3 / 2);
        }

        int end = len - 3;
        int i = start;
        int n = 0;

        while (i <= end) {
            int d = (((data[i]) & 0x0ff) << 16) | (((data[i + 1]) & 0x0ff) << 8) | ((data[i + 2]) & 0x0ff);

            buf.append(charTab[(d >> 18) & 63]);
            buf.append(charTab[(d >> 12) & 63]);
            buf.append(charTab[(d >> 6) & 63]);
            buf.append(charTab[d & 63]);

            i += 3;

            if (n++ >= 14) {
                n = 0;
                buf.append("\r\n");
            }
        }


        if (i == start + len - 2) {
            int d = (((data[i]) & 0x0ff) << 16) | (((data[i + 1]) & 255) << 8);

            buf.append(charTab[(d >> 18) & 63]);
            buf.append(charTab[(d >> 12) & 63]);
            buf.append(charTab[(d >> 6) & 63]);
            buf.append("=");
        } else if (i == start + len - 1) {
            int d = ((data[i]) & 0x0ff) << 16;

            buf.append(charTab[(d >> 18) & 63]);
            buf.append(charTab[(d >> 12) & 63]);
            buf.append("==");
        }

        return buf;
    }
}
