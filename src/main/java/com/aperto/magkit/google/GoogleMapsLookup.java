package com.aperto.magkit.google;

import org.apache.commons.digester.Digester;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.stripToEmpty;

/**
 * Class allows to send a request to the google maps web service and parses out a GoogleMapsResult list.
 * TODO: old implementation for the V2 google api. Migrate to V3 API, see degewo project.
 *
 * @author Mayo Fragoso
 */
public class GoogleMapsLookup {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleMapsLookup.class);
    private static final String URL = "http://maps.google.com/maps/geo";
    private static final String ENCODING = "UTF-8";
    private String _key = "";

    /**
     * Concatenates the specified fields to a valid google maps address and returns the records found by the google maps service.
     * It is recomended allways to include either the city or the country name. The resulting list will be an empty list if no
     * records are found.
     *
     * @param street   the street to find
     * @param streetnr the streetnr to find
     * @param zipcode  the zipcode to find
     * @param city     the city to find
     * @param country  the country to find
     * @return the GoogleMapsResult list containing the records found.
     * @throws RuntimeException when the google maps service sends an error status a RuntimeException is thrown containing the corresponding  <code>GoogleMapsLookup.Status</code> name as message
     */
    public List<GoogleMapsResult> lookup(String street, String streetnr, String zipcode, String city, String country) {
        String address = stripToEmpty(country) + " " + stripToEmpty(city) + " " + stripToEmpty(zipcode) + " " + stripToEmpty(street) + " " + stripToEmpty(streetnr);
        return lookup(address.trim());
    }

    /**
     * Returns the records found by the google maps service. It is recomended allways to include either the city or the country name.
     * The resulting list will be an empty list if no records are found.
     *
     * @param address the address to find
     * @return the GoogleMapsResult list containing the records found.
     * @throws RuntimeException when the google maps service sends an error status a RuntimeException is thrown containing the corresponding  <code>GoogleMapsLookup.Status</code> name as message
     */
    public List<GoogleMapsResult> lookup(String address) {
        if (isEmpty(address)) {
            throw new IllegalArgumentException("address must not be empty or null!");
        }

        Digester digester = new Digester();
        initDigester(digester);

        InputSource xmlSource = processGoogleMapsRequest(address);
        Status status;
        GoogleResponse response = null;
        if (xmlSource != null) {
            try {
                response = (GoogleResponse) digester.parse(xmlSource);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                closeQuietly(xmlSource.getCharacterStream());
            }
            status = response.getStatus();
        } else {
            throw new RuntimeException();
        }

        if (status == null) {
            throw new RuntimeException("Unknown google response status");
        } else if (!Status.SUCCESS.equals(status) && !Status.UNKNOWN_ADDRESS.equals(status)) {
            throw new RuntimeException(status.toString());
        }

        List<GoogleMapsResult> result = response.getResults();

        LOGGER.debug("resulting addresses: " + result);

        return result;
    }

    private void initDigester(Digester digester) {
        digester.setValidating(false);
        digester.addObjectCreate("kml/Response", GoogleResponse.class);
        digester.addObjectCreate("kml/Response/Placemark", GoogleMapsResult.class);
        digester.addBeanPropertySetter("kml/Response/Placemark/AddressDetails/Country/CountryNameCode", "country");
        digester.addBeanPropertySetter("kml/Response/Placemark/AddressDetails/Country/AdministrativeArea/SubAdministrativeArea/Locality/LocalityName", "city");
        digester.addBeanPropertySetter("kml/Response/Placemark/AddressDetails/Country/AdministrativeArea/SubAdministrativeArea/Locality/Thoroughfare/ThoroughfareName", "street");
        digester.addBeanPropertySetter("kml/Response/Placemark/AddressDetails/Country/AdministrativeArea/SubAdministrativeArea/Locality/PostalCode/PostalCodeNumber", "zipcode");
        digester.addBeanPropertySetter("kml/Response/Placemark/Point/coordinates", "coordinates");
        digester.addBeanPropertySetter("kml/Response/Placemark/address", "address");
        digester.addSetNext("kml/Response/Placemark", "addResult");
        digester.addBeanPropertySetter("kml/Response/Status/code", "statusCode");
    }

    private InputSource processGoogleMapsRequest(String address) {
        InputSource inputSource = null;
        try {
            HttpClient client = new HttpClient();
            String url = URL + "?output=xml&q=" + URLEncoder.encode(address, ENCODING) + "&key=" + _key + "&oe=" + ENCODING;
            HttpMethod method = new GetMethod(url);
            int status = client.executeMethod(method);
            LOGGER.debug("HTTP response headers: " + Arrays.asList(method.getResponseHeaders()));
            if (Status.getStatusForCode(String.valueOf(status)) == Status.SUCCESS) {
                inputSource = new InputSource(method.getResponseBodyAsStream());
                inputSource.setEncoding(ENCODING);
            } else {
                LOGGER.error("Failed to call google geocoder: " + status + " " + method.getStatusText(), new Exception());
            }
        } catch (RuntimeException e) {
            LOGGER.error("Can't call Google Maps request.", e);
        } catch (Exception e) {
            LOGGER.error("Can't call Google Maps request.", e);
        }
        return inputSource;
    }

    public void setKey(String key) {
        _key = key;
    }

    /**
     * Used to parse the results with digester.
     */
    public static class GoogleResponse {
        private String _statusCode;
        private List<GoogleMapsResult> _results = new ArrayList<GoogleMapsResult>();

        public Status getStatus() {
            return Status.getStatusForCode(_statusCode);
        }

        public void setStatusCode(String statusCode) {
            _statusCode = statusCode;
        }

        public List<GoogleMapsResult> getResults() {
            return _results;
        }

        public void addResult(GoogleMapsResult result) {
            _results.add(result);
        }
    }

    /**
     * Enum for the google maps request error status codes.
     *
     * @author mayo.fragoso
     */
    public static enum Status {
        SUCCESS("200"), BAD_REQUEST("400"), GOOGLE_ENCODER_ERROR("500"), MISSING_QUERY("601"), UNKNOWN_ADDRESS("602"), LEGAL_PROBLEM("603"), NO_ROUTE("604"), BAD_KEY("610"), TOO_MANY_QUERIES("620");

        private String _code;

        Status(String status) {
            _code = status;
        }

        public static Status getStatusForCode(String code) {
            Status status = null;
            for (Status s : Status.values()) {
                if (s.getCode().equals(code)) {
                    status = s;
                    break;
                }
            }
            return status;
        }

        public String getCode() {
            return _code;
        }
    }
}
