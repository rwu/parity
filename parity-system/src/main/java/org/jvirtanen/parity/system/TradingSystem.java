package org.jvirtanen.parity.system;

import static org.jvirtanen.parity.util.Applications.*;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import org.joda.time.LocalDate;
import org.jvirtanen.config.Configs;

class TradingSystem {

    public static final long EPOCH_MILLIS = new LocalDate().toDateTimeAtStartOfDay().getMillis();

    public static void main(String[] args) throws IOException {
        if (args.length != 1)
            usage("parity-system <configuration-file>");

        try {
            main(config(args[0]));
        } catch (ConfigException | FileNotFoundException e) {
            error(e);
        }
    }

    private static void main(Config config) throws IOException {
        String      marketDataSession        = config.getString("market-data.session");
        InetAddress marketDataMulticastGroup = Configs.getInetAddress(config, "market-data.multicast-group");
        int         marketDataMulticastPort  = Configs.getPort(config, "market-data.multicast-port");
        int         marketDataRequestPort    = Configs.getPort(config, "market-data.request-port");

        MarketDataServer marketData = MarketDataServer.create(marketDataSession,
                new InetSocketAddress(marketDataMulticastGroup, marketDataMulticastPort),
                marketDataRequestPort);

        int orderEntryPort = Configs.getPort(config, "order-entry.port");

        OrderEntryServer orderEntry = OrderEntryServer.create(orderEntryPort);

        marketData.version();

        new Events(marketData, orderEntry).run();
    }

}
