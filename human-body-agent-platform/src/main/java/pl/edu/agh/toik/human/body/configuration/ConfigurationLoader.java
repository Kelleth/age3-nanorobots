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

package pl.edu.agh.toik.human.body.configuration;

import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import static org.age.compute.mas.util.TimeMeasurement.measureTime;

public final class ConfigurationLoader {

    private ConfigurationLoader() {
    }

    public static Configuration load(final Reader configuration) {
        return measureTime(() -> {
            final ImportCustomizer importCustomizer = new ImportCustomizer();
            importCustomizer.addStaticImport("org.age.compute.mas.configuration.ConfigurationDsl", "configuration");

            final CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
            compilerConfiguration.addCompilationCustomizers(importCustomizer);

            final GroovyShell gs = new GroovyShell(compilerConfiguration);

            return (Configuration) gs.evaluate(configuration);
        }, "Configuration loaded in: ");
    }

    public static Configuration loadFromClassPath(final String configuration) {
        return load(ConfigurationLoader.class.getClassLoader().getResourceAsStream(configuration));
    }

    public static Configuration load(final InputStream configuration) {
        return load(new InputStreamReader(configuration));
    }

    public static Configuration load(final String configuration) {
        return load(new StringReader(configuration));
    }
}
