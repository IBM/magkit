package com.aperto.magkit.taglib;

import org.apache.log4j.Logger;
import org.apache.myfaces.tobago.apt.annotation.TagAttribute;
import org.apache.myfaces.tobago.apt.annotation.Tag;
import org.apache.myfaces.tobago.apt.annotation.BodyContent;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import static org.apache.taglibs.standard.tag.common.core.Util.escapeXml;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLDecoder;

/**
 * Returns the url decoded String. Uses java.net.URLDecoder for decoding.
 * Default encoding in UTF-8.
 *
 * @author wolf.bubenik (02.03.2009)
 */
@Tag(name = "decodeUrl", bodyContent = BodyContent.JSP)
public class DecodeUrlTag extends TagSupport {
    private static final Logger LOGGER = Logger.getLogger(DecodeUrlTag.class);
    private static final String HTML_EXTENSION = ".html";

    private String _var;
    private String _url;
    private String _encoding = "UTF-8";

    @TagAttribute
    public void setVar(final String var) {
        _var = var;
    }

    @TagAttribute(required = true)
    public void setUrl(final String url) {
        _url = url;
    }

    @TagAttribute
    public void setEncoding(String encoding) {
        _encoding = encoding;
    }

    /**
     * URL decode String.
     *
     * @throws javax.servlet.jsp.JspException
     */
    @Override
    public int doEndTag() throws JspException {
        String decodedUrl = "";
        try {
            decodedUrl = URLDecoder.decode(_url, _encoding);
        } catch (UnsupportedEncodingException e) {
            throw new NestableRuntimeException(e);
        }
        if (StringUtils.isNotBlank(decodedUrl)) {
            if (StringUtils.isNotBlank(_var)) {
                pageContext.setAttribute(_var, decodedUrl);
            } else {
                JspWriter out = pageContext.getOut();
                try {
                    out.print(escapeXml(decodedUrl));
                } catch (IOException e) {
                    throw new NestableRuntimeException(e);
                }
            }
        }
        return super.doEndTag();
    }
}