/*
 * This file is part of Kmeel.
 * Copyright (C) 2017  Marten4n6
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.kmeel.plugins;

import com.github.kmeel.api.model.objects.AttachmentRow;
import com.github.kmeel.api.model.objects.ID;
import com.github.kmeel.plugins.model.PSTModel;
import com.pff.*;
import com.rtfparserkit.converter.text.StringTextConverter;
import com.rtfparserkit.parser.RtfStreamSource;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.UUID;

/**
 * @author Marten4n6
 */
@Slf4j
public class Utils {

    /**
     * @return A human readable byte size
     */
    public static String humanReadableByteCount(long bytes) {
        boolean si = false;
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * @return The RTF, HTML or plaintext body of the PSTObject
     */
    public static String getBody(SimpleDateFormat DATE_FORMAT, PSTObject pstObject) {
        if (pstObject instanceof PSTAppointment) {
            // Appointment
            PSTAppointment appointment = (PSTAppointment)pstObject;
            StringBuilder builder = new StringBuilder();

            if (!appointment.getLocation().isEmpty()) builder.append("<b>Location: </b>").append(appointment.getLocation()).append("<br/>");
            if (!appointment.getAllAttendees().isEmpty()) builder.append("<b>All Attendees: </b>").append(appointment.getAllAttendees()).append("<br/>");
            if (!appointment.getToAttendees().isEmpty()) builder.append("<b>To Attendees: </b>").append(appointment.getToAttendees()).append("<br/>");
            if (!appointment.getCCAttendees().isEmpty()) builder.append("<b>CC Attendees: </b>").append(appointment.getToAttendees()).append("<br/>");
            if (appointment.getStartTime() != null) builder.append("<b>Start Time: </b>").append(DATE_FORMAT.format(appointment.getStartTime())).append("<br/>");
            if (appointment.getEndTime() != null) builder.append("<b>End Time: </b>").append(DATE_FORMAT.format(appointment.getEndTime())).append("<br/>");
            builder.append("<b>Recurring: </b>").append(appointment.isRecurring()).append("<br/>");

            if (appointment.isOnlineMeeting()) builder.append("<b>Online Meeting: </b>").append(appointment.isOnlineMeeting()).append("<br/>");
            if (!appointment.getNetMeetingOrganizerAlias().isEmpty()) builder.append("<b>Organizer Alias: </b>").append(appointment.getNetMeetingOrganizerAlias()).append("<br/>");
            if (!appointment.getNetMeetingDocumentPathName().isEmpty()) builder.append("<b>Meeting Document: </b>").append(appointment.getNetMeetingDocumentPathName()).append("<br/>");
            if (!appointment.getNetMeetingServer().isEmpty()) builder.append("<b>Meeting Server: </b>").append(appointment.getNetMeetingServer()).append("<br/>");
            if (!appointment.getConferenceServerPassword().isEmpty()) builder.append("<b>Conference Server Password: </b>").append(appointment.getConferenceServerPassword()).append("<br/>");

            builder.append("<br/>");

            try {
                if (!Utils.getRTFFromString(appointment.getRTFBody()).isEmpty()) {
                    builder.append(Utils.getRTFFromString(appointment.getRTFBody()).replaceAll("\n", "<br/>"));
                } else if (!appointment.getBodyHTML().isEmpty()) {
                    builder.append(appointment.getBodyHTML());
                } else {
                    builder.append(appointment.getBody().replaceAll("\n", "<br/>"));
                }
            } catch (PSTException | IOException ex) {
                log.error(ex.getMessage(), ex);
            }

            return builder.toString();
        } else if (pstObject instanceof PSTContact) {
            // Contact
            PSTContact contact = (PSTContact) pstObject;
            StringBuilder builder = new StringBuilder();

            // Here we go...
            if (!contact.getAccount().isEmpty()) builder.append("<b>Account: </b>").append(contact.getAccount()).append("<br/>");
            if (contact.getAnniversary() != null) builder.append("<b>Anniversary: </b>").append(DATE_FORMAT.format(contact.getAnniversary())).append("<br/>");
            if (!contact.getAssistant().isEmpty()) builder.append("<b>Assistant: </b>").append(contact.getAssistant()).append("<br/>");
            if (!contact.getAssistantTelephoneNumber().isEmpty()) builder.append("<b>Assistant Telephone Number: </b>").append(contact.getAssistantTelephoneNumber()).append("<br/>");
            if (contact.getBirthday() != null) builder.append("<b>Birthday: </b>").append(DATE_FORMAT.format(contact.getBirthday())).append("<br/>");
            if (!contact.getBusiness2TelephoneNumber().isEmpty()) builder.append("<b>Business Telephone Number 2: </b>").append(contact.getBusiness2TelephoneNumber()).append("<br/>");
            if (!contact.getBusinessAddressCity().isEmpty()) builder.append("<b>Business City: </b>").append(contact.getBusinessAddressCity()).append("<br/>");
            if (!contact.getBusinessAddressCountry().isEmpty()) builder.append("<b>Business Country: </b>").append(contact.getBusinessAddressCountry()).append("<br/>");
            if (!contact.getBusinessAddressStateOrProvince().isEmpty()) builder.append("<b>Business State/Province: </b>").append(contact.getBusinessAddressStateOrProvince()).append("<br/>");
            if (!contact.getBusinessAddressStreet().isEmpty()) builder.append("<b>Business Street: </b>").append(contact.getBusinessAddressStreet()).append("<br/>");
            if (!contact.getBusinessFaxNumber().isEmpty()) builder.append("<b>Business Fax Number: </b>").append(contact.getBusinessFaxNumber()).append("<br/>");
            if (!contact.getBusinessHomePage().isEmpty()) builder.append("<b>Business Homepage: </b>").append(contact.getBusinessHomePage()).append("<br/>");
            if (!contact.getBusinessPoBox().isEmpty()) builder.append("<b>Business P.O. Box: </b>").append(contact.getBusinessPoBox()).append("<br/>");
            if (!contact.getBusinessPostalCode().isEmpty()) builder.append("<b>Business Postal Code: </b>").append(contact.getBusinessPostalCode()).append("<br/>");
            if (!contact.getBusinessTelephoneNumber().isEmpty()) builder.append("<b>Business Telephone Number: </b>").append(contact.getBusinessTelephoneNumber()).append("<br/>");
            if (!contact.getCallbackTelephoneNumber().isEmpty()) builder.append("<b>Callback Telephone Number: </b>").append(contact.getCallbackTelephoneNumber()).append("<br/>");
            if (!contact.getCarTelephoneNumber().isEmpty()) builder.append("<b>Car Telephone Number: </b>").append(contact.getCarTelephoneNumber()).append("<br/>");
            if (!contact.getChildrensNames().isEmpty()) builder.append("<b>Children's Names: </b>").append(contact.getChildrensNames()).append("<br/>");
            if (!contact.getCompanyMainPhoneNumber().isEmpty()) builder.append("<b>Company Main Phone Number: </b>").append(contact.getCompanyMainPhoneNumber()).append("<br/>");
            if (!contact.getCompanyName().isEmpty()) builder.append("<b>Company Name: </b>").append(contact.getCompanyName()).append("<br/>");
            if (!contact.getComputerNetworkName().isEmpty()) builder.append("<b>Computer Network Name: </b>").append(contact.getComputerNetworkName()).append("<br/>");
            if (!contact.getCustomerId().isEmpty()) builder.append("<b>Customer ID: </b>").append(contact.getCustomerId()).append("<br/>");
            if (!contact.getDepartmentName().isEmpty()) builder.append("<b>Department Name: </b>").append(contact.getDepartmentName()).append("<br/>");
            if (!contact.getEmail1AddressType().isEmpty()) builder.append("<b>Email Address Type: </b>").append(contact.getEmail1AddressType()).append("<br/>");
            if (!contact.getEmail1DisplayName().isEmpty()) builder.append("<b>Email Display Name: </b>").append(contact.getEmail1DisplayName()).append("<br/>");
            if (!contact.getEmail1EmailAddress().isEmpty()) builder.append("<b>Email Email Address: </b>").append(contact.getEmail1EmailAddress()).append("<br/>");
            if (!contact.getEmail1EmailType().isEmpty()) builder.append("<b>Email Type: </b>").append(contact.getEmail1EmailType()).append("<br/>");
            if (!contact.getEmail1OriginalDisplayName().isEmpty()) builder.append("<b>Email Original Display Name: </b>").append(contact.getEmail1OriginalDisplayName()).append("<br/>");
            if (!contact.getEmail2AddressType().isEmpty()) builder.append("<b>Second Email Address Type: </b>").append(contact.getEmail2AddressType()).append("<br/>");
            if (!contact.getEmail2DisplayName().isEmpty()) builder.append("<b>Second Email Display Name: </b>").append(contact.getEmail2DisplayName()).append("<br/>");
            if (!contact.getEmail2EmailAddress().isEmpty()) builder.append("<b>Second Email Email Address: </b>").append(contact.getEmail2EmailAddress()).append("<br/>");
            if (!contact.getEmail2OriginalDisplayName().isEmpty()) builder.append("<b>Second Email Original Display Name: </b>").append(contact.getEmail2OriginalDisplayName()).append("<br/>");
            if (!contact.getEmail3AddressType().isEmpty()) builder.append("<b>Third Email Address Type: </b>").append(contact.getEmail3AddressType()).append("<br/>");
            if (!contact.getEmail3DisplayName().isEmpty()) builder.append("<b>Third Email Display Name: </b>").append(contact.getEmail3DisplayName()).append("<br/>");
            if (!contact.getEmail3EmailAddress().isEmpty()) builder.append("<b>Third Email Email Address: </b>").append(contact.getEmail3EmailAddress()).append("<br/>");
            if (!contact.getEmail3OriginalDisplayName().isEmpty()) builder.append("<b>Third Email Original Display Name: </b>").append(contact.getEmail3OriginalDisplayName()).append("<br/>");
            if (!contact.getFax1EmailAddress().isEmpty()) builder.append("<b>Fax Email Address: </b>").append(contact.getFax1EmailAddress()).append("<br/>");
            if (!contact.getFax1OriginalDisplayName().isEmpty()) builder.append("<b>Fax Original Display Name: </b>").append(contact.getFax1OriginalDisplayName()).append("<br/>");
            if (!contact.getFax2EmailAddress().isEmpty()) builder.append("<b>Second Fax Email Address: </b>").append(contact.getFax2EmailAddress()).append("<br/>");
            if (!contact.getFax2OriginalDisplayName().isEmpty()) builder.append("<b>Second Fax Original Display Name: </b>").append(contact.getFax2OriginalDisplayName()).append("<br/>");
            if (!contact.getFax3EmailAddress().isEmpty()) builder.append("<b>Third Fax Email Address: </b>").append(contact.getFax3EmailAddress()).append("<br/>");
            if (!contact.getFax3OriginalDisplayName().isEmpty()) builder.append("<b>Third Fax Original Display Name: </b>").append(contact.getFax3OriginalDisplayName()).append("<br/>");
            if (!contact.getFileUnder().isEmpty()) builder.append("<b>File Under: </b>").append(contact.getFileUnder()).append("<br/>");
            if (!contact.getFreeBusyLocation().isEmpty()) builder.append("<b>Free Busy Location: </b>").append(contact.getFreeBusyLocation()).append("<br/>");
            if (!contact.getFtpSite().isEmpty()) builder.append("<b>FTP Site: </b>").append(contact.getFtpSite()).append("<br/>");
            if (!contact.getGeneration().isEmpty()) builder.append("<b>Generation: </b>").append(contact.getGeneration()).append("<br/>");
            if (!contact.getGivenName().isEmpty()) builder.append("<b>Given Name: </b>").append(contact.getGivenName()).append("<br/>");
            if (!contact.getGovernmentIdNumber().isEmpty()) builder.append("<b>Government ID Number: </b>").append(contact.getGovernmentIdNumber()).append("<br/>");
            if (!contact.getHobbies().isEmpty()) builder.append("<b>Hobbies: </b>").append(contact.getHobbies()).append("<br/>");
            if (!contact.getHome2TelephoneNumber().isEmpty()) builder.append("<b>Home 2 Telephone Number: </b>").append(contact.getHome2TelephoneNumber()).append("<br/>");
            if (!contact.getHomeAddress().isEmpty()) builder.append("<b>Home Address: </b>").append(contact.getHomeAddress()).append("<br/>");
            if (!contact.getHomeAddressCity().isEmpty()) builder.append("<b>Home City: </b>").append(contact.getHomeAddressCity()).append("<br/>");
            if (!contact.getHomeAddressCountry().isEmpty()) builder.append("<b>Home Country: </b>").append(contact.getHomeAddressCountry()).append("<br/>");
            if (!contact.getHomeAddressPostOfficeBox().isEmpty()) builder.append("<b>Home P.O. Box: </b>").append(contact.getHomeAddressPostOfficeBox()).append("<br/>");
            if (!contact.getHomeAddressPostalCode().isEmpty()) builder.append("<b>Home Postal Code: </b>").append(contact.getHomeAddressPostalCode()).append("<br/>");
            if (!contact.getHomeAddressStateOrProvince().isEmpty()) builder.append("<b>Home State/Province: </b>").append(contact.getHomeAddressStateOrProvince()).append("<br/>");
            if (!contact.getHomeAddressStreet().isEmpty()) builder.append("<b>Home Street: </b>").append(contact.getHomeAddressStreet()).append("<br/>");
            if (!contact.getHomeFaxNumber().isEmpty()) builder.append("<b>Home Fax Number: </b>").append(contact.getHomeFaxNumber()).append("<br/>");
            if (!contact.getHomeTelephoneNumber().isEmpty()) builder.append("<b>Home Telephone Number: </b>").append(contact.getHomeTelephoneNumber()).append("<br/>");
            if (!contact.getInitials().isEmpty()) builder.append("<b>Initials: </b>").append(contact.getInitials()).append("<br/>");
            if (!contact.getInstantMessagingAddress().isEmpty()) builder.append("<b>Instant Messaging Address: </b>").append(contact.getInstantMessagingAddress()).append("<br/>");
            if (!contact.getIsdnNumber().isEmpty()) builder.append("<b>ISDN Number: </b>").append(contact.getIsdnNumber()).append("<br/>");
            if (!contact.getKeyword().isEmpty()) builder.append("<b>Keyword: </b>").append(contact.getKeyword()).append("<br/>");
            if (!contact.getLanguage().isEmpty()) builder.append("<b>Language: </b>").append(contact.getLanguage()).append("<br/>");
            if (!contact.getLocation().isEmpty()) builder.append("<b>Location: </b>").append(contact.getLocation()).append("<br/>");
            if (!contact.getManagerName().isEmpty()) builder.append("<b>Manager Name: </b>").append(contact.getManagerName()).append("<br/>");
            if (!contact.getMhsCommonName().isEmpty()) builder.append("<b>MHS Common Name: </b>").append(contact.getMhsCommonName()).append("<br/>");
            if (!contact.getMiddleName().isEmpty()) builder.append("<b>Middle Name: </b>").append(contact.getMiddleName()).append("<br/>");
            if (!contact.getMobileTelephoneNumber().isEmpty()) builder.append("<b>Mobile Telephone Number: </b>").append(contact.getMobileTelephoneNumber()).append("<br/>");
            if (!contact.getNickname().isEmpty()) builder.append("<b>Nickname: </b>").append(contact.getNickname()).append("<br/>");
            if (!contact.getNote().trim().isEmpty()) builder.append("<b>Note: </b>").append(contact.getNote()).append("<br/>");
            if (!contact.getOfficeLocation().isEmpty()) builder.append("<b>Office Location: </b>").append(contact.getOfficeLocation()).append("<br/>");
            if (!contact.getOrganizationalIdNumber().isEmpty()) builder.append("<b>Organizational ID Number: </b>").append(contact.getOrganizationalIdNumber()).append("<br/>");
            if (!contact.getOriginalDisplayName().isEmpty()) builder.append("<b>Original Display Name: </b>").append(contact.getOriginalDisplayName()).append("<br/>");
            if (!contact.getOtherAddress().isEmpty()) builder.append("<b>Other Address: </b>").append(contact.getOtherAddress()).append("<br/>");
            if (!contact.getOtherAddressCity().isEmpty()) builder.append("<b>Other Address City: </b>").append(contact.getOtherAddressCity()).append("<br/>");
            if (!contact.getOtherAddressCountry().isEmpty()) builder.append("<b>Other Address Country: </b>").append(contact.getOtherAddressCountry()).append("<br/>");
            if (!contact.getOtherAddressPostOfficeBox().isEmpty()) builder.append("<b>Other Address P.O. Box: </b>").append(contact.getOtherAddressPostOfficeBox()).append("<br/>");
            if (!contact.getOtherAddressPostalCode().isEmpty()) builder.append("<b>Other Address Postal Code: </b>").append(contact.getOtherAddressPostalCode()).append("<br/>");
            if (!contact.getOtherAddressStateOrProvince().isEmpty()) builder.append("<b>Other Address State/Province: </b>").append(contact.getOtherAddressStateOrProvince()).append("<br/>");
            if (!contact.getOtherAddressStreet().isEmpty()) builder.append("<b>Other Address Street: </b>").append(contact.getOtherAddressStreet()).append("<br/>");
            if (!contact.getOtherTelephoneNumber().isEmpty()) builder.append("<b>Other Telephone Number: </b>").append(contact.getOtherTelephoneNumber()).append("<br/>");
            if (!contact.getPagerTelephoneNumber().isEmpty()) builder.append("<b>Pager Telephone Number: </b>").append(contact.getPagerTelephoneNumber()).append("<br/>");
            if (!contact.getPersonalHomePage().isEmpty()) builder.append("<b>Personal Homepage: </b>").append(contact.getPersonalHomePage()).append("<br/>");
            if (!contact.getPostalAddress().isEmpty()) builder.append("<b>Postal Address: </b>").append(contact.getPostalAddress()).append("<br/>");
            if (contact.getPostalAddressId() != 0) builder.append("<b>Account: </b>").append(contact.getPostalAddressId()).append("<br/>");
            if (!contact.getPreferredByName().isEmpty()) builder.append("<b>Preferred Name: </b>").append(contact.getPreferredByName()).append("<br/>");
            if (!contact.getPrimaryFaxNumber().isEmpty()) builder.append("<b>Primary Fax Number: </b>").append(contact.getPrimaryFaxNumber()).append("<br/>");
            if (!contact.getPrimaryTelephoneNumber().isEmpty()) builder.append("<b>Primary Telephone Number: </b>").append(contact.getPrimaryTelephoneNumber()).append("<br/>");
            if (!contact.getProfession().isEmpty()) builder.append("<b>Profession: </b>").append(contact.getProfession()).append("<br/>");
            if (!contact.getRadioTelephoneNumber().isEmpty()) builder.append("<b>Radio Telephone Number: </b>").append(contact.getRadioTelephoneNumber()).append("<br/>");
            if (!contact.getSMTPAddress().isEmpty()) builder.append("<b>SMTP Address: </b>").append(contact.getSMTPAddress()).append("<br/>");
            if (!contact.getSpouseName().isEmpty()) builder.append("<b>Spouse Name: </b>").append(contact.getSpouseName()).append("<br/>");
            if (!contact.getSurname().isEmpty()) builder.append("<b>Surname: </b>").append(contact.getSurname()).append("<br/>");
            if (!contact.getTelexNumber().isEmpty()) builder.append("<b>Telex Number: </b>").append(contact.getTelexNumber()).append("<br/>");
            if (!contact.getTitle().isEmpty()) builder.append("<b>Title: </b>").append(contact.getTitle()).append("<br/>");
            if (!contact.getTransmittableDisplayName().isEmpty()) builder.append("<b>Transmittable Display Name: </b>").append(contact.getTransmittableDisplayName()).append("<br/>");
            if (!contact.getTtytddPhoneNumber().isEmpty()) builder.append("<b>TTYTDD Phone Number: </b>").append(contact.getTtytddPhoneNumber()).append("<br/>");
            if (!contact.getWorkAddress().isEmpty()) builder.append("<b>Work Address: </b>").append(contact.getWorkAddress()).append("<br/>");
            if (!contact.getWorkAddressCity().isEmpty()) builder.append("<b>Work City: </b>").append(contact.getWorkAddressCity()).append("<br/>");
            if (!contact.getWorkAddressCountry().isEmpty()) builder.append("<b>Work Country: </b>").append(contact.getWorkAddressCountry()).append("<br/>");
            if (!contact.getWorkAddressPostOfficeBox().isEmpty()) builder.append("<b>Work P.O. Box: </b>").append(contact.getWorkAddressPostOfficeBox()).append("<br/>");
            if (!contact.getWorkAddressPostalCode().isEmpty()) builder.append("<b>Work Postal Code: </b>").append(contact.getWorkAddressPostalCode()).append("<br/>");
            if (!contact.getWorkAddressState().isEmpty()) builder.append("<b>Work State: </b>").append(contact.getWorkAddressState()).append("<br/>");
            if (!contact.getWorkAddressStreet().isEmpty()) builder.append("<b>Work Street: </b>").append(contact.getWorkAddressStreet()).append("<br/>");

            try {
                if (!Utils.getRTFFromString(contact.getRTFBody()).isEmpty()) {
                    builder.append(Utils.getRTFFromString(contact.getRTFBody()).replaceAll("\n", "<br/>"));
                } else if (!contact.getBodyHTML().isEmpty()) {
                    builder.append(contact.getBodyHTML());
                } else {
                    builder.append(contact.getBody().replaceAll("\n", "<br/>"));
                }
            } catch (PSTException | IOException ex) {
                log.error(ex.getMessage(), ex);
            }

            return builder.toString();
        } else if (pstObject instanceof PSTTask) {
            // Task
            PSTTask task = (PSTTask)pstObject;
            StringBuilder builder = new StringBuilder();

            builder.append("<b>Complete: </b>").append(task.isTaskComplete()).append("<br/>");
            if (task.getTaskDateCompleted() != null) builder.append("<b>Date Completed: </b>").append(DATE_FORMAT.format(task.getTaskDateCompleted())).append("<br/>");
            if (!task.getTaskAssigner().isEmpty()) builder.append("<b>Task Assigner: </b>").append(task.getTaskAssigner()).append("<br/>");
            if (!task.getTaskOwner().isEmpty()) builder.append("<b>Task Owner: </b>").append(task.getTaskOwner()).append("<br/>");
            if (!task.getTaskLastUser().isEmpty()) builder.append("<b>Last User: </b>").append(task.getTaskLastUser()).append("<br/>");
            if (!task.getTaskRole().isEmpty()) builder.append("</b>Task Role: </b>").append(task.getTaskRole()).append("<br/>");
            builder.append("<b>Team Task: </b>").append(task.isTeamTask()).append("<br/>");

            try {
                if (!Utils.getRTFFromString(task.getRTFBody()).isEmpty()) {
                    builder.append(Utils.getRTFFromString(task.getRTFBody()).replaceAll("\n", "<br/>"));
                } else if (!task.getBodyHTML().isEmpty()) {
                    builder.append(task.getBodyHTML());
                } else {
                    builder.append(task.getBody().replaceAll("\n", "<br/>"));
                }
            } catch (PSTException | IOException ex) {
                log.error(ex.getMessage(), ex);
            }

            return builder.toString();
        } else {
            // Normal message
            PSTMessage message = (PSTMessage) pstObject;

            try {
                if (!Utils.getRTFFromString(message.getRTFBody()).isEmpty()) {
                    return Utils.getRTFFromString(message.getRTFBody()).replaceAll("\n", "<br/>");
                } else if (!message.getBodyHTML().isEmpty()) {
                    return message.getBodyHTML();
                } else {
                    return message.getBody().replaceAll("\n", "<br/>");
                }
            } catch (PSTException | IOException ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        return "";
    }

    public static String getAttachmentName(PSTAttachment attachment) {
        String attachmentName;

        if (!attachment.getDisplayName().trim().isEmpty()) {
            attachmentName = attachment.getDisplayName();
        } else if (!attachment.getLongFilename().trim().isEmpty()) {
            attachmentName = attachment.getLongFilename();
        } else if (!attachment.getFilename().trim().isEmpty()) {
            attachmentName = attachment.getFilename();
        } else if (!attachment.getLongPathname().trim().isEmpty()) {
            attachmentName = attachment.getLongPathname();
        } else {
            attachmentName = "UNKNOWN_ATTACHMENT_NAME_" + UUID.randomUUID().toString().split("-")[0];
        }
        return attachmentName;
    }

    private static String getRTFFromString(String rtf) {
        try {
            StringTextConverter converter = new StringTextConverter();
            converter.convert(new RtfStreamSource(new ByteArrayInputStream(rtf.getBytes(StandardCharsets.UTF_8))));
            return converter.getText().replace("converted from text;", "").replace("converted from html;", "");
        } catch (Exception ex) {
            return rtf;
        }
    }
}
