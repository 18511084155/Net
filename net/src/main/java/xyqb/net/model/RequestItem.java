package xyqb.net.model;

import java.io.File;
import java.util.HashMap;

/**
 * 网络信息管理对象
 *
 * @author czz
 * @Date 2014/11/26
 */
public class RequestItem {
    public String action;// 请求方法
    public String method;// 请求get/post
    public String info;
    public Object[] pathParams;
    public String[] param;// 请求参数
    public String entity;
    public HashMap<String,File> partBody;
    public HashMap<String,String> cookies;
    public HashMap<String,String> params;
    public String dynamicUrl;//动态的服务器域名
    public String url;// 请求url前缀
    public boolean filter;//非空过滤
    public boolean extras;//添加附加数据,部分接口不需要.例授权
    public String[] files;//提交上传文件
    public boolean replace;//附加数据与现在数据重复时,是否替换
    public boolean encrypt;//请求参数是否加密
    public int version;//接口版本号
    public HashMap<String,String> headers;

    public RequestItem() {
        this.filter = true;
        this.extras = true;
        this.replace = true;
        this.headers=new HashMap<>();
        this.cookies=new HashMap<>();
        this.params=new HashMap<>();
        this.partBody=new HashMap<>();
    }
}
