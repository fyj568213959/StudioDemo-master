package cy.studiodemo.bean;

import java.util.ArrayList;

/**
 * Created by cuiyue on 15/8/20.
 */
public class DealData {

    private String errno;
    private String msg;
    private Data data;

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

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }


    public class Data {

        private String total;
        private ArrayList<Deal> deals;

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }

        public ArrayList<Deal> getDeals() {
            return deals;
        }

        public void setDeals(ArrayList<Deal> deals) {
            this.deals = deals;
        }

    }
}
