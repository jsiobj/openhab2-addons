/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler.station;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.config.NetatmoModuleConfiguration;
import org.openhab.binding.netatmo.handler.NetatmoModuleHandler;

import io.swagger.client.model.NADashboardData;

/**
 * {@link NAModule2Handler} is the class used to handle the wind module
 * capable of reporting wind angle and strength
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
public class NAModule2Handler extends NetatmoModuleHandler<NetatmoModuleConfiguration> {

    public NAModule2Handler(Thing thing) {
        super(thing, NetatmoModuleConfiguration.class);
    }

    @Override
    protected State getNAThingProperty(String chanelId) {
        NADashboardData dashboardData = module.getDashboardData();
        switch (chanelId) {
            case CHANNEL_WIND_ANGLE:
                return toDecimalType(dashboardData.getWindAngle());
            case CHANNEL_WIND_STRENGTH:
                return toDecimalType(dashboardData.getWindStrength());
            case CHANNEL_GUST_ANGLE:
                return toDecimalType(dashboardData.getGustAngle());
            case CHANNEL_GUST_STRENGTH:
                return toDecimalType(dashboardData.getGustStrength());
            default:
                return super.getNAThingProperty(chanelId);
        }
    }

}
