package cy.studiodemo.bean;

/**
 * Created by cuiyue on 15/7/27.
 */
public class MenuItem {

    private String name;
    private int icon;

    public MenuItem(String name, int icon) {
        this.name = name;
        this.icon = icon;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }
}
