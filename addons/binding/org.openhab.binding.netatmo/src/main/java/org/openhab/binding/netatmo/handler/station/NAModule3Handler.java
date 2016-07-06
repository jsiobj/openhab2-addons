/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler.station;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.CHANNEL_RAIN;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.config.NetatmoModuleConfiguration;
import org.openhab.binding.netatmo.handler.NetatmoModuleHandler;

/**
 * {@link NAModule3Handler} is the class used to handle the Rain Gauge
 * capable of measuring precipitation
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
public class NAModule3Handler extends NetatmoModuleHandler<NetatmoModuleConfiguration> {

    public NAModule3Handler(Thing thing) {
        super(thing, NetatmoModuleConfiguration.class);
    }

    @Override
    protected State getNAThingProperty(String chanelId) {
        switch (chanelId) {
            case CHANNEL_RAIN:
                return toDecimalType(module.getDashboardData().getRain());
            default:
                return super.getNAThingProperty(chanelId);
        }
    }

}
