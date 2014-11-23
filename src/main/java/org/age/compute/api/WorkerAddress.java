/*
 * Created: 2014-11-04
 * $Id$
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
