/*
 * Copyright (C) 2008-2017, Juick
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

package com.juick.xmpp.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.text.RandomStringGenerator;
import rocks.xmpp.addr.Jid;

/**
 * Created by vitalyster on 05.12.2016.
 */
public class DialbackUtils {
    private static RandomStringGenerator generator = new RandomStringGenerator.Builder().withinRange('a', 'z').build();
    private DialbackUtils() {
        throw new IllegalStateException();
    }

    public static String generateDialbackKey(Jid to, Jid from, String id) {
        return new HmacUtils(HmacAlgorithms.HMAC_SHA_256, DigestUtils.sha256(generator.generate(15)))
                .hmacHex(to.toEscapedString() + " " + from.toEscapedString() + " " + id);
    }
}
