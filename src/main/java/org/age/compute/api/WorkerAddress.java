/*
 * Created: 2014-11-04
 * $Id$
 */

package org.age.compute.api;

import java.io.Serializable;

import javax.annotation.concurrent.ThreadSafe;

import org.checkerframework.checker.igj.qual.Immutable;

/**
 * A compute-level address for workers.
 */
@ThreadSafe
@Immutable
public interface WorkerAddress extends Serializable {}
