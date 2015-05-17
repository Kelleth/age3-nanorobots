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

import groovy.transform.EqualsAndHashCode
import pl.edu.agh.toik.human.body.Action

@EqualsAndHashCode
class AgentDescriptorDsl implements AgentDescriptor {

    private Map<String, Object> settings = [:]

    AgentDescriptor.AgentClass agentClass
    String name = null
    List<Class<Action>> actions = []
    int quantity = 1

    AgentDescriptorDsl() {
    }

    def agent(@DelegatesTo(AgentDescriptorDsl) Closure<Object> closure) {
        closure.resolveStrategy = Closure.TO_SELF
        closure()
    }

    def propertyMissing(String name) {
        throw new CannotLoadConfigurationException("Unknown property: $name, at agent: $agentPath")
    }

    def propertyMissing(String name, value) {
        throw new CannotLoadConfigurationException("Unknown property: $name, at agent: $agentPath")
    }

    private String getAgentPath() {
        (parent != null ? parent.agentPath + " >> " : "") + (name != null ? name : "<anonymous agent>")
    }


    @Override
    public String toString() {
        return "Agent(" + agentClass + ")"
    }

    @Override
    AgentDescriptor.AgentClass agentClass() {
        return agentClass
    }

    @Override
    String name() {
        return name
    }

    @Override
    Map<String, Object> settings() {
        return settings
    }

    @Override
    List<Class<Action>> actions() {
        return actions
    }

    void setSettings(Map<String, Object> settings) {
        this.settings = settings
    }
    
    void settings(Closure<Object> mapContent) {
        def builder = new MapBuilder()
        mapContent.resolveStrategy = Closure.DELEGATE_FIRST
        mapContent.delegate = builder
        mapContent()
        this.settings = builder.map
    }
    
}
