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
public interface XmppListener {

    /**
     * This event is sent when a parser or connection error occurs.
     */
    public void onConnectionFailed(final String msg);

    /**
     * This event occurs when the login/authentication process succeeds.
     */
    public void onAuth(final String resource);

    /**
     * This event occurs when the login/authentication process fails.
     *
     * @param message some error information
     */
    public void onAuthFailed(final String message);
}
