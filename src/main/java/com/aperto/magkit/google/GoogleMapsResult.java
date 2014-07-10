package com.aperto.magkit.google;

import java.math.BigDecimal;

/**
 * Class Representing a GoogleMaps Result.
 *
 * @author Mathias Broekelmann
 * @author Mayo Fragoso
 */
public class GoogleMapsResult {
    private BigDecimal _longitude;
    private BigDecimal _latitude;
    private String _street;
    private String _zipcode;
    private String _city;
    private String _country;
    private String _address;

    /**
     * @param street the street value from google maps for this position
     */
    public void setStreet(String street) {
        _street = street;
    }

    /**
     * @param zipcode the zipcode value from google maps for this position
     */
    public void setZipcode(String zipcode) {
        _zipcode = zipcode;
    }

    /**
     * @param city the city value from google maps for this position
     */
    public void setCity(String city) {
        _city = city;
    }

    /**
     * @param country the country value from google maps for this position
     */
    public void setCountry(String country) {
        _country = country;
    }

    /**
     * @param longitude longitude value from google maps for this position
     */
    public void setLongitude(BigDecimal longitude) {
        _longitude = longitude;
    }

    /**
     * @param latitude latitude value from google maps for this position
     */
    public void setLatitude(BigDecimal latitude) {
        _latitude = latitude;
    }

    /**
     * Allows to set the longitude and latitude as comma separated coordinates string (lng,lat).
     * @param coordinates coordinates value from google maps for this position
     */
    public void setCoordinates(String coordinates) {
        String[] tokens = coordinates.split(",");
        _longitude = BigDecimal.valueOf(Double.parseDouble(tokens[0]));
        _latitude = BigDecimal.valueOf(Double.parseDouble(tokens[1]));
    }

    /**
     * Return the longitude value from google maps for this position.
     */
    public BigDecimal getLongitude() {
        return _longitude;
    }

    /**
     * Return the latitude value from google maps for this position.
     */
    public BigDecimal getLatitude() {
        return _latitude;
    }

    /**
     * Return the street value from google maps for this position.
     */
    public String getStreet() {
        return _street;
    }

    /**
     * Return the zipcode value from google maps for this position.
     */
    public String getZipcode() {
        return _zipcode;
    }

    /**
     * Return the city value from google maps for this position.
     */
    public String getCity() {
        return _city;
    }

    /**
     * Return the country value from google maps for this position.
     */
    public String getCountry() {
        return _country;
    }

    /**
     * Return the explicit textual address value from google maps for this position.
     */
    public String getAddress() {
        return _address;
    }

    /**
     * Return the explicit textual address value from google maps for this position.
     */
    public void setAddress(String adress) {
        _address = adress;
    }

    @Override
    public String toString() {
        String string = "[" + _country + " " + _city + " " + _zipcode + " " + _street + " " + _latitude + "," + _longitude;

        if (_address != null) {
            string = string + ", (" + _address + ")";
        }

        string += "]";

        return string;
    }
}