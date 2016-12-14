package uk.gov.hmrc.address.v2;

public enum LogicalState {

    Approved(1),
    Alternative(3),
    Provisional(6),
    Historical(8);

    //-------------------------------------------------------------------------

    public final int code;

    LogicalState(int code) {
        this.code = code;
    }

    public static LogicalState lookup(int code) {
        for (LogicalState v: values()) {
            if (v.code == code) return v;
        }
        return null;
    }
}
