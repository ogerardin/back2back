package org.ogerardin.update.channel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import org.ogerardin.update.Release;
import org.ogerardin.update.ReleaseChannel;
import org.ogerardin.update.ReleaseData;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This aims to be a generic release list extractor from any HTML page containing a list of releases.
 *
 * First the {@link #url} is downloaded and converted to a DOM tree.
 * Then the XPath expression {@link #releaseXPath} is evaluated on this DOM tree to obtain a list of nodes, each of
 * which is supposed to match one release. The version, download URL and description are then extracted by applying
 * respectively the XPath expressions {@link #versionXPath}, {@link #zipDownloadUrlXPath} and {@link #descriptionXPath}
 * on the release's DOM node.
 *
 * Limitations:
 *  - no support for indirection (if the required data is on a linked page, not on the main page)
 *  - no support for filtering (excluding nodes that are not actual releases)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Slf4j
public class WebScrapingChannel extends AbstractHttpChannel implements ReleaseChannel {

    private final String releaseXPath;
    private final String versionXPath;
    private final String zipDownloadUrlXPath;
    private final String descriptionXPath;

    private URL url;

    public WebScrapingChannel(String url, String releaseXPath, String versionXPath, String zipDownloadUrlXPath, String descriptionXPath) throws MalformedURLException {
        this(new URL(url), releaseXPath, versionXPath, zipDownloadUrlXPath, descriptionXPath);
    }

    public WebScrapingChannel(URL url, String releaseXPath, String versionXPath, String zipDownloadUrlXPath, String descriptionXPath) {
        this.releaseXPath = releaseXPath;
        this.versionXPath = versionXPath;
        this.zipDownloadUrlXPath = zipDownloadUrlXPath;
        this.descriptionXPath = descriptionXPath;
        this.url = url;
    }

    @Override
    public Release[] getReleases() {
        String html = restTemplate.getForObject(url.toString(), String.class);

        Document doc = Jsoup.parse(html);
        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(doc);

        List<Release> releases = new ArrayList<>();
        try {
            XPath xpathObject = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) xpathObject.evaluate(releaseXPath, w3cDoc, XPathConstants.NODESET);
            for (int i=0; i<nodes.getLength(); i++) {
                Node node = nodes.item(i);
                String version = (String) xpathObject.evaluate(versionXPath, node, XPathConstants.STRING);
                String download = (String) xpathObject.evaluate(zipDownloadUrlXPath, node, XPathConstants.STRING);
                String descr = (String) xpathObject.evaluate(descriptionXPath, node, XPathConstants.STRING);
                releases.add(new ReleaseData(version, new URL(url, download), descr));
            }

        } catch (XPathExpressionException | MalformedURLException e) {
            log.error("Failed to parse release ", e);
        }

        return releases.toArray(new Release[0]);
    }

}

