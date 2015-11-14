package cy.studiodemo.bean;

import java.io.Serializable;

/**
 * Created by cuiyue on 15/8/13.
 */

public class NewsTab {

    //往xutil里面存，必须有一个int 类型的id
    private int id;
    private String name;
    private boolean isChecked;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
