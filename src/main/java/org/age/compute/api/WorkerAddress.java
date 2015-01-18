/*
 * Copyright (C) 2014 Intelligent Information Systems Group.
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
/*
 * Created: 2014-11-04.
 */

package org.age.compute.api;

import org.checkerframework.checker.igj.qual.Immutable;

import java.io.Serializable;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A compute-level address for workers.
 */
@ThreadSafe
@Immutable
public interface WorkerAddress extends Serializable {}
