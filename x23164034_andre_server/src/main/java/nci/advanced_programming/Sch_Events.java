package nci.advanced_programming;

//Author: André Pont De Anda
//Student ID: x23164034
public class Sch_Events {
    String date = "";
    String time = "";
    String desc = "";

    public Sch_Events(String date, String time, String desc) {
        this.date = date;
        this.time = time;
        this.desc = desc;
    }

    public void setDate(String date){
        this.date = date;
    }

    
    public void setTime(String time){
        this.time = time;
    }

    
    public void setDesc(String desc){
        this.desc = desc;
    }

    public String getDate() {
        return date;
    }

    public String getDesc() {
        return desc;
    }

    public String getTime() {
        return time;
    }

    public String getEvent(){
        return "" + date + "; " + time+ "; "+ desc+"; ";
    }
}
