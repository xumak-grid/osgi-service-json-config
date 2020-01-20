package com.xumak.grid.util;

/**
 * Created by jesquivel on 10/2/17.
 */
public final class ErrorBean {

    /**
     * Private Constructor.
     */
    private ErrorBean() {

    }

    private String error;

    /***
     * Private method for set error message.
     * @param error request error message.
     */
    private ErrorBean(final String error) {
        this.error = error;
    }

    /***
     * Get error message.
     * @return String error message.
     */
    public String getError() {
        return error;
    }

    /***
     * Set Error message.
     * @param error request error message.
     */
    public void setError(final String error) {
        this.error = error;
    }

    /***
     * Static method for create an ErrorBean object.
     * @param error request error message.
     * @return ErrorBean object.
     */
    public static ErrorBean createError(final String error) {
        return new ErrorBean(error);
    }

}
