package cy.studiodemo.bean;

import com.lidroid.xutils.http.HttpHandler;

import java.io.File;
import java.io.Serializable;

/**
 * Created by cuiyue on 15/9/6.
 */
public class DownloadAppInfo implements Serializable {

    public static final int STATUS_NOT_DOWNLOAD = 0;//没有下载
    public static final int STATUS_WAIT = 1;//等待下载
    public static final int STATUS_START = 2;//开始下载
    public static final int STATUS_DOWNLOADING = 3;//下载中
    public static final int STATUS_PAUSE = 4;//暂停下载
    public static final int STATUS_COMPLETE = 5;//下载完成
    public static final int STATUS_INSTALLED = 6;//安装完成
    public static final int STATUS_DOWNLOAD_ERROR = 7;//下载错误
    //往xutil里面存，必须有一个int 类型的id
    private int id;
    private String name;
    private String url; //网络下载地址
    private String img;
    private int status;
    private int max;
    private int progress;
    private int position; //在list里面的位置
    private String path;//本地保存路径
    private String packageName;//程序包名

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

}
