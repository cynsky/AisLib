/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.ais.bus;

import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import dk.dma.ais.bus.configuration.AisBusConfiguration;
import dk.dma.ais.bus.configuration.consumer.StdoutConsumerConfiguration;
import dk.dma.ais.bus.configuration.consumer.TcpWriterConsumerConfiguration;
import dk.dma.ais.bus.configuration.filter.DownSampleFilterConfiguration;
import dk.dma.ais.bus.configuration.filter.DuplicateFilterConfiguration;
import dk.dma.ais.bus.configuration.filter.FilterConfiguration;
import dk.dma.ais.bus.configuration.provider.TcpClientProviderConfiguration;

public class AisBusTest {

    @Test
    public void confTest() throws JAXBException {
        AisBusConfiguration conf = new AisBusConfiguration();
        // Bus Filters
        conf.getFilters().add(new DownSampleFilterConfiguration());
        conf.getFilters().add(new DuplicateFilterConfiguration());
        // Provider
        TcpClientProviderConfiguration rrReader = new TcpClientProviderConfiguration();
        rrReader.getHostPort().add("ais163.sealan.dk:65262");
        rrReader.getFilters().add(new DownSampleFilterConfiguration(300));
        conf.getProviders().add(rrReader);              
        
        // Consumer
        StdoutConsumerConfiguration stdoutConsumer = new StdoutConsumerConfiguration();
        stdoutConsumer.getFilters().add(new DownSampleFilterConfiguration(600));
        stdoutConsumer.setConsumerPullMaxElements(1);        
        conf.getConsumers().add(stdoutConsumer);
        
        // Consumer
        TcpWriterConsumerConfiguration tcpWriter = new TcpWriterConsumerConfiguration();
        tcpWriter.setPort(8089);
        tcpWriter.setHost("localhost");
        conf.getConsumers().add(tcpWriter);
        
        // Save
        AisBusConfiguration.save("aisbus.xml", conf);
        
        // Load
        conf = AisBusConfiguration.load("aisbus.xml");      
        Assert.assertEquals(conf.getBusQueueSize(), 10000);        
        
    }
    
    @Test
    public void confLoadTest() throws JAXBException {
        AisBusConfiguration conf = AisBusConfiguration.load("src/main/resources/aisbus-example.xml");
        Assert.assertEquals(conf.getBusQueueSize(), 10000);
        List<FilterConfiguration> filters = conf.getFilters();
        for (FilterConfiguration filter : filters) {
            if (filter instanceof DownSampleFilterConfiguration) {
                Assert.assertEquals(((DownSampleFilterConfiguration)filter).getSamplingRate(), 60);
            }
            else if (filter instanceof DuplicateFilterConfiguration) {
                Assert.assertEquals(((DuplicateFilterConfiguration)filter).getWindowSize(), 10000);
            }
            else {
                Assert.fail();
            }
        }
//        StdoutConsumerConfiguration consumerConf = (StdoutConsumerConfiguration)conf.getConsumers().get(1);
//        Assert.assertEquals(consumerConf.getConsumerPullMaxElements(), 1);
    }
    
    @Test
    public void factoryTest() throws JAXBException, InterruptedException {
        AisBus aisBus = AisBusFactory.get("src/main/resources/aisbus-example.xml");
        aisBus.start();
        aisBus.startConsumers();
        aisBus.startProviders();
        
        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        
    }


}
