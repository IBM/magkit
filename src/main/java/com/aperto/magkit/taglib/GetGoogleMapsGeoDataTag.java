package com.aperto.magkit.taglib;

import com.aperto.magkit.google.GoogleMapsLookup;
import com.aperto.magkit.google.GoogleMapsResult;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.myfaces.tobago.apt.annotation.TagAttribute;
import org.apache.myfaces.tobago.apt.annotation.Tag;
import org.apache.myfaces.tobago.apt.annotation.BodyContent;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.List;

/**
 * Get geo data (longitude and latitude) from a address.
 * Use either a complete address or several address data (street, country).
 * Saves geo data to vars with names 'latitude' and 'longitude'.
 *
 * @author diana.racho (17.04.2009)
 */
@Tag(name = "getGoogleGeoData", bodyContent = BodyContent.JSP)
public class GetGoogleMapsGeoDataTag extends TagSupport {
    private static final Logger LOGGER = Logger.getLogger(GetGoogleMapsGeoDataTag.class);

    private static final String VAR_LONGITUDE = "longitude";
    private static final String VAR_LATITUDE = "latitude";
    private String _googleMapKey;
    private String _address = StringUtils.EMPTY;
    private String _street = StringUtils.EMPTY;
    private String _streetNr = StringUtils.EMPTY;
    private String _zipCode = StringUtils.EMPTY;
    private String _city = StringUtils.EMPTY;
    private String _country = StringUtils.EMPTY;

    @TagAttribute(required = true)
    public void setGoogleMapKey(String googleMapKey) {
        _googleMapKey = googleMapKey;
    }

    @TagAttribute
    public void setAddress(String address) {
        _address = address;
    }

    @TagAttribute
    public void setStreet(String street) {
        _street = street;
    }

    @TagAttribute
    public void setStreetNr(String streetNr) {
        _streetNr = streetNr;
    }

    @TagAttribute
    public void setZipCode(String zipCode) {
        _zipCode = zipCode;
    }

    @TagAttribute
    public void setCity(String city) {
        _city = city;
    }

    @TagAttribute
    public void setCountry(String country) {
        _country = country;
    }

    @Override
    public int doEndTag() throws JspException {
        if (!isGeoDataEmpty()) {
            GoogleMapsResult result = getGeoData();
            if (result != null) {
                pageContext.setAttribute(VAR_LONGITUDE, result.getLongitude().doubleValue());
                pageContext.setAttribute(VAR_LATITUDE, result.getLatitude().doubleValue());
            }
        }
        return super.doEndTag();
    }

    private boolean isGeoDataEmpty() {
        boolean isGeoDataEmpty = false;
        if (StringUtils.isBlank(_address)) {
            if (StringUtils.isBlank(_zipCode) && StringUtils.isBlank(_city) && StringUtils.isBlank(_country)) {
                isGeoDataEmpty = true;
            }
        }
        return isGeoDataEmpty;
    }

    public GoogleMapsResult getGeoData() {
        GoogleMapsResult result = null;
        List<GoogleMapsResult> list;
        GoogleMapsLookup lookup = new GoogleMapsLookup();
        lookup.setKey(_googleMapKey);
        if (StringUtils.isNotBlank(_address)) {
            list = lookup.lookup(_address);
        } else {
            list = lookup.lookup(_street, _streetNr, _zipCode, _city, _country);
        }
        if (list != null && !list.isEmpty()) {
            result = list.get(0);
        }
        return result;
    }
}