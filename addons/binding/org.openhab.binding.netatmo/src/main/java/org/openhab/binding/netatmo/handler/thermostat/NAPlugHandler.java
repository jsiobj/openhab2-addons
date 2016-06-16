/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler.thermostat;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.handler.NetatmoBridgeHandler;
import org.openhab.binding.netatmo.handler.NetatmoDeviceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.client.api.ThermostatApi;
import io.swagger.client.model.NAPlace;
import io.swagger.client.model.NAPlug;
import io.swagger.client.model.NAThermostat;
import io.swagger.client.model.NAThermostatDataResponse;

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
    ThermostatApi thermostatApi;

    public NAPlugHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void bridgeHandlerInitialized(ThingHandler thingHandler, Bridge bridge) {
        logger.debug("Initialiazing bridge for thing : {}", this.getThing().getLabel());
        super.bridgeHandlerInitialized(thingHandler, bridge);
        try {
            bridgeHandler = (NetatmoBridgeHandler) getBridge().getHandler();
            thermostatApi = bridgeHandler.getThermostatApi();
        } catch (Exception e) {
            logger.error("Cannot create NAPlugHandler : {}", e.getMessage());
        }
        logger.debug("First data refresh");
        updateChannels();
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void initialize() {
        super.initialize();
        logger.debug("Scheduling data refresh");
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                updateChannels();
            }
        }, 1, configuration.refreshInterval, TimeUnit.SECONDS);
    }

    public void updateChannels() {

        NAThermostatDataResponse naThermDataResponse = null;
        NAPlug naPlug;

        try {
            naThermDataResponse = thermostatApi.getthermostatsdata((String) getConfig().get(EQUIPMENT_ID));
            naPlug = naThermDataResponse.getBody().getDevices().get(0); // This should pull out only one device as
                                                                        // EQUIPEMENT_ID was specified in
                                                                        // getthermostatsdata
        } catch (Exception e) {
            logger.error("Cannot get thermostat data : {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            return;
        }

        try {
            for (Channel channel : getThing().getChannels()) {
                String channelId = channel.getUID().getId();
                State state = getNAChannelValue(naPlug, channelId);
                if (state != null) {
                    logger.debug("Update state for channel {}. New state is {}", channelId, state);
                    updateState(channel.getUID(), state);
                } else {
                    logger.warn("Could not get value for channel {}", channelId);
                }
            }
            // super.updateChannels();
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
        }

        // Let's call updateChannels for thermostat "child" object with data just pulled out with getthermostatsdata()
        // There is only one thermostat for a plug, no need for a loop
        NAThermostat naTherm1 = naPlug.getModules().get(0);
        String naThermThingIdString = naTherm1.getId().replaceAll("[^a-zA-Z0-9_]", "");
        ThingUID naThermThingId = new ThingUID(THERM1_THING_TYPE, bridgeHandler.getThing().getUID(),
                naThermThingIdString);
        NATherm1Handler naThermHandler = (NATherm1Handler) bridgeHandler.getThingByUID(naThermThingId).getHandler();
        naThermHandler.updateChannels(naTherm1);
    }

    protected State getNAChannelValue(NAPlug naPlug, String channelId) {

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

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
        } else {
            logger.warn("This Thing is read-only and can only handle REFRESH command");
        }
    }

}
