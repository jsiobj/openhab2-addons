/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.config.NetatmoBridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.client.ApiClient;
import io.swagger.client.api.StationApi;
import io.swagger.client.api.ThermostatApi;
import io.swagger.client.auth.OAuth;
import io.swagger.client.auth.OAuthFlow;
import io.swagger.client.model.NAUserAdministrative;
import retrofit.RestAdapter.LogLevel;

/**
 * {@link NetatmoBridgeHandler} is the handler for a Netatmo API and connects it
 * to the framework. The devices and modules uses the
 * {@link NetatmoBridgeHandler} to request informations about their status
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 * @author Jean-Sébastien Roques - reworked to use latest Netatmo API for Thermostat - Work in progress
 *
 */
public class NetatmoBridgeHandler extends BaseBridgeHandler {

    private static Logger logger = LoggerFactory.getLogger(NetatmoBridgeHandler.class);

    private NetatmoBridgeConfiguration configuration;
    protected NAUserAdministrative admin;

    public ApiClient apiClient;
    public StationApi stationApi;
    public ThermostatApi thermostatApi;

    public NetatmoBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Netatmo API bridge handler.");

        configuration = getConfigAs(NetatmoBridgeConfiguration.class);

        apiClient = new ApiClient();

        // We'll use TrustingOkHttpClient because Netatmo certificate is a StartTTLS
        // not trusted by default java certificate control mechanism
        OAuth auth = new OAuth(new TrustingOkHttpClient(),
                OAuthClientRequest.tokenLocation("https://api.netatmo.net/oauth2/token"));
        auth.setFlow(OAuthFlow.password);
        auth.setAuthenticationRequestBuilder(OAuthClientRequest.authorizationLocation(""));

        apiClient.getApiAuthorizations().put("password_oauth", auth);
        apiClient.getTokenEndPoint().setClientId(configuration.clientId).setClientSecret(configuration.clientSecret)
                .setUsername(configuration.username).setPassword(configuration.password);

        apiClient.configureFromOkclient(new TrustingOkHttpClient());
        apiClient.getTokenEndPoint().setScope(getApiScope());
        apiClient.getAdapterBuilder().setLogLevel(LogLevel.NONE);

        try {
            getStationApi();
            getThermostatApi();

            // Get user administrative data from either station or thermostat API
            if (configuration.readStation) {
                admin = stationApi.getuser().getBody().getAdministrative();
            } else if (configuration.readThermostat) {
                admin = thermostatApi.getthermostatsdata(null).getBody().getUser().getAdministrative();
            } else {
                throw new Exception("Please select at least Station or Thermostat in configuration");
            }
        } catch (Exception e) {
            logger.error("Failed to get API data");
        }

        updateStatus(ThingStatus.ONLINE);
        updateChannels();

    }

    private void updateChannels() {
        logger.debug("Updating Netatmo bridge channels");

        for (Channel channel : getThing().getChannels()) {
            String channelId = channel.getUID().getId();
            State state = getNAChannelValue(channelId);
            if (state != null) {
                logger.debug("Update state for channel {}. New state is {}", channelId, state);
                updateState(channel.getUID(), state);
            } else {
                logger.warn("Could not get value for channel {}", channelId);
            }

        }
    }

    private State getNAChannelValue(String channelId) {
        /*
         * TODO : translate numeric values to significative text
         *
         * unit : 0 -> metric system, 1 -> imperial system
         * windunit: 0 -> kph, 1 -> mph, 2 -> ms, 3 -> beaufort, 4 -> knot
         * pressureunit: 0 -> mbar, 1 -> inHg, 2 -> mmHg
         * lang: user locale reg_locale: user regional preferences (used for displaying date)
         * feel_like: algorithme used to compute feel like temperature, 0 -> humidex, 1 ->
         * heat-indexCzQCOSte9IEKCGxKTb21
         */

        switch (channelId) {
            case CHANNEL_UNIT:
                return new DecimalType(admin.getUnit());
            case CHANNEL_WIND_UNIT:
                return new DecimalType(admin.getWindunit());
            case CHANNEL_PRESSURE_UNIT:
                return new DecimalType(admin.getPressureunit());

            default:
                logger.warn("{} : Unknown or unsupported channel for Netatmo Bridge", channelId);
                return null;
        }
    }

    private String getApiScope() {
        StringBuilder stringBuilder = new StringBuilder();

        if (configuration.readStation) {
            stringBuilder.append("read_station ");
        }

        if (configuration.readThermostat) {
            stringBuilder.append("read_thermostat write_thermostat ");
        }

        return stringBuilder.toString().trim();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.warn("This Bridge is read-only and cannot handle commands");
    }

    public StationApi getStationApi() throws Exception {
        if (configuration.readStation) {
            if (stationApi == null) {
                stationApi = apiClient.createService(StationApi.class);
            }
            return stationApi;
        }

        throw new Exception("Configuration does not allow access to StationApi");
    }

    public ThermostatApi getThermostatApi() throws Exception {
        if (configuration.readThermostat) {
            if (thermostatApi == null) {
                thermostatApi = apiClient.createService(ThermostatApi.class);
            }
            return thermostatApi;
        }
        throw new Exception("Configuration does not allow access to ThermostatApi");
    }
}
