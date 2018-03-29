/*
 * Copyright © 2017 Ivar Grimstad (ivar.grimstad@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mvcspec.ozark.test.encoding;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests if Content-Type and encoding when using JSPs or Facelets.
 *
 * @author Florian Hirsch
 */
public class EncodingIT {

    private String webUrl;
    private WebClient webClient;

    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        if (webUrl == null) {
            webUrl = "http://localhost:8080/test-encoding/";
        }
        webClient = new WebClient();
        webClient.getOptions().setRedirectEnabled(true);
    }

    @After
    public void tearDown() {
        webClient.closeAllWindows();
    }

    /**
     * If @Produces is not used the Content-Type should default to text/html; charset=utf-8
     */
    @Test
    public void should_default_to_text_html() throws Exception {
        HtmlPage defaultJSPPage = webClient.getPage(webUrl + "resources/jsp/default");
        checkContentType(defaultJSPPage, "text/html; charset=utf-8");
        checkUmlatus(defaultJSPPage);
    }

    /**
     * If @Produces is used without charset attribute the charset should default to utf-8.
     */
    @Test
    public void should_default_to_utf_8() throws Exception {
        HtmlPage defaultJSPPage = webClient.getPage(webUrl + "resources/jsp/produces");
        checkContentType(defaultJSPPage, "text/html; charset=utf-8");
        checkUmlatus(defaultJSPPage);
    }

    /**
     * If @Produces is used with charset attribute the value should be used as is and should
     * set the charset.
     */
    @Test
    public void should_use_produces_charset() throws Exception {
        HtmlPage defaultJSPPage = webClient.getPage(webUrl + "resources/jsp/produces-iso-8859-15");
        checkContentType(defaultJSPPage, "text/html; charset=iso-8859-15");
        checkUmlatus(defaultJSPPage);
    }

    /**
     * The contentType page directive of a JSP should be ignored.
     * Ozark should log a warning.
     */
    @Test
    public void should_ignore_jsp_content_type() throws Exception {
        HtmlPage defaultJSPPage = webClient.getPage(webUrl + "resources/jsp/ignores-jsp-content-type");
        checkContentType(defaultJSPPage, "text/html; charset=utf-8");
        checkUmlatus(defaultJSPPage);
    }

    private void checkContentType(HtmlPage page, String contentType) {
        MediaType given = MediaType.valueOf(page.getWebResponse().getContentType());
        MediaType expected = MediaType.valueOf(contentType);
        // comparing on our own as MediaType#equals compares charset values case-sensitive
        assertThat("Type does not match", given.getType(), is(expected.getType()));
        assertThat("SubType does not match", given.getSubtype(), is(expected.getSubtype()));
        assertThat("Charset does not match", given.getParameters().get("charset"), is(expected.getParameters().get("charset")));
    }

    private void checkUmlatus(HtmlPage page) {
        String umlauts = page.getElementById("umlauts").asText();
        assertThat(umlauts, is("äöü"));
    }

}
