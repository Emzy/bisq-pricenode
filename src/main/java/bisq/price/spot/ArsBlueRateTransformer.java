/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.price.spot;

import bisq.price.spot.providers.BlueRateProvider;
import bisq.price.util.bluelytics.ArsBlueMarketGapProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.OptionalDouble;

@Component
@Slf4j
public class ArsBlueRateTransformer implements ExchangeRateTransformer {
    private final ArsBlueMarketGapProvider blueMarketGapProvider;

    public ArsBlueRateTransformer(ArsBlueMarketGapProvider blueMarketGapProvider) {
        this.blueMarketGapProvider = blueMarketGapProvider;
    }

    @Override
    public Optional<ExchangeRate> apply(ExchangeRateProvider provider, ExchangeRate originalExchangeRate) {
        if (provider instanceof BlueRateProvider) {
            return Optional.of(originalExchangeRate);
        }

        OptionalDouble sellGapMultiplier = blueMarketGapProvider.get();
        if (sellGapMultiplier.isEmpty()) {
            return Optional.empty();
        }

        double blueRate = originalExchangeRate.getPrice() * sellGapMultiplier.getAsDouble();

        ExchangeRate newExchangeRate = new ExchangeRate(
                originalExchangeRate.getCurrency(),
                blueRate,
                originalExchangeRate.getTimestamp(),
                originalExchangeRate.getProvider()
        );

        log.info(String.format("%s transformed from %s to %s",
                originalExchangeRate.getCurrency(), originalExchangeRate.getPrice(), blueRate));

        return Optional.of(newExchangeRate);
    }

    @Override
    public String supportedCurrency() {
        return "ARS";
    }
}
