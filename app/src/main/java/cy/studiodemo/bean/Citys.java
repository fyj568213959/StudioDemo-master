package cy.studiodemo.bean;

import java.util.List;

/**
 * Created by cuiyue on 15/8/24.
 */
public class Citys {

    private String errno;
    private String msg;
    private List<CityModel> cities;

    public String getErrno() {
        return errno;
    }

    public void setErrno(String errno) {
        this.errno = errno;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<CityModel> getCities() {
        return cities;
    }

    public void setCities(List<CityModel> cities) {
        this.cities = cities;
    }

}
