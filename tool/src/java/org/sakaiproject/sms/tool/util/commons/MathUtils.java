/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.sms.tool.util.commons;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import org.sakaiproject.sms.tool.util.commons.exception.LocalizedFormats;



/**
 * Some useful additions to the built-in functions in {@link Math}.
 * @version $Revision$ $Date$
 */
public final class MathUtils {

    /** Smallest positive number such that 1 - EPSILON is not numerically equal to 1. */
    public static final double EPSILON = 0x1.0p-53;

    /** Safe minimum, such that 1 / SAFE_MIN does not overflow.
     * <p>In IEEE 754 arithmetic, this is also the smallest normalized
     * number 2<sup>-1022</sup>.</p>
     */
    public static final double SAFE_MIN = 0x1.0p-1022;

    /**
     * 2 &pi;.
     * @since 2.1
     */
    public static final double TWO_PI = 2 * Math.PI;

    /** -1.0 cast as a byte. */
    private static final byte  NB = (byte)-1;

    /** -1.0 cast as a short. */
    private static final short NS = (short)-1;

    /** 1.0 cast as a byte. */
    private static final byte  PB = (byte)1;

    /** 1.0 cast as a short. */
    private static final short PS = (short)1;

    /** 0.0 cast as a byte. */
    private static final byte  ZB = (byte)0;

    /** 0.0 cast as a short. */
    private static final short ZS = (short)0;

    /** Gap between NaN and regular numbers. */
    private static final int NAN_GAP = 4 * 1024 * 1024;

    /** Offset to order signed double numbers lexicographically. */
    private static final long SGN_MASK = 0x8000000000000000L;

    /** All long-representable factorials */
    private static final long[] FACTORIALS = new long[] {
                       1l,                  1l,                   2l,
                       6l,                 24l,                 120l,
                     720l,               5040l,               40320l,
                  362880l,            3628800l,            39916800l,
               479001600l,         6227020800l,         87178291200l,
           1307674368000l,     20922789888000l,     355687428096000l,
        6402373705728000l, 121645100408832000l, 2432902008176640000l };

    /**
     * Private Constructor
     */
    private MathUtils() {
        super();
    }

    
	 /**
    	* For a byte value x, this method returns (byte)(+1) if x >= 0 and
     	* (byte)(-1) if x < 0.
     	*
     	* @param x the value, a byte
     	* @return (byte)(+1) or (byte)(-1), depending on the sign of x
     	*/
 	public static byte indicator(final byte x) {
 	return (x >= ZB) ? PB : NB;
 	}
    
    
     /**
     * Round the given value to the specified number of decimal places. The
     * value is rounded using the {@link BigDecimal#ROUND_HALF_UP} method.
     *
     * @param x the value to round.
     * @param scale the number of digits to the right of the decimal point.
     * @return the rounded value.
     * @since 1.1
     */
    public static double round(double x, int scale) {
        return round(x, scale, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Round the given value to the specified number of decimal places. The
     * value is rounded using the given method which is any method defined in
     * {@link BigDecimal}.
     *
     * @param x the value to round.
     * @param scale the number of digits to the right of the decimal point.
     * @param roundingMethod the rounding method as defined in
     *        {@link BigDecimal}.
     * @return the rounded value.
     * @since 1.1
     */
    public static double round(double x, int scale, int roundingMethod) {
        try {
            return (new BigDecimal
                   (Double.toString(x))
                   .setScale(scale, roundingMethod))
                   .doubleValue();
        } catch (NumberFormatException ex) {
            if (Double.isInfinite(x)) {
                return x;
            } else {
                return Double.NaN;
            }
        }
    }
    
    
    /**
     * Get the next machine representable number after a number, moving
     * in the direction of another number.
     * <p>
     * If <code>direction</code> is greater than or equal to<code>d</code>,
     * the smallest machine representable number strictly greater than
     * <code>d</code> is returned; otherwise the largest representable number
     * strictly less than <code>d</code> is returned.</p>
     * <p>
     * If <code>d</code> is NaN or Infinite, it is returned unchanged.</p>
     *
     * @param d base number
     * @param direction (the only important thing is whether
     * direction is greater or smaller than d)
     * @return the next machine representable number in the specified direction
     * @since 1.2
     */
    public static double nextAfter(double d, double direction) {

        // handling of some important special cases
        if (Double.isNaN(d) || Double.isInfinite(d)) {
                return d;
        } else if (d == 0) {
                return (direction < 0) ? -Double.MIN_VALUE : Double.MIN_VALUE;
        }
        // special cases MAX_VALUE to infinity and  MIN_VALUE to 0
        // are handled just as normal numbers

        // split the double in raw components
        long bits     = Double.doubleToLongBits(d);
        long sign     = bits & 0x8000000000000000L;
        long exponent = bits & 0x7ff0000000000000L;
        long mantissa = bits & 0x000fffffffffffffL;

        if (d * (direction - d) >= 0) {
                // we should increase the mantissa
                if (mantissa == 0x000fffffffffffffL) {
                        return Double.longBitsToDouble(sign |
                                        (exponent + 0x0010000000000000L));
                } else {
                        return Double.longBitsToDouble(sign |
                                        exponent | (mantissa + 1));
                }
        } else {
                // we should decrease the mantissa
                if (mantissa == 0L) {
                        return Double.longBitsToDouble(sign |
                                        (exponent - 0x0010000000000000L) |
                                        0x000fffffffffffffL);
                } else {
                        return Double.longBitsToDouble(sign |
                                        exponent | (mantissa - 1));
                }
        }

    }

    
    

    /**
     * Round the given value to the specified number of decimal places. The
     * value is rounding using the {@link BigDecimal#ROUND_HALF_UP} method.
     *
     * @param x the value to round.
     * @param scale the number of digits to the right of the decimal point.
     * @return the rounded value.
     * @since 1.1
     */
    public static float round(float x, int scale) {
        return round(x, scale, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Round the given value to the specified number of decimal places. The
     * value is rounded using the given method which is any method defined in
     * {@link BigDecimal}.
     *
     * @param x the value to round.
     * @param scale the number of digits to the right of the decimal point.
     * @param roundingMethod the rounding method as defined in
     *        {@link BigDecimal}.
     * @return the rounded value.
     * @since 1.1
     */
    public static float round(float x, int scale, int roundingMethod) {
        float sign = indicator(x);
        float factor = (float)Math.pow(10.0f, scale) * sign;
        return (float)roundUnscaled(x * factor, sign, roundingMethod) / factor;
    }

    
     	 /**
     	* For a float value x, this method returns +1.0F if x >= 0 and -1.0F if x <
     	* 0. Returns <code>NaN</code> if <code>x</code> is <code>NaN</code>.
     	*
     	* @param x the value, a float
     	* @return +1.0F or -1.0F, depending on the sign of x
     	*/
     	public static float indicator(final float x) {
     	if (Float.isNaN(x)) {
     	return Float.NaN;
     	}
    	return (x >= 0.0F) ? 1.0F : -1.0F;
     	} 
    
    
    /**
     * Round the given non-negative, value to the "nearest" integer. Nearest is
     * determined by the rounding method specified. Rounding methods are defined
     * in {@link BigDecimal}.
     *
     * @param unscaled the value to round.
     * @param sign the sign of the original, scaled value.
     * @param roundingMethod the rounding method as defined in
     *        {@link BigDecimal}.
     * @return the rounded value.
     * @since 1.1
     */
    private static double roundUnscaled(double unscaled, double sign,
        int roundingMethod) {
        switch (roundingMethod) {
        case BigDecimal.ROUND_CEILING :
            if (sign == -1) {
                unscaled = Math.floor(nextAfter(unscaled, Double.NEGATIVE_INFINITY));
            } else {
                unscaled = Math.ceil(nextAfter(unscaled, Double.POSITIVE_INFINITY));
            }
            break;
        case BigDecimal.ROUND_DOWN :
            unscaled = Math.floor(nextAfter(unscaled, Double.NEGATIVE_INFINITY));
            break;
        case BigDecimal.ROUND_FLOOR :
            if (sign == -1) {
                unscaled = Math.ceil(nextAfter(unscaled, Double.POSITIVE_INFINITY));
            } else {
                unscaled = Math.floor(nextAfter(unscaled, Double.NEGATIVE_INFINITY));
            }
            break;
        case BigDecimal.ROUND_HALF_DOWN : {
            unscaled = nextAfter(unscaled, Double.NEGATIVE_INFINITY);
            double fraction = unscaled - Math.floor(unscaled);
            if (fraction > 0.5) {
                unscaled = Math.ceil(unscaled);
            } else {
                unscaled = Math.floor(unscaled);
            }
            break;
        }
        case BigDecimal.ROUND_HALF_EVEN : {
            double fraction = unscaled - Math.floor(unscaled);
            if (fraction > 0.5) {
                unscaled = Math.ceil(unscaled);
            } else if (fraction < 0.5) {
                unscaled = Math.floor(unscaled);
            } else {
                // The following equality test is intentional and needed for rounding purposes
                if (Math.floor(unscaled) / 2.0 == Math.floor(Math
                    .floor(unscaled) / 2.0)) { // even
                    unscaled = Math.floor(unscaled);
                } else { // odd
                    unscaled = Math.ceil(unscaled);
                }
            }
            break;
        }
        case BigDecimal.ROUND_HALF_UP : {
            unscaled = nextAfter(unscaled, Double.POSITIVE_INFINITY);
            double fraction = unscaled - Math.floor(unscaled);
            if (fraction >= 0.5) {
                unscaled = Math.ceil(unscaled);
            } else {
                unscaled = Math.floor(unscaled);
            }
            break;
        }
        case BigDecimal.ROUND_UNNECESSARY :
            if (unscaled != Math.floor(unscaled)) {
                throw new ArithmeticException("Inexact result from rounding");
            }
            break;
        case BigDecimal.ROUND_UP :
            unscaled = Math.ceil(nextAfter(unscaled,  Double.POSITIVE_INFINITY));
            break;
        default :
            throw MathRuntimeException.createIllegalArgumentException(
                  LocalizedFormats.INVALID_ROUNDING_METHOD,
                  roundingMethod,
                  "ROUND_CEILING",     BigDecimal.ROUND_CEILING,
                  "ROUND_DOWN",        BigDecimal.ROUND_DOWN,
                  "ROUND_FLOOR",       BigDecimal.ROUND_FLOOR,
                  "ROUND_HALF_DOWN",   BigDecimal.ROUND_HALF_DOWN,
                  "ROUND_HALF_EVEN",   BigDecimal.ROUND_HALF_EVEN,
                  "ROUND_HALF_UP",     BigDecimal.ROUND_HALF_UP,
                  "ROUND_UNNECESSARY", BigDecimal.ROUND_UNNECESSARY,
                  "ROUND_UP",          BigDecimal.ROUND_UP);
        }
        return unscaled;
    }

    
}
