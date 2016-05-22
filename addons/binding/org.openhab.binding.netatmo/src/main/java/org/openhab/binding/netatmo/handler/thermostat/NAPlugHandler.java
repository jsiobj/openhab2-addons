/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler.thermostat;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.handler.NetatmoBridgeHandler;
import org.openhab.binding.netatmo.handler.NetatmoDeviceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.client.model.NAPlace;
import io.swagger.client.model.NAPlug;

/**
 * {@link NAPlugHandler} is the class used to handle the plug
 * device of a thermostat set
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 * @author Jean-Sébastien Roques - reworked to use latest Netatmo API for Thermostat - Work in progress
 *
 */
public class NAPlugHandler extends NetatmoDeviceHandler {

    private static Logger logger = LoggerFactory.getLogger(NAPlugHandler.class);
    private NAPlug naPlug;
    // private Configuration config = this.get

    public NAPlugHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void bridgeHandlerInitialized(ThingHandler thingHandler, Bridge bridge) {
        logger.debug("Initialiazing bridge for thing : {}", this.getThing().getLabel());
        super.bridgeHandlerInitialized(thingHandler, bridge);
        try {
            // Here, only 1 device should be retrieved as getthermostatsdata is called using EQUIPEMENT_ID
            // which contains the netatmo device (Plug) id
            bridgeHandler = (NetatmoBridgeHandler) getBridge().getHandler();
            naPlug = bridgeHandler.getThermostatApi().getthermostatsdata((String) getConfig().get(EQUIPMENT_ID))
                    .getBody().getDevices().get(0);
        } catch (Exception e) {
            logger.error("Cannot create NAPlugHandler : {}", e.getMessage());
        }

        updateStatus(ThingStatus.ONLINE);
        updateChannels();
    }

    @Override
    protected State getNAChannelValue(String channelId) {

        switch (channelId) {

            case CHANNEL_LAST_STATUS_STORE:
                return new DateTimeType(timestampToCalendar(naPlug.getLastStatusStore()));
            case CHANNEL_LOCATION:
                NAPlace place = naPlug.getPlace();
                return new PointType(new DecimalType(place.getLocation().get(1)),
                        new DecimalType(place.getLocation().get(0)), new DecimalType(place.getAltitude()));
            case CHANNEL_WIFI_STATUS:
                Integer wifiStatus = naPlug.getWifiStatus();
                return new DecimalType(getSignalStrength(wifiStatus));

            default:
                logger.warn("{} : Unknown or unsupported channel for NAPlug", channelId);
                return null;
        }
    }

    /*
     * @Override
     * protected void updateChannels() {
     * try {
     * // bridgeHandler = (NetatmoBridgeHandler) getBridge().getHandler();
     *
     * for (Channel channel : getThing().getChannels()) {
     * String channelId = channel.getUID().getId();
     * State state = getNAPlugChannelValue(channelId);
     * if (state != null) {
     * logger.debug("Update state for channel {}. New state is {}", channelId, state);
     * updateState(channel.getUID(), state);
     * } else {
     * logger.warn("Could not get value for channel {}", channelId);
     * }
     * }
     * super.updateChannels();
     * } catch (Exception e) {
     * updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
     * }
     * }
     */

}
