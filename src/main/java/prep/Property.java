package prep;

public class Property {
    private final String value, role, subject;

    public Property(String value, String role, String subject){
        this.value = value;
        this.role = role;
        this.subject = subject;
    }

    public Property(String value, String role){
        this.value = value;
        this.role = role;
        subject = "";
    }


    public String getValue(){
        return this.value;
    }

    public String getRole(){
        return this.role;
    }

    public String getSubject(){
        return this.subject;
    }

    @Override
    public String toString(){
        return "Property(" + value + ", " + role + ", " + subject + ")";
    }

    @Override
    public int hashCode(){
        return this.toString().hashCode();
    }
}
