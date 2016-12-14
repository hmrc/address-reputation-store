package uk.gov.hmrc.address.v2;

public enum StreetClassification {

    Footpath(4), // or general pedestrian way
    Cycleway(6),
    All_Vehicles(8),
    Restricted_Byway(9),
    Bridleway(10);

    //-------------------------------------------------------------------------

    public final int code;

    StreetClassification(int code) {
        this.code = code;
    }

    public static StreetClassification lookup(int code) {
        for (StreetClassification v: values()) {
            if (v.code == code) return v;
        }
        return null;
    }
}
