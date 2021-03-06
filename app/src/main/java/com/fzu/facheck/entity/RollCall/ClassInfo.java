package com.fzu.facheck.entity.RollCall;

import java.util.List;

public class ClassInfo {
    public String code;
    public String startTime;
    public String endTime;
    public String classCode;
    public String teacherName;
    private List<Student> studentList;
    private List<Record> recordList;
    public static class Student{
        private String phoneNumber;
        private String name;
        public Student(String id,String name){
            this.phoneNumber=id;
            this.name=name;
        }
        public String getName(){
            return name;
        }
        public String getPhoneNumber(){
            return phoneNumber;
        }
    }
    public static class Record{
        private String recordid;
        private String time;
        private String attendratio;
        public Record(String time,String attend,String recordid){
            this.time=time;
            this.attendratio=attend;
            this.recordid=recordid;
        }
        public String getTime(){
            return time;
        }
        public String getAttendratio(){
            return attendratio;
        }
        public String getRecordid(){return recordid;}

    }
    public List<Student> getStudents(){
        return studentList;
    }
    public List<Record> getRecords(){
        return recordList;
    }
}
