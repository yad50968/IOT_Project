package liutzuyuan.gateway;


class User {

    private String ID;
    private String IDX;
    private String XPV;
    private String UKP;
    private String Ku;

    User(String ID, String IDX, String XPV, String UKP, String Ku) {
        this.ID = ID;
        this.IDX = IDX;
        this.XPV = XPV;
        this.UKP = UKP;
        this.Ku = Ku;
    }

    String getID(){
        return this.ID;
    }
    String getIDX(){
        return this.IDX;
    }
    String getXPV(){
        return this.XPV;
    }
    String getUKP(){
        return this.UKP;
    }
    String getKu(){ return this.Ku; }
}
