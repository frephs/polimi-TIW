package it.polimi.auctionapp.utils;

public enum MessageType {
    INFO,
    WARNING,
    ERROR,
    SUCCESS;

    public final String getClassName() {
        return this.toString().toLowerCase();
    }

    public final String wrap(String message) {
        return "<div class=\"" + this.getClassName() + "\"> <p>" + message + "</p></div>";
    }


}
