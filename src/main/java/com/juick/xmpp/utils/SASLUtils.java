/*
 * Juick
 * Copyright (C) 2008-2011, Ugnich Anton
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.juick.xmpp.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Ugnich Anton
 */
public class SASLUtils {

    public static HashMap<String, String> parseSASLChallenge(String c) {
        HashMap<String, String> ret = new HashMap<String, String>();

        String items[] = c.split(",");
        for (int i = 0; i < items.length; i++) {
            String keyval[] = items[i].split("=", 2);
            String key = keyval[0].trim();
            String val = keyval[1];
            if (val.charAt(0) == '"') {
                val = val.substring(1, val.length() - 2);
            }
            ret.put(key, val);
        }

        return ret;
    }

    public static String compileSASLResponse(HashMap<String, String> kv) {
        String ret = "";

        Set<String> keys = kv.keySet();
        for (Iterator<String> i = keys.iterator(); i.hasNext();) {
            String key = i.next();
            if (!ret.isEmpty()) {
                ret += ", ";
            }
            ret += key + "=\"" + kv.get(key) + "\"";
        }

        return ret;
    }
}
