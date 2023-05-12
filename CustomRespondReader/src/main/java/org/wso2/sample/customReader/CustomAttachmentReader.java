package org.wso2.sample.customReader;

import org.apache.axiom.soap.SOAPBody;

import java.io.*;

import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.activation.DataHandler;
import javax.mail.internet.MimeMultipart;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.apache.synapse.core.axis2.Axis2MessageContext;

import java.util.stream.Collectors;
import javax.mail.MessagingException;

import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;

import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

/**
This class is provided as a sample to assist users.
It is not recommended to use this class directly in a production environment without thorough testing to ensure its suitability for the intended purpose.
*/

public class CustomAttachmentReader extends AbstractMediator {

    private static final String soapart = "soapart";
    private static final String pdfpart = "pdfpart";
    private static final String pdfpartContentType = "application/pdf";

    public boolean mediate(final MessageContext context) {
        try {
            this.getResponseAttachments(context);
        } catch (MessagingException | IOException e) {
            this.log.error((Object) e);
        }
        return true;
    }

    public boolean getResponseAttachments(final MessageContext msgCtx) throws MessagingException, IOException {
        final org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) msgCtx).getAxis2MessageContext();
        this.log.info((Object) "[CustomAttachmentReader] ClassMediator Invocation START.");
        String contentType = (String) ((Map) ((Axis2MessageContext) msgCtx).getAxis2MessageContext().getProperty("TRANSPORT_HEADERS")).get("Content-Type");
        if (contentType != null && contentType.contains("multipart/related")) {
            // Decoding Base64 encoded String
            final SOAPBody soapBody = msgCtx.getEnvelope().getBody();
            final String binaryValue = soapBody.getFirstElement().getText();
            final byte[] decoded = Base64.decodeBase64(binaryValue.getBytes());
            try {
                MimeMultipart mimeMultipart = new MimeMultipart(new ByteArrayDataSource(decoded, "multipart/related"));
                for (int i = 0; i < mimeMultipart.getCount(); i++) {
                    if (i == 0) {
                        // Decoding Base64 encoded String for SOAP message
                        final String soapEnvelopeData = new BufferedReader(
                                new InputStreamReader(mimeMultipart.getBodyPart(i).getInputStream(), StandardCharsets.UTF_8))
                                .lines()
                                .collect(Collectors.joining("\n"));
                        OMElement omElementSoap = AXIOMUtil.stringToOM(soapEnvelopeData);
                        msgCtx.setProperty("OMSoapBody", omElementSoap);
                        DataSource soapSource = new ByteArrayDataSource(omElementSoap.toString(), soapart);
                        DataHandler soapSourceDataHandler = new DataHandler(soapSource);
                        axis2MessageContext.addAttachment(soapart, soapSourceDataHandler);
                        msgCtx.setProperty("ExtractedSoapMessage", omElementSoap.toString());
                    }
                    if (i == 1) {
                        // Decoding Base64 encoded String for PDF attachment
                        DataSource pdfDataSource = new ByteArrayDataSource(mimeMultipart.getBodyPart(i).getInputStream(), pdfpartContentType);
                        DataHandler pdfDataHandler = new DataHandler(pdfDataSource);
                        axis2MessageContext.addAttachment(pdfpart, pdfDataHandler);
                        // Adding Pdf attachment to the message context
                        byte[] pdfBytes = IOUtils.toByteArray(mimeMultipart.getBodyPart(i).getInputStream());
                        // Write PDF bytes to file
                        FileUtils.writeByteArrayToFile(new File("/home/chandimav/Documents/my-blog/certificatoPdf.pdf"), pdfBytes);
                        String certificatoPdfencodedb64 = new String(Base64.encodeBase64(pdfBytes));
                        msgCtx.setProperty("test", certificatoPdfencodedb64);
                    }
                }
            } catch (MessagingException e) {
                e.printStackTrace();
            } catch (XMLStreamException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }
}