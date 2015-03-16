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

package org.age.compute.agent.configuration

import org.age.compute.agent.agent.AgentBehavior
import org.assertj.core.api.Assertions
import org.testng.annotations.Test

class ConfigurationDslTest {

    static class Foo extends AgentBehavior {
        @Override
        void doStep(int stepNumber) {}
    }

    static class Bar extends AgentBehavior {
        @Override
        void doStep(int stepNumber) {}
    }

    void foo(Runnable x) {
        x.run()
    }

    @Test
    public void should_load_agents_hierarchy_properly() {
        Configuration configuration = org.age.compute.agent.configuration.ConfigurationDsl.configuration {
            workplace {
                agent {
                    name = "A"
                    agentClass = Foo

                    agent {
                        name = "A.1"
                        agentClass = Foo
                        agent {
                            name = "A.1.1"
                            agentClass = Bar
                        }
                    }
                    agent {
                        name = "A.2"
                        agentClass = Bar
                    }
                }
                agent {
                    name = "B"
                    agentClass = Bar
                }
                agent {
                    name = "C"
                    agentClass = Foo
                }
            }
        }

        final hierarchy = configuration.workplaces.get(0).agents

        Assertions.assertThat(hierarchy).hasSize(3) // A, B, C
        Assertions.assertThat(hierarchy.get(0).name).isEqualTo("A")
        Assertions.assertThat(hierarchy.get(0).agentClass).isEqualTo(Foo)
        Assertions.assertThat(hierarchy.get(0).children).hasSize(2) // A.1, A.2

        Assertions.assertThat(hierarchy.get(0).children.get(0).name).isEqualTo("A.1")
        Assertions.assertThat(hierarchy.get(0).children.get(0).agentClass).isEqualTo(Foo)
        Assertions.assertThat(hierarchy.get(0).children.get(0).children).hasSize(1) // A.1.1

        Assertions.assertThat(hierarchy.get(0).children.get(0).children.get(0).name).isEqualTo("A.1.1")
        Assertions.assertThat(hierarchy.get(0).children.get(0).children.get(0).agentClass).isEqualTo(Bar)
        Assertions.assertThat(hierarchy.get(0).children.get(0).children.get(0).children).isEmpty()

        Assertions.assertThat(hierarchy.get(0).children.get(1).name).isEqualTo("A.2")
        Assertions.assertThat(hierarchy.get(0).children.get(1).agentClass).isEqualTo(Bar)
        Assertions.assertThat(hierarchy.get(0).children.get(1).children).isEmpty()

        Assertions.assertThat(hierarchy.get(1).name).isEqualTo("B")
        Assertions.assertThat(hierarchy.get(1).agentClass).isEqualTo(Bar)
        Assertions.assertThat(hierarchy.get(1).children).isEmpty()

        Assertions.assertThat(hierarchy.get(2).name).isEqualTo("C")
        Assertions.assertThat(hierarchy.get(2).agentClass).isEqualTo(Foo)
        Assertions.assertThat(hierarchy.get(2).children).isEmpty()
    }

}
