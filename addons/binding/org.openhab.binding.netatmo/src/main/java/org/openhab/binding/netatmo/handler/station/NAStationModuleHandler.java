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
import org.openhab.binding.netatmo.handler.NetatmoModuleHandler;

import io.swagger.client.model.NADeviceListBody;
import io.swagger.client.model.NAModule;

/**
 * {@link NAStationModuleHandler} is the class used to handle the Weather
 * Station main module
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 * @author Jean-Sébastien Roques - reworked to use latest Netatmo API for Thermostat - Work in progress
 *
 */
public class NAStationModuleHandler extends NetatmoModuleHandler {
    public NAStationModuleHandler(Thing thing) {
        super(thing);
    }

    protected void updateChannels() {
        try {
            NADeviceListBody deviceList = bridgeHandler.getStationApi().devicelist(actualApp, getParentId(), false)
                    .getBody();
            for (NAModule module : deviceList.getModules()) {
                if (module.getId().equalsIgnoreCase(getId())) {
                    // this.module = module;
                    // super.updateChannels();
                }
            }

        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
        }
    }
    /*
     * @Override
     * protected State getNAThingProperty(String chanelId) {
     * switch (chanelId) {
     * case CHANNEL_RAIN:
     * return new DecimalType(dashboard.getRain());
     * case CHANNEL_SUM_RAIN1:
     * return new DecimalType(dashboard.getSumRain1());
     * case CHANNEL_SUM_RAIN24:
     * return new DecimalType(dashboard.getSumRain24());
     * case CHANNEL_WIND_ANGLE:
     * return new DecimalType(dashboard.getWindAngle());
     * case CHANNEL_WIND_STRENGTH:
     * return new DecimalType(dashboard.getWindStrength());
     * case CHANNEL_GUST_ANGLE:
     * return new DecimalType(dashboard.getGustAngle());
     * case CHANNEL_GUST_STRENGTH:
     * return new DecimalType(dashboard.getGustStrength());
     * default:
     * return super.getNAThingProperty(chanelId);
     * }
     * }
     */

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub

    }
}