/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler.thermostat;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import java.util.Calendar;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.handler.NetatmoModuleHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.client.model.NADevice;
import io.swagger.client.model.NADeviceListBody;
import io.swagger.client.model.NAPlug;
import io.swagger.client.model.NAThermostat;

/**
 * {@link NATherm1Handler} is the class used to handle the thermostat
 * module of a thermostat set
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 * @author Jean-Sébastien Roques - reworked to use latest Netatmo API for Thermostat - Work in progress
 *
 */
public class NATherm1Handler extends NetatmoModuleHandler {

    private static Logger logger = LoggerFactory.getLogger(NATherm1Handler.class);
    private NAThermostat naTherm1;
    private Integer setpointDefaultDuration = null;

    public NATherm1Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void bridgeHandlerInitialized(ThingHandler thingHandler, Bridge bridge) {
        super.bridgeHandlerInitialized(thingHandler, bridge);
        try {
            // Here, only 1 thermostat (module) should be retrieved as getthermostatsdata() is called using PARENT_ID
            // which contains the Netatmo Plug/Relay (device) id and there can be only 1 thermostat (module) per
            // Plug/Relay
            String naPlugId = (String) getConfig().get(PARENT_ID);
            NAPlug naPlug = bridgeHandler.getThermostatApi().getthermostatsdata(naPlugId).getBody().getDevices().get(0);
            naTherm1 = naPlug.getModules().get(0);
        } catch (Exception e) {
            logger.error("Cannot create naTherm1 handler : {}", e.getMessage());
        }
        updateStatus(ThingStatus.ONLINE);
        updateChannels();
    }

    private int getBatteryPercent(int batteryVp) {
        // With new battery, API may return a value superior to batteryMax !
        int correctedVp = Math.min(batteryVp, batteryMax);
        return (100 * (correctedVp - batteryMin) / (batteryMax - batteryMin));
    }

    private boolean isBatteryLow(int batteryVp) {
        return (batteryVp < batteryLow);
    }

    /*
     * @Override
     * protected void updateChannels() {
     * try {
     * for (Channel channel : getThing().getChannels()) {
     * String channelId = channel.getUID().getId();
     * State state = getNATherm1ChannelValue(channelId);
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
     *
     * }
     */

    @Override
    protected State getNAChannelValue(String channelId) {

        switch (channelId) {
            case CHANNEL_SETPOINT_MODE:
                return new StringType(naTherm1.getSetpoint().getSetpointMode());
            case CHANNEL_SETPOINT_TEMP:
                return new DecimalType(naTherm1.getMeasured().getSetpointTemp());
            case CHANNEL_TEMPERATURE:
                Float tmp = naTherm1.getMeasured().getTemperature();
                return new DecimalType(tmp);
            case CHANNEL_THERM_RELAY_CMD:
                return new PercentType(naTherm1.getThermRelayCmd());
            case CHANNEL_BATTERY_LEVEL:
                return new DecimalType(getBatteryPercent(naTherm1.getBatteryVp()));
            case CHANNEL_LOW_BATTERY:
                return isBatteryLow(naTherm1.getBatteryVp()) ? OnOffType.ON : OnOffType.OFF;
            case CHANNEL_LAST_MESSAGE:
                return new DateTimeType(timestampToCalendar(naTherm1.getLastMessage()));
            case CHANNEL_RF_STATUS:
                Integer rfStatus = naTherm1.getRfStatus();
                return new DecimalType(getSignalStrength(rfStatus));

            default:
                logger.debug("{} : Unknown or unsupported channel for NATherm1", channelId);
                return null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            switch (channelUID.getId()) {
                case CHANNEL_SETPOINT_MODE:
                    bridgeHandler.getThermostatApi().setthermpoint(getParentId(), getId(), command.toString(), null,
                            null);
                    break;
                case CHANNEL_SETPOINT_TEMP:

                    if (setpointDefaultDuration == null) {
                        NADeviceListBody deviceListBody = bridgeHandler.getThermostatApi()
                                .devicelist(actualApp, getParentId(), false).getBody();
                        NADevice plugDevice = deviceListBody.getDevices().get(0);
                        setpointDefaultDuration = plugDevice.getSetpointDefaultDuration();
                    }

                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.MINUTE, setpointDefaultDuration);
                    bridgeHandler.getThermostatApi().setthermpoint(getParentId(), getId(), "manual",
                            (int) (cal.getTimeInMillis() / 1000), Float.parseFloat(command.toString()));
                    break;
            }

            updateChannels();
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
        }
    }

}
