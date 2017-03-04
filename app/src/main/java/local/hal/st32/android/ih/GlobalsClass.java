package local.hal.st32.android.ih;

import android.app.Application;
import android.content.Intent;

/**
 * Created by fei on 2016/11/27.
 */

public class GlobalsClass extends Application
{
    String PATH = "http://192.168.1.106:8080/IHAndroid/";

    String employeeID = "";

    String repositoryID = "";



    public String getRepositoryID() {
        return repositoryID;
    }

    public void setRepositoryID(String repositoryID) {
        this.repositoryID = repositoryID;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public String URL(String servletName) {
        return this.PATH + servletName;
    }

//    public void Logout(String activityName,String transitionName)
//    {
//        employeeID = "";
//        Intent intent = new Intent(activityName,transitionName);
//
//        startActivity(intent);
//
//    }

}
