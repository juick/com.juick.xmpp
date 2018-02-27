/*
 * Juick
 * Copyright (C) 2008-2013, ugnich
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
package com.juick.xmpp.helpers;

import org.apache.commons.text.StringEscapeUtils;

/**
 *
 * @author ugnich
 */
public class XMPPError {

    public static final class Type {

        public static final String auth = "auth";
        public static final String cancel = "cancel";
        public static final String continue_ = "continue";
        public static final String modify = "modify";
        public static final String wait = "wait";
    }
    private final static String TagName = "error";
    public String by = null;
    private String type;
    private String condition;
    private String text = null;

    public XMPPError(String type, String condition) {
        this.type = type;
        this.condition = condition;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("<").append(TagName).append("");
        if (by != null) {
            str.append(" by=\"").append(StringEscapeUtils.escapeXml10(by)).append("\"");
        }
        if (type != null) {
            str.append(" type=\"").append(StringEscapeUtils.escapeXml10(type)).append("\"");
        }

        if (condition != null) {
            str.append(">");
            str.append("<").append(StringEscapeUtils.escapeXml10(condition)).append(" xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\"");
            if (text != null) {
                str.append(">").append(StringEscapeUtils.escapeXml10(text)).append("</").append(StringEscapeUtils.escapeXml10(condition))
                    .append(">");
            } else {
                str.append("/>");
            }
            str.append("</").append(TagName).append(">");
        } else {
            str.append("/>");
        }

        return str.toString();
    }
}
