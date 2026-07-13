package de.agiehl.mediauploader.upload;

public class InvalidUploadException extends IllegalArgumentException {

    private final String messageCode;

    public InvalidUploadException(String messageCode) {
        super(messageCode);
        this.messageCode = messageCode;
    }

    public String getMessageCode() {
        return messageCode;
    }
}
