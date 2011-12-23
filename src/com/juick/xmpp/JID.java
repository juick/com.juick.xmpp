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
package com.juick.xmpp;

/**
 *
 * @author Ugnich Anton
 */
public class JID {

    public String Username;
    public String Host;
    public String Resource;

    public JID(String jid) {
        int at = jid.indexOf('@');
        if (at == -1) {
            Username = "";
        } else {
            Username = jid.substring(0, at);
        }
        int slash = jid.indexOf('/');
        if (slash == -1) {
            Host = jid.substring(at + 1);
            Resource = "";
        } else {
            Host = jid.substring(at + 1, slash);
            Resource = jid.substring(slash + 1);
        }
    }

    public JID(String Username, String Host, String Resource) {
        this.Username = Username;
        this.Host = Host;
        this.Resource = Resource;
    }

    public String Bare() {
        if (Username.length() > 0) {
            return Username + '@' + Host;
        } else {
            return Host;
        }
    }

    public boolean equalsBare(JID jid) {
        return Bare().toLowerCase().equals(jid.Bare().toLowerCase());
    }

    public String toString() {
        if (Resource.length() > 0) {
            return Bare() + '/' + Resource;
        } else {
            return Bare();
        }
    }
}
