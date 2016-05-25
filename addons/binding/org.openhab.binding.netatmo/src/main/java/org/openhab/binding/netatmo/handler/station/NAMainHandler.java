/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler.station;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.netatmo.handler.NetatmoDeviceHandler;

//import io.swagger.client.model.NADeviceListBody;

/**
 * {@link NAMainHandler} is the base class for all current Netatmo
 * weather station equipments (both modules and devices)
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 * @author Jean-Sébastien Roques - reworked to use latest Netatmo API for Thermostat - Work in progress
 *
 */
public class NAMainHandler extends NetatmoDeviceHandler {
    public NAMainHandler(Thing thing) {
        super(thing);
    }

    protected void updateChannels() {
        try {
            // Aargh, this silently fails and screws the runnable if the api is not responding as expected (e.g. to many
            // queries
            // NADeviceListBody deviceList = bridgeHandler.getStationApi().devicelist(actualApp, getId(),
            // false).getBody();
            // device = deviceList.getDevices().get(0);

            // super.updateChannels();
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub

    }

}
