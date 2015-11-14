package cy.studiodemo.bean;

public class CityModel {

    private int id;
    private String city_id; //100010000
    private String city_name; //北京市
    private String short_name; //北京
    private String city_pinyin; //beijing
    private String short_pinyin; //bj
    private String nameSort; //城市首字母

    public String getCity_id() {
        return city_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCity_id(String city_id) {
        this.city_id = city_id;
    }

    public String getCity_name() {
        return city_name;
    }

    public void setCity_name(String city_name) {
        this.city_name = city_name;
    }

    public String getShort_name() {
        return short_name;
    }

    public void setShort_name(String short_name) {
        this.short_name = short_name;
    }

    public String getCity_pinyin() {
        return city_pinyin;
    }

    public void setCity_pinyin(String city_pinyin) {
        this.city_pinyin = city_pinyin;
    }

    public String getShort_pinyin() {
        return short_pinyin;
    }

    public void setShort_pinyin(String short_pinyin) {
        this.short_pinyin = short_pinyin;
    }

    public String getNameSort() {
        return nameSort;
    }

    public void setNameSort(String nameSort) {
        this.nameSort = nameSort;
    }


}
