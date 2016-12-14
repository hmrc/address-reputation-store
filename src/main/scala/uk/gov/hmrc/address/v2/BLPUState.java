package uk.gov.hmrc.address.v2;

public enum BLPUState {

    Under_Construction(1),
    In_Use(2),
    Unoccupied(3), // also covers vacant, derelict
    Demolished(4),
    Planning_Permission_Granted(6);

    //-------------------------------------------------------------------------

    public final int code;

    BLPUState(int code) {
        this.code = code;
    }

    public static BLPUState lookup(int code) {
        for (BLPUState v : values()) {
            if (v.code == code) return v;
        }
        return null;
    }
}
