package Cinema.util;

import java.util.List;


public class District {
    private String name;
    private int code;
    private String division_type;
    private List<Ward> wards;

    public String getName() { return name; }
    public int getCode() { return code; }
    public String getDivisionType() { return division_type; }
    public List<Ward> getWards() { return wards; }
}
