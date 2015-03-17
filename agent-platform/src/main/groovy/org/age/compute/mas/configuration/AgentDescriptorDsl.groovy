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

import groovy.transform.EqualsAndHashCode
import org.age.compute.mas.action.Action
import org.age.compute.mas.agent.AgentBehavior
import org.age.compute.mas.exception.CannotLoadConfigurationException

@EqualsAndHashCode
class AgentDescriptorDsl implements AgentDescriptor {
    
    private final AgentDescriptorDsl parent
    private final List<AgentDescriptorDsl> children = []
    private Map<String, Object> settings = [:]

    Class<? extends AgentBehavior> agentClass
    String name = null
    List<Class<Action>> actions = []
    int quantity = 1

    AgentDescriptorDsl(AgentDescriptorDsl parent) {
        this.parent = parent
    }

    def agent(@DelegatesTo(AgentDescriptorDsl) Closure<Object> closure) {
        def child = new AgentDescriptorDsl(this)
        children.add(child)

        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = child
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

    private void printHierarchy() {
        printHierarchy(this, 0)
    }

    private void printHierarchy(AgentDescriptorDsl agent, int indent) {
        indent.times { print '  ' }
        println agent
        agent.children.each { child ->
            printHierarchy(child, indent + 1)
        }
    }


    @Override
    public String toString() {
        return "Agent(" + agentClass + ")"
    }

    @Override
    List<AgentDescriptorDsl> children() {
        return Collections.unmodifiableList(children)
    }

    @Override
    Class<? extends AgentBehavior> agentClass() {
        return agentClass
    }

    @Override
    String name() {
        return name
    }

    @Override
    Optional<AgentDescriptorDsl> parent() {
        return Optional.ofNullable(parent)
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
