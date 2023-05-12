package org.wso2.sample;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;

import java.io.ByteArrayOutputStream;

/**
 This class is provided as a sample to assist users.
 It is not recommended to use this class directly in a production environment without thorough testing to ensure its suitability for the intended purpose.
 */
public class AttachmentFormatter implements MessageFormatter {

    MimeMultipart multipart = null;
    @Override
    public byte[] getBytes(MessageContext context, OMOutputFormat format) throws AxisFault {
        return new byte[0];
    }

    @Override
    public void writeTo(MessageContext context, OMOutputFormat format, OutputStream out, boolean preserve) throws AxisFault {
        try {
            writeMultipartMessage(context, out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeMultipartMessage(MessageContext context, OutputStream out) throws IOException, MessagingException {

        // Write the multipart message to the output stream
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        multipart.writeTo(byteArrayOutputStream);
        out.write(byteArrayOutputStream.toByteArray());
    }


    public String getContentType(MessageContext context, OMOutputFormat format, String soapAction) {
        String ContentType = null;
        try {
            ContentType = generateMultipart(context);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return  ContentType;

     //   return "multipart/related; boundary=\"MIME_boundary\""; // return the content type of the message as "multipart/related"
    }

    private String generateMultipart(MessageContext context) throws MessagingException {

            // Get the attachments from the message context
            Attachments attachments = context.getAttachmentMap();

            // Create a MIME multipart message with one body part for each attachment
            multipart = new MimeMultipart("related");
            String[] contentIDs = attachments.getAllContentIDs();

            // Create a body part for the SOAP envelope
            DataHandler dataHandler = attachments.getDataHandler("soapart");
            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.setDataHandler(dataHandler);
            attachmentPart.setHeader("Content-type",   "application/soap+xml;charset=UTF-8");
            multipart.addBodyPart(attachmentPart);

            // Create a body part for the PDF attachment
            DataHandler dataHandlerpdf = attachments.getDataHandler("pdfpart");
            MimeBodyPart attachmentPartpdf = new MimeBodyPart();
            attachmentPartpdf.setDataHandler(dataHandlerpdf);
            attachmentPartpdf.setHeader("Content-type", "application/pdf");
            multipart.addBodyPart(attachmentPartpdf);

        return multipart.getContentType();
    }


    public URL getTargetAddress(MessageContext context, OMOutputFormat format, URL targetURL) throws AxisFault {
        return targetURL;
    }


    public void addHeader(MessageContext context, OMOutputFormat format, org.apache.axiom.soap.SOAPHeaderBlock header) throws AxisFault {
        // Add SOAP headers to the message here, if necessary
    }


    public String formatSOAPAction(MessageContext context, OMOutputFormat format, String soapAction) {
        return soapAction;
    }


    public void setCharSetEncoding(MessageContext messageContext, OMOutputFormat format) {
        // Set the character encoding
        format.setCharSetEncoding("UTF-8");
    }


    public OMOutputFormat getOMOutputFormat(MessageContext context) {
        // Set any output format options here, if necessary
        return new OMOutputFormat();
    }


    public String getContentTypeForAttachments(java.util.Map<String, DataHandler> arg0) {
        // Not used in this implementation
        return null;
    }


    public String formatBinaryContent(String contentType) {
        // Use the default binary formatter to format the attachments
        return new AttachmentFormatter().formatBinaryContent(contentType);
    }


}

