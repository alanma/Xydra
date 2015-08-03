package org.xydra.core.serialize.json;

/**
 * _S_AX-like _A_PI for _J_SON.
 *
 * @author xamde
 *
 */
public interface SAJ {

    /**
     * Parsing a primitive double value
     *
     * @param d
     * @throws JSONException
     */
    void onDouble(double d) throws JSONException;

    /**
     * Parsing a primitive integer value
     *
     * @param i
     * @throws JSONException
     */
    void onInteger(int i) throws JSONException;

    /**
     * Parsing a primitive boolean value
     *
     * @param b
     * @throws JSONException
     */
    void onBoolean(boolean b) throws JSONException;

    /**
     * Parsing a primitive string value
     *
     * @param s
     * @throws JSONException
     */
    void onString(String s) throws JSONException;

    /**
     * @throws JSONException
     */
    void arrayStart() throws JSONException;

    /**
     * @throws JSONException
     */
    void arrayEnd() throws JSONException;

    /**
     * @throws JSONException
     */
    void objectStart() throws JSONException;

    /**
     * @throws JSONException
     */
    void objectEnd() throws JSONException;

    /**
     * Parsing a primitive null value
     *
     * @throws JSONException
     */
    void onNull() throws JSONException;

    /**
     * Parsing a primitive long value
     *
     * @param l
     * @throws JSONException
     */
    void onLong(long l) throws JSONException;

    /**
     * Only legal within 'objectStart' and 'objectEnd'
     *
     * @param key
     * @throws JSONException
     */
    void onKey(String key) throws JSONException;
}
