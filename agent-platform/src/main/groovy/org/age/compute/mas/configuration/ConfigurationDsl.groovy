/*
 * Copyright (C) 2014-2015 Intelligent Information Systems Group.
 *
 * This file is part of AgE.
 *
 * AgE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AgE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AgE.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.age.compute.mas.configuration

import java.time.Duration

/**
 * Entry point to configuration DSL. Reading about Groovy DSL support is recommended
 */
class ConfigurationDsl implements Configuration {
    
    private List<WorkplaceDescriptorDsl> workplaces = []
    private StopCondition stopCondition = new InfiniteStopCondition()
    
    static Configuration configuration(@DelegatesTo(ConfigurationDsl) Closure<Object> closure) {
        final config = new ConfigurationDsl()
        closure.delegate = config
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure()
        return config
    }
    
    void workplace(@DelegatesTo(WorkplaceDescriptorDsl) Closure<Object> closure) {
        final config = new WorkplaceDescriptorDsl()
        closure.delegate = config
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure()
        workplaces.add(config)
    }

    def propertyMissing(String name) {
        throw new CannotLoadConfigurationException("Unknown property: $name")
    }

    def propertyMissing(String name, value) {
        throw new CannotLoadConfigurationException("Unknown property: $name")
    }

    @Override
    List<WorkplaceDescriptor> workplaces() {
        return workplaces
    }

    @Override
    StopCondition stopCondition() {
        return stopCondition
    }
    
    void setStopCondition(Class<? extends StopCondition> stopConditionClass) {
        stopCondition = stopConditionClass.newInstance()
    }

    void setStopCondition(StopCondition stopCondition) {
        this.stopCondition = stopCondition
    }

    void setComputationDurationInSeconds(long seconds) {
        this.stopCondition = new TimedStopCondition(Duration.ofSeconds(seconds))
    }
    
    void repeat(int times, Closure block) {
        block.delegate = this
        block.resolveStrategy = Closure.DELEGATE_FIRST
        
        for (int i = 0; i < times; ++i) {
            block(i)
        }
    }

}
