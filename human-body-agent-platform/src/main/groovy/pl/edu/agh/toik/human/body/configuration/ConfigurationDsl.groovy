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

package pl.edu.agh.toik.human.body.configuration


/**
 * Entry point to configuration DSL. Reading about Groovy DSL support is recommended
 */
class ConfigurationDsl implements Configuration {

    private List<AgentDescriptor> agents = []

    static Configuration configuration(@DelegatesTo(ConfigurationDsl) Closure<Object> closure) {
        final config = new ConfigurationDsl()
        closure.delegate = config
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure()
        return config
    }

    void agent(@DelegatesTo(AgentDescriptorDsl) Closure<Object> closure) {
        final config = new AgentDescriptorDsl()
        closure.delegate = config
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure()
        agents.add(config)
    }


    @Override
    List<AgentDescriptor> agents() {
        return agents;
    }

    void repeat(long times, Closure block) {
        block.delegate = this
        block.resolveStrategy = Closure.DELEGATE_FIRST

        for (long i = 0; i < times; ++i) {
            block(i)
        }
    }
}
