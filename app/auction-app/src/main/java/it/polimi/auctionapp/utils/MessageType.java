package it.polimi.auctionapp.utils;

public enum MessageType {
    INFO,
    WARNING,
    ERROR,
    SUCCESS;

    public final String getCSSClassName() {
        return this.toString().toLowerCase();
    }

    public final String wrap(String message) {
        return (
            "<div class=\"" +
            this.getCSSClassName() +
            "\"> <span class='icon'> </span> <p>" +
            message +
            "</p> </div>"
        );
    }
}
